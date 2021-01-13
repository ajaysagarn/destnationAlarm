package com.meteoriteapps.android.destinationalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Alarm Dismissed!", Toast.LENGTH_SHORT).show();
        RecentsListFragment.resumeclicked = true;
        MapActivity.Ma.OnResumeClicked(true);

        if (Alarm.alarm_active) {
            Alarm.finishAlarm();
        }
        if (MapActivity.recentsOpenFlag) {
            MapActivity.Ma.removerecentsFragment();
        }
    }
}
