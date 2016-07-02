package com.aitorvs.android.fingerlock.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.aitorvs.android.fingerlock.*;
import com.aitorvs.android.fingerlock.internal.FingerLockApi;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
@SuppressWarnings("ResourceType")
public class FingerprintDialog extends DialogFragment
        implements TextView.OnEditorActionListener, FingerLockResultCallback {

    // Tag to pass fragment request code argument
    private static final String ARG_REQUEST_CODE = "request_code";

    // Tag to pass fragment cancelable argument
    private static final String ARG_CANCELABLE = "cancelable";

    // Tag to pass fragment key name argument
    private static final String ARG_KEY_NAME = "key_name";

    // TAG to put/get params inside bundles
    private static final String TAG_STAGE = "stage";

    // fingerlock library object
    private FingerLockApi.FingerLockImpl mFingerLock;

    // reference to the caller context
    private Context mContext;

    public interface Callback {
        void onFingerprintDialogAuthenticated();

        void onFingerprintDialogVerifyPassword(FingerprintDialog dialog, String password);

        void onFingerprintDialogStageUpdated(FingerprintDialog dialog, Stage stage);

        void onFingerprintDialogCancelled();
    }

    static final long ERROR_TIMEOUT_MILLIS = 1600;
    static final long SUCCESS_DELAY_MILLIS = 1300;
    static final String TAG = FingerprintDialog.class.getSimpleName();

    private View mFingerprintContent;
    private View mBackupContent;
    private EditText mPassword;
    private CheckBox mUseFingerprintFutureCheckBox;
    private TextView mPasswordDescriptionTextView;
    private TextView mNewFingerprintEnrolledTextView;
    private ImageView mFingerprintIcon;
    private TextView mFingerprintStatus;
    private static InputMethodManager mInputMethodManager;

    private Stage mLastStage;
    private Stage mStage = Stage.FINGERPRINT;
    private Callback mCallback;

    public FingerprintDialog() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TAG_STAGE, mStage);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() == null || !getArguments().containsKey(ARG_KEY_NAME))
            throw new IllegalStateException("FingerprintDialog must be shown with show(Activity, String, int).");
        else if (savedInstanceState != null)
            mStage = (Stage) savedInstanceState.getSerializable(TAG_STAGE);
        setCancelable(getArguments().getBoolean(ARG_CANCELABLE, true));

        // create the FingerLock library instance
        mFingerLock = FingerLockApi.create();

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.sign_in)
                .customView(R.layout.fingerprint_dialog_container, false)
                .positiveText(android.R.string.cancel)
                .negativeText(R.string.use_password)
                .autoDismiss(false)
                .cancelable(getArguments().getBoolean(ARG_CANCELABLE, true))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.cancel();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (mStage == Stage.FINGERPRINT) {
                            goToBackup(materialDialog);
                        } else {
                            verifyPassword();
                        }
                    }
                }).build();

        final View v = dialog.getCustomView();
        assert v != null;
        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mBackupContent = v.findViewById(R.id.backup_container);
        mPassword = (EditText) v.findViewById(R.id.password);
        mPassword.setOnEditorActionListener(this);
        mPasswordDescriptionTextView = (TextView) v.findViewById(R.id.password_description);
        mUseFingerprintFutureCheckBox = (CheckBox) v.findViewById(R.id.use_fingerprint_in_future_check);
        mNewFingerprintEnrolledTextView = (TextView) v.findViewById(R.id.new_fingerprint_enrolled_description);
        mFingerprintIcon = (ImageView) v.findViewById(R.id.fingerprint_icon);
        mFingerprintStatus = (TextView) v.findViewById(R.id.fingerprint_status);
        mFingerprintStatus.setText(R.string.initializing);

        return dialog;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateStage(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callback)) {
            throw new IllegalStateException("Components showing a FingerprintDialog must implement FingerprintDialog.Callback.");
        }
        mCallback = (Callback) context;
        mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();

        if (arguments != null) {
            String keyName = arguments.getString(ARG_KEY_NAME, "");
            mFingerLock.register(mContext, keyName, this);
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerLock.unregister(this);
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause: called");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mCallback != null)
            mCallback.onFingerprintDialogCancelled();
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup(MaterialDialog dialog) {
        mStage = Stage.PASSWORD;
        updateStage(dialog);
        mPassword.requestFocus();
        // Show the keyboard.
        mPassword.postDelayed(mShowKeyboardRunnable, 500);
        // Fingerprint is not used anymore. Stop listening for it.
        mFingerLock.stop();
    }

    private void toggleButtonsEnabled(boolean enabled) {
        MaterialDialog dialog = (MaterialDialog) getDialog();
        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(enabled);
        dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(enabled);
    }

    private void verifyPassword() {
        toggleButtonsEnabled(false);
        mCallback.onFingerprintDialogVerifyPassword(this, mPassword.getText().toString());
    }

    public void notifyPasswordValidation(boolean valid) {
        final MaterialDialog dialog = (MaterialDialog) getDialog();
        final View positive = dialog.getActionButton(DialogAction.POSITIVE);
        final View negative = dialog.getActionButton(DialogAction.NEGATIVE);
        toggleButtonsEnabled(true);

        if (valid) {
            if (mStage == Stage.KEY_INVALIDATED &&
                    mUseFingerprintFutureCheckBox.isChecked()) {
                // Re-create the key so that fingerprints including new ones are validated.
                mFingerLock.recreateKey(this);
                mStage = Stage.FINGERPRINT;
            }
            mPassword.setText("");
            mCallback.onFingerprintDialogAuthenticated();
            dismiss();
        } else {
            mPasswordDescriptionTextView.setText(R.string.invalid_password);
            final int red = ContextCompat.getColor(getActivity(), R.color.material_red_500);
            MDTintHelper.setTint(mPassword, red);
            ((TextView) positive).setTextColor(red);
            ((TextView) negative).setTextColor(red);
        }
    }

    private final Runnable mShowKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            mInputMethodManager.showSoftInput(mPassword, 0);
        }
    };

    private void updateStage(@Nullable MaterialDialog dialog) {
        if (mLastStage == null || (mLastStage != mStage && mCallback != null)) {
            mLastStage = mStage;
            mCallback.onFingerprintDialogStageUpdated(this, mStage);
        }
        if (dialog == null)
            dialog = (MaterialDialog) getDialog();
        if (dialog == null) return;
        switch (mStage) {
            case FINGERPRINT:
                dialog.setActionButton(DialogAction.POSITIVE, android.R.string.cancel);
                dialog.setActionButton(DialogAction.NEGATIVE, R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                mBackupContent.setVisibility(View.GONE);
                break;
            case KEY_INVALIDATED:
                // Intentional fall through
            case PASSWORD:
                dialog.setActionButton(DialogAction.POSITIVE, android.R.string.cancel);
                dialog.setActionButton(DialogAction.NEGATIVE, android.R.string.ok);
                mFingerprintContent.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                if (mStage == Stage.KEY_INVALIDATED) {
                    // Fingerprint is not used anymore. Stop listening for it.
                    mFingerLock.stop();
                    mPasswordDescriptionTextView.setVisibility(View.GONE);
                    mNewFingerprintEnrolledTextView.setVisibility(View.VISIBLE);
                    mUseFingerprintFutureCheckBox.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            verifyPassword();
            return true;
        }
        return false;
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        KEY_INVALIDATED,
        PASSWORD
    }

    private void showError(CharSequence error) {
        if (getActivity() == null) return;
        mFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mFingerprintStatus.setText(error);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.warning_color));
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintStatus.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;
            mFingerprintStatus.setTextColor(ColorAttr.getColor(getActivity(), android.R.attr.textColorSecondary));
            mFingerprintStatus.setText(getResources().getString(R.string.fingerprint_hint));
            mFingerprintIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };

    // FingerLock callbacks

    @Override
    public void onFingerLockError(@FingerLock.FingerLockErrorState int errorType, Exception e) {
        switch (errorType) {

            case FingerLock.FINGERPRINT_ERROR_HELP:
                showError(e.getMessage());
                break;
            case FingerLock.FINGERPRINT_NOT_RECOGNIZED:
                showError(getResources().getString(R.string.fingerprint_not_recognized));
                break;
            case FingerLock.FINGERPRINT_NOT_SUPPORTED:
                goToBackup(null);
                break;
            case FingerLock.FINGERPRINT_REGISTRATION_NEEDED:
                mPasswordDescriptionTextView.setText(R.string.no_fingerprints_registered);
                goToBackup(null);
                break;
            case FingerLock.FINGERPRINT_PERMISSION_DENIED:
            case FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR:
                showError(e.getMessage());
                mFingerprintIcon.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goToBackup(null);
                    }
                }, ERROR_TIMEOUT_MILLIS);
                break;
        }
    }

    @Override
    public void onFingerLockAuthenticationSucceeded() {
        toggleButtonsEnabled(false);
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.success_color));
        mFingerprintStatus.setText(getResources().getString(R.string.fingerprint_success));
        mFingerprintIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onFingerprintDialogAuthenticated();
                dismiss();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    @Override
    public void onFingerLockReady() {
        mFingerLock.start();
    }

    @Override
    public void onFingerLockScanning(boolean invalidKey) {
        mFingerprintStatus.setText(R.string.fingerprint_hint);
        if (invalidKey)
            mStage = Stage.KEY_INVALIDATED;
        updateStage(null);

    }

    /**
     * Creates a builder for the {@link FingerprintDialog} dialog
     */
    public static class Builder {

        private String keyName;
        private int requestCode = -1;
        private boolean cancelable = true;
        private FragmentActivity context;

        /**
         * Set the caller context.
         *
         * @param context caller {@link Context}. Shall be {@link android.app.Activity} or
         *                {@link Fragment} and implement {@link Callback} interface
         * @param <T>
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public <T extends FragmentActivity & Callback> Builder with(@NonNull T context) {
            this.context = context;
            return this;
        }

        /**
         * Set the keyname
         *
         * @param keyName key string
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setKeyName(@NonNull String keyName) {
            this.keyName = keyName;
            return this;
        }

        /**
         * Set the request code
         *
         * @param requestCode positive integer number
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setRequestCode(@IntRange(from = 0, to = Integer.MAX_VALUE) int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        /**
         * Set whether the dialog is cancelable or not
         *
         * @param cancelable <code>true</code> if cancelable (default = true)
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * Call this method to show and get the {@link FingerprintDialog} reference
         *
         * @param <T>
         * @return {@link FingerprintDialog} dialog
         */
        @Nullable
        public <T extends FragmentActivity & Callback> FingerprintDialog show() {
            if (context == null || TextUtils.isEmpty(this.keyName) || requestCode < 0) {
                return null;
            }
            FingerprintDialog dialog = getVisible(context);
            if (dialog != null)
                dialog.dismiss();
            dialog = new FingerprintDialog();
            Bundle args = new Bundle();
            args.putString(ARG_KEY_NAME, keyName);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putBoolean(ARG_CANCELABLE, cancelable);
            dialog.setArguments(args);
            dialog.show(context.getSupportFragmentManager(), TAG);
            mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            return dialog;
        }

        private <T extends FragmentActivity> FingerprintDialog getVisible(T context) {
            Fragment frag = context.getSupportFragmentManager().findFragmentByTag(TAG);
            if (frag != null && frag instanceof FingerprintDialog)
                return (FingerprintDialog) frag;
            return null;
        }
    }
}