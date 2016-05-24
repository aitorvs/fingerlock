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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FingerLock {
    // context
    private static Context mContext;

    private static final FingerLockImpl impl;
    private static FingerprintManager mFingerprintManager;

    private static FingerprintAuthHandler mAuthenticationHandler;

    /**
     * listener callbacks
     */
    private static FingerLockResultCallback mCallback;

    static {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            impl = new MncFingerLockImpl();
        } else {
            impl = new BaseFingerLockImpl();
        }
    }

    interface FingerLockImpl {
        /**
         * Returns <code>true</code> when fingerprint authentication is available and supported
         *
         * @return <code>true</code> when supported
         */
        boolean isFingerprintAuthSupported();

        boolean isFingerprintRegistered();

        void start();

        void stop();
    }

    /**
     * Baseline implementation, pre-M
     */
    static class BaseFingerLockImpl implements FingerLockImpl {

        @Override
        public boolean isFingerprintAuthSupported() {
            invalidContext();

            return false;
        }

        @Override
        public boolean isFingerprintRegistered() {
            //noinspection PointlessBooleanExpression
            return !isFingerprintAuthSupported() || false;
        }

        @Override
        public void start() {
            // TODO: 23/05/16 implement
        }

        @Override
        public void stop() {
            // TODO: 23/05/16 implement
        }
    }

    /**
     * M implementation
     */
    @TargetApi(Build.VERSION_CODES.M)
    static class MncFingerLockImpl implements FingerLockImpl {

        @Override
        public boolean isFingerprintAuthSupported() {
            invalidContext();

            // check permissions
            int granted = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.USE_FINGERPRINT);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                // not granted
                return false;
            }

            // return hardware support
            //noinspection MissingPermission
            return mFingerprintManager != null
                    && mFingerprintManager.isHardwareDetected()
                    && mFingerprintManager.hasEnrolledFingerprints();

        }

        @Override
        public boolean isFingerprintRegistered() {
            if (!isFingerprintAuthSupported()) {
                return false;
            }

            // no need to check for permissions, as it's done inside previous call
            //noinspection MissingPermission
            return mFingerprintManager != null && mFingerprintManager.hasEnrolledFingerprints();
        }

        @Override
        public void start() {
            if (!isFingerprintAuthSupported()) {
                mCallback.onFingerLockError(FINGERPRINT_NOT_SUPPORTED, new Exception("Fingerprint authentication not supported in this device"));
            } else if (mAuthenticationHandler != null && !mAuthenticationHandler.isReady()) {
                // auth handler already listening...do nothing
            } else {
                mCallback.onFingerLockScanning(false);
                mAuthenticationHandler = new FingerprintAuthHandler(null, mCallback);
                //noinspection MissingPermission
                mAuthenticationHandler.start(mFingerprintManager);
            }
        }

        @Override
        public void stop() {
            if (mAuthenticationHandler != null) {
                mAuthenticationHandler.stop();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    private static FingerprintManager getFingerprintManager() {

        if (mFingerprintManager == null) {
            invalidContext();

            mFingerprintManager = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
        }

        return mFingerprintManager;
    }

    private static void invalidContext() throws IllegalStateException {
        if (mContext == null) {
            throw new IllegalStateException("Callback listener not registered");
        }
    }

    public static boolean isFingerprintAuthSupported() {
        return impl.isFingerprintAuthSupported();
    }

    public static boolean isFingerprintRegistered() {
        return impl.isFingerprintRegistered();
    }

    public static void register(@NonNull Context context, @NonNull FingerLockResultCallback callback) {
        mContext = context;
        mCallback = callback;
        mFingerprintManager = getFingerprintManager();

        // call on ready
        if (mCallback != null && isFingerprintAuthSupported()) {
            mCallback.onFingerLockReady();
        }
    }

    public static void unregister(@NonNull FingerLockResultCallback listener) {
        if (listener == mCallback) {
            mCallback = null;
            mAuthenticationHandler = null;
        } else {
            // FIXME: 23/05/16 onError here?
        }
    }

    public static void start() {
        impl.start();
    }

    public static void stop() {
        impl.stop();
    }

    /**
     * This interface is the contract for receiving the results of the fingerpring authentication
     * activity
     */
    public interface FingerLockResultCallback {

        /**
         * This callback method is called when figerprint authentication failed
         *
         * @param errorType error type
         * @param e         exception raised
         */
        void onFingerLockError(@FingerLock.FingerlockErrorState int errorType, Exception e);

        /**
         * This callback method is called when the fingerprint has been recognized and authenticated
         * correctly
         */
        void onFingerLockAuthenticationSucceeded();

        void onFingerLockReady();

        /**
         * This callback method is called when the FingerLock is ready and scanning for fingerprints.
         * The boolean param indicates whether password dialog should be used instead of fingerprint
         * because either a new fingerprint was registered or
         *
         * @param shouldUsePassword <code>true</code> when password should be used instead of fingerprint
         */
        void onFingerLockScanning(boolean shouldUsePassword);
    }


    /**
     * Error state
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FINGERPRINT_NOT_SUPPORTED, FINGERPRINT_NOT_RECOGNIZED, FINGERPRINT_PERMISSION_DENIED, FINGERPRINT_REGISTRATION_NEEDED, FINGERPRINT_INVALID_STATE, FINGERPRINT_ERROR_HELP, FINGERPRINT_UNRECOVERABLE_ERROR})
    public @interface FingerlockErrorState {
    }

    public static final int FINGERPRINT_NOT_SUPPORTED = 0;
    public static final int FINGERPRINT_NOT_RECOGNIZED = 1;
    public static final int FINGERPRINT_PERMISSION_DENIED = 2;
    public static final int FINGERPRINT_REGISTRATION_NEEDED = 3;
    public static final int FINGERPRINT_INVALID_STATE = 4;
    public static final int FINGERPRINT_ERROR_HELP = 5;
    public static final int FINGERPRINT_UNRECOVERABLE_ERROR = 6;

}
