package com.bogdanorzea.happyhome.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.bogdanorzea.happyhome.data.Home;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import timber.log.Timber;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.HOMES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Home.deleteHome;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.MEMBERS_PATH;

public class HomeEditorActivity extends AppCompatActivity {

    private static final String HOME_KEY = "home_key";
    private String mUserUid;
    private String mHomeId;
    private Home mHome;
    private EditText mHomeLocationEditText;
    private EditText mHomeNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHomeNameEditText = findViewById(R.id.home_name);
        mHomeLocationEditText = findViewById(R.id.home_location);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid")) {
                mUserUid = intent.getStringExtra("userUid");
            }

            if (intent.hasExtra("homeId")) {
                mHomeId = intent.getStringExtra("homeId");
            }
        }

        if (savedInstanceState != null) {
            mHome = savedInstanceState.getParcelable(HOME_KEY);
            if (mHomeId != null) {
                mHome.id = mHomeId;
            }
            displayHome();
        } else if (!TextUtils.isEmpty(mUserUid) && !TextUtils.isEmpty(mHomeId)) {
            displayFromFirebase(mUserUid, mHomeId);
        } else {
            mHome = new Home();
        }
    }

    private void displayFromFirebase(final String userId, final String homeId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(HOMES_PATH)
                .child(homeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mHome = dataSnapshot.getValue(Home.class);
                        if (mHome != null) {
                            mHome.id = homeId;

                            displayHome();
                        } else {
                            Timber.d("Error retrieving utility with id %s", homeId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void displayHome() {
        mHomeNameEditText.setText(mHome.name, TextView.BufferType.EDITABLE);
        mHomeLocationEditText.setText(mHome.location, TextView.BufferType.EDITABLE);
    }

    private void saveCurrentHomeToFirebase() {
        String homeNameString = mHomeNameEditText.getText().toString();
        String homeLocationString = mHomeLocationEditText.getText().toString();

        if (TextUtils.isEmpty(homeNameString) || TextUtils.isEmpty(homeLocationString)) {
            Toast.makeText(HomeEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mHome.user_id = mUserUid;
        mHome.name = homeNameString;
        mHome.location = homeLocationString;

        DatabaseReference databaseReference = null;
        if (!TextUtils.isEmpty(mHomeId)) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(HOMES_PATH)
                    .child(mHomeId);
        } else {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(HOMES_PATH)
                    .push();

            databaseReference.child(MEMBERS_PATH)
                    .child(mUserUid)
                    .setValue("editor");

            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(MEMBERS_PATH)
                    .child(mUserUid)
                    .child(HOMES_PATH)
                    .child(databaseReference.getKey())
                    .setValue(true);
        }

        databaseReference.child("name").setValue(mHome.name);
        databaseReference.child("location").setValue(mHome.location);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home_editor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (TextUtils.isEmpty(mHomeId)) {
            menu.findItem(R.id.action_set_current).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else {
            setCurrentMenuIcon(menu.findItem(R.id.action_set_current));
        }

        return true;
    }

    private void setCurrentMenuIcon(MenuItem menuItem) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
        String currentHomeId = sharedPref.getString(getString(R.string.current_home_id), "");

        if (currentHomeId.equals(mHomeId)) {
            menuItem.setIcon(R.drawable.round_star_white_24);
        } else {
            menuItem.setIcon(R.drawable.round_star_border_white_24);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCurrentHomeToFirebase();
                return true;
            case R.id.action_set_current:
                saveHomeCurrent();
                setCurrentMenuIcon(item);
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

    private void saveHomeCurrent() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
        sharedPref.edit()
                .putString(getString(R.string.current_home_id), mHome.id)
                .apply();
    }

    private void confirmDelete() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteHome(mHomeId);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this home?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mHome.user_id = mUserUid;
        mHome.name = mHomeNameEditText.getText().toString();
        mHome.location = mHomeLocationEditText.getText().toString();

        outState.putParcelable(HOME_KEY, mHome);
    }
}
