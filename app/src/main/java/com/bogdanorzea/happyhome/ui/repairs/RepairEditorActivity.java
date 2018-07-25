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
import android.widget.CheckBox;
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

import timber.log.Timber;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.HOMES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.REPAIRS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.REPAIR_PHOTOS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Repair.deleteRepair;

public class RepairEditorActivity extends AppCompatActivity {
    private static final String REPAIR_KEY = "repair_object";
    private static final int RC_PHOTO_PICKER = 24;

    private String mHomeId;
    private String mRepairId;
    private Repair mRepair;

    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private EditText mNameEditText;
    private EditText mLocationEditText;
    private EditText mDescriptionEditText;
    private EditText mCostEditText;
    private CheckBox mIsFixedCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameEditText = findViewById(R.id.repair_name);
        mLocationEditText = findViewById(R.id.repair_location);
        mDescriptionEditText = findViewById(R.id.repair_description);
        mCostEditText = findViewById(R.id.repair_cost);
        mImageView = findViewById(R.id.repair_image);
        mProgressBar = findViewById(R.id.progressBar);
        mIsFixedCheckBox = findViewById(R.id.repair_fixed);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("homeId")) {
                mHomeId = intent.getStringExtra("homeId");
            }

            if (intent.hasExtra("repairId")) {
                setTitle("Edit repair");
                mRepairId = intent.getStringExtra("repairId");
            } else {
                setTitle("Add repair");
            }
        }

        if (savedInstanceState != null) {
            mRepair = savedInstanceState.getParcelable(REPAIR_KEY);
            displayRepair();
        } else if (!TextUtils.isEmpty(mHomeId) && !TextUtils.isEmpty(mRepairId)) {
            displayRepairFromFirebase(mHomeId, mRepairId);
        } else {
            mRepair = new Repair();
        }
    }

    private void displayRepairFromFirebase(final String mHomeId, final String repairId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(REPAIRS_PATH)
                .child(repairId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mRepair = dataSnapshot.getValue(Repair.class);
                        if (mRepair != null) {
                            displayRepair();
                        } else {
                            Timber.d("Error retrieving repair with id %s", repairId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void displayRepair() {
        mNameEditText.setText(mRepair.name, TextView.BufferType.EDITABLE);
        mLocationEditText.setText(mRepair.location, TextView.BufferType.EDITABLE);
        mDescriptionEditText.setText(mRepair.description, TextView.BufferType.EDITABLE);
        mCostEditText.setText(mRepair.cost.toString(), TextView.BufferType.EDITABLE);
        mIsFixedCheckBox.setChecked(mRepair.fixed);

        if (!TextUtils.isEmpty(mRepair.image_uri)) {
            Glide.with(RepairEditorActivity.this)
                    .load(mRepair.image_uri)
                    .into(mImageView);
        }
    }

    private void saveCurrentRepairToFirebase() {
        String repairNameString = mNameEditText.getText().toString();
        String repairLocationString = mLocationEditText.getText().toString();
        String repairDescriptionString = mDescriptionEditText.getText().toString();
        String repairCostString = mCostEditText.getText().toString();

        if (TextUtils.isEmpty(repairNameString) || TextUtils.isEmpty(repairLocationString) ||
                TextUtils.isEmpty(repairDescriptionString) || TextUtils.isEmpty(repairCostString)) {
            Toast.makeText(RepairEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mRepair.home_id = mHomeId;
        mRepair.name = repairNameString;
        mRepair.location = repairLocationString;
        mRepair.description = repairDescriptionString;
        mRepair.cost = Double.parseDouble(repairCostString);
        mRepair.fixed = mIsFixedCheckBox.isChecked();

        DatabaseReference databaseReference = null;
        if (!TextUtils.isEmpty(mRepairId)) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(REPAIRS_PATH)
                    .child(mRepairId);
        } else {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(REPAIRS_PATH)
                    .push();

            FirebaseDatabase.getInstance().getReference()
                    .child(HOMES_PATH)
                    .child(mHomeId)
                    .child(REPAIRS_PATH)
                    .child(databaseReference.getKey())
                    .setValue(true);
        }

        databaseReference.setValue(mRepair);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            mProgressBar.setVisibility(View.VISIBLE);

            Uri selectedImageUri = data.getData();

            final StorageReference photoRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child(REPAIR_PHOTOS_PATH)
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
                                mRepair.image_uri = task.getResult().toString();

                                Glide.with(RepairEditorActivity.this)
                                        .load(mRepair.image_uri)
                                        .apply(new RequestOptions().override(1024, 1024).centerCrop())
                                        .into(mImageView);

                                mProgressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(RepairEditorActivity.this, "Error uploading the image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_repair_editor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (TextUtils.isEmpty(mRepairId)) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCurrentRepairToFirebase();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRepair.home_id = mHomeId;
        mRepair.name = mNameEditText.getText().toString();
        mRepair.location = mLocationEditText.getText().toString();
        mRepair.description = mDescriptionEditText.getText().toString();
        mRepair.cost = Double.parseDouble(mCostEditText.getText().toString());
        mRepair.fixed = mIsFixedCheckBox.isChecked();

        outState.putParcelable(REPAIR_KEY, mRepair);
    }
}
