package com.stockbrowser.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;

/**
 * @hide
 */
public class BrowserContract {
    /**
     * The authority for the browser provider
     */
    public static final String AUTHORITY = "com.stockbrowser";

    /**
     * A content:// style uri to the authority for the browser provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * An optional insert, update or delete URI parameter that allows the caller
     * to specify that it is a sync adapter. The default value is false. If true
     * the dirty flag is not automatically set and the "syncToNetwork" parameter
     * is set to false when calling
     * {@link ContentResolver#notifyChange(Uri, android.database.ContentObserver, boolean)}.
     */
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

    /**
     * A parameter for use when querying any table that allows specifying a limit on the number
     * of rows returned.
     */
    public static final String PARAM_LIMIT = "limit";

    /**
     * Generic columns for use by sync adapters. The specific functions of
     * these columns are private to the sync adapter. Other clients of the API
     * should not attempt to either read or write these columns.
     */
    interface BaseSyncColumns {
        /**
         * Generic column for use by sync adapters.
         */
        public static final String SYNC1 = "sync1";
        /**
         * Generic column for use by sync adapters.
         */
        public static final String SYNC2 = "sync2";
        /**
         * Generic column for use by sync adapters.用于存储旧的title
         */
        public static final String SYNC3 = "sync3";
        /**
         * Generic column for use by sync adapters. 用于存储旧的url
         */
        public static final String SYNC4 = "sync4";
        /**
         * Generic column for use by sync adapters. 用于记录同步后的父文件夹
         */
        public static final String SYNC5 = "sync5";
    }

    /**
     * Columns that appear when each row of a table belongs to a specific
     * account, including sync information that an account may need.
     */
    interface SyncColumns extends BaseSyncColumns {
        /**
         * The name of the account instance to which this row belongs, which when paired with
         * {@link #ACCOUNT_TYPE} identifies a specific account.
         * <P>Type: TEXT</P>
         */
        public static final String ACCOUNT_NAME = "account_name";

        /**
         * The type of account to which this row belongs, which when paired with
         * {@link #ACCOUNT_NAME} identifies a specific account.
         * <P>Type: TEXT</P>
         */
        public static final String ACCOUNT_TYPE = "account_type";

        /**
         * String that uniquely identifies this row to its source account.
         * <P>Type: TEXT</P>
         */
        public static final String SOURCE_ID = "sourceid";

        /**
         * Version number that is updated whenever this row or its related data
         * changes.
         * <P>Type: INTEGER</P>
         */
        public static final String VERSION = "version";

        /**
         * Flag indicating that {@link #VERSION} has changed, and this row needs
         * to be synchronized by its owning account.
         * 指示标志{版本}已经改变，该行需要其拥有帐户同步。
         * 1 表示版本发生变化了 0 表示版本相同了
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String DIRTY = "dirty";

        /**
         * The time that this row was last modified by a client (msecs since the epoch).
         * <P>Type: INTEGER</P>
         */
        public static final String DATE_MODIFIED = "modified";
    }

    interface CommonColumns {
        /**
         * The unique ID for a row.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String _ID = "_id";

        /**
         * The URL of the bookmark.
         * <P>Type: TEXT (URL)</P>
         */
        public static final String URL = "url";

        /**
         * The user visible title of the bookmark.
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The time that this row was created on its originating client (msecs
         * since the epoch).
         * <P>Type: INTEGER</P>
         */
        public static final String DATE_CREATED = "created";
    }

    interface ImageColumns {
        /**
         * The favicon of the bookmark, may be NULL.
         * Must decode via {@link BitmapFactory#decodeByteArray}.
         * <p>Type: BLOB (image)</p>
         */
        public static final String FAVICON = "favicon";

        /**
         * A thumbnail of the page,may be NULL.
         * Must decode via {@link BitmapFactory#decodeByteArray}.
         * <p>Type: BLOB (image)</p>
         */
        public static final String THUMBNAIL = "thumbnail";

        /**
         * The touch icon for the web page, may be NULL.
         * Must decode via {@link BitmapFactory#decodeByteArray}.
         * <p>Type: BLOB (image)</p>
         *
         * @hide
         */
        public static final String TOUCH_ICON = "touch_icon";
    }

    interface HistoryColumns {
        /**
         * The date the item was last visited, in milliseconds since the epoch.
         * <p>Type: INTEGER (date in milliseconds since January 1, 1970)</p>
         */
        public static final String DATE_LAST_VISITED = "date";

        /**
         * The number of times the item has been visited.
         * <p>Type: INTEGER</p>
         */
        public static final String VISITS = "visits";

        public static final String USER_ENTERED = "user_entered";
    }

    /**
     * Convenience definitions for use in implementing chrome bookmarks sync in the Bookmarks table.
     */
    public static final class ChromeSyncColumns {
        /**
         * The server unique ID for an item
         */
        public static final String SERVER_UNIQUE = BaseSyncColumns.SYNC3;
        public static final String FOLDER_NAME_ROOT = "google_chrome";
        public static final String FOLDER_NAME_BOOKMARKS = "2345_browser_phone_bookmarks";
        public static final String FOLDER_NAME_BOOKMARKS_BAR = "bookmark_bar";
        public static final String FOLDER_NAME_OTHER_BOOKMARKS = "other_bookmarks";
        /**
         * The client unique ID for an item
         */
        public static final String CLIENT_UNIQUE = BaseSyncColumns.SYNC4;

