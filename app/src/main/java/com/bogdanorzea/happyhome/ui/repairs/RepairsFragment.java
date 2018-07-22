package com.bogdanorzea.happyhome.ui.repairs;


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

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Repair;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RepairsFragment extends Fragment {
    private final String mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private RepairAdapter mAdapter;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mHomeUtilitiesDatabaseReference;

    public RepairsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_repairs, container, false);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        final String homeId = sharedPref.getString(getString(R.string.current_home_id), "");

        mHomeUtilitiesDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("homes")
                .child(homeId)
                .child("repairs");

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RepairEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", homeId);
                startActivity(intent);
            }
        });

        List<Repair> utilityList = new ArrayList();
        mAdapter = new RepairAdapter(getContext(), utilityList);

        ListView utilityListView = rootView.findViewById(R.id.list_view);
        utilityListView.setAdapter(mAdapter);

        utilityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), RepairEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", homeId);
                intent.putExtra("repairId", view.getTag().toString());
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
                    String repairId = dataSnapshot.getKey();

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("repairs")
                            .child(repairId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Repair repair = dataSnapshot.getValue(Repair.class);
                                    if (repair == null) {
                                        return;
                                    }

                                    repair.id = dataSnapshot.getKey();
                                    mAdapter.add(repair);
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

            mHomeUtilitiesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }


    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mHomeUtilitiesDatabaseReference.removeEventListener(mChildEventListener);
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

        mAdapter.clear();
        attachDatabaseReadListener();
    }
}
