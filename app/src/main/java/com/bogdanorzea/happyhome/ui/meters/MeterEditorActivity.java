package com.bogdanorzea.happyhome.ui.meters;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Meter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import timber.log.Timber;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.HOMES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.METERS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Meter.deleteMeter;

public class MeterEditorActivity extends AppCompatActivity {

    private static final String METER_KEY = "meter_key";
    private String mUserUid;
    private String mHomeId;
    private String mMeterId;
    private Meter mMeter;
    private EditText mMeterNameEditText;
    private EditText mMeterLocationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMeterNameEditText = findViewById(R.id.meter_name);
        mMeterLocationEditText = findViewById(R.id.meter_location);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("meterId")) {
                setTitle("Edit meter");
                mMeterId = intent.getStringExtra("meterId");
            } else {
                setTitle("Add meter");
            }
        }

        if (savedInstanceState != null) {
            mMeter = savedInstanceState.getParcelable(METER_KEY);
            displayMeter();
        } else if (!TextUtils.isEmpty(mHomeId) && !TextUtils.isEmpty(mMeterId)) {
            displayMeterFromFirebase(mHomeId, mMeterId);
        } else {
            mMeter = new Meter();
        }
    }

    private void displayMeterFromFirebase(final String homeId, final String meterId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(METERS_PATH)
                .child(meterId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mMeter = dataSnapshot.getValue(Meter.class);
                        if (mMeter != null) {
                            displayMeter();
                        } else {
                            Timber.d("Error retrieving utility with id %s", meterId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void displayMeter() {
        mMeterNameEditText.setText(mMeter.name, TextView.BufferType.EDITABLE);
        mMeterLocationEditText.setText(mMeter.location, TextView.BufferType.EDITABLE);
    }

    private void saveCurrentMeterToFirebase() {
        String meterNameString = mMeterNameEditText.getText().toString();
        String meterLocationString = mMeterLocationEditText.getText().toString();

        if (TextUtils.isEmpty(meterNameString) || TextUtils.isEmpty(meterLocationString)) {
            Toast.makeText(MeterEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mMeter.home_id = mHomeId;
        mMeter.name = meterNameString;
        mMeter.location = meterLocationString;

        DatabaseReference meterReference = null;
        if (!TextUtils.isEmpty(mMeterId)) {
            meterReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(METERS_PATH)
                    .child(mMeterId);
        } else {
            meterReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(METERS_PATH)
                    .push();

            FirebaseDatabase.getInstance().getReference()
                    .child(HOMES_PATH)
                    .child(mHomeId)
                    .child(METERS_PATH)
                    .child(meterReference.getKey())
                    .setValue(true);
        }

        meterReference.child("home_id").setValue(mMeter.home_id);
        meterReference.child("name").setValue(mMeter.name);
        meterReference.child("location").setValue(mMeter.location);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_utility_editor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (TextUtils.isEmpty(mMeterId)) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCurrentMeterToFirebase();
                return true;
            case R.id.action_delete:
                confirmDelete();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmDelete() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteMeter(mMeterId);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this meter?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(METER_KEY, mMeter);
    }

}
