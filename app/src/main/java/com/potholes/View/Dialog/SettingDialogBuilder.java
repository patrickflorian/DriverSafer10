package com.potholes.View.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.potholes.db.Settings;
import com.potholes.driversafer.R;

/**
 * Created by Lelouch on 01/06/2018.
 */

public class SettingDialogBuilder extends AlertDialog.Builder {

    public static Button positive_button;
    public static Button negative_button;
    public static CheckBox notif_cb;
    public static CheckBox collaborate_cb;
    public static CheckBox camera_cb;
    public static SeekBar rayon_sb;

    private AsyncTask mRegistrationTask;
    private View dialogView;


    public SettingDialogBuilder(Context context, View view) {
        super(context);
        dialogView = view;

        positive_button = view.findViewById(R.id.dialog_positive_btn);
        negative_button = view.findViewById(R.id.dialog_negative_btn);

        notif_cb = view.findViewById(R.id.en_notif);
        notif_cb.setChecked(Settings.isNotification_enabled());
        notif_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.setNotification_enabled(b);
            }
        });

        collaborate_cb = view.findViewById(R.id.en_col);
        collaborate_cb.setChecked(Settings.isCollaborate_enabled());
        collaborate_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.setCollaborate_enabled(b);
            }
        });

        camera_cb = view.findViewById(R.id.en_cam);
        camera_cb.setChecked(Settings.isCamera_enabled());
        camera_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.setCamera_enabled(b);
            }
        });

        rayon_sb = view.findViewById(R.id.rayon);
        rayon_sb.setProgress((int) Settings.getRayonDetection());
        rayon_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Settings.setRayonDetection(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

    }

    public void setNegativeButtonListener(String label, View.OnClickListener onClickListener) {
        negative_button.setOnClickListener(onClickListener);
        negative_button.setText(label);
    }

    public void setPositiveButtonListener(String label, View.OnClickListener onClickListener) {
        positive_button.setOnClickListener(onClickListener);
        positive_button.setText(label);
    }

    /*
       public boolean attemptRegistration() {
           if (mRegistrationTask != null) {
               return false;
           }

          // Reset errors.
           name.setError(null);
           email.setError(null);
           cni.setError(null);
           pass.setError(null);
           confirm_pass.setError(null);

           // Store values at the time of the login attempt.

           String emailVal = email.getText().toString();
           String cniVal = cni.getText().toString();
           String password = pass.getText().toString();
           String confirm = confirm_pass.getText().toString();
           String nom = name.getText().toString();

           boolean cancel = false;
           View focusView = null;

           //check valid name
           if (TextUtils.isEmpty(nom)) {
               name.setError(getContext().getString(R.string.error_field_required));
               focusView = name;
               cancel = true;
           }
           // Check for a valid password, if the user entered one.
           if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
               pass.setError(getContext().getString(R.string.error_invalid_password));
               focusView = pass;
               cancel = true;
           }
           if (!TextUtils.isEmpty(password) && !isconfirmPasswordValid(password, confirm)) {
               pass.setError("do no match password field");
               focusView = pass;
               cancel = true;
           }

           // Check for a valid email address.
           if (TextUtils.isEmpty(emailVal)) {
               email.setError(getContext().getString(R.string.error_field_required));
               focusView = email;
               cancel = true;
           } else if (!isEmailValid(emailVal)) {
               this.email.setError(getContext().getString(R.string.error_invalid_email));
               focusView = email;
               cancel = true;
           }

           if (cancel) {
               // There was an error; don't attempt login and focus the first
               // form field with an error.
               focusView.requestFocus();
           } else {
               // Show a progress spinner, and kick off a background task to
               // perform the user login attempt.

           }
           return cancel;
       }*/
    @Override
    public AlertDialog create() {
        setView(dialogView);

        return super.create();
    }


}
