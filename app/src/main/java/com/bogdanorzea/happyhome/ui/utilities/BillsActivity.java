package com.bogdanorzea.happyhome.ui.utilities;

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

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Bill;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.BILLS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.UTILITIES_KEY;

public class BillsActivity extends AppCompatActivity {
    private String mUserUid;
    private String mHomeId;
    private String mUtilityId;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDatabaseReference;
    private BillAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listView = findViewById(R.id.list_view);
        List<Bill> billList = new ArrayList();
        mAdapter = new BillAdapter(this, billList);
        listView.setAdapter(mAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("utilityId")) {
                mUtilityId = intent.getStringExtra("utilityId");
            }
        }

        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(UTILITIES_KEY)
                .child(mUtilityId)
                .child(BILLS_PATH);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BillsActivity.this, BillEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", mHomeId);
                intent.putExtra("utilityId", mUtilityId);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BillsActivity.this, BillEditorActivity.class);
                intent.putExtra("userUid", mUserUid);
                intent.putExtra("homeId", mHomeId);
                intent.putExtra("utilityId", mUtilityId);
                intent.putExtra("billId", view.getTag().toString());
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
                            .child(BILLS_PATH)
                            .child(snapshotKey)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Bill bill = dataSnapshot.getValue(Bill.class);
                                    if (bill == null) {
                                        return;
                                    }

                                    bill.id = dataSnapshot.getKey();
                                    mAdapter.add(bill);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bills_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                editCurrentUtility();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmDelete() {

    }

    private void editCurrentUtility() {
        Intent intent = new Intent(this, UtilitiesEditorActivity.class);
        intent.putExtra("userUid", mUserUid);
        intent.putExtra("homeId", mHomeId);
        intent.putExtra("utilityId", mUtilityId);

        startActivity(intent);
    }
}
