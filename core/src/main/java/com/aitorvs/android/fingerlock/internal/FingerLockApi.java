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
