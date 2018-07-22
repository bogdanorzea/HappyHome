package com.bogdanorzea.happyhome.ui.repairs;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Repair;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Repair.deleteRepair;

public class RepairEditorActivity extends AppCompatActivity {
    private static final int RC_PHOTO_PICKER = 24;
    private String mUserUid;
    private String mHomeId;
    private String mRepairId;
    private String mRepairImageUri;

    private ImageView mRepairImageView;
    private ProgressBar mProgressBar;
    private EditText mRepairNameEditText;
    private EditText mRepairLocationEditText;
    private EditText mRepairDescriptionEditText;
    private EditText mRepairCostEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRepairNameEditText = findViewById(R.id.repair_name);
        mRepairLocationEditText = findViewById(R.id.repair_location);
        mRepairDescriptionEditText = findViewById(R.id.repair_description);
        mRepairCostEditText = findViewById(R.id.repair_cost);
        mRepairImageView = findViewById(R.id.repair_image);
        mProgressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("repairId")) {
                mRepairId = intent.getStringExtra("repairId");

                loadRepair(mRepairId);
            }
        }

        mRepairImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
    }

    private void loadRepair(String repairId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("repairs")
                .child(repairId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Repair repair = dataSnapshot.getValue(Repair.class);

                        mRepairNameEditText.setText(repair.name, TextView.BufferType.EDITABLE);
                        mRepairLocationEditText.setText(repair.location, TextView.BufferType.EDITABLE);
                        mRepairDescriptionEditText.setText(repair.description, TextView.BufferType.EDITABLE);
                        mRepairCostEditText.setText(repair.cost.toString(), TextView.BufferType.EDITABLE);

                        if (!TextUtils.isEmpty(repair.image_uri)) {
                            Glide.with(RepairEditorActivity.this)
                                    .load(repair.image_uri)
                                    .into(mRepairImageView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void saveRepair() {
        String repairNameString = mRepairNameEditText.getText().toString();
        String repairLocationString = mRepairLocationEditText.getText().toString();
        String repairDescriptionString = mRepairDescriptionEditText.getText().toString();
        String repairCostString = mRepairCostEditText.getText().toString();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            mRepairImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);

            Uri selectedImageUri = data.getData();

            final StorageReference photoRef = FirebaseStorage.getInstance()
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
                                        .apply(new RequestOptions().override(1024, 1024).centerCrop())
                                        .into(mRepairImageView);

                                mProgressBar.setVisibility(View.GONE);
                                mRepairImageView.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(RepairEditorActivity.this, "Error uploading the image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_repair_editor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (TextUtils.isEmpty(mRepairId)) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveRepair();
                return true;
            case R.id.action_add_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
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
                        deleteRepair(mRepairId);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this repair?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

}
