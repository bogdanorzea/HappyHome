package com.bogdanorzea.happyhome;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HomeAdapter extends ArrayAdapter<Home> {

    public HomeAdapter(@NonNull Context context, List<Home> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Home home = getItem(position);

        TextView name = convertView.findViewById(android.R.id.text1);
        name.setText(home.name);

        TextView location = convertView.findViewById(android.R.id.text2);
        location.setText(home.location);

        return convertView;
    }
}
