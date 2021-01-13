package com.meteoriteapps.android.destinationalarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class About_Dialog extends DialogFragment {

    private static final String TAG = "about_Dialog";
    private Button close;
    private TextView pp;


    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_about, null);
        close = view.findViewById(R.id.about_close);
        pp = view.findViewById(R.id.privacyp);
        pp.setMovementMethod(new ScrollingMovementMethod());

        builder.setView(view);


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });


        return builder.create();


    }
}
