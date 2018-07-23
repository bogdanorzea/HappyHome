package com.bogdanorzea.happyhome.ui.meters;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.METERS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.READINGS_PATH;

public class ReadingsActivity extends AppCompatActivity {
    private String mUserUid;
    private String mHomeId;
    private String mMeterId;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mHomeUtilitiesDatabaseReference;
    private ReadingAdapter mAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listView = findViewById(R.id.list_view);
        TextView emptyView = findViewById(R.id.empty_view);
        mProgressBar = findViewById(R.id.progressBar);

        List<Reading> arrayList = new ArrayList();
        mAdapter = new ReadingAdapter(this, arrayList);

        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("meterId")) {
                mMeterId = intent.getStringExtra("meterId");
            }

            if (intent.hasExtra("meterName")) {
                setTitle(intent.getStringExtra("meterName"));
            }
        }

        mHomeUtilitiesDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(METERS_PATH)
                .child(mMeterId)
                .child(READINGS_PATH);

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
                    mProgressBar.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(READINGS_PATH)
                            .child(snapshotKey)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Reading reading = dataSnapshot.getValue(Reading.class);
                                    if (reading == null) {
                                        return;
                                    }

                                    reading.id = dataSnapshot.getKey();
                                    mAdapter.add(reading);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_readings_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                editCurrentMeter();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editCurrentMeter() {
        Intent intent = new Intent(this, MeterEditorActivity.class);
        intent.putExtra("userUid", mUserUid);
        intent.putExtra("homeId", mHomeId);
        intent.putExtra("meterId", mMeterId);

        startActivity(intent);
    }
}
