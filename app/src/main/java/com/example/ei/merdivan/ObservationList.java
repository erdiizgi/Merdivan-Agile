package com.example.ei.merdivan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KAAN BURAK SENER on 11.05.2015.
 */
public class ObservationList extends Activity {
    final List<Observation> observations = new ArrayList<Observation>();
    private String jsonPath = "observations.json";
    private EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_list);

        try {
            getObservations();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        inputSearch = (EditText) findViewById(R.id.inputSearch);

        final ListView our_observation_list = (ListView) findViewById(R.id.list);
        final ObservationListAdapter adapter = new ObservationListAdapter(this, observations);
        our_observation_list.setAdapter(adapter);

        our_observation_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // selected item
                Observation observation_data = adapter.getItem(position);

                // Launching new Activity on selecting single List Item
                Intent i = new Intent(ObservationList.this, ObservationDetail.class);

                // sending data to new activity
                i.putExtra("observation status", observation_data.getStatus());
                i.putExtra("observation topic", observation_data.getTopic());
                i.putExtra("observation vote", (int)observation_data.getVote());
                i.putExtra("observation date", observation_data.getDate());
                i.putExtra("observation summary", observation_data.getSummary());
                i.putExtra("observation address", observation_data.getAddress());
                i.putExtra("observation user", observation_data.getUser());
                i.putExtra("observation lat", (double)observation_data.getLat());
                i.putExtra("observation lng", (double)observation_data.getLng());

                startActivity(i);
            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Observation> temp = new ArrayList<Observation>();
                int textlength = inputSearch.getText().length();
                temp.clear();
                for (int i = 0; i < observations.size(); i++) {
                    if (textlength <= observations.get(i).getTopic().length()) {
                        if(inputSearch.getText().toString().equalsIgnoreCase((String)observations.get(i).getTopic().subSequence(0,textlength))) {
                            temp.add(observations.get(i));
                        }
                    }
                }

                ObservationListAdapter tempAdapter = new ObservationListAdapter(ObservationList.this, temp);
                our_observation_list.setAdapter(tempAdapter);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void getObservations() throws JSONException {
        JSONObject json = null;

        try {
            json = new JSONObject(loadJSONFromAsset());

            JSONArray jArray = json.getJSONArray("observations");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                int id = json_data.getInt("id");
                String topic = json_data.getString("topic");
                String status = json_data.getString("status");
                String user = json_data.getString("user");
                int vote = json_data.getInt("vote");
                String date = json_data.getString("date");
                String summary = json_data.getString("summary");
                String address = json_data.getString("address");
                Double lat = json_data.getDouble("lat");
                Double lng = json_data.getDouble("lng");

                observations.add(new Observation(id, topic, status, user, vote, date, summary, address, lat, lng));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
