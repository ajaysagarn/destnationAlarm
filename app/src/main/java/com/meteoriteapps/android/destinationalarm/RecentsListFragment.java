package com.meteoriteapps.android.destinationalarm;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;


public class RecentsListFragment extends Fragment {

    private ClickListener mClicked;
    private ListView recentslist;
    private ArrayList<String> list;
    private static String deletename;
    public static boolean mflag = true;
    public static boolean resumeclicked = false;
    listadapter myAdapter ;
    public static GetLatLng stoptask;

    public static final String TAG="RecentsListFragment";


    public interface ClickListener{
        public void OnplayClicked(LatLng position);
        public void OnResumeClicked(boolean stop);
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAdapter = new listadapter();
        stoptask = new GetLatLng("");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inside list Fragment");



        View view= inflater.inflate(R.layout.list_recents,container,false);
        if(view == null){
            Log.d(TAG, "onCreateView: view is empty");
        }
        recentslist = (ListView) view.findViewById(R.id.recentslist);
        list = MapActivity.destnames;


         recentslist.setAdapter(myAdapter);

        AdView listbanner = view.findViewById(R.id.listbanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        listbanner.loadAd(adRequest);

        if(myAdapter.getCount()==0){
            view = inflater.inflate(R.layout.list_empty,container,false);
        }


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mClicked = (ClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    class listadapter extends BaseAdapter{

        private ArrayList<String> names;
        TextView dname;
        View lview;

        listadapter(){
            this.names = list;
        }

        @Override
        public int getCount() {
            return list.size();

        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }



        @Override
        public View getView(final int i, View view, final ViewGroup viewGroup) {


            lview = getLayoutInflater().inflate(R.layout.list_info,null);
            dname = lview.findViewById(R.id.tview);
            dname.setText(list.get(i));
            String name= dname.getText().toString();
            ImageView deleteimg = lview.findViewById(R.id.deleteimg);
            deleteimg.setTag(name);
            ImageView playimg = lview.findViewById(R.id.playimg);
            playimg.setTag(name);
            ImageView resumeimg = lview.findViewById(R.id.pauseimg);
            resumeimg.setTag(name);

            if(name.equals(MapActivity.selectedDestination)){
                playimg.setVisibility(View.INVISIBLE);
                resumeimg.setVisibility(View.VISIBLE);
            }


            deleteimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Deleting "+view.getTag().toString());
                    deletename = view.getTag().toString();
                    if(!deletename.equals(MapActivity.selectedDestination)) {
                        new DeleteTask().execute();
                        list.remove(i);
                        notifyDataSetChanged();
                    }
                    else
                        Toast.makeText(getActivity(),"Cannot Delete Active Alarm!",Toast.LENGTH_SHORT).show();


                }
            });

            playimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Play clicked for: "+ view.getTag().toString());
                    if(mflag == true) {
                        MapActivity.selectedDestination = view.getTag().toString();
                        Log.d(TAG, "onClick: selecdestn: "+MapActivity.selectedDestination);
                        view.setVisibility(View.INVISIBLE);
                        View pview = (View) view.getParent();
                        ImageView resume = pview.findViewById(R.id.pauseimg);
                        resume.setVisibility(View.VISIBLE);
                        MapActivity.alarmActive=true;
                        new GetLatLng(view.getTag().toString()).execute();
                    }
                    else{
                        Toast.makeText(getActivity(),"Destination Already Set!!",Toast.LENGTH_SHORT).show();
                    }

                }
            });

            resumeimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Resume Clicked for : "+ view.getTag().toString());
                    view.setVisibility(View.INVISIBLE);
                    View pview = (View) view.getParent();
                    ImageView play = pview.findViewById(R.id.playimg);
                    play.setVisibility(View.VISIBLE);
                    resumeclicked = true;
                    new GetLatLng(view.getTag().toString()).execute();


                }
            });


            return lview;
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapActivity.recentsOpenFlag=false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MapActivity.recentsOpenFlag=false;
    }

    public class DeleteTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            MapActivity.destdb.myDao().Deletedestn(deletename);
            String[] names = MapActivity.destdb.myDao().loadAllDestins();
            //MapActivity.destnames.clear();
            MapActivity.destnames = new ArrayList<String>(Arrays.asList(names));
            Log.d(TAG, "doInBackground: Deleted "+deletename);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            recentslist.invalidateViews();
            Toast.makeText(getActivity(),"Destination Deleted!!",Toast.LENGTH_LONG).show();
            
            super.onPostExecute(aVoid);
        }
    }



    public class GetLatLng extends AsyncTask<Void,Void,Void>{
        String dname;
        double lat,lng;
        LatLng position;
        GetLatLng(String name){
            this.dname = name;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            lat = MapActivity.destdb.myDao().GetLat(dname);
            lng = MapActivity.destdb.myDao().GetLng(dname);
            position = new LatLng(lat,lng);

           // MapActivity.Ma.startAlarm(position);
            //mPlayClicked.OnplayClicked(position);

            Log.d(TAG, "doInBackground: Latitude : "+lat+" Longitude"+lng);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(mflag){
                mflag = false;
                mClicked.OnplayClicked(position);

            }
            else if(resumeclicked== false){
                Toast.makeText(getActivity(),"Destination Already Set!!",Toast.LENGTH_SHORT).show();
            }
            else {
                resumeclicked = false;
                mClicked.OnResumeClicked(true);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(
                isVisibleToUser);

        // Refresh tab data:

        if (getFragmentManager() != null) {

            getFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }



}
