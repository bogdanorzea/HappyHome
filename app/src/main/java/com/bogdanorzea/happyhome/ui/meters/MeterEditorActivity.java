package com.bogdanorzea.happyhome.ui.meters;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

public class MeterEditorActivity extends AppCompatActivity {
    private String mUserUid;
    private String mHomeId;
    private String mMeterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText meterName = findViewById(R.id.meter_name);
        final EditText meterLocation = findViewById(R.id.meter_location);
        Button addButton = findViewById(R.id.add_button);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("meterId")) {
                mMeterId = intent.getStringExtra("meterId");

                addButton.setText("Update");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("meters")
                        .child(mMeterId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Meter meter = dataSnapshot.getValue(Meter.class);

                                meterName.setText(meter.name, TextView.BufferType.EDITABLE);
                                meterLocation.setText(meter.location, TextView.BufferType.EDITABLE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String meterNameString = meterName.getText().toString();
                String meterLocationString = meterLocation.getText().toString();

                if (TextUtils.isEmpty(meterNameString) || TextUtils.isEmpty(meterLocationString)) {
                    Toast.makeText(MeterEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Meter meter = new Meter();
                meter.home_id = mHomeId;
                meter.name = meterNameString;
                meter.location = meterLocationString;

                DatabaseReference meterReference = null;
                if (!TextUtils.isEmpty(mMeterId)) {
                    meterReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("meters")
                            .child(mMeterId);
                } else {
                    meterReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("meters")
                            .push();

                    FirebaseDatabase.getInstance().getReference().child("homes")
                            .child(mHomeId)
                            .child("meters")
                            .child(meterReference.getKey())
                            .setValue(true);
                }

                meterReference.setValue(meter);

                finish();
            }
        });
    }

}
