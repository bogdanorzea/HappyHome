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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Repair;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.HOMES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.REPAIRS_PATH;

public class RepairsFragment extends Fragment {
    private final String mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private RepairAdapter mAdapter;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDatabaseReference;
    private ProgressBar mProgressBar;

    public RepairsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Repairs");

        View rootView = inflater.inflate(R.layout.listview_with_fab, container, false);

        SharedPreferences sharedPref = getActivity()
                .getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
        final String homeId = sharedPref.getString(getString(R.string.current_home_id), "");

        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(HOMES_PATH)
                .child(homeId)
                .child(REPAIRS_PATH);

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

        List<Repair> arrayList = new ArrayList();
        mAdapter = new RepairAdapter(getContext(), arrayList);

        ListView listView = rootView.findViewById(R.id.list_view);
        TextView emptyView = rootView.findViewById(R.id.empty_view);
        mProgressBar = rootView.findViewById(R.id.progressBar);

        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), RepairEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", homeId);
                intent.putExtra("repairId", view.getTag().toString());

                startActivity(intent);
            }
        });

        initializeAdMobView(rootView);

        return rootView;
    }

    private void initializeAdMobView(View rootView) {
        MobileAds.initialize(getContext(), getString(R.string.admob_id));

        AdView mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String snapshotKey = dataSnapshot.getKey();
                    mProgressBar.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(REPAIRS_PATH)
                            .child(snapshotKey)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Repair object = dataSnapshot.getValue(Repair.class);

                                    if (object == null) {
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                        return;
                                    }

                                    object.id = dataSnapshot.getKey();
                                    mAdapter.add(object);

                                    mProgressBar.setVisibility(View.INVISIBLE);
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

            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
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
