package com.meteoriteapps.android.destinationalarm;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;



public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RecentsListFragment.ClickListener, Destination_Dialog.DialogListener {


    private static final String TAG = "MapActivity";
    private static final String RADIUS_PREF = "radius_pref";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;


    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private ImageView magimg;

    private Button set_dest;
    private ImageView setingimg;
    private ImageView recents_img;
    private Button cancel;
    private ImageView dest_img;
    private ImageView navbar;
    private MarkerOptions destMarker;
    private Marker set_dest_marker;
    private CircleOptions circleOptions;
    private Circle circle;
    private Intent intent;
    private DrawerLayout mDrawerLayout;

    private boolean flag=false;
    public  static boolean recentsOpenFlag = false;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter AutoComplete;

    private Place mPlace;
    private SharedPreferences shapref;
    private LatLng Dest_Loc;
    public static double radius;
    private NavigationView navigationView;
    //tables

    public static Database destdb;
    public static Destinations recents ;
    public static boolean cancel_press = false;
    public static ArrayList<String> destnames;
    public static MapActivity Ma;
    public static String selectedDestination;
    public static boolean alarmActive = false;
    public static int openhelp = 1;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentTransaction fragmentTransaction;
    public static RecentsListFragment listFragment;
    private Destination_Dialog dialog;
    private InterstitialAd mInterstitialAd;
    private int random;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        createNotificationChannel();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Ma= this;

        Random r = new Random();
        random = r.nextInt(10);
        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        listFragment= new RecentsListFragment();
        navbar = (ImageView) findViewById(R.id.navbar);
        shapref = PreferenceManager.getDefaultSharedPreferences(this);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.searchtext);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        magimg = (ImageView) findViewById(R.id.magimg);
        set_dest = (Button) findViewById(R.id.set_dest);
        dest_img = (ImageView) findViewById(R.id.dest_img);
        setingimg = (ImageView) findViewById(R.id.setingimg);
        cancel = (Button) findViewById(R.id.cancel);
        recents_img=(ImageView)findViewById(R.id.recents_img);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        destdb = Room.databaseBuilder(getApplicationContext(),Database.class,"destdb").build();


        listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shapref, String key) {

                        if((key.equals(RADIUS_PREF)) && MapActivity.alarmActive ){

                            MapActivity.Ma.changeRadius();

                        }
                    }
                };

        shapref.registerOnSharedPreferenceChangeListener(listener);

        MobileAds.initialize(this, String.valueOf(R.string.admob_appId));

        AdView banner = findViewById(R.id.banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        banner.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7716271911015174/6563098993");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        View header = navigationView.getHeaderView(0);
        AdView navbanner = header.findViewById(R.id.header_banner);
        AdRequest navadRequest = new AdRequest.Builder().build();
        navbanner.loadAd(navadRequest);

        new LoadDestns().execute();
        getLocationPermission();



    }



    @Override
    protected void onDestroy() {
        removeNotification();
        if(alarmActive){
            Toast.makeText(MapActivity.this,"Alarm Dismissed",Toast.LENGTH_LONG).show();
            OnResumeClicked(true);
        }
        super.onDestroy();

    }



    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }


    private void getDeviceLocation() {


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        } else {
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }



    private void init() {
        mMap.setBuildingsEnabled(false);

        mGeoDataClient = Places.getGeoDataClient(this,null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this,  null);

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        AutoComplete = new PlaceAutocompleteAdapter(this, mGeoDataClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(AutoComplete);

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchText.setCursorVisible(true);
            }
        });

        mSearchText.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
                                                  @Override
                                                  public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                                      if (i == EditorInfo.IME_ACTION_SEARCH
                                                              || i == EditorInfo.IME_ACTION_DONE
                                                              || i == EditorInfo.IME_ACTION_GO
                                                              || i == EditorInfo.IME_ACTION_NEXT
                                                              || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                                                              || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                                                          geoLocate();

                                                      }



                                                      return false;
                                                  }
                                              });

        magimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoLocate();
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        setingimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sintent= new Intent(MapActivity.this,Settings.class);
                startActivity(sintent);

            }
        });

        set_dest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recents = new Destinations();
                Dest_Loc= mMap.getCameraPosition().target;
                recents.setDlat(Dest_Loc.latitude);
                recents.setDlong(Dest_Loc.longitude);

                dialog = new Destination_Dialog();
                dialog.show(fragmentManager,"TAG");
                                     
              

            }



        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               OnResumeClicked(true);


            }
        });

        navbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        recents_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!recentsOpenFlag) {
                    recentsOpenFlag = true;
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.fragment_container, listFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                }
                else if(recentsOpenFlag)
                {
                    removerecentsFragment();
                }

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch(menuItem.getItemId()){
                    case R.id.Settings :mDrawerLayout.closeDrawers();
                                         Intent sintent= new Intent(MapActivity.this,Settings.class);
                                         startActivity(sintent);
                                         break;
                    case R.id.Recents :mDrawerLayout.closeDrawers();
                                        if(!recentsOpenFlag) {
                                            recentsOpenFlag = true;
                                            fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.add(R.id.fragment_container, listFragment);
                                            fragmentTransaction.addToBackStack(null);
                                            fragmentTransaction.commit();
                                        }
                                        else{}
                                        break;
                    case R.id.About : mDrawerLayout.closeDrawers();
                                      About_Dialog aboutDialog= new About_Dialog();
                                      aboutDialog.show(fragmentManager,"about");
                                      break;
                    case R.id.help : mDrawerLayout.closeDrawers();
                                     openhelp = 2;
                                     Intent help_intent = new Intent(MapActivity.this,welcome_activity.class);
                                     startActivity(help_intent);
                                     break;
                }

                return true;
            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Log.d(TAG, "onAdClosed: ad reloaded");
            }

        });

        hideSoftKeyboard();

    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();
        Log.d(TAG, "Searchstring: " + searchString);
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        if (Geocoder.isPresent()) {


            try {
                list = geocoder.getFromLocationName(searchString, 1);

            } catch (IOException e) {
                Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
            }
        } else {
            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();

        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }


    @Override
    public void OnplayClicked(LatLng position){

            if(random>10){
                random -=10;

            }
            if(random==3 || random == 6 || random ==9){
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    random++;
                } else {
                    Log.d(TAG, "The interstitial wasn't loaded yet.");

                }

            }
            else{
                random++;

            }

           Log.d(TAG, "onPostExecute: Setting Marker");
           radius = Double.parseDouble(shapref.getString("radius_pref", ""));

           if(position.latitude == 0.0 || position.longitude == 0.0 )
            destMarker = new MarkerOptions().position(Dest_Loc).title("");
           else {
                destMarker = new MarkerOptions().position(position).title("");
                moveCamera(position,DEFAULT_ZOOM,"");
           }
            set_dest_marker = mMap.addMarker(destMarker);
            Dest_Loc = set_dest_marker.getPosition();
            dest_img.setVisibility(View.INVISIBLE);
            set_dest.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.VISIBLE);

            circleOptions = new CircleOptions()
                    .center(Dest_Loc)
                    .radius(radius)
                    .strokeWidth((float) 0.5)
                    .fillColor(Color.argb(50, 102, 255, 153))
                    .strokeColor(Color.argb(100, 0, 230, 77));
            circle = mMap.addCircle(circleOptions);
            flag = true;
          //  setNotification();
            intent = new Intent(MapActivity.this, LocationTracker.class);

            intent.putExtra("Latitude", Dest_Loc.latitude);
            intent.putExtra("Longitude", Dest_Loc.longitude);

            startService(intent);

    }

    @Override
    public void OnResumeClicked(boolean stop) {
        set_dest_marker.remove();
        circle.remove();
        dest_img.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        set_dest.setVisibility(View.VISIBLE);
        removeNotification();
        LocationTracker.trackeractive=false;
        if(stop)
        stopService(intent);
        alarmActive = false;
        RecentsListFragment.mflag=true;
        selectedDestination="";

    }

    @Override
    public void OnOkClicked() {
        new DBinsertTask().execute();
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.animateCamera(update);

       hideSoftKeyboard();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


     /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = AutoComplete.getItem(i);
            final String placeId = item.getPlaceId();

            mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        mPlace = places.get(0);
                        LatLng LatLngObj = mPlace.getLatLng();
                        Log.i(TAG, "Place found: " + mPlace.getName());
                        moveCamera(LatLngObj, DEFAULT_ZOOM, (String) mPlace.getName());
                        places.release();
                    } else {
                        Log.e(TAG, "Place not found.");
                    }
                }
            });


        }
    };



    private void removeNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(004);

    }

    //creating notification channel for API 28 and above
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("004", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public class DBinsertTask extends AsyncTask<Void,Void,Void>{

        boolean repeat_flag = false;
        String name;
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: Task started");


            if (!recents.getDname().isEmpty()) {

                name = recents.getDname();
                for (int i = 0; i < destnames.size(); i++) {

                    if (name.equals(destnames.get(i))){
                        repeat_flag = true;
                    }
                }
                if (repeat_flag == false) {
                    destdb.myDao().addDest(recents);
                    String[] names = destdb.myDao().loadAllDestins();
                    destnames.clear();
                    destnames = new ArrayList<String>(Arrays.asList(names));
                }

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "onPostExecute:  inside post execute");

            if(repeat_flag == true){
                Toast.makeText(getBaseContext(),"Alarm "+name+" already exists. Enter a different name",Toast.LENGTH_LONG ).show();
                MapActivity.selectedDestination="";
                RecentsListFragment.mflag=true;
            }
            else if(!recents.getDname().isEmpty()) {

                    OnplayClicked(new LatLng(0.0,0.0));

                }

                cancel_press=false;
            return;
        }
    }

    public class LoadDestns extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            String[] names = destdb.myDao().loadAllDestins();
            destnames = new ArrayList<String>(Arrays.asList(names));
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(recentsOpenFlag==true){
            removerecentsFragment();

        }
    }

    public void removerecentsFragment(){
        getSupportFragmentManager().beginTransaction().remove(listFragment).commit();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {

        if(mDrawerLayout.isDrawerOpen(navigationView)){
            mDrawerLayout.closeDrawers();
        }

        else if(recentsOpenFlag){
           super.onBackPressed();
        }
        else if(alarmActive) {
            moveTaskToBack(true);
        }

        else {
            super.onBackPressed();
        }
    }

    public void changeRadius(){
        radius = Double.parseDouble(shapref.getString("radius_pref", ""));
        circle.remove();
        circleOptions = new CircleOptions()
                .center(Dest_Loc)
                .radius(radius)
                .strokeWidth((float) 0.5)
                .fillColor(Color.argb(50, 102, 255, 153))
                .strokeColor(Color.argb(100, 0, 230, 77));
        circle = mMap.addCircle(circleOptions);
    }


}

