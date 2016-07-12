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

import android.content.Context;
import android.support.annotation.NonNull;
import com.aitorvs.android.fingerlock.FingerLockResultCallback;

/**
 * {@hide}
 */
public final class FingerLockApi {

    public static FingerLockImpl create() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return new FingerLockApi23();
        } else {
            // legacy stub implementation. Disable fingerprint
            return new FingerLockApiBase();
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
}
