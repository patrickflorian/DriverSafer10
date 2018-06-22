package com.potholes.View.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.potholes.db.Potholes;
import com.potholes.driversafer.R;

/**
 * Created by Lelouch on 17/06/2018.
 */

public class EditPotholesDialog extends AlertDialog.Builder {
    public Switch etat_view;
    public TextInputEditText lat_view;
    public TextInputEditText lng_view;
    public TextInputEditText surface_view;
    public TextInputEditText prof_view;
    public Potholes potholes;
    private View dialogView;
    private Button positive_button;
    private Button negative_button;


    public EditPotholesDialog(Context context, View view, Potholes p) {
        super(context);
        dialogView = view;

        etat_view = view.findViewById(R.id.etat);
        lat_view = view.findViewById(R.id.lat);
        lng_view = view.findViewById(R.id.lng);
        surface_view = view.findViewById(R.id.surface);
        prof_view = view.findViewById(R.id.profondeur);
        this.potholes = p;

        //init all fields contains
        etat_view.setChecked(!p.isEtat());
        lat_view.setText(String.valueOf(p.getLat()));
        lng_view.setText(String.valueOf(p.getLng()));
        surface_view.setText(String.valueOf(p.getSurface()));
        prof_view.setText(String.valueOf(p.getProfondeur()));

        positive_button = view.findViewById(R.id.dialog_positive_btn);
        negative_button = view.findViewById(R.id.dialog_negative_btn);
    }

    public void setNegativeButtonListener(String label, View.OnClickListener onClickListener) {
        negative_button.setOnClickListener(onClickListener);
        negative_button.setText(label);
    }

    public void setPositiveButtonListener(String label, View.OnClickListener onClickListener) {
        positive_button.setOnClickListener(onClickListener);
        positive_button.setText(label);
    }

    @Override
    public AlertDialog create() {
        setView(dialogView);

        return super.create();
    }

    public boolean attemptEdit() {

        // Reset errors.
        lat_view.setError(null);
        lng_view.setError(null);
        surface_view.setError(null);
        prof_view.setError(null);

        String lat;
        String lng;
        String surface;
        String profondeur;

        // Store values at the time of the login attempt.

        lat = (lat_view.getText().toString());
        lng = (lng_view.getText().toString());
        surface = (surface_view.getText().toString());
        profondeur = (prof_view.getText().toString());

        View focusView = null;
        boolean cancel = false;
        //check valid lat
        if (TextUtils.isEmpty(lat)) {
            lat_view.setError(getContext().getString(R.string.error_field_required));
            focusView = lat_view;
            cancel = true;
        }
        //check valid lng
        if (TextUtils.isEmpty(lng)) {
            lng_view.setError(getContext().getString(R.string.error_field_required));
            focusView = lng_view;
            cancel = true;
        }
        //check valid surface
        if (TextUtils.isEmpty(surface)) {
            surface_view.setError(getContext().getString(R.string.error_field_required));
            focusView = surface_view;
            cancel = true;
        }
        //check valid lng
        if (TextUtils.isEmpty(profondeur)) {
            prof_view.setError(getContext().getString(R.string.error_field_required));
            focusView = prof_view;
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
            potholes.setEtat(!etat_view.isChecked());
            potholes.setLat(Double.valueOf(lat));
            potholes.setLng(Double.valueOf(lng));
            potholes.setSurface(Double.valueOf(surface));
            potholes.setProfondeur(Double.valueOf(profondeur));
        }
        return cancel;
    }


}
