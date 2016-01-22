/*
 * Copyright (C) 2006 The Android Open Source Project
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
 * limitations under the License.
 */

package com.stockbrowser;

import android.app.Application;
import android.util.Log;
import android.webkit.CookieSyncManager;

import timber.log.Timber;

public class Browser extends Application {

	// Set to true to enable verbose logging.
	final static boolean LOGV_ENABLED = true;
	// Set to true to enable extra debug logging.
	final static boolean LOGD_ENABLED = true;
	private final static String LOGTAG = "browser";

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
//			Timber.plant(new CrashReportingTree());
		}

		if (LOGV_ENABLED)
			Log.v(LOGTAG, "Browser.onCreate: this=" + this);

		// create CookieSyncManager with current Context
		CookieSyncManager.createInstance(this);
		BrowserSettings.initialize(getApplicationContext());
		Preloader.initialize(getApplicationContext());
	}
}