        private ChromeSyncColumns() {
        }
    }

    /**
     * The bookmarks table, which holds the user's browser bookmarks.
     */
    public static final class Bookmarks implements CommonColumns, ImageColumns, SyncColumns {
        /**
         * The content:// style URI for this table
         * 这个表格的URI样式
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "bookmarks");
        /**
         * The content:// style URI for the default folder
         * 默认文件夹的URI样式
         */
        public static final Uri CONTENT_URI_DEFAULT_FOLDER =
                Uri.withAppendedPath(CONTENT_URI, "folder");
        /**
         * Query parameter used to specify an account name
         * 通过这个参数指定某个账户
         */
        public static final String PARAM_ACCOUNT_NAME = "acct_name";
        /**
         * Query parameter used to specify an account type
         * 通过这个参数指定某个账户类型
         */
        public static final String PARAM_ACCOUNT_TYPE = "acct_type";
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of bookmarks.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/bookmark";
        /**
         * The MIME type of a {@link #CONTENT_URI} of a single bookmark.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/bookmark";
        /**
         * Query parameter to use if you want to see deleted bookmarks that are still
         * around on the device and haven't been synced yet.
         * 使用查询参数，如果你想看到已删除的书签仍然
         *          *周围的设备上，并没有同步
         *
         * @see #IS_DELETED
         */
        public static final String QUERY_PARAMETER_SHOW_DELETED = "show_deleted";
        /**
         * Flag indicating if an item is a folder or bookmark. Non-zero values indicate
         * a folder and zero indicates a bookmark.
         * 1表示文件夹 0表示一条网页数据 -1表示自动提示
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String IS_FOLDER = "folder";
        /**
         * The ID of the parent folder. ID 0 is the root folder.
         * 父文件夹的_ID    如果是0表示根目录
         * <P>Type: INTEGER (reference to item in the same table)</P>
         */
        public static final String PARENT = "parent";
        /**
         * The source ID for an item's parent. Read-only.
         * 父对象的源ID 是只读的
         *
         * @see #PARENT
         */
        public static final String PARENT_SOURCE_ID = "parent_source";
        /**
         * The position of the bookmark in relation to it's siblings that share the same
         * 相对于它的兄弟姐妹共享相同的位置中的书签
         * {@link #PARENT}. May be negative.
         * <P>Type: INTEGER</P>
         */
        public static final String POSITION = "position";
        /**
         * The item that the bookmark should be inserted after.
         * 该项目后应插入书签。可能是负面的。
         * May be negative.
         * <P>Type: INTEGER</P>
         */
        public static final String INSERT_AFTER = "insert_after";
        /**
         * order
         */
        public static final String SORT = "sort";
        /**
         * to_index
         */
        public static final String TO_INDEX = "to_index";
        /**
         * The source ID for the item that the bookmark should be inserted after. Read-only.
         * May be negative.
         * <P>Type: INTEGER</P>
         *
         * @see #INSERT_AFTER
         */
        public static final String INSERT_AFTER_SOURCE_ID = "insert_after_source";
        /**
         * A flag to indicate if an item has been deleted. Queries will not return deleted
         * entries unless you add the {@link #QUERY_PARAMETER_SHOW_DELETED} query paramter
         * to the URI when performing your query.
         * 0表示没删除   1表示删除掉
         * <p>Type: INTEGER (non-zero if the item has been deleted, zero if it hasn't)
         *
         * @see #QUERY_PARAMETER_SHOW_DELETED
         */
        public static final String IS_DELETED = "deleted";

        /**
         * This utility class cannot be instantiated.
         */
        private Bookmarks() {
        }

        /**
         * Builds a URI that points to a specific folder.
         *
         * @param folderId the ID of the folder to point to
         *                 创建一个文件夹的URI
         */
        public static final Uri buildFolderUri(long folderId) {
            return ContentUris.withAppendedId(CONTENT_URI_DEFAULT_FOLDER, folderId);
        }
    }

    /**
     * Read-only table that lists all the accounts that are used to provide bookmarks.
     */
    public static final class Accounts {
        /**
         * Directory under {@link Bookmarks#CONTENT_URI}
         */
        public static final Uri CONTENT_URI =
                AUTHORITY_URI.buildUpon().appendPath("accounts").build();

        /**
         * The name of the account instance to which this row belongs, which when paired with
         * {@link #ACCOUNT_TYPE} identifies a specific account.
         * <P>Type: TEXT</P>
         */
        public static final String ACCOUNT_NAME = "account_name";

        /**
         * The type of account to which this row belongs, which when paired with
         * {@link #ACCOUNT_NAME} identifies a specific account.
         * <P>Type: TEXT</P>
         */
        public static final String ACCOUNT_TYPE = "account_type";

