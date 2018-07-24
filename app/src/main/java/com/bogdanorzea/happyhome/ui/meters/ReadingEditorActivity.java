package com.bogdanorzea.happyhome.ui.meters;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.METERS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Meter.deleteReading;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.READINGS_PATH;
import static com.bogdanorzea.happyhome.utils.StringUtils.DATA_FORMAT_USER;
import static com.bogdanorzea.happyhome.utils.StringUtils.getIsoFormatFromDateString;
import static com.bogdanorzea.happyhome.utils.StringUtils.getReadableFormatFromDateString;

public class ReadingEditorActivity extends AppCompatActivity {

    private static final String READING_KEY = "reading_key";
    private String mUserUid;
    private String mHomeId;
    private String mMeterId;
    private String mReadingId;
    private EditText mReadingDateEditText;
    private EditText mReadingValueEditText;
    private Reading mReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReadingDateEditText = findViewById(R.id.reading_date);
        mReadingValueEditText = findViewById(R.id.reading_value);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId") &&
                    intent.hasExtra("meterId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
                mMeterId = intent.getStringExtra("meterId");
            }

            if (intent.hasExtra("readingId")) {
                setTitle("Edit reading");
                mReadingId = intent.getStringExtra("readingId");
            } else {
                setTitle("Add reading");
            }
        }

        if (savedInstanceState != null) {
            mReading = savedInstanceState.getParcelable(READING_KEY);
            displayReading();
        } else if (!TextUtils.isEmpty(mMeterId) && !TextUtils.isEmpty(mReadingId)) {
            displayReadingFromFirebase(mMeterId, mReadingId);
        } else {
            mReading = new Reading();
        }

        setDatePickers();
    }

    private void setDatePickers() {
        final Calendar readingDateCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener issueDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                readingDateCalendar.set(Calendar.YEAR, year);
                readingDateCalendar.set(Calendar.MONTH, monthOfYear);
                readingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(DATA_FORMAT_USER, Locale.US);
                mReadingDateEditText.setText(sdf.format(readingDateCalendar.getTime()));
            }
        };

        mReadingDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReadingEditorActivity.this,
                        issueDateSetListener,
                        readingDateCalendar.get(Calendar.YEAR),
                        readingDateCalendar.get(Calendar.MONTH),
                        readingDateCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    private void saveCurrentReadingToFirebase() {
        String readingDateString = getIsoFormatFromDateString(mReadingDateEditText.getText().toString());
        String readingValueString = mReadingValueEditText.getText().toString();

        if (TextUtils.isEmpty(readingDateString) || TextUtils.isEmpty(readingValueString)) {
            Toast.makeText(ReadingEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mReading.meter_id = mMeterId;
        mReading.value = Double.parseDouble(readingValueString);
        mReading.date = readingDateString;

        DatabaseReference readingDatabaseReference = null;
        if (!TextUtils.isEmpty(mReadingId)) {
            readingDatabaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(READINGS_PATH)
                    .child(mReadingId);
        } else {
            readingDatabaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(READINGS_PATH)
                    .push();

            FirebaseDatabase.getInstance().getReference()
                    .child(METERS_PATH)
                    .child(mMeterId)
                    .child(READINGS_PATH)
                    .child(readingDatabaseReference.getKey())
                    .setValue(true);
        }

        readingDatabaseReference.setValue(mReading);

        finish();
    }

    private void displayReadingFromFirebase(String meterId, final String readingId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(READINGS_PATH)
                .child(readingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mReading = dataSnapshot.getValue(Reading.class);
                        if (mReading != null) {
                            displayReading();
                        } else {
                            Timber.d("Error retrieving reading with id %s", readingId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void displayReading() {
        mReadingDateEditText.setText(getReadableFormatFromDateString(mReading.date), TextView.BufferType.EDITABLE);
        mReadingValueEditText.setText(Double.toString(mReading.value), TextView.BufferType.EDITABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reading_editor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (TextUtils.isEmpty(mReadingId)) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCurrentReadingToFirebase();
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
                        deleteReading(mReadingId);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mReading.meter_id = mMeterId;
        mReading.date = getIsoFormatFromDateString(mReadingDateEditText.getText().toString());
        mReading.value = Double.parseDouble(mReadingValueEditText.getText().toString());

        outState.putParcelable(READING_KEY, mReading);
    }
}
