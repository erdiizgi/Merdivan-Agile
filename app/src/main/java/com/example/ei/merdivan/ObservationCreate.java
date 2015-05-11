package com.example.ei.merdivan;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import android.widget.PopupWindow;
import android.view.LayoutInflater;

public class ObservationCreate extends ActionBarActivity implements LocationListener, LocationSource, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private OnLocationChangedListener mListener;
    private	GPSTracker gps;
    private Button mBtnFind;  // Address search button
    private EditText etPlace; // Address input
    private Marker current;
    public ImageButton imageButton1,imageButton2,imageButton3,imageButton4,imageButton5,imageButton6,imageButton7,imageButton8;
    private PopupWindow pwindo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_create);
        setUpMapIfNeeded();


        // Getting reference to EditText
        etPlace = (EditText) findViewById(R.id.et_place);

        // Getting reference to the find button
        mBtnFind = (Button) findViewById(R.id.btn_show);

        // Setting click event listener for the find button
        mBtnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting the place entered
                String location = etPlace.getText().toString();

                if(location==null || location.equals("")) {
                    Toast.makeText(getBaseContext(), "Adres Girilmedi", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://maps.googleapis.com/maps/api/geocode/json?";

                try {
                    // encoding special characters like space in the user input place
                    location = URLEncoder.encode(location, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String address = "address=" + location;

                String sensor = "sensor=false";

                // url , from where the geocoding data is fetched
                url = url + address + "&" + sensor;

                // Instantiating DownloadTask to get places from Google Geocoding service
                // in a non-ui thread
                DownloadTask downloadTask = new DownloadTask();

                // Start downloading the geocoding places
                downloadTask.execute(url);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.observation_create_menu, menu);
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.

        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */

    @Override
    public void onLocationChanged(Location location)
    {
        if( mListener != null )
        {
            mListener.onLocationChanged( location );
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(false);
        LatLng ll = new LatLng(38.41885, 27.12872);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 12));
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_current_location:
                dropPin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void dropPin() {
        gps = new GPSTracker(ObservationCreate.this);

        if(gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            current = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Yeni Gözlem"));
            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        } else {
            gps.showSettingsAlert();
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker arg0) {

                initiatePopupWindow();
            }
        });
    }


    private void initiatePopupWindow() {
        try {
// We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) ObservationCreate.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.screen_popup,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, 550, 1150, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);



            imageButton1 = (ImageButton) layout.findViewById(R.id.ImageButton1);

            imageButton1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this, "Elektrik Kesintisi", Toast.LENGTH_SHORT).show();

/*
                    mMap.addMarker(new MarkerOptions()
                                    .position(LatLng)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.electric)));
                                            */


                }

            });




            imageButton2 = (ImageButton) layout.findViewById(R.id.ImageButton2);

            imageButton2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Su Sorunu", Toast.LENGTH_SHORT).show();

                 /*   mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.water)));
                            */

                }

            });





            imageButton3 = (ImageButton) layout.findViewById(R.id.ImageButton3);

            imageButton3.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Trafik Sorunu", Toast.LENGTH_SHORT).show();
/*
                    mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic)));
*/
                }

            });




            imageButton4 = (ImageButton) layout.findViewById(R.id.ImageButton4);

            imageButton4.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Yol Sorunu", Toast.LENGTH_SHORT).show();

                    /*
                    mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cone)));
                            */

                }

            });




            imageButton5 = (ImageButton) layout.findViewById(R.id.ImageButton5);

            imageButton5.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Çevre ve Parklar", Toast.LENGTH_SHORT).show();

             /*       mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flower)));
                            */

                }

            });



            imageButton6 = (ImageButton) layout.findViewById(R.id.ImageButton6);

            imageButton6.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Çöp ve Atık", Toast.LENGTH_SHORT).show();

/*
                    mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.trash)));
                            */

                }

            });




            imageButton7 = (ImageButton) layout.findViewById(R.id.ImageButton7);

            imageButton7.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Gürültü Kirliliği", Toast.LENGTH_SHORT).show();

                    /*
                    mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.megaphone)));

                            */

                }

            });




            imageButton8 = (ImageButton) layout.findViewById(R.id.ImageButton8);

            imageButton8.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Diğer", Toast.LENGTH_SHORT).show();

/*
                    mMap.addMarker(new MarkerOptions()
                            .position(LatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.other)));
                            */



                }

            });



        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private OnClickListener cancel_button_click_listener = new OnClickListener() {
        public void onClick(View v) {
            pwindo.dismiss();

        }
    };



    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch(Exception e){
            Log.d("Exception", e.toString());
        } finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }
    /** A class, to download Places from Geocoding webservice */
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {

            // Instantiating ParserTask which parses the json data from Geocoding webservice
            // in a non-ui thread
            ParserTask parserTask = new ParserTask();

            // Start parsing the places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }

    /** A class to parse the Geocoding Places in non-ui thread */
    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            GeocodeJSONParser parser = new GeocodeJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            // Clears all the existing markers
            mMap.clear();

            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("formatted_address");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker
                markerOptions.title(name);

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                // Locate the first location
                if (i == 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
}