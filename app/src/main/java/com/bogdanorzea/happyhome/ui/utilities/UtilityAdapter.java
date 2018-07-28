package com.bogdanorzea.happyhome.ui.utilities;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Utility;

import java.util.List;

class UtilityAdapter extends ArrayAdapter<Utility> {

    UtilityAdapter(@NonNull Context context, List<Utility> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.listview_item, parent, false);
        }

        Utility utility = getItem(position);

        TextView name = convertView.findViewById(R.id.text1);
        name.setText(utility.name);

        TextView location = convertView.findViewById(R.id.text2);
        location.setText(utility.company_name);

        convertView.setTag(utility.id);

        return convertView;
    }
}
