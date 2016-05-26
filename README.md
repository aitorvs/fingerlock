#FingerLock
FingerLock is a library designed to make fingerprint authentication an easy task for Android developers.

**Note**: this library is powered by [material-dialogs](https://github.com/afollestad/material-dialogs),
depending on this library will automatically depend on Material Dialogs.

# Gradle Dependency

[![Release](https://img.shields.io/github/release/aitorvs/fingerlock.svg?label=jitpack)](https://jitpack.io/#aitorvs/fingerlock)

##Repository

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

## Dependencies

###Core

The *core* module contains the core class `FingerLock` to perform full fingerprint authentication.

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs.fingerlock:core::x.x.x'
}
```

###Fingerprint Dialog Extension

The *dialog* extension module is powered by [material-dialogs](https://github.com/afollestad/material-dialogs)
library and provides an out-of-the-box-ready material authentication dialog based on the design guidelines on fingerprint
authentication.

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs.fingerlock:dialog::x.x.x'
}
```
# Core

## (Core) FingerLock in 4 steps

###1. Register your fingerprint listener component (recommended to use `onResume`)

```java
    @Override
    protected void onResume() {
        super.onResume();

        // register and use a key to increase security
        FingerLock.register(this, KEY_NAME, this);
    }
```

The first parameter is the `Context`. It can be either the caller context but also application context.
The second parameter shall be a unique non-empty `String` that severs as the key name for the encryption cipher.
The last parameter is the callback where the fingerprint events will land on.

###2. Start the fingerprint scanning

It is as simple as calling the `start()` method.

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ... Some code here


        mButton = (Button) findViewById(R.id.button_authenticate);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerLock.start();
            }
        });

        // ... Some more code here
    }
```

###3. Handle the callbacks

####Library is ready

```java
    @Override
    public void onFingerLockReady() {
        // Called right after registration if the device supports fingerprint authentication.
        // This is normally a good place to call FingerLock.start()
    }
```

This method is called when the library finishes the registration process successfully.

This is normally a good place to call `FingerLock.start()` so start the fingerprint(s) scanning process.


####Fingerprint(s) Scanning

```java
    @Override
    public void onFingerLockScanning(boolean invalidKey) {
        // Called to notify the fingerprint scanning is started successfully as a result of FingerLock.start()
        // The 'invalidKey' parameter will be true when the key is no longer valid. This happens when
        // the user disables the lock screen, resets or adds a new fingerprint after the key was created (i.e. FingerLock.register())
    }
```

The callback `onFingerLockScanning(boolean)` is called when the library has started
scanning for fingerprint(s) to authenticate the user. The input parameter `invalidKey` flags when the key provided
during registration is no longer valid. Either because the user disabled the lock screen, device reset or
a new fingerprint was added.
For security purposes it is recommended to stop scanning fingerpring(s) calling `FingerLock.stop()` and
fallback to any other type of authentication (i.e. password) that authenticates the user and let
them use fingerprint the next time.

####Authenticated

```java
    @Override
    public void onFingerLockAuthenticationSucceeded() {
        // Called when the user fingerprint has been correctly authenticated
    }
```

This method is called upon successful fingerprint authentication.

####Error

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

###4. Unregister when done

Ensure to unregister the component to avoid memory leaks (recommended to be done in `onPause()`)

```java
    @Override
    protected void onPause() {
        super.onPause();

        // unregister to stop receiving fingerprint events
        FingerLock.unregister(this);
    }
```

# Dialog extension

The *dialog* extension module provides an out-of-the-box-ready material design dialog implementation
that follows the design rules for fingerprint authentication and handles the core library module for
you.

## (Dialog) FingerLock in 3 steps

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

### 1. Implement the dialog callbacks

```java
public class MainActivity extends AppCompatActivity
        implements FingerprintDialog.Callback {
    //...Looooots of code here

}
```

### 2. Show the dialog

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show fingerprint dialog
        FingerprintDialog.show(MainActivity.this, KEY_NAME, REQUEST_CODE);
    }
```

The first parameter is the `Context` of the caller or application context.
The second parameter shall be a unique non-empty `String` that severs as the key name for the encryption cipher.
The third parameter is a positive integer value that represents a request code.

### 3. Handle callbacks

####Authenticated

```java
    @Override
    public void onFingerprintDialogAuthenticated() {
        // Authentication is successful
    }
```

####Verify password

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

####State updated

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

####Authentication cancelled

Called when the user cancels the authentication dialog.

```java
    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
    }
```