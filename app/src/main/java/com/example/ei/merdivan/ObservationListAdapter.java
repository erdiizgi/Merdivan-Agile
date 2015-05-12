package com.example.ei.merdivan;

/**
 * Created by KAAN BURAK SENER on 11.05.2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ObservationListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<Observation> mObservationList;

    public ObservationListAdapter(Activity activity, List<Observation> observations) {
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObservationList = observations;
    }

    @Override
    public int getCount() {
        return mObservationList.size();
    }

    @Override
    public Observation getItem(int position) {
        return mObservationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        rowView = mInflater.inflate(R.layout.observation_row_layout, null);
        ImageView imageView = (ImageView)rowView.findViewById(R.id.observation_icon);
        TextView textView = (TextView)rowView.findViewById(R.id.observation_type);
        TextView textView2 = (TextView)rowView.findViewById(R.id.observation_user);
        TextView textView3 = (TextView)rowView.findViewById(R.id.observation_date);


        Observation observation = mObservationList.get(position);

        textView.setText(observation.getTopic());
        textView2.setText(observation.getUser());
        textView3.setText(observation.getDate());

        switch (observation.getTopic()) {
            case "Ağaç/Park":
                imageView.setImageResource(R.drawable.flower);
                break;
            case "Diğer İstek":
                imageView.setImageResource(R.drawable.caution);
                break;
            case "Elektrik Sorunu":
                imageView.setImageResource(R.drawable.plug);
                break;
            case "Haşere/Hayvan":
                imageView.setImageResource(R.drawable.fan);
                break;
            case "Su Sorunu":
                imageView.setImageResource(R.drawable.water_drop);
                break;
            case "Şehir Estetiği":
                imageView.setImageResource(R.drawable.spray_paint);
                break;
            case "Trafik Sorunu":
                imageView.setImageResource(R.drawable.traffic_light);
                break;
            case "Yol Tehlikesi":
                imageView.setImageResource(R.drawable.cone);
                break;
            default:
                break;
        }

        return rowView;
    }
}
