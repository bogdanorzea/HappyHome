package com.bogdanorzea.happyhome.ui.utilities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.google.firebase.auth.FirebaseAuth;

public class UtilitiesFragment extends Fragment {
    private final String mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public UtilitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_utilities, container, false);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        final String homeId = sharedPref.getString(getString(R.string.current_home_id), "");

        Toast.makeText(getContext(), "The current home ID is : " + homeId, Toast.LENGTH_SHORT).show();


        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UtilitiesEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", homeId);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
