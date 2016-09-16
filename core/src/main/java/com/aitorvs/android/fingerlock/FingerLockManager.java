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

/**
 * FingerLock utility to manage state and behavior of the {@linkplain FingerLock} library.
 */
public interface FingerLockManager {
    /**
     * Returns <code>true</code> when fingerprint authentication is available and supported
     *
     * @return <code>true</code> when supported
     */
    boolean isFingerprintAuthSupported();

    /**
     * Returns <code>true</code> when the user has at least one fingerprint registered
     *
     * @return <code>true</code> when at least one fingerprint is registered
     */
    boolean isFingerprintRegistered();

    /**
     * Call this method to start fingerprint scanning
     */
    void start();

    /**
     * Call this method to force stopping fingerprint scanning
     */
    void stop();

    /**
     * Call this method to re-create the keys so that new (added) fingerprints are validated.
     *
     * @param listener instance that implements {@linkplain FingerLockResultCallback} interface
     */
    void recreateKey(final FingerLockResultCallback listener);
}
