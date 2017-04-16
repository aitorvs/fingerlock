# FingerLock
FingerLock is a library designed to make fingerprint authentication an easy task for Android developers.

**Note**: this library is powered by [material-dialogs](https://github.com/afollestad/material-dialogs),
depending on this library will automatically depend on Material Dialogs.

Check the related post on [Medium.](https://medium.com/@aitorvs/android-fingerprint-authentication-44c047179d9a#.6kxauxim3)

# Gradle Dependency

[![](https://jitpack.io/v/com.github.aitorvs.fingerlock/core.svg)](https://jitpack.io/#com.github.aitorvs.fingerlock/core)
[![](https://jitpack.io/v/com.github.aitorvs.fingerlock/dialog.svg)](https://jitpack.io/#com.github.aitorvs.fingerlock/dialog)
[![Build Status](https://travis-ci.org/aitorvs/fingerlock.svg)](https://travis-ci.org/aitorvs/fingerlock)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FingerLock-green.svg?style=true)](https://android-arsenal.com/details/1/4330)

## Repository

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

## Dependencies

### Core

The *core* module contains the core class `FingerLock` to perform full fingerprint authentication.

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs.fingerlock:core:1.0.0'
}
```

### Fingerprint Dialog Extension

The *dialog* extension module is powered by [material-dialogs](https://github.com/afollestad/material-dialogs)
library and provides an out-of-the-box-ready material authentication dialog based on the design guidelines on fingerprint
authentication.

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs.fingerlock:dialog:1.0.0'
}
```
### Manifest

The library requires the `USE_FINGERPRINT` permission. Just add the following to your `AndroidManifest.xml` file
 
 ```
 <uses-permission android:name="android.permission.USE_FINGERPRINT"/>
 ```

# Core

The core module requires Android M and above and also a device supporting fingerprint sensor. If you target
devices before M or not having fingerprint sensors, use the dialog extension.

## (Core) FingerLock in 4 steps

### 1. Implement the `FingerLockResultCallback` inside your Activity/Fragment

```java
public class MainActivity extends AppCompatActivity
        implements FingerLockResultCallback {
        //...
}
```

### 2. Initialize the library (e.g. inside `onCreate()` method)


```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        private FingerLockManager mFingerLockManager;
        
        // ... Some code here

        // returns a manager object to manage the fingerprint authentication
        mFingerLockManager = FingerLock.initialize(this, KEY_NAME);
    }
```

### 3. Start the fingerprint scanning

It is as simple as calling the `start()` method.

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ... Some code here


        mButton = (Button) findViewById(R.id.button_authenticate);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFingerLockManager.start();
            }
        });

        // ... Some more code here
    }
```

### 4. Handle the callbacks

#### Library is ready

```java
    @Override
    public void onFingerLockReady() {
        // Called right after registration if the device supports fingerprint authentication.
        // This is normally a good place to call mFingerLockManager.start()
    }
```

This method is called when the library finishes the registration process successfully.

This is normally a good place to call `start()` so start the fingerprint(s) scanning process.


#### Fingerprint(s) Scanning

```java
    @Override
    public void onFingerLockScanning(boolean invalidKey) {
        // Called to notify the fingerprint scanning is started successfully as a result of mFingerLockManager.start()
        // The 'invalidKey' parameter will be true when the key is no longer valid. This happens when
        // the user disables the lock screen, resets or adds a new fingerprint after the key was created (i.e. mFingerLockManager.register())
    }
```

The callback `onFingerLockScanning(boolean)` is called when the library has started
scanning for fingerprint(s) to authenticate the user. The input parameter `invalidKey` flags when the key provided
during registration is no longer valid. Either because the user disabled the lock screen, device reset or
a new fingerprint was added.
For security purposes it is recommended to stop scanning fingerpring(s) calling `stop()` and
fallback to any other type of authentication (i.e. password) that authenticates the user and let
them use fingerprint the next time.

#### Authenticated

```java
    @Override
    public void onFingerLockAuthenticationSucceeded() {
        // Called when the user fingerprint has been correctly authenticated
    }
```

This method is called upon successful fingerprint authentication.

#### Error

The callback provides error events that may happen throughout the fingerprint authentication process.

```java
    @Override
    public void onFingerLockError(@FingerLock.FingerLockErrorState int errorType, Exception e) {
        // Called every time there's an error at any stage during the authentication

        switch (errorType) {
            case FingerLock.FINGERPRINT_PERMISSION_DENIED:
                // USE_PERMISSION is denied by the user, fallback to password authentication
                break;
            case FingerLock.FINGERPRINT_ERROR_HELP:
                // there's some kind of recoverable error that can be solved. Call e.getMessage()
                // to get help about the error
                break;
            case FingerLock.FINGERPRINT_NOT_RECOGNIZED:
                // The fingerprint was not recognized, try another one
                break;
            case FingerLock.FINGERPRINT_NOT_SUPPORTED:
                // Fingerprint authentication is not supported by the device. Fallback to password
                // authentication
                break;
            case FingerLock.FINGERPRINT_REGISTRATION_NEEDED:
                // There are no fingerprints registered in this device.
                // Go to Settings -> Security -> Fingerprint and register at least one
                break;
            case FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR:
                // Unrecoverable internal error occurred. Unregister and register back
                break;
        }
    }
```

# Dialog extension

The *dialog* extension module provides an out-of-the-box-ready material design dialog implementation
that follows the design rules for fingerprint authentication and handles the core library module for
you.
The dialog extension **automatically falls back to password authentication** when the device does not 
support fingerprint authentication.


```java
public class MainActivity extends AppCompatActivity implements FingerprintDialog.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show fingerprint dialog
        FingerprintDialog.show(MainActivity.this, KEY_NAME, REQUEST_CODE);
    }

    @Override
    public void onFingerprintDialogAuthenticated() {
        // Authentication is successful
    }

    @Override
    public void onFingerprintDialogVerifyPassword(final FingerprintDialog dialog, final String password) {
        // Password verification has been requested. Use this method to verify the `password` passed
        // as parameter against your backend

        // Simulate exchange with backend
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.notifyPasswordValidation(password.equals("aitorvs"));
            }
        }, 1500);
    }

    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
        Log.d(TAG, "Dialog stage: " + stage.name());
    }

    public enum Stage {
        FINGERPRINT,        // fingerprint authentication allowed
        KEY_INVALIDATED,    // key invalidated, password to be required
        PASSWORD            // password authentication selected by the user
    }

    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
    }
}
```

## (Dialog) FingerLock in 3 steps

### 1. Implement the dialog callbacks

```java
public class MainActivity extends AppCompatActivity
        implements FingerprintDialog.Callback {
    //...Looooots of code here

}
```

### 2. Create and show the dialog

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create and show the fingerprint dialog using the Builder
        new FingerprintDialog.Builder()
                .with(MainActivity.this)    // context, must call
                .setKeyName(KEY_NAME)       // String key name, must call
                .setRequestCode(69)         // request code identifier, must call
                .show();                    // show the dialog
    }
```