        /**
         * The ID of the account's root folder. This will be the ID of the folder
         * returned when querying {@link Bookmarks#CONTENT_URI_DEFAULT_FOLDER}.
         * <P>Type: INTEGER</P>
         */
        public static final String ROOT_ID = "root_id";
    }

    /**
     * The history table, which holds the browsing history.
     */
    public static final class History implements CommonColumns, HistoryColumns, ImageColumns {
        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "history");
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of browser history items.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/browser-history";
        /**
         * The MIME type of a {@link #CONTENT_URI} of a single browser history item.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/browser-history";

        /**
         * This utility class cannot be instantiated.
         */
        private History() {
        }
    }

    /**
     * The search history table.
     *
     * @hide
     */
    public static final class Searches {
        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "searches");
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of browser search items.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/searches";
        /**
         * The MIME type of a {@link #CONTENT_URI} of a single browser search item.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/searches";
        /**
         * The unique ID for a row.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String _ID = "_id";
        /**
         * The user entered search term.
         */
        public static final String SEARCH = "search";
        /**
         * The date the search was performed, in milliseconds since the epoch.
         * <p>Type: NUMBER (date in milliseconds since January 1, 1970)</p>
         */
        public static final String DATE = "date";

        private Searches() {
        }
    }

    /**
     * A table provided for sync adapters to use for storing private sync state data.
     *
     * @see SyncStateContract
     */
   /* public static final class SyncState implements SyncStateContract.Columns {
        *//**
     * This utility class cannot be instantiated
     *//*
        private SyncState() {}

        public static final String CONTENT_DIRECTORY =
                SyncStateContract.Constants.CONTENT_DIRECTORY;

        *//**
     * The content:// style URI for this table
     *//*
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

        *//**
     * @see android.provider.SyncStateContract.Helpers#get
     *//*
        public static byte[] get(ContentProviderClient provider, Account account)
                throws RemoteException {
            return SyncStateContract.Helpers.get(provider, CONTENT_URI, account);   去掉
        }

        *//**
     * @see android.provider.SyncStateContract.Helpers#get
     *//*
        public static Pair<Uri, byte[]> getWithUri(ContentProviderClient provider, Account account)
                throws RemoteException {
            return SyncStateContract.Helpers.getWithUri(provider, CONTENT_URI, account); 去掉
        }

        *//**
     * @see android.provider.SyncStateContract.Helpers#set
     *//*
        public static void set(ContentProviderClient provider, Account account, byte[] data)
                throws RemoteException {
            SyncStateContract.Helpers.set(provider, CONTENT_URI, account, data);
        }

        *//**
     * @see android.provider.SyncStateContract.Helpers#newSetOperation
     *//*
        public static ContentProviderOperation newSetOperation(Account account, byte[] data) {
            return SyncStateContract.Helpers.newSetOperation(CONTENT_URI, account, data);       去掉
        }
    }*/

    /**
     * Stores images for URLs. Only support query() and update().
     *
     * @hide
     */
    public static final class Images implements ImageColumns {
        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "images");
        /**
         * The URL the images came from.
         * <P>Type: TEXT (URL)</P>
         */
        public static final String URL = "url_key";

        /**
         * This utility class cannot be instantiated
         */
        private Images() {
        }
    }

    /**
     * A combined view of bookmarks and history. All bookmarks in all folders are included and
     * no folders are included.
     */
    public static final class Combined implements CommonColumns, HistoryColumns, ImageColumns {
        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "combined");
        /**
         * Flag indicating that an item is a bookmark. A value of 1 indicates a bookmark, a value
         * of 0 indicates a history item.
         * <p>Type: INTEGER (boolean)</p>
         */
        public static final String IS_BOOKMARK = "bookmark";

        /**
         * This utility class cannot be instantiated
         */
        private Combined() {
        }
    }

    /**
     * A table that stores settings specific to the browser. Only support query and insert.
     */
    public static final class Settings {
        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "settings");
        /**
         * Key for a setting value.
         */
        public static final String KEY = "key";
        /**
         * Value for a setting.
         */
        public static final String VALUE = "value";
        /**
         * If set to non-0 the user has opted into bookmark sync.
         */
        public static final String KEY_SYNC_ENABLED = "sync_enabled";

        /**
         * This utility class cannot be instantiated
         */
        private Settings() {
        }

        /**
         * Returns true if bookmark sync is enabled
         */
        static public boolean isSyncEnabled(Context context) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(CONTENT_URI, new String[]{VALUE},
                        KEY + "=?", new String[]{KEY_SYNC_ENABLED}, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    return false;
                }
                return cursor.getInt(0) != 0;
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        /**
         * Sets the bookmark sync enabled setting.
         */
        static public void setSyncEnabled(Context context, boolean enabled) {
            ContentValues values = new ContentValues();
            values.put(KEY, KEY_SYNC_ENABLED);
            values.put(VALUE, enabled ? 1 : 0);
            context.getContentResolver().insert(CONTENT_URI, values);
        }
    }
}
