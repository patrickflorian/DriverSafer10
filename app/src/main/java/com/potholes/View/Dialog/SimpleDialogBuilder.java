package com.potholes.View.Dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.potholes.driversafer.R;

/**
 * Created by Lelouch on 31/05/2018.
 */

public class SimpleDialogBuilder extends AlertDialog.Builder {


    public static final String DIALOG_STYLE_DANGER = "danger";
    public static final String DIALOG_STYLE_SUCCESS = "success";
    public static final String DIALOG_STYLE_WARNING = "warning";

    public Button positive_button;
    public Button negative_button;
    public String DialogStyle;
    public String title = "Alert";
    public String message = "do you want to close";
    ImageView icon;
    private View dialogView;

    private TextView message_view;
    private TextView title_view;

    public SimpleDialogBuilder(Context context, View view, String mesg, String dialogStyle) {
        super(context);
        dialogView = view;

        positive_button = view.findViewById(R.id.dialog_positive_btn);
        negative_button = view.findViewById(R.id.dialog_negative_btn);
        message_view = view.findViewById(R.id.et_name);
        message = mesg;
        message_view.setText(message);
        title_view = view.findViewById(R.id.dialog_title);
        icon = view.findViewById(R.id.ic);
        setDialogStyle(dialogStyle);

    }

    public void setPositiveButtonListener(String label, View.OnClickListener onClickListener) {
        positive_button.setOnClickListener(onClickListener);
        positive_button.setText(label);
    }

    public void setNegativeButtonListener(String label, View.OnClickListener onClickListener) {
        negative_button.setOnClickListener(onClickListener);
        negative_button.setText(label);
    }


    @Override
    public AlertDialog create() {
        setView(dialogView);
        title_view.setText(title);
        message_view.setText(message);
        return super.create();
    }

    @SuppressLint("ResourceAsColor")
    public void setDialogStyle(String dialogStyle) {
        if (dialogStyle == DIALOG_STYLE_DANGER) {
            title_view.setBackground(getContext().getDrawable(R.drawable.background_transparent_error));
            title_view.setTextColor(dialogView.getResources().getColor(R.color.colorDriverSafer_error));
            icon.setImageResource(R.drawable.ic_warning_black_24dp);
        }
        if (dialogStyle == DIALOG_STYLE_WARNING) {
            title_view.setBackground(getContext().getDrawable(R.drawable.background_transparent_warning));
            title_view.setTextColor(dialogView.getResources().getColor(R.color.colorDriverSafer_warning));
            icon.setImageResource(R.drawable.ic_warning);
        }
        if (dialogStyle == DIALOG_STYLE_SUCCESS) {
            title_view.setBackground(getContext().getDrawable(R.drawable.background_transparent_success));
            title_view.setTextColor(dialogView.getResources().getColor(R.color.colorDriverSafer_success));
            icon.setImageResource(R.drawable.ic_check_circle_black_24dp);
        }
    }

    public void hidePositiveButton(Boolean hide) {

        if (hide) positive_button.setVisibility(View.INVISIBLE);
    }


}
