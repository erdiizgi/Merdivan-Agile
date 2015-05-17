package com.example.ei.merdivan;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private ImageButton cameraButton, cancelEditText;;
    private Button cancelButton,sendButton;
    private EditText textBox;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String jsonPath = "observations.json";
    private String topic ="";
    private String status = "Onaylanmamış";
    private String user = "TestUser";
    private int vote = 0;
    private String date = "";
    private String summary;
    private String address = "";
    double latitude = 0;
    double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_create);
        setUpMapIfNeeded();

        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        textBox = (EditText) findViewById(R.id.textBox);
        cancelEditText = (ImageButton) findViewById(R.id.cancel_edit_text);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        date = sdf.format(c.getTime());

        cameraButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });



        ImageButton btn = (ImageButton) findViewById(R.id.cancel_edit_text);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                EditText et = (EditText) findViewById(R.id.textBox);
                et.setText("");
            }
        });



        sendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendJSON();
            }
        });

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

    public int NextObservationsId() throws JSONException {
        JSONObject json = null;
        int nextId = 0;

        try {
            json = new JSONObject(loadJSONFromAsset());

            JSONArray jArray = json.getJSONArray("observations");
            nextId = jArray.length()+1;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextId;
    }

    public void setAddressFromPositions()
    {
        try {

            Geocoder geo = new Geocoder(ObservationCreate.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                address = "Waiting for Location";
            }
            else {
                if (addresses.size() > 0) {
                    address = addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
                    //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }

    public void sendJSON()
    {
        int id = 0;
        JSONObject jsonObj = new JSONObject();
        try {
            id = NextObservationsId();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("id", id);
            jsonObj.put("topic", topic);
            jsonObj.put("status", status);
            jsonObj.put("user", user);
            jsonObj.put("vote", vote);
            jsonObj.put("date", date);
            jsonObj.put("summary", "No Summary");
            jsonObj.put("address", address);
            jsonObj.put("latitude", latitude);
            jsonObj.put("longitude", longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String send = "Observation:"+ id +", "+topic+", "+status+", "+user+", "+vote+", "+date+", "+address+", "+latitude+", "+longitude;
        Toast.makeText(getApplicationContext(),jsonObj.toString(), Toast.LENGTH_LONG).show();

    }



    public String loadJSONFromAsset() throws IOException {
        String json = null;
        try {

            InputStream is = getAssets().open(jsonPath);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
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



    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }




    private void initiatePopupWindow() {
        try {
// We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) ObservationCreate.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.screen_popup,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, 450, 800, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);



            imageButton1 = (ImageButton) layout.findViewById(R.id.ImageButton1);

            imageButton1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this, "Elektrik Sorunu", Toast.LENGTH_SHORT).show();
                    topic = "Elektrik Sorunu";
                    cameraButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.VISIBLE);
                    cancelEditText.setVisibility(View.VISIBLE);


                    mMap.clear();

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    setAddressFromPositions();

                    current= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Elektrik Sorunu")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.plug)));

                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);



                }

            });


            imageButton2 = (ImageButton) layout.findViewById(R.id.ImageButton2);

            imageButton2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Su Sorunu", Toast.LENGTH_SHORT).show();
                    topic = "Su Sorunu";
                    cameraButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.VISIBLE);
                    cancelEditText.setVisibility(View.VISIBLE);

                    mMap.clear();

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    setAddressFromPositions();

                    current= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Su Sorunu")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.water_drop)));

                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);

                }

            });

            imageButton3 = (ImageButton) layout.findViewById(R.id.ImageButton3);

            imageButton3.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Trafik Sorunu", Toast.LENGTH_SHORT).show();
                    topic = "Trafik Sorunu";
                    cameraButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.VISIBLE);
                    cancelEditText.setVisibility(View.VISIBLE);

                    mMap.clear();

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    setAddressFromPositions();

                    current= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Trafik Sorunu")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_light)));

                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);


                }

            });




            imageButton5 = (ImageButton) layout.findViewById(R.id.ImageButton5);

            imageButton5.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Ağaç/Park", Toast.LENGTH_SHORT).show();
                    topic = "Ağaç/Park";
                    cameraButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.VISIBLE);
                    cancelEditText.setVisibility(View.VISIBLE);

                    mMap.clear();

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    setAddressFromPositions();

                    current= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Ağaç/Park")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flower)));

                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);

                }

            });



            imageButton6 = (ImageButton) layout.findViewById(R.id.ImageButton9);

            imageButton6.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    cameraButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.VISIBLE);
                    cancelEditText.setVisibility(View.VISIBLE);

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Haşere/Hayvan", Toast.LENGTH_SHORT).show();
                    topic = "Haşere/Hayvan";
                    mMap.clear();

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    setAddressFromPositions();

                    current= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Haşere/Hayvan")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.fan)));

                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);
                }

            });




            imageButton8 = (ImageButton) layout.findViewById(R.id.ImageButton8);

            imageButton8.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    pwindo.dismiss();
                    Toast.makeText(ObservationCreate.this,"Diğer İstek", Toast.LENGTH_SHORT).show();
                    topic = "Diğer İstek";
                    cameraButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    textBox.setVisibility(View.VISIBLE);
                    cancelEditText.setVisibility(View.VISIBLE);

                    mMap.clear();

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    setAddressFromPositions();

                    current= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Diğer İstek")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.caution)));

                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);

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



///////////////////////////////////////// CAMERA  PART ////////////////////////////////////////////////////////////////////////////////////////////
    private void selectImage() {
        final CharSequence[] items = { "Resim Çek", "Galeriden Seç",
                "İptal" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ObservationCreate.this);
        builder.setTitle("Resim Ekle!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Resim Çek")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Galeriden Seç")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Dosya Seç"),
                            SELECT_FILE);
                } else if (items[item].equals("İptal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

    }

    ///////////////////////////////////////END OF CAMERA PART////////////////////////////////////////////////////////////////////////


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