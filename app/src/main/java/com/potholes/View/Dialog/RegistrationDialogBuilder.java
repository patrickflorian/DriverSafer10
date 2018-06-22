package com.potholes.View.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.potholes.driversafer.R;

/**
 * Created by Lelouch on 01/06/2018.
 */

public class RegistrationDialogBuilder extends AlertDialog.Builder {
    public static final String DIALOG_STYLE_DANGER = "danger";
    public static final String DIALOG_STYLE_SUCCESS = "success";
    public static final String DIALOG_STYLE_WARNING = "warning";
    public TextInputEditText name;
    public TextInputEditText email;
    public TextInputEditText cni;
    public TextInputEditText pass;
    public TextInputEditText confirm_pass;
    public View dialogView;
    public String emailVal = "";
    public String cniVal = "";
    public String password = "";
    public String confirm = "";
    public String nom = "";
    private Button positive_button;
    private Button negative_button;

    public RegistrationDialogBuilder(Context context, View view) {
        super(context);
        dialogView = view;

        positive_button = view.findViewById(R.id.dialog_positive_btn);
        negative_button = view.findViewById(R.id.dialog_negative_btn);

        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        cni = view.findViewById(R.id.cni);
        pass = view.findViewById(R.id.pass);
        confirm_pass = view.findViewById(R.id.confirm);

    }

    public void setNegativeButtonListener(String label, View.OnClickListener onClickListener) {
        negative_button.setOnClickListener(onClickListener);
        negative_button.setText(label);
    }

    public void setPositiveButtonListener(String label, View.OnClickListener onClickListener) {
        positive_button.setOnClickListener(onClickListener);
        positive_button.setText(label);
    }


    //retourne vrai en cas d'echec et faux en cas de reussitte
    public boolean attemptRegistration() {

        // Reset errors.
        name.setError(null);
        email.setError(null);
        cni.setError(null);
        pass.setError(null);
        confirm_pass.setError(null);

        // Store values at the time of the login attempt.

        emailVal = email.getText().toString();
        cniVal = cni.getText().toString();
        password = pass.getText().toString();
        confirm = confirm_pass.getText().toString();
        nom = name.getText().toString();

        View focusView = null;
        boolean cancel = false;
        //check valid name
        if (TextUtils.isEmpty(nom)) {
            name.setError(getContext().getString(R.string.error_field_required));
            focusView = name;
            cancel = true;
        }
        //check valid cni
        if (TextUtils.isEmpty(cniVal)) {
            cni.setError(getContext().getString(R.string.error_field_required));
            focusView = cni;
            cancel = true;
        }
        if (!isCniValid(cniVal)) {
            cni.setError("incorrect cni value");
            focusView = cni;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            pass.setError(getContext().getString(R.string.error_invalid_password));
            focusView = pass;
            cancel = true;
        }
        if (!TextUtils.isEmpty(confirm) && !isconfirmPasswordValid(password, confirm)) {
            confirm_pass.setError("do no match password field");
            focusView = confirm_pass;
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
            cancel = false;
        }
        return cancel;
    }


    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isCniValid(String cni) {
        return cni.length() >= 9;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 8;
    }

    private boolean isconfirmPasswordValid(String pass, String confirm) {
        return (pass.contentEquals(confirm));
    }


    @Override
    public AlertDialog create() {
        setView(dialogView);

        return super.create();
    }


}
