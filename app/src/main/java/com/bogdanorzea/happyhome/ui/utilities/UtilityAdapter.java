package com.bogdanorzea.happyhome.ui.utilities;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.data.Utility;

import java.util.List;

public class UtilityAdapter extends ArrayAdapter<Utility> {

    public UtilityAdapter(@NonNull Context context, List<Utility> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Utility utility = getItem(position);

        // Utility name
        TextView name = convertView.findViewById(android.R.id.text1);
        name.setText(utility.name);

        // Utility location
        TextView location = convertView.findViewById(android.R.id.text2);
        location.setText(utility.location);

        // Tag the view with the Firebase ID
        convertView.setTag(utility.id);

        return convertView;
    }
}
