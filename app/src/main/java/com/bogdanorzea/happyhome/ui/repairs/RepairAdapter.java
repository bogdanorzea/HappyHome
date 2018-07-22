package com.bogdanorzea.happyhome.ui.repairs;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Repair;

import java.util.List;

public class RepairAdapter extends ArrayAdapter<Repair> {

    public RepairAdapter(@NonNull Context context, List<Repair> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.listview_item, parent, false);
        }

        Repair repair = getItem(position);

        // Repair name
        TextView name = convertView.findViewById(R.id.text1);
        name.setText(repair.name.toString());

        // Repair location
        TextView location = convertView.findViewById(R.id.text2);
        location.setText(repair.location);

        // Tag the view with the Firebase ID
        convertView.setTag(repair.id);

        return convertView;
    }
}
