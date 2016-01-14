/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.stockbrowser;

import com.stockbrowser.compats.BrowserContract;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

//import android.content.CursorLoader;

public class BookmarksLoader extends CursorLoader {
	public static final String ARG_ACCOUNT_TYPE = "acct_type";
	public static final String ARG_ACCOUNT_NAME = "acct_name";

	public static final int COLUMN_INDEX_ID = 0;
	public static final int COLUMN_INDEX_URL = 1;
	public static final int COLUMN_INDEX_TITLE = 2;
	public static final int COLUMN_INDEX_FAVICON = 3;
	public static final int COLUMN_INDEX_THUMBNAIL = 4;
	public static final int COLUMN_INDEX_TOUCH_ICON = 5;
	public static final int COLUMN_INDEX_IS_FOLDER = 6;
	public static final int COLUMN_INDEX_PARENT = 8;
	public static final int COLUMN_INDEX_TYPE = 9;

	public static final String[] PROJECTION = new String[]{
			BrowserContract.Bookmarks._ID, // 0
			BrowserContract.Bookmarks.URL, // 1
			BrowserContract.Bookmarks.TITLE, // 2
			BrowserContract.Bookmarks.FAVICON, // 3
			BrowserContract.Bookmarks.THUMBNAIL, // 4
			BrowserContract.Bookmarks.TOUCH_ICON, // 5
			BrowserContract.Bookmarks.IS_FOLDER, // 6
			BrowserContract.Bookmarks.POSITION, // 7
			BrowserContract.Bookmarks.PARENT, // 8
			BrowserContract.Bookmarks.TYPE, // 9
	};

	String mAccountType;
	String mAccountName;

	public BookmarksLoader(Context context, String accountType, String accountName) {
		super(context, addAccount(BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER, accountType,
						accountName),
				PROJECTION, null, null, null);
		mAccountType = accountType;
		mAccountName = accountName;
	}

	static Uri addAccount(Uri uri, String accountType, String accountName) {
		return uri.buildUpon().appendQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_TYPE,
				accountType).
				appendQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_NAME, accountName).build();
	}

	@Override
	public void setUri(Uri uri) {
		super.setUri(addAccount(uri, mAccountType, mAccountName));
	}
}
