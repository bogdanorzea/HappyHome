package com.bogdanorzea.happyhome.ui.home;

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
import com.bogdanorzea.happyhome.data.Home;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeEditorActivity extends AppCompatActivity {

    private String mUserUid;
    private String mHomeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText homeName = findViewById(R.id.home_name);
        final EditText homeLocation = findViewById(R.id.home_location);
        Button addButton = findViewById(R.id.add_button);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid")) {
                mUserUid = intent.getStringExtra("userUid");
            }

            if (intent.hasExtra("homeId")) {
                mHomeId = intent.getStringExtra("homeId");

                addButton.setText("Update");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("homes")
                        .child(mHomeId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Home home = dataSnapshot.getValue(Home.class);

                                homeName.setText(home.name, TextView.BufferType.EDITABLE);
                                homeLocation.setText(home.location, TextView.BufferType.EDITABLE);
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
                String homeNameString = homeName.getText().toString();
                String homeLocationString = homeLocation.getText().toString();

                if (TextUtils.isEmpty(homeNameString) || TextUtils.isEmpty(homeLocationString)) {
                    Toast.makeText(HomeEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Home home = new Home();
                home.name = homeNameString;
                home.location = homeLocationString;

                DatabaseReference databaseReference = null;
                if (!TextUtils.isEmpty(mHomeId)) {
                    databaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("homes")
                            .child(mHomeId);
                } else {
                    databaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("homes")
                            .push();

                    databaseReference.child("members")
                            .child(mUserUid)
                            .setValue("editor");

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("members")
                            .child(mUserUid)
                            .child("homes")
                            .child(databaseReference.getKey())
                            .setValue(true);
                }

                databaseReference.child("name")
                        .setValue(homeNameString);

                databaseReference.child("location")
                        .setValue(homeLocationString);

                finish();
            }
        });
    }
}
