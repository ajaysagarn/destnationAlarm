package com.meteoriteapps.android.destinationalarm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class LocationTracker extends Service {

    private static final String TAG = "LocationTrackService";
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = 10;
    public static boolean trackeractive = true;
    protected LatLng Destination;
    protected LatLng Current;
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private LocationManager mLocationManager = null;
    private SharedPreferences shapref;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        setNotification();
        shapref = PreferenceManager.getDefaultSharedPreferences(this);
        //radius= Double.parseDouble(shapref.getString("radius_pref",""));
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        Bundle extras = intent.getExtras();
        Destination = new LatLng(extras.getDouble("Latitude"), extras.getDouble("Longitude"));

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    protected double DistanceBetween(LatLng src, LatLng dest) {
        Location current = new Location("");
        Location Destination = new Location("");

        Destination.setLatitude(dest.latitude);
        Destination.setLongitude(dest.longitude);

        current.setLatitude(src.latitude);
        current.setLongitude(src.longitude);


        double Distance = current.distanceTo(Destination);
        Log.d(TAG, "DistanceBetween: distance = " + Distance);
        // Toast.makeText(MapActivity.this, "Distance= "+Distance, Toast.LENGTH_SHORT).show();
        return Distance;
    }

    private void startAlarm() {
        Intent alarmIntent = new Intent("android.intent.action.MAIN");
        alarmIntent.setClass(this, Alarm.class);
        alarmIntent.putExtra("distance", MapActivity.radius);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(alarmIntent);

    }

   /* private void sendBroadcasts(){
        Intent intents = new Intent("DESTINATION.REACHED");
        intents.putExtra("Destination_reached",true);
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(intents);
        Log.d(TAG, "sendBroadcasts: Broadcast sent!");
    }*/

    private void setNotification() {
        Intent Nintent = new Intent(this, MapActivity.class);
        Nintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pnintent = PendingIntent.getActivity(this, 0, Nintent, PendingIntent.FLAG_CANCEL_CURRENT);


        Intent dismissIntent = new Intent(this, NotificationReceiver.class);
        dismissIntent.setAction("Dismiss_Alert");
        PendingIntent dismissPendingIntent =
                PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "004")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.noti_text))
                .setContentText(getString(R.string.not_des))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pnintent)
                .addAction(R.drawable.ic_dismiss, getString(R.string.dismiss),
                        dismissPendingIntent)
                .setOngoing(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        startForeground(004, mBuilder.build());
        //notificationManager.notify(004, mBuilder.build());

    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {

            mLastLocation.set(location);
            Current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (DistanceBetween(Current, Destination) <= MapActivity.radius) {
                trackeractive = false;

                startAlarm();
                stopSelf();
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }


}


