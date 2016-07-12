package com.aitorvs.android.fingerlock;

/*
 * Copyright (C) 10/07/16 aitorvs
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
 * Library public interface
 */
public interface FingerLockLibraryCalls {
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

    void recreateKey(final Object listener);
}
