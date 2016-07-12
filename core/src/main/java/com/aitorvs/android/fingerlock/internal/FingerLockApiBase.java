package com.aitorvs.android.fingerlock.internal;

/*
 * Copyright (C) 12/07/16 aitorvs
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
import android.support.annotation.NonNull;

import com.aitorvs.android.fingerlock.FingerLock;
import com.aitorvs.android.fingerlock.FingerLockResultCallback;

public class FingerLockApiBase implements FingerLockApi.FingerLockImpl {
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
