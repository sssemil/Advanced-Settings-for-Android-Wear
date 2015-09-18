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

package com.android.app;

import android.content.res.Configuration;
import android.os.IInterface;
import android.os.RemoteException;

/**
 * System private API for talking with the activity manager service.  This
 * provides calls from the application back to the activity manager.
 */
public interface IActivityManager extends IInterface {
    String descriptor = "android.app.IActivityManager";

    Configuration getConfiguration() throws RemoteException;

    void updateConfiguration(Configuration values) throws RemoteException;
}