Additionally, one could also call `setCancelable(Boolean)` in the builder
to set the dialog as cancelable or not. This call is optional and default
value is set to `true`.

### 3. Handle callbacks

#### Authenticated

```java
    @Override
    public void onFingerprintDialogAuthenticated() {
        // Authentication is successful
    }
```

#### Verify password

```java
    @Override
    public void onFingerprintDialogVerifyPassword(final FingerprintDialog dialog, final String password) {
        // Password verification has been requested. Use this method to verify the `password` passed
        // as parameter against your backend

        // Simulate exchange with backend
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.notifyPasswordValidation(password.equals("aitorvs"));
            }
        }, 1500);
    }
```

Called when password verification is required. Either because triggered automatically when the provided
key is no longer valid or because the user required so.

Once the password is verified, notify the dialog calling `dialog.notifyPasswordValidation(boolean)`.

#### State updated

```java
    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
        Log.d(TAG, "Dialog stage: " + stage.name());
    }
```

Method called at every internal stage change. Possible states are:

```java
    public enum Stage {
        FINGERPRINT,        // fingerprint authentication allowed
        KEY_INVALIDATED,    // key invalidated, password to be required
        PASSWORD            // password authentication selected by the user
    }
```

It is normally not necessary to act on any of the stages.

#### Authentication cancelled

Called when the user cancels the authentication dialog.

```java
    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
    }
```

# License
```
The MIT License (MIT)

Copyright (c) 2016 Aitor Viana Sanchez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```