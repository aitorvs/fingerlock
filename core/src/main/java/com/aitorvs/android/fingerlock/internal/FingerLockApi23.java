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

package com.aitorvs.android.fingerlock.internal;

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

@TargetApi(Build.VERSION_CODES.M)
class FingerLockApi23 implements FingerLockApi.FingerLockImpl {

    private static final String TAG = FingerLockApi23.class.getSimpleName();
    private static FingerprintAuthHandler mAuthenticationHandler;
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

                mCallback.onFingerLockScanning(!mKey.isKeyValid());
            } catch (NullKeyException e) {
                // key is not yet created. Create it and retry
                mKey.recreateKey();
                try {
                    mCallback.onFingerLockScanning(!mKey.isKeyValid());
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
    }}
