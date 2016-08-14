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
 * This interface is the contract for receiving the results of the fingerprint authentication
 * activity
 */
public interface FingerLockResultCallback {

    /**
     * This callback method is called when figerprint authentication failed
     *
     * @param errorType error type
     * @param e         exception raised
     */
    void onFingerLockError(@FingerLock.FingerLockErrorState int errorType, Exception e);

    /**
     * This callback method is called when the fingerprint has been recognized and authenticated
     * correctly
     */
    void onFingerLockAuthenticationSucceeded();

    /**
     * This callback method is called when the library is ready
     */
    void onFingerLockReady();

    /**
     * This callback method is called when the FingerLock is ready and scanning for fingerprints.
     * The boolean param indicates when the provide key (if provided) is invalid either because
     * the user disables the lock screen or resets or adds new fingerprint(s) after the key
     * was created.
     * In such situation it is recommended to either re-register with a new unique key or use
     * password instead for increase security.
     *
     * @param invalidKey <code>true</code> when the key store key is invalid/obsolete because
     */
    void onFingerLockScanning(boolean invalidKey);
}
