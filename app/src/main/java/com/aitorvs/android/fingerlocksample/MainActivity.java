package com.aitorvs.android.fingerlocksample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aitorvs.android.fingerlock.FingerLock;


public class MainActivity extends AppCompatActivity implements FingerLock.FingerLockResultCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mStatus;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatus = (TextView) findViewById(R.id.status);
        mButton = (Button) findViewById(R.id.beginAuthentication);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int granted = ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT);
            if (granted != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, 69);
        }

        FingerLock.register(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FingerLock.unregister(this);
    }

    @Override
    public void onFingerLockError(@FingerLock.FingerlockErrorState int errorType, Exception e) {
        switch (errorType) {
            case FingerLock.FINGERPRINT_PERMISSION_DENIED:
            case FingerLock.FINGERPRINT_ERROR_HELP:
            case FingerLock.FINGERPRINT_INVALID_STATE:
            case FingerLock.FINGERPRINT_NOT_RECOGNIZED:
            case FingerLock.FINGERPRINT_NOT_SUPPORTED:
            case FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
            case FingerLock.FINGERPRINT_REGISTRATION_NEEDED:
                break;
        }
    }

    @Override
    public void onFingerLockAuthenticationSucceeded() {
        mStatus.setText(R.string.status_authenticated);

        // Setup button to start listening again
        mButton.setText(R.string.start_scanning);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerLock.start();
            }
        });
    }

    @Override
    public void onFingerLockReady() {
        mStatus.setText("Fingerprint ready");
        mButton.setText(R.string.start_scanning);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerLock.start();
            }
        });
        mButton.setEnabled(true);
    }

    @Override
    public void onFingerLockScanning(boolean newFingerprint) {
        // clicking the button will stop the scanning
        mButton.setText(R.string.stop_scanning);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop listening
                FingerLock.stop();
                mStatus.setText(R.string.status_ready);
                // Clicking the button again will start listening again
                mButton.setText(R.string.start_scanning);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FingerLock.start();
                    }
                });
            }
        });

        mStatus.setText(newFingerprint ? R.string.status_scanning_new : R.string.status_scanning);
    }
}
