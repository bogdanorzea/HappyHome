package com.bogdanorzea.happyhome;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomesFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mHomesDatabaseReference;
    private DatabaseReference mMembersDatabaseReference;

    public HomesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mHomesDatabaseReference = mFirebaseDatabase.getReference().child("homes");
        mMembersDatabaseReference = mFirebaseDatabase.getReference().child("members");


        View rootView = inflater.inflate(R.layout.fragment_homes, container, false);
        Button addHomeButton = rootView.findViewById(R.id.add_home_button);

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    String userUid = user.getUid();
                    DatabaseReference newHome = mHomesDatabaseReference.push();
                    newHome.child("members")
                            .child(userUid)
                            .setValue("true");

                    mMembersDatabaseReference.child(userUid)
                            .child("homes")
                            .child(newHome.getKey())
                            .setValue("true");
                }
            }
        });

        return rootView;
    }
}
