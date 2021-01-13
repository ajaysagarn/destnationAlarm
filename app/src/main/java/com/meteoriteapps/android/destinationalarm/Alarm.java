package com.meteoriteapps.android.destinationalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class Alarm extends AppCompatActivity {
    private static final String TAG = "LocationTrackService";
    public static boolean alarm_active = false;
    private static Vibrator v;
    private static MediaPlayer r;
    private static Alarm alarm;
    AudioManager audioManager;
    private Button dismiss;
    private MapActivity m;
    private Uri alert;
    private double distance;
    private TextView message;
    private View mContentView;
    private SharedPreferences shapref;
    private boolean headphones;
    private String unit = "meters";

    public static void finishAlarm() {
        v.cancel();
        r.stop();
        alarm.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarm = this;
        alarm_active = true;
        shapref = PreferenceManager.getDefaultSharedPreferences(this);


        m = MapActivity.Ma;
        mContentView = new View(this);
        headphones = shapref.getBoolean("headphone_pref", true);


        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Bundle extra = getIntent().getExtras();
        distance = extra.getDouble("distance");
        if (distance >= 1000) {
            distance = distance / 1000;
            unit = "KM";
        }
        message = (TextView) findViewById(R.id.message);
        message.setText("Destination is " + (int) distance + "" + unit + " away!");

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        dismiss = (Button) findViewById(R.id.Dismiss_Button);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {1000, 1000, 1000};

        if (shapref.getBoolean("vibration_pref", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                //deprecated in API 26
                v.vibrate(pattern, 0);
            }
        }
        Log.d(TAG, "onCreate: ringtone selected is " + shapref.getString("ringtone_pref", ""));

        if (shapref.getBoolean("sound_pref", true)) {
            alert = Uri.parse(shapref.getString("ringtone_pref", ""));
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            r = new MediaPlayer();
            if ((audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn()) && headphones) {
                Log.d(TAG, "onCreate: headphone connected");
                r.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else {
                Log.d(TAG, "onCreate: playing in stream alarm");
                r.setAudioStreamType(AudioManager.STREAM_ALARM);
            }
            try {
                r.setDataSource(this, alert);
                r.setLooping(true);
                r.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            r.start();


        }

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarm_active = false;
                v.cancel();
                r.stop();
                m.OnResumeClicked(true);
                if (MapActivity.recentsOpenFlag) {
                    MapActivity.Ma.removerecentsFragment();
                }
                finish();

            }
        });


    }

    @Override
    public void onBackPressed() {

    }


}
