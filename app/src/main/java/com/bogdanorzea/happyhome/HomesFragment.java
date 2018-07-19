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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomesFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mHomesDatabaseReference;
    private DatabaseReference mMembersDatabaseReference;
    private ChildEventListener mChildEventListener;
    private HomeAdapter mHomeAdapter;

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

        FloatingActionButton addHomeButton = rootView.findViewById(R.id.add_home_button);
        ListView homesListView = rootView.findViewById(R.id.homes_list_view);

        List<Home> homeList = new ArrayList();
        mHomeAdapter = new HomeAdapter(getContext(), homeList);
        homesListView.setAdapter(mHomeAdapter);

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    String userUid = user.getUid();
                    DatabaseReference newHome = mHomesDatabaseReference.push();
                    newHome.child("members")
                            .child(userUid)
                            .setValue("editor");

                    mMembersDatabaseReference.child(userUid)
                            .child("homes")
                            .child(newHome.getKey())
                            .setValue("true");
                }
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
                    Home home = dataSnapshot.getValue(Home.class);

                    mHomeAdapter.add(home);
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

            mHomesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mHomesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        detachDatabaseReadListener();
    }
}
