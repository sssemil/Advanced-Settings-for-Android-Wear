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

package sssemil.com.languagesettingsprovider;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.android.app.IActivityManager;
import com.android.internal.os.BinderInternal;

import java.util.ArrayList;
import java.util.HashMap;


interface IApplicationThread extends IInterface {
}

interface IServiceManager extends IInterface {
    String descriptor = "android.os.IServiceManager";
    int GET_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION;
    int CHECK_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION + 1;
    int ADD_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION + 2;
    int LIST_SERVICES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION + 3;

    /**
     * Retrieve an existing service called @a name from the
     * service manager.  Blocks for a few seconds waiting for it to be
     * published if it does not already exist.
     */
    IBinder getService(String name) throws RemoteException;

    /**
     * Retrieve an existing service called @a name from the
     * service manager.  Non-blocking.
     */
    IBinder checkService(String name) throws RemoteException;

    /**
     * Place a new @a service called @a name into the service
     * manager.
     */
    void addService(String name, IBinder service, boolean allowIsolated)
            throws RemoteException;

    /**
     * Return a list of all currently running services.
     */
    String[] listServices() throws RemoteException;
}

public abstract class ActivityManagerNative extends Binder implements IActivityManager {

    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            IBinder b = ServiceManager.getService("activity");
            Log.v("ActivityManager", "default service binder = " + b);
            IActivityManager am = asInterface(b);
            Log.v("ActivityManager", "default service = " + am);
            return am;
        }
    };

    /**
     * Cast a Binder object into an activity manager interface, generating
     * a proxy if needed.
     */
    static public IActivityManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IActivityManager in =
                (IActivityManager) obj.queryLocalInterface(descriptor);
        if (in != null) {
            return in;
        }

        return new ActivityManagerProxy(obj);
    }

    /**
     * Retrieve the system's default/global activity manager.
     */
    static public IActivityManager getDefault() {
        return gDefault.get();
    }

    public IBinder asBinder() {
        return this;
    }
}

abstract class Singleton<T> {
    private T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}

class ServiceManager {
    private static final String TAG = "ServiceManager";

    private static IServiceManager sServiceManager;
    private static HashMap<String, IBinder> sCache = new HashMap<>();

    private static IServiceManager getIServiceManager() {
        if (sServiceManager != null) {
            return sServiceManager;
        }

        // Find the service manager
        sServiceManager = ServiceManagerNative.asInterface(BinderInternal.getContextObject());
        return sServiceManager;
    }

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get
     * @return a reference to the service, or <code>null</code> if the service doesn't exist
     */
    public static IBinder getService(String name) {
        try {
            IBinder service = sCache.get(name);
            if (service != null) {
                return service;
            } else {
                return getIServiceManager().getService(name);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
        }
        return null;
    }
}

class ProfilerInfo implements Parcelable {

    public static final Parcelable.Creator<ProfilerInfo> CREATOR =
            new Parcelable.Creator<ProfilerInfo>() {
                public ProfilerInfo createFromParcel(Parcel in) {
                    return new ProfilerInfo(in);
                }

                public ProfilerInfo[] newArray(int size) {
                    return new ProfilerInfo[size];
                }
            };
    /* Name of profile output file. */
    public final String profileFile;
    /* Indicates sample profiling when nonzero, interval in microseconds. */
    public final int samplingInterval;

    /* Automatically stop the profiler when the app goes idle. */
    public final boolean autoStopProfiler;
    /* File descriptor for profile output file, can be null. */
    public ParcelFileDescriptor profileFd;

    private ProfilerInfo(Parcel in) {
        profileFile = in.readString();
        profileFd = in.readInt() != 0 ? ParcelFileDescriptor.CREATOR.createFromParcel(in) : null;
        samplingInterval = in.readInt();
        autoStopProfiler = in.readInt() != 0;
    }

