package com.potholes.View.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.potholes.driversafer.R;

/**
 * Created by User on 10/2/2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void rendowWindowText(Marker marker, View view) {

        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title);

        if (!title.equals("")) {
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.snippet);

        if (!snippet.equals("")) {
            tvSnippet.setText(snippet);
        }

        ImageView info_img = view.findViewById(R.id.info_img);

        if (title.equals("Potholes")) {
            info_img.setImageResource(R.drawable.ic_info_potholes);
            tvTitle.setBackgroundColor(Color.YELLOW);
            tvTitle.setTextColor(Color.BLACK);
        }
        if (title.equals("Driver Car")) {
            info_img.setImageResource(R.drawable.ic_map_car);
            tvTitle.setBackgroundColor(Color.BLACK);
            tvTitle.setTextColor(Color.YELLOW);
        }
        if (title.equals("Point de Départ du trajet")) {
            info_img.setImageResource(R.drawable.ic_start);
            tvTitle.setBackgroundColor(Color.RED);
        }
        if (title.equals("Arrivée")) {
            info_img.setImageResource(R.drawable.ic_arrival);
            tvTitle.setBackgroundColor(Color.BLUE);
        }

    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

}
