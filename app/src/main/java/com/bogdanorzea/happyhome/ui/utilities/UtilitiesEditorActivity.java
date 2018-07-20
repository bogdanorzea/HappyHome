package com.bogdanorzea.happyhome.ui.utilities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Utility;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UtilitiesEditorActivity extends AppCompatActivity {

    private String mUserUid;
    private String mHomeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }
        }

        final EditText companyName = findViewById(R.id.company_name);
        final EditText companyWebsite = findViewById(R.id.company_website);
        final EditText meterName = findViewById(R.id.meter_name);
        final EditText meterLocation = findViewById(R.id.meter_location);

        Button addHomeButton = findViewById(R.id.add_button);

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

                DatabaseReference utilityReference = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("utilities")
                        .push();
                utilityReference.setValue(utility);

                FirebaseDatabase.getInstance().getReference().child("homes")
                        .child(mHomeId)
                        .child("utilities")
                        .child(utilityReference.getKey())
                        .setValue(true);

                finish();
            }
        });
    }
}
