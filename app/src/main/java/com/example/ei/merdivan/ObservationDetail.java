package com.example.ei.merdivan;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
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

public class ObservationDetail extends ActionBarActivity implements LocationListener, LocationSource {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private OnLocationChangedListener mListener;
    private	GPSTracker gps;
    private Button mBtnVote;  // Vote Button
    private Button mBtnUser;  // User Button
    private TextView tvObsTopic; // Observation Topic
    private TextView tvObsDate; // Observation Date
    private TextView tvObsSummary; // Observation Summary
    private TextView tvObsStatus; // Observation Status
    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_detail);

        Intent i = getIntent();

        Bundle extras = getIntent().getExtras();

        // getting attached intent data
        String status = extras.getString("observation status");
        String topic = extras.getString("observation topic");
        int vote = extras.getInt("observation vote");
        String date = extras.getString("observation date");
        String summary = extras.getString("observation summary");
        String user = extras.getString("observation user");
        String address = extras.getString("observation address");
        lat = extras.getDouble("observation lat");
        lng = extras.getDouble("observation lng");

        setUpMapIfNeeded();

        mBtnVote = (Button)findViewById(R.id.vote_button);
        tvObsTopic = (TextView)findViewById(R.id.observation_type);
        mBtnUser = (Button)findViewById(R.id.user_button);  // User Button
        tvObsDate = (TextView)findViewById(R.id.observation_date); // Observation Date
        tvObsSummary = (TextView)findViewById(R.id.observation_summary); // Observation Summary
        tvObsStatus = (TextView)findViewById(R.id.observation_status); // Observation Summary

        mBtnVote.setText(String.valueOf(vote) + "+ Oy");
        tvObsTopic.setText(topic);
        mBtnUser.setText(user);
        tvObsDate.setText("Açıklama: " + date);
        tvObsSummary.setText(summary);
        tvObsStatus.setText(status);

        if(status.equals("Onaylanmış")){
            tvObsStatus.setTextColor(Color.parseColor("#4cd964"));
        } else {
            tvObsStatus.setTextColor(Color.parseColor("#d04456"));
        }

        dropPin(topic, address, lat, lng);

        mBtnVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Toast.makeText(getApplicationContext(),
                        "1 Oy Arttırıldı!", Toast.LENGTH_LONG).show();
            }
        });

        mBtnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Toast.makeText(getApplicationContext(),
                        "Veritabanı bağlantısı olmadığından kimlik sayfasına gidilemiyor!", Toast.LENGTH_LONG).show();
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
        getMenuInflater().inflate(R.menu.observation_detail_menu, menu);
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
        LatLng ll = new LatLng(lat, lng);
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

    public void dropPin(String topic, String address, double lat, double lng) {
        gps = new GPSTracker(ObservationDetail.this);

        switch (topic) {
            case "Ağaç/Park":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.flower)));
                break;
            case "Diğer İstek":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.caution)));
                break;
            case "Elektrik Sorunu":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.plug)));
                break;
            case "Haşere/Hayvan":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.fan)));
                break;
            case "Su Sorunu":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.water_drop)));
                break;
            case "Şehir Estetiği":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.spray_paint)));
                break;
            case "Trafik Sorunu":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_light)));
                break;
            case "Yol Tehlikesi":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(topic + " : " + address).icon(BitmapDescriptorFactory.fromResource(R.drawable.cone)));
                break;
            default:
                break;
        }

    }
}
