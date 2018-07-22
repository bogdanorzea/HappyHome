package com.bogdanorzea.happyhome.ui.meters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.data.Reading;

import java.util.List;

public class ReadingAdapter extends ArrayAdapter<Reading> {

    public ReadingAdapter(@NonNull Context context, List<Reading> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Reading item = getItem(position);

        // Reading date
        TextView date = convertView.findViewById(android.R.id.text1);
        date.setText(item.date);

        // Reading value
        TextView value = convertView.findViewById(android.R.id.text2);
        value.setText(Double.toString(item.value));

        // Tag the view with the Firebase ID
        convertView.setTag(item.id);

        return convertView;
    }
}
