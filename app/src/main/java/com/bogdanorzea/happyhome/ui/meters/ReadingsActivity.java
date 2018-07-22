package com.bogdanorzea.happyhome.ui.meters;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Reading;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReadingsActivity extends AppCompatActivity {
    private String mUserUid;
    private String mHomeId;
    private String mMeterId;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mHomeUtilitiesDatabaseReference;
    private ReadingAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listView = findViewById(R.id.list_view);
        List<Reading> billList = new ArrayList();
        mAdapter = new ReadingAdapter(this, billList);
        listView.setAdapter(mAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("meterId")) {
                mMeterId = intent.getStringExtra("meterId");
            }
        }

        mHomeUtilitiesDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("meters")
                .child(mMeterId)
                .child("readings");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadingsActivity.this, ReadingEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", mHomeId);
                intent.putExtra("meterId", mMeterId);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ReadingsActivity.this, ReadingEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", mHomeId);
                intent.putExtra("meterId", mMeterId);
                intent.putExtra("readingId", view.getTag().toString());
                startActivity(intent);
            }
        });
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String snapshotKey = dataSnapshot.getKey();

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("readings")
                            .child(snapshotKey)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Reading reading = dataSnapshot.getValue(Reading.class);
                                    reading.id = dataSnapshot.getKey();

                                    mAdapter.add(reading);
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
