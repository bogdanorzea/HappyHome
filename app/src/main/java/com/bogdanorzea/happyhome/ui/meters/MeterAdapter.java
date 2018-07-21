package com.bogdanorzea.happyhome.ui.meters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.data.Meter;

import java.util.List;

public class MeterAdapter extends ArrayAdapter<Meter> {

    public MeterAdapter(@NonNull Context context, List<Meter> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Meter meter = getItem(position);

        // Meter name
        TextView name = convertView.findViewById(android.R.id.text1);
        name.setText(meter.name);

        // Meter location
        TextView location = convertView.findViewById(android.R.id.text2);
        location.setText(meter.location);

        // Tag the view with the Firebase ID
        convertView.setTag(meter.id);

        return convertView;
    }
}
