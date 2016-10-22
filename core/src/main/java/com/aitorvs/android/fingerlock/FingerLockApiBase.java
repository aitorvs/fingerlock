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
import android.support.annotation.NonNull;

class FingerLockApiBase implements FingerLockApi.FingerLockImpl {
    @Override
    public boolean isFingerprintAuthSupported() {
        return false;
    }

    @Override
    public boolean isFingerprintRegistered() {
        return false;
    }

    @Override
    public boolean isFingerprintPermissionGranted() {
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void register(@NonNull Context context, @NonNull final String keyName,
                         @NonNull FingerLockResultCallback callback) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(keyName);
        Preconditions.checkNotNull(callback);

        callback.onFingerLockError(FingerLock.FINGERPRINT_NOT_SUPPORTED,
                new Exception("Fingerprint authentication not supported in this device"));
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
