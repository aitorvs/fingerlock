package com.aitorvs.android.fingerlock;

/*
 * Copyright (C) 23/05/16 aitorvs
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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aitorvs.android.fingerlock.internal.FingerLockApi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class FingerLock extends Fragment {

    private static final String ARG_KEY_NAME = "ARG_KEY_NAME";
    private static final String TAG = FingerLock.class.getSimpleName();
    private FingerLockApi.FingerLockImpl impl;
    private Context mContext;
    private FingerLockResultCallback mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.impl = FingerLockApi.create();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate: called");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof FingerLockResultCallback) {
            mCallback = (FingerLockResultCallback) context;
        } else {
            throw new IllegalStateException("Callback listener not implemented");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();

        if (arguments != null) {
            String keyName = arguments.getString(ARG_KEY_NAME);
            impl.register(mContext, keyName, mCallback);
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "onResume: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        impl.unregister(mCallback);
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause: called");
    }


    /**
     * Call this method to initialize the library
     *
     * @param activity {@link AppCompatActivity} object
     * @param keyName  key name
     * @return library reference
     */
    public static FingerLock initialize(@NonNull AppCompatActivity activity, @NonNull String keyName) {
        //noinspection ConstantConditions
        if (activity == null) {
            return null;
        }


        FingerLock fragment = createInstance(keyName);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, "FingerLock")
                .commitNow();

        return fragment;
    }

    /**
     * Convenience method to create the library without initialize it
     *
     * @param keyName keyname
     * @return uninitialized library reference
     */
    public static FingerLock createInstance(@NonNull String keyName) {
        FingerLock fragment = new FingerLock();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_KEY_NAME, keyName);
        fragment.setArguments(arguments);

        return fragment;
    }

    /**
     * Returns <code>true</code> when fingerprint authentication is available and supported
     *
     * @return <code>true</code> when supported
     */
    public boolean isFingerprintAuthSupported() {
        return impl.isFingerprintAuthSupported();
    }

    /**
     * Returns <code>true</code> when the user has at least one fingerprint registered
     *
     * @return <code>true</code> when at least one fingerprint is registered
     */
    public boolean isFingerprintRegistered() {
        return impl.isFingerprintRegistered();
    }

    /**
     * Call this method to start fingerprint scanning
     */
    public void start() {
        impl.start();
    }

    /**
     * Call this method to force stopping fingerprint scanning
     */
    public void stop() {
        impl.stop();
    }

    public boolean inUseBy(FingerLockResultCallback listener) {
        return impl.inUseBy(listener);
    }

    public void recreateKey(final Object listener) {
        if (listener instanceof FingerLockResultCallback) {
            impl.recreateKey((FingerLockResultCallback) listener);
        }
    }

    /**
     * Error state
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FINGERPRINT_NOT_SUPPORTED, FINGERPRINT_NOT_RECOGNIZED, FINGERPRINT_PERMISSION_DENIED, FINGERPRINT_REGISTRATION_NEEDED, FINGERPRINT_ERROR_HELP, FINGERPRINT_UNRECOVERABLE_ERROR})
    public @interface FingerLockErrorState {
    }

    public static final int FINGERPRINT_NOT_SUPPORTED = 0;
    public static final int FINGERPRINT_NOT_RECOGNIZED = 1;
    public static final int FINGERPRINT_PERMISSION_DENIED = 2;
    public static final int FINGERPRINT_REGISTRATION_NEEDED = 3;
    public static final int FINGERPRINT_ERROR_HELP = 5;
    public static final int FINGERPRINT_UNRECOVERABLE_ERROR = 6;
}