    public int describeContents() {
        if (profileFd != null) {
            return profileFd.describeContents();
        } else {
            return 0;
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(profileFile);
        if (profileFd != null) {
            out.writeInt(1);
            profileFd.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeInt(samplingInterval);
        out.writeInt(autoStopProfiler ? 1 : 0);
    }
}

class ActivityManagerProxy implements IActivityManager {
    static final String TAG_TIMELINE = "Timeline";

    int START_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION + 2;
    int GET_CONFIGURATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION + 45;
    int UPDATE_CONFIGURATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION + 46;
    private IBinder mRemote;

    public ActivityManagerProxy(IBinder remote) {
        mRemote = remote;
    }

    public IBinder asBinder() {
        return mRemote;
    }

    public int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
                             String resolvedType, IBinder resultTo, String resultWho, int requestCode,
                             int startFlags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        if (intent.getComponent() != null) {
            Log.i(TAG_TIMELINE, "Timeline: Activity_launch_request id:"
                    + intent.getComponent().getPackageName() + " time:"
                    + SystemClock.uptimeMillis());
        }

        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        mRemote.transact(START_ACTIVITY_TRANSACTION, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    public Configuration getConfiguration() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        mRemote.transact(GET_CONFIGURATION_TRANSACTION, data, reply, 0);
        reply.readException();
        Configuration res = Configuration.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return res;
    }

    public void updateConfiguration(Configuration values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        values.writeToParcel(data, 0);
        mRemote.transact(UPDATE_CONFIGURATION_TRANSACTION, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }
}

abstract class ServiceManagerNative extends Binder implements IServiceManager {
    public ServiceManagerNative() {
        attachInterface(this, descriptor);
    }

    /**
     * Cast a Binder object into a service manager interface, generating
     * a proxy if needed.
     */
    static public IServiceManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IServiceManager in =
                (IServiceManager) obj.queryLocalInterface(descriptor);
        if (in != null) {
            return in;
        }

        return new ServiceManagerProxy(obj);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        try {
            switch (code) {
                case IServiceManager.GET_SERVICE_TRANSACTION: {
                    data.enforceInterface(IServiceManager.descriptor);
                    String name = data.readString();
                    IBinder service = getService(name);
                    reply.writeStrongBinder(service);
                    return true;
                }

                case IServiceManager.CHECK_SERVICE_TRANSACTION: {
                    data.enforceInterface(IServiceManager.descriptor);
                    String name = data.readString();
                    IBinder service = checkService(name);
                    reply.writeStrongBinder(service);
                    return true;
                }

                case IServiceManager.ADD_SERVICE_TRANSACTION: {
                    data.enforceInterface(IServiceManager.descriptor);
                    String name = data.readString();
                    IBinder service = data.readStrongBinder();
                    boolean allowIsolated = data.readInt() != 0;
                    addService(name, service, allowIsolated);
                    return true;
                }

                case IServiceManager.LIST_SERVICES_TRANSACTION: {
                    data.enforceInterface(IServiceManager.descriptor);
                    String[] list = listServices();
                    reply.writeStringArray(list);
                    return true;
                }
            }
        } catch (RemoteException e) {
        }

        return false;
    }

    public IBinder asBinder() {
        return this;
    }
}

class ServiceManagerProxy implements IServiceManager {
    private IBinder mRemote;

    public ServiceManagerProxy(IBinder remote) {
        mRemote = remote;
    }

    public IBinder asBinder() {
        return mRemote;
    }

    public IBinder getService(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        mRemote.transact(GET_SERVICE_TRANSACTION, data, reply, 0);
        IBinder binder = reply.readStrongBinder();
        reply.recycle();
        data.recycle();
        return binder;
    }

    public IBinder checkService(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        mRemote.transact(CHECK_SERVICE_TRANSACTION, data, reply, 0);
        IBinder binder = reply.readStrongBinder();
        reply.recycle();
        data.recycle();
        return binder;
    }

    public void addService(String name, IBinder service, boolean allowIsolated)
            throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        data.writeStrongBinder(service);
        data.writeInt(allowIsolated ? 1 : 0);
        mRemote.transact(ADD_SERVICE_TRANSACTION, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

    public String[] listServices() throws RemoteException {
        ArrayList<String> services = new ArrayList<>();
        int n = 0;
        while (true) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(IServiceManager.descriptor);
            data.writeInt(n);
            n++;
            try {
                boolean res = mRemote.transact(LIST_SERVICES_TRANSACTION, data, reply, 0);
                if (!res) {
                    break;
                }
            } catch (RuntimeException e) {
                // The result code that is returned by the C++ code can
                // cause the call to throw an exception back instead of
                // returning a nice result...  so eat it here and go on.
                break;
            }
            services.add(reply.readString());
            reply.recycle();
            data.recycle();
        }
        String[] array = new String[services.size()];
        services.toArray(array);
        return array;
    }
}