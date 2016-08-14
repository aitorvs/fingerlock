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

import android.Manifest;
import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresPermission;

import com.aitorvs.android.fingerlock.FingerLock;
import com.aitorvs.android.fingerlock.FingerLockResultCallback;

@TargetApi(Build.VERSION_CODES.M)
class FingerprintAuthHandler extends FingerprintManager.AuthenticationCallback {

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
