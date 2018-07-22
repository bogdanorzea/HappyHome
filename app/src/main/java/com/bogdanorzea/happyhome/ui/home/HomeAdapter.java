package com.bogdanorzea.happyhome.ui.home;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Home;

import java.util.List;

public class HomeAdapter extends ArrayAdapter<Home> {

    public HomeAdapter(@NonNull Context context, List<Home> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.listview_item, parent, false);
        }

        Home home = getItem(position);

        TextView name = convertView.findViewById(R.id.text1);
        name.setText(home.name);

        TextView location = convertView.findViewById(R.id.text2);
        location.setText(home.location);

        convertView.setTag(home.id);

        return convertView;
    }
}
