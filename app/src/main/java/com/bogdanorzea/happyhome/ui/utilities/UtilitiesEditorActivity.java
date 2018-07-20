package com.bogdanorzea.happyhome.ui.utilities;

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
import com.bogdanorzea.happyhome.data.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UtilitiesEditorActivity extends AppCompatActivity {

    private String mUserUid;
    private String mHomeId;
    private String mUtilityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText companyName = findViewById(R.id.company_name);
        final EditText companyWebsite = findViewById(R.id.company_website);
        final EditText meterName = findViewById(R.id.meter_name);
        final EditText meterLocation = findViewById(R.id.meter_location);
        Button addHomeButton = findViewById(R.id.add_button);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("utilityId")) {
                mUtilityId = intent.getStringExtra("utilityId");

                addHomeButton.setText("Update");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("utilities")
                        .child(mUtilityId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Utility utility = dataSnapshot.getValue(Utility.class);

                                meterName.setText(utility.name, TextView.BufferType.EDITABLE);
                                meterLocation.setText(utility.location, TextView.BufferType.EDITABLE);
                                companyName.setText(utility.company_name, TextView.BufferType.EDITABLE);
                                companyWebsite.setText(utility.company_website, TextView.BufferType.EDITABLE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }

        addHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyNameString = companyName.getText().toString();
                String companyWebsiteString = companyWebsite.getText().toString();
                String meterNameString = meterName.getText().toString();
                String meterLocationString = meterLocation.getText().toString();

                if (TextUtils.isEmpty(companyNameString) || TextUtils.isEmpty(companyWebsiteString) ||
                        TextUtils.isEmpty(meterNameString) || TextUtils.isEmpty(meterLocationString)) {
                    Toast.makeText(UtilitiesEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Utility utility = new Utility();
                utility.home_id = mHomeId;
                utility.name = meterNameString;
                utility.location = meterLocationString;
                utility.company_name = companyNameString;
                utility.company_website = companyWebsiteString;

                DatabaseReference utilityReference = null;
                if (!TextUtils.isEmpty(mUtilityId)) {
                    utilityReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("utilities")
                            .child(mUtilityId);
                } else {
                    utilityReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("utilities")
                            .push();

                    FirebaseDatabase.getInstance().getReference().child("homes")
                            .child(mHomeId)
                            .child("utilities")
                            .child(utilityReference.getKey())
                            .setValue(true);
                }

                utilityReference.setValue(utility);

                finish();
            }
        });
    }
}
