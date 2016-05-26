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

    compile 'com.github.aitorvs:fingerlock:x.x.x'
}
```

###Fingerprint Dialog Extension

The *dialog* extension module is powered by [material-dialogs](https://github.com/afollestad/material-dialogs)
library and provides an out-of-the-box-ready material authentication dialog based on the design guidelines on fingerprint
authentication.

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs:fingerlock:dialog:x.x.x'
}
```


