package com.meteoriteapps.android.destinationalarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class Destination_Dialog extends DialogFragment{

    private static final String TAG="Destination_Dialog";
    private EditText destname;
    private DialogListener mdialoglistener;

    public interface DialogListener{
        public void OnOkClicked();

    }

   /* @Override
    public void onStop() {
        super.onStop();
        if(MapActivity.cancel_press!=true){ MapActivity.cancel_press=true; }
    }*/


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mdialoglistener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_destination,null);
        destname= view.findViewById(R.id.dest_name);


        builder.setTitle("Enter Alarm name");
        builder.setIcon(R.drawable.ic_recents);
        builder.setView(view);


       builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {
               Log.d(TAG, "onClick: Cancel pressed");
               if(MapActivity.recents.getDname().isEmpty())
                   MapActivity.recents.setDname("");
               MapActivity.cancel_press=true;
               getDialog().dismiss();
           }
       });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: Getting dest");
                String dest = destname.getText().toString();
                if (!dest.isEmpty()) {
                    RecentsListFragment.mflag=false;
                    MapActivity.recents.setDname(dest);
                    MapActivity.selectedDestination = dest;
                    Log.d(TAG, "onClick: Destination Entered"+dest);
                    MapActivity.alarmActive = true;
                    if(MapActivity.recentsOpenFlag){
                        MapActivity.Ma.removerecentsFragment();
                    }
                    mdialoglistener.OnOkClicked();
                }
                else
                    MapActivity.cancel_press=true;
                getDialog().dismiss();
            }
        });


        return builder.create();




    }


}

