package com.bogdanorzea.happyhome;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomesFragment extends Fragment {

    // Current user Firebase UID
    private final String mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private DatabaseReference mMemberHomesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private HomeAdapter mHomeAdapter;

    public HomesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMemberHomesDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("members")
                .child(mUserUid)
                .child("homes");

        View rootView = inflater.inflate(R.layout.fragment_homes, container, false);

        FloatingActionButton addHomeButton = rootView.findViewById(R.id.add_home_button);
        ListView homesListView = rootView.findViewById(R.id.homes_list_view);

        List<Home> homeList = new ArrayList();
        mHomeAdapter = new HomeAdapter(getContext(), homeList);
        homesListView.setAdapter(mHomeAdapter);

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference homeReference = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("homes")
                        .push();

                homeReference.child("members")
                        .child(mUserUid)
                        .setValue("editor");

                homeReference.child("name")
                        .setValue("Unknown house name");

                homeReference.child("location")
                        .setValue("Unknown location");

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("members")
                        .child(mUserUid)
                        .child("homes")
                        .child(homeReference.getKey())
                        .setValue("true");
            }
        });

        attachDatabaseReadListener();

        return rootView;
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String homeID = dataSnapshot.getKey();

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("homes")
                            .child(homeID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Home home = dataSnapshot.getValue(Home.class);
                                    mHomeAdapter.add(home);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            mMemberHomesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMemberHomesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        detachDatabaseReadListener();
    }
}
