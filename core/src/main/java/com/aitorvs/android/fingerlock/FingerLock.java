/*
 * Copyright (c) 2016 Aitor Viana Sanchez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aitorvs.android.fingerlock;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.aitorvs.android.fingerlock.Preconditions.checkNotNull;

public final class FingerLock extends Fragment implements FingerLockManager {

    private static final String ARG_KEY_NAME = "ARG_KEY_NAME";
    private static final String TAG = FingerLock.class.getSimpleName();
    private static final String TAG_FINGER_LOCK_FRAGMENT = "TagFingerLockFragment";
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
    public static FingerLockManager initialize(@NonNull AppCompatActivity activity, @NonNull String keyName) {
        // Have we created the fragment before ?
        FingerLock fragment = (FingerLock) checkNotNull(activity).getSupportFragmentManager().findFragmentByTag(TAG_FINGER_LOCK_FRAGMENT);
        if (fragment == null) {
            fragment = createInstance(checkNotNull(keyName));
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(fragment, TAG_FINGER_LOCK_FRAGMENT)
                    .commitNow();
        }

        return fragment;
    }

    /**
     * Convenience method to create the library without initialize it
     *
     * @param keyName keyname
     * @return uninitialized library reference
     */
    private static FingerLock createInstance(@NonNull String keyName) {
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
    @Override
    public boolean isFingerprintAuthSupported() {
        return impl.isFingerprintAuthSupported();
    }

    /**
     * Returns <code>true</code> when the user has at least one fingerprint registered
     *
     * @return <code>true</code> when at least one fingerprint is registered
     */
    @Override
    public boolean isFingerprintRegistered() {
        return impl.isFingerprintRegistered();
    }

    /**
     * Call this method to start fingerprint scanning
     */
    @Override
    public void start() {
        impl.start();
    }

    /**
     * Call this method to force stopping fingerprint scanning
     */
    @Override
    public void stop() {
        impl.stop();
    }

    @Override
    public void recreateKey(@NonNull final FingerLockResultCallback listener) {
        impl.recreateKey(checkNotNull(listener));
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
