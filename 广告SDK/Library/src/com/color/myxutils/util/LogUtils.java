/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.color.myxutils.util;

import android.util.Log;

/**
 * 
 * @author lixinyuan
 *
 * 
 */
public class LogUtils {

	private LogUtils() {
	}

	public static boolean debug = true;

	private static String generateTag() {
		StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
		String tag = "%s.%s(L:%d)";
		String callerClazzName = caller.getClassName();
		callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
		tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
		return tag;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean isDebug) {
		debug = isDebug;
	}

	public static void d(String content) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.d(tag, content);
	}

	public static void d(String content, Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.d(tag, content, tr);
	}

	public static void e(String content) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.e(tag, content);
	}

	public static void e(String content, Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.e(tag, content, tr);
	}

	public static void i(String content) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.i(tag, content);
	}

	public static void i(String content, Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.i(tag, content, tr);
	}

	public static void v(String content) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.v(tag, content);
	}

	public static void v(String content, Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.v(tag, content, tr);
	}

	public static void w(String content) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.w(tag, content);
	}

	public static void w(String content, Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.w(tag, content, tr);
	}

	public static void w(Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.w(tag, tr);
	}

	public static void wtf(String content) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.wtf(tag, content);
	}

	public static void wtf(String content, Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.wtf(tag, content, tr);
	}

	public static void wtf(Throwable tr) {
		if (!debug)
			return;
		String tag = generateTag();
		Log.wtf(tag, tr);
	}

}
