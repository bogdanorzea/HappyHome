package com.bogdanorzea.happyhome.ui.home;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeEditorActivity extends AppCompatActivity {

    private String mUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUID")) {
                mUserUid = intent.getStringExtra("userUID");
            }
        }

        final EditText homeName = findViewById(R.id.home_name);
        final EditText homeLocation = findViewById(R.id.home_location);
        Button addButton = findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = homeName.getText().toString();
                String location = homeLocation.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(location)) {
                    Toast.makeText(HomeEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference homeReference = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("homes")
                        .push();

                homeReference.child("members")
                        .child(mUserUid)
                        .setValue("editor");

                homeReference.child("name")
                        .setValue(name);

                homeReference.child("location")
                        .setValue(location);

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("members")
                        .child(mUserUid)
                        .child("homes")
                        .child(homeReference.getKey())
                        .setValue(true);

                finish();
            }
        });
    }
}
