package com.aitorvs.android.fingerlock.internal;

/*
 * Copyright (C) 30/06/16 aitorvs
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.aitorvs.android.fingerlock.FingerLock;
import com.aitorvs.android.fingerlock.FingerLockResultCallback;

import java.security.InvalidParameterException;

/**
 * {@hide}
 */
public final class FingerLockApi {
    private static FingerprintAuthHandler mAuthenticationHandler;

    public static FingerLockImpl create() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return new FingerLockApi.Api23FingerLockImpl();
        } else {
            // legacy stub implementation. Disable fingerprint
            return new FingerLockApi.BaseFingerLockImpl();
        }
    }

    /**
     * {@hide}
     */
    public interface FingerLockImpl {
        /**
         * {@hide}
         * Returns <code>true</code> when fingerprint authentication is available and supported
         *
         * @return <code>true</code> when supported
         */
        boolean isFingerprintAuthSupported();

        /**
         * {@hide}
         * Returns <code>true</code> when the user has at least one fingerprint registered
         *
         * @return <code>true</code> when at least one fingerprint is registered
         */
        boolean isFingerprintRegistered();

        /**
         * {@hide}
         * Call this method to start fingerprint scanning
         */
        void start();

        /**
         * {@hide}
         * Call this method to force stopping fingerprint scanning
         */
        void stop();

        /**
         * {@hide}
         * Register a fingerprint activity listener
         *
         * @param context  caller context
         * @param keyName  key name
         * @param callback callbacks
         */
        void register(@NonNull Context context, @NonNull final String keyName, @NonNull FingerLockResultCallback callback);

        /**
         * {@hide}
         * Call this method to avoid any memory leakage. Good place is <code>onPause</code>
         *
         * @param listener previously registered listener
         * @return <code>true</code> when executed successfully
         */
        boolean unregister(@NonNull FingerLockResultCallback listener);

        boolean inUseBy(FingerLockResultCallback listener);

        /**{@hide}
         * Recreate the secret key.
         */
        void recreateKey(FingerLockResultCallback listener);
    }

    /**{@hide}
     * Baseline implementation, pre-M
     */
    static class BaseFingerLockImpl implements FingerLockImpl {


        @Override
        public boolean isFingerprintAuthSupported() {
            return false;
        }

        @Override
        public boolean isFingerprintRegistered() {
            return false;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(@NonNull Context context, @NonNull final String keyName, @NonNull FingerLockResultCallback callback) {
            //noinspection ConstantConditions
            if (callback != null) {
                // error out to inform the user
                callback.onFingerLockError(FingerLock.FINGERPRINT_NOT_SUPPORTED, new Exception("Fingerprint authentication not supported in this device"));
            }
        }

        @Override
        public boolean unregister(@NonNull FingerLockResultCallback listener) {
            return true;
        }

        @Override
        public boolean inUseBy(FingerLockResultCallback listener) {
            return false;
        }

        @Override
        public void recreateKey(FingerLockResultCallback listener) {
        }
    }

    /**{@hide}
     * M implementation
     */
    @TargetApi(Build.VERSION_CODES.M)
    static class Api23FingerLockImpl implements FingerLockImpl {

        private static final String TAG = Api23FingerLockImpl.class.getSimpleName();
        private Context mContext;
        private Key mKey;
        private FingerLockResultCallback mCallback;

        // specific of the implementation for API >=23
        private FingerprintManager mFingerprintManager;

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
                mCallback.onFingerLockError(FingerLock.FINGERPRINT_NOT_SUPPORTED, new Exception("Fingerprint authentication not supported in this device"));
            } else if (mAuthenticationHandler != null && mAuthenticationHandler.isStarted()) {
                // auth handler already listening...do nothing
            } else {
                try {
                    mAuthenticationHandler = new FingerprintAuthHandler(null, mCallback);
                    //noinspection MissingPermission
                    mAuthenticationHandler.start(mFingerprintManager);

                    mCallback.onFingerLockScanning(!mKey.cipherInit());
                } catch (NullKeyException e) {
                    // key is not yet created. Create it and retry
                    mKey.recreateKey();
                    try {
                        mCallback.onFingerLockScanning(!mKey.cipherInit());
                    } catch (NullKeyException e1) {
                        // something went wrong unregister and notify
                        stop();
                        mCallback.onFingerLockError(FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR, new Exception("Key creation failed."));
                    }
                }
            }
        }

        @Override
        public void stop() {
            if (mAuthenticationHandler != null) {
                // cancel and flag it as self cancelled
                mAuthenticationHandler.stop(true);
            }
        }

        @Override
        public void register(@NonNull Context context, @NonNull final String keyName, @NonNull FingerLockResultCallback callback) {
            // double check
            //noinspection ConstantConditions
            if (context == null || callback == null || keyName == null) {
                throw new InvalidParameterException("Invalid or null input parameters");
            } else if (mCallback != null) {
                // already registered, force clean unregister
                forceUnregister();
            }
            Log.e(TAG, "Registering " + keyName);

            mContext = context;
            mCallback = callback;
            mKey = new Key(keyName);

            mFingerprintManager = getFingerprintManager();

            if (!isFingerprintAuthSupported()) {
                callback.onFingerLockError(FingerLock.FINGERPRINT_NOT_SUPPORTED, new Exception("Fingerprint authentication not supported in this device"));
            } else if (!isFingerprintRegistered()) {
                callback.onFingerLockError(FingerLock.FINGERPRINT_REGISTRATION_NEEDED, new Exception("No fingerprints registered in this device"));
            } else {
                // all systems Go!
                callback.onFingerLockReady();
            }
        }

        @Override
        public void recreateKey(final FingerLockResultCallback listener) {
            if (mCallback == listener) {
                mKey.recreateKey();
            } else {
                Log.e(TAG, "recreateKey: non-registered listener trying to recreate key");
            }
        }

        @Override
        public boolean inUseBy(FingerLockResultCallback listener) {
            Log.e(TAG, "inUseBy: " + (mCallback == listener ? "true" : "false"));
            return mContext != null && mCallback != null && mCallback == listener;
        }

        @Override
        public boolean unregister(@NonNull FingerLockResultCallback listener) {
            if (mCallback == listener) {
                mCallback = null;
                mContext = null;

                stop();
                Log.e(TAG, "unregister: OK");
                return true;
            }

            return false;
        }

        private void forceUnregister() {
            mCallback = null;
            mContext = null;

            stop();
            Log.e(TAG, "Force unregister: OK");
        }

        private void invalidContext() throws IllegalStateException {
            if (mContext == null) {
                throw new IllegalStateException("Callback listener not registered");
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Nullable
        private FingerprintManager getFingerprintManager() {

            if (mFingerprintManager == null) {
                invalidContext();

                mFingerprintManager = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
            }

            return mFingerprintManager;
        }
    }

}
