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
import com.bogdanorzea.happyhome.data.Reading;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReadingEditorActivity extends AppCompatActivity {

    private String mUserUid;
    private String mHomeId;
    private String mMeterId;
    private String mReadingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText readingDate = findViewById(R.id.reading_date);
        final EditText readingValue = findViewById(R.id.reading_value);
        Button addButton = findViewById(R.id.add_button);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId") &&
                    intent.hasExtra("meterId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
                mMeterId = intent.getStringExtra("meterId");
            }

            if (intent.hasExtra("readingId")) {
                mReadingId = intent.getStringExtra("readingId");

                addButton.setText("Update");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("readings")
                        .child(mReadingId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Reading reading = dataSnapshot.getValue(Reading.class);

                                readingDate.setText(reading.date, TextView.BufferType.EDITABLE);
                                readingValue.setText(Double.toString(reading.value), TextView.BufferType.EDITABLE);
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
                String readingDateString = readingDate.getText().toString();
                String readingValueString = readingValue.getText().toString();

                if (TextUtils.isEmpty(readingDateString) || TextUtils.isEmpty(readingValueString)) {
                    Toast.makeText(ReadingEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Reading reading = new Reading();
                reading.meter_id = mMeterId;
                reading.value = Double.parseDouble(readingValueString);
                reading.date = readingDateString;

                DatabaseReference readingDatabaseReference = null;
                if (!TextUtils.isEmpty(mReadingId)) {
                    readingDatabaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("readings")
                            .child(mReadingId);
                } else {
                    readingDatabaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("readings")
                            .push();

                    FirebaseDatabase.getInstance().getReference().child("meters")
                            .child(mMeterId)
                            .child("readings")
                            .child(readingDatabaseReference.getKey())
                            .setValue(true);
                }

                readingDatabaseReference.setValue(reading);

                finish();
            }
        });
    }
}
