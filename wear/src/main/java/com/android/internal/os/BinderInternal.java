/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.internal.os;

import android.os.IBinder;
import android.os.SystemClock;
import android.util.EventLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.lang.Object;
/**
 * Private and debugging Binder APIs.
 * 
 * @see IBinder
 */
public class BinderInternal {
    static WeakReference<GcWatcher> sGcWatcher
            = new WeakReference<>(new GcWatcher());
    static final ArrayList<Runnable> sGcWatchers = new ArrayList<>();
    static Runnable[] sTmpWatchers = new Runnable[1];
    static long sLastGcTime;

    static final class GcWatcher {
        @Override
        protected void finalize() throws Throwable {
            handleGc();
            sLastGcTime = SystemClock.uptimeMillis();
            synchronized (sGcWatchers) {
                sTmpWatchers = sGcWatchers.toArray(sTmpWatchers);
            }
            for (Runnable sTmpWatcher : sTmpWatchers) {
                if (sTmpWatcher != null) {
                    sTmpWatcher.run();
                }
            }
            sGcWatcher = new WeakReference<>(new GcWatcher());
        }
    }


    /**
     * Return the global "context object" of the system.  This is usually
     * an implementation of IServiceManager, which you can use to find
     * other services.
     */
    public static final native IBinder getContextObject();
    
    static native final void handleGc();
}
