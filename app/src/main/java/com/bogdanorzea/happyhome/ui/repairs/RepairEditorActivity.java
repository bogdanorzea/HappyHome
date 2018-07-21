package com.bogdanorzea.happyhome.ui.repairs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Repair;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RepairEditorActivity extends AppCompatActivity {
    private static final int RC_PHOTO_PICKER = 24;
    private String mUserUid;
    private String mHomeId;
    private String mRepairId;
    private String mRepairImageUri;
    private ImageView mRepairImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button addButton = findViewById(R.id.add_button);

        final EditText repairName = findViewById(R.id.repair_name);
        final EditText repairLocation = findViewById(R.id.repair_location);
        final EditText repairDescription = findViewById(R.id.repair_description);
        final EditText repairCost = findViewById(R.id.repair_cost);
        mRepairImage = findViewById(R.id.repair_image);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("repairId")) {
                mRepairId = intent.getStringExtra("repairId");

                addButton.setText("Update");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("repairs")
                        .child(mRepairId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Repair repair = dataSnapshot.getValue(Repair.class);

                                repairName.setText(repair.name.toString(), TextView.BufferType.EDITABLE);
                                repairLocation.setText(repair.location, TextView.BufferType.EDITABLE);
                                repairDescription.setText(repair.description, TextView.BufferType.EDITABLE);
                                repairCost.setText(repair.cost.toString(), TextView.BufferType.EDITABLE);

                                if (!TextUtils.isEmpty(repair.image_uri)) {
                                    Glide.with(RepairEditorActivity.this)
                                            .load(repair.image_uri)
                                            .into(mRepairImage);
                                }
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
                String repairNameString = repairName.getText().toString();
                String repairLocationString = repairLocation.getText().toString();
                String repairDescriptionString = repairDescription.getText().toString();
                String repairCostString = repairCost.getText().toString();

                if (TextUtils.isEmpty(repairNameString) || TextUtils.isEmpty(repairLocationString) ||
                        TextUtils.isEmpty(repairDescriptionString) || TextUtils.isEmpty(repairCostString)) {
                    Toast.makeText(RepairEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Repair repair = new Repair();
                repair.home_id = mHomeId;
                repair.name = repairNameString;
                repair.location = repairLocationString;
                repair.description = repairDescriptionString;
                repair.cost = Double.parseDouble(repairCostString);
                repair.image_uri = mRepairImageUri;

                DatabaseReference repairDatabaseReference = null;
                if (!TextUtils.isEmpty(mRepairId)) {
                    repairDatabaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("repairs")
                            .child(mRepairId);
                } else {
                    repairDatabaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("repairs")
                            .push();

                    FirebaseDatabase.getInstance().getReference().child("homes")
                            .child(mHomeId)
                            .child("repairs")
                            .child(repairDatabaseReference.getKey())
                            .setValue(true);
                }

                repairDatabaseReference.setValue(repair);

                finish();
            }
        });

        Button photoButton = findViewById(R.id.add_photo);

        // ImagePickerButton shows an image picker to upload a image for a message
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            final StorageReference photoRef =FirebaseStorage.getInstance()
                    .getReference()
                    .child("repair_photos")
                    .child(selectedImageUri.getLastPathSegment());

            photoRef.putFile(selectedImageUri)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return photoRef.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                mRepairImageUri = task.getResult().toString();

                                Glide.with(RepairEditorActivity.this)
                                        .load(mRepairImageUri)
                                        .into(mRepairImage);
                            } else {
                                Toast.makeText(RepairEditorActivity.this, "Error uploading the image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
