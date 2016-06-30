package com.aitorvs.android.fingerlock;

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
