# androidstockbrowser

Stock Browser for Android

## 代码分析

`Controller`是`WebView`、`UI`、`Activity`的桥梁
启动界面`BrowserActivity`中最重要的方法是`createController()`，Controller被创建时会调用
`mController.setUi(new PhoneUi(this, controller))`    

`PhoneUi`包含`NavigationBarPhone`    
`TitleBar`包含`NavigationBarPhone`

`BaseUi`里关联一个`custom_screen.xml`布局，并会创建一个`TitleBar`
`custom_screen.xml`里有一个`id`为`main_content`的`FrameLayout`，叫做`mContentView`，
`Tab`切换都会从`mContentView`去添加和移除视图

```
<FrameLayout
	android:id="@+id/fixed_titlebar_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>

<FrameLayout
    android:id="@+id/main_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

其中`fixed_title_container`是放置TitleBar的，参考`BaseUi.addFixedTitleBar`


### 重要的类

`NavScreen`是点击Tab Switch按钮后显示的Tab导航界面

### 加载URL流程
`Controller.loadUrl` -> `Tab.loadUrl`

`Tab.loadUrl`里会1. 设置正在加载URL状态标志;2. 调用`WebViewController.onPageStarted`;最后调用`WebView.loadUrl`

`WebViewController.onPageStarted`十分关键，会调用`BaseUi.onTabDataChanged`，然后会调用`TitleBar.onTabDataChanged`和`NavigationBarBase.onTabDataChanged`

对菜单项的更新不在`TitleBar.onTabDataChanged`和`NavigationBarBase.onTabDataChanged`中；

### 有关`Controller`的`onPageStarted`和`onPageFinished`方法
有关`Controller.onPageFinished`是在`Tab.mWebViewClient.onPageFinished`中被回调；

### 分析“前进”、“后退”菜单项更新状态逻辑
这些菜单项是埋藏在“更多”菜单项里，每次点击该按钮，会创建、更新一个`PopupMenu`，所以每次加载网页都不会主动更新菜单状态，而是在点击“更多”菜单项时，才更新状态（Lazy Initialization）。

### 分析“Stop”按钮状态更新逻辑
在Tab加载网页时，会在TitleBar上显示一个Stop按钮，并在加载完后隐藏。Stop按钮作为控件状态更新的典型，厘清该控制流程非常重要。

1. 首先发现该按钮位于`NavigationBarPhone`内；
2. 发现在更新Stop按钮的状态位于`onProgressStarted`、`onProgressStopped`、`onStateChanged`中；

`UrlInputView`使用了Observer设计模式，其为一个主题（Subject）对象，而`NavigationBarPhone`为观察者，彼此通过`UrlInputView.StateListener.StateListener`接口来进行通信，其中`NavigationBarPhone`实现`UrlInputView.StateListener`接口。

`NavigationBarPhone`的`onProgressStarted`和`onProgressStopped`被回调的地方是`TitleBar.setProgress`，而`TitleBar.setProgress`是被`BaseUi.onProgressChanged`调用的；

### 分析`Back`逻辑
BrowserActivity将onKeyDown和onKeyUp方法的处理委托给Controller的onKeyDown和onKeyUp。当按下返回键，Controller.onBackKey会被回调：
```java
protected void onBackKey() {
    if (!mUi.onBackKey()) {
        WebView subwindow = mTabControl.getCurrentSubWindow();
        if (subwindow != null) {
            if (subwindow.canGoBack()) {
                subwindow.goBack();
            } else {
                dismissSubWindow(mTabControl.getCurrentTab());
            }
        } else {
            goBackOnePageOrQuit();
        }
    }
}
```

可见，Controller先将处理委托给`UI.onBackKey`，仅当返回`false`时，Controller会让当前的SubWindow来消化该事件，如果没有SubWindow，则由`Controller.goBackOnePageOrQuit`处理该事件。

当当前Tab不为null，并且无法goBack时，如果有parent则切换到parent后再关闭tab，如果没有则关闭当前Tab，即调用`Controller.closeCurrentTab`。

### `NavScreen`代码分析
`NavScreen`是多标签切换界面，它的布局是`nav_screen.xml`。
布局中重要的类是`NavTabScroller`，它主要负责Tab页滚动。
`ViewConfiguration.get(Context).hasPermanentMenuKey()`判断是否有固态Menu键
`NavTabScoller`通过设置`BaseAdapter`来绑定数据

## 内存释放
`BrowserActivity.onLowMemory`会委托`Controller.onLowMemory`释放内存。
```java
@Override
public void onLowMemory() {
    super.onLowMemory();
    mController.onLowMemory();
}
```
`Controller.onLowMemory`会调用`mTabControl.freeMemory`来释放内存：
```java
/**
  * Free the memory in this order, 1) free the background tabs; 2) free the
  * WebView cache;
  */
void freeMemory() {
    if (getTabCount() == 0) return;

    // free the least frequently used background tabs
    Vector<Tab> tabs = getHalfLeastUsedTabs(getCurrentTab());
    if (tabs.size() > 0) {
        Log.w(LOGTAG, "Free " + tabs.size() + " tabs in the browser");
        for (Tab t : tabs) {
            // store the WebView's state.
            t.saveState();
            // destroy the tab
            t.destroy();
        }
        return;
    }

    // free the WebView's unused memory (this includes the cache)
    Log.w(LOGTAG, "Free WebView's unused memory and cache");
    WebView view = getCurrentWebView();
    if (view != null) {
        view.freeMemory();
    }
}
```

## 网址重定向的分析
当发生网址重定向时，WebView会多次回调WebViewClient.onPageStarted方法。比如在地址栏输入`qq.com`后，回调方式如下：
```
V/Tab: ⇢ onPageStarted(view=BrowserWebView, url="http://qq.com/", favicon=Bitmap@23b4987f)
V/Tab: ⇢ onPageStarted(view=BrowserWebView, url="http://www.qq.com/", favicon=Bitmap@23b4987f)
V/Tab: ⇢ onPageStarted(view=BrowserWebView, url="http://xw.qq.com/index.htm", favicon=Bitmap@23b4987f)
V/Tab: ⇢ onReceivedIcon(view=BrowserWebView, icon=Bitmap@315bd90a)
V/Tab: ⇢ onPageFinished(view=BrowserWebView, url="http://xw.qq.com/index.htm")
```
可见没重定向一次，就会回调`onPageStarted`方法，倘若需要多次重定向，则每次都会回调`onPageStarted`方法。

