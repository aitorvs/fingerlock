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

import android.Manifest;
import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresPermission;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthHandler extends FingerprintManager.AuthenticationCallback {

    private final FingerprintManager.CryptoObject mCryptoObject;
    private CancellationSignal mCancellationSignal;
    private FingerLockResultCallback mCallback;

    // flags when the operation is canceled internally
    private boolean mSelfCancelled;

    FingerprintAuthHandler(FingerprintManager.CryptoObject cryptoObject, FingerLockResultCallback callback) {
        mCryptoObject = cryptoObject;
        mCallback = callback;
    }

    public boolean isReady() {
        return mCancellationSignal == null;
    }

    public boolean isStarted() {
        // authentication already started and scanning
        return mCancellationSignal != null;
    }

    @RequiresPermission(Manifest.permission.USE_FINGERPRINT)
    public void start(FingerprintManager fpm) {
        if (fpm == null || mCallback == null) {
            // FIXME: 23/05/16 report error?
            return;
        }
        mSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        fpm.authenticate(mCryptoObject, mCancellationSignal, 0 /* flags */, this, null);;
    }

    public void stop(boolean self) {
        if (mCancellationSignal != null) {
            mSelfCancelled = self;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }
    
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (mCallback != null && !mSelfCancelled) {
            mCallback.onFingerLockError(FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR, new Exception(errString.toString()));
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        if (mCallback != null) {
            mCallback.onFingerLockError(FingerLock.FINGERPRINT_ERROR_HELP, new Exception(helpString.toString()));
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (mCallback != null) {
            mCallback.onFingerLockAuthenticationSucceeded();
        }
        // auto stop
        stop(true);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (mCallback != null) {
            mCallback.onFingerLockError(FingerLock.FINGERPRINT_NOT_RECOGNIZED, new Exception("Fingerprint not recognized, try again."));
        }
    }
}
