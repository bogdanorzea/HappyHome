package com.bogdanorzea.happyhome.ui.home;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Home;
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

        FloatingActionButton addHomeButton = rootView.findViewById(R.id.fab);
        ListView listView = rootView.findViewById(R.id.list_view);

        List<Home> homes = new ArrayList();
        mHomeAdapter = new HomeAdapter(getContext(), homes);
        listView.setAdapter(mHomeAdapter);

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HomeEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                startActivity(intent);
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String homeId = view.getTag().toString();

                Toast.makeText(getContext(), "You clicked on home with id: " + homeId, Toast.LENGTH_SHORT).show();

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                sharedPref.edit()
                        .putString(getString(R.string.current_home_id), homeId)
                        .apply();

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), HomeEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", view.getTag().toString());
                startActivity(intent);
            }
        });

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
                                    home.id = dataSnapshot.getKey();

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

    @Override
    public void onResume() {
        super.onResume();

        mHomeAdapter.clear();
        attachDatabaseReadListener();
    }
}
