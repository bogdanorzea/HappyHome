package com.bogdanorzea.happyhome.ui.meters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Reading;

import java.util.List;
import java.util.Locale;

import static com.bogdanorzea.happyhome.utils.StringUtils.getReadableFormatFromDateString;

class ReadingAdapter extends ArrayAdapter<Reading> {

    ReadingAdapter(@NonNull Context context, List<Reading> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.listview_item, parent, false);
        }

        Reading item = getItem(position);
        if (item != null) {
            TextView date = convertView.findViewById(R.id.text1);
            date.setText(String.format(Locale.US, "%.3f", item.value));

            TextView value = convertView.findViewById(R.id.text2);
            value.setText(getReadableFormatFromDateString(item.date));

            convertView.setTag(item.id);
        }

        return convertView;
    }
}
