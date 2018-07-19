package com.bogdanorzea.happyhome.ui.bills;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;

public class UtilityBillsFragment extends Fragment {

    public UtilityBillsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_utility_bills, container, false);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String homeId = sharedPref.getString(getString(R.string.current_home_id), "");

        Toast.makeText(getContext(), "The current home ID is : " + homeId, Toast.LENGTH_SHORT).show();

        return rootView;
    }
}
