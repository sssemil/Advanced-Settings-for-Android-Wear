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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Private and debugging Binder APIs.
 *
 * @see IBinder
 */
public class BinderInternal {
    static final ArrayList<Runnable> sGcWatchers = new ArrayList<>();
    static WeakReference<GcWatcher> sGcWatcher
            = new WeakReference<>(new GcWatcher());
    static Runnable[] sTmpWatchers = new Runnable[1];
    static long sLastGcTime;

    /**
     * Return the global "context object" of the system.  This is usually
     * an implementation of IServiceManager, which you can use to find
     * other services.
     */
    public static final native IBinder getContextObject();

    static native final void handleGc();

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
}
