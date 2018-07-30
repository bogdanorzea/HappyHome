package com.bogdanorzea.happyhome.ui.utilities;

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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Bill;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.BILLS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.UTILITIES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Utility.deleteBill;
import static com.bogdanorzea.happyhome.utils.StringUtils.DATA_FORMAT_USER;
import static com.bogdanorzea.happyhome.utils.StringUtils.getIsoFormatFromDateString;
import static com.bogdanorzea.happyhome.utils.StringUtils.getReadableFormatFromDateString;

public class BillEditorActivity extends AppCompatActivity {

    private static final String BILL_KEY = "bill_key";
    private String mUserUid;
    private String mHomeId;
    private String mUtilityId;
    private String mBillId;
    private Bill mBill;
    private EditText mIssueDateEditText;
    private EditText mDueDateEditText;
    private EditText mBillValueEditText;
    private CheckBox mIsPayedCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mIssueDateEditText = findViewById(R.id.issue_date);
        mDueDateEditText = findViewById(R.id.due_date);
        mBillValueEditText = findViewById(R.id.bill_value);
        mIsPayedCheckBox = findViewById(R.id.bill_paid);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId") && intent.hasExtra("utilityId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
                mUtilityId = intent.getStringExtra("utilityId");
            }

            if (intent.hasExtra("billId")) {
                setTitle(getString(R.string.title_edit_bill));
                mBillId = intent.getStringExtra("billId");
            } else {
                setTitle(getString(R.string.title_add_bill));
            }
        }

        if (savedInstanceState != null) {
            mBill = savedInstanceState.getParcelable(BILL_KEY);
            displayBill();
        } else if (!TextUtils.isEmpty(mBillId) && !TextUtils.isEmpty(mUtilityId)) {
            displayBillFromFirebase(mUtilityId, mBillId);
        } else {
            mBill = new Bill();
        }

        setDatePickers();
    }

    private void setDatePickers() {
        final Calendar issueCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener issueDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                issueCalendar.set(Calendar.YEAR, year);
                issueCalendar.set(Calendar.MONTH, monthOfYear);
                issueCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(DATA_FORMAT_USER, Locale.US);
                mIssueDateEditText.setText(sdf.format(issueCalendar.getTime()));
            }
        };

        mIssueDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BillEditorActivity.this,
                        issueDateSetListener,
                        issueCalendar.get(Calendar.YEAR),
                        issueCalendar.get(Calendar.MONTH),
                        issueCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        final Calendar dueCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dueDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dueCalendar.set(Calendar.YEAR, year);
                dueCalendar.set(Calendar.MONTH, monthOfYear);
                dueCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(DATA_FORMAT_USER, Locale.US);
                mDueDateEditText.setText(sdf.format(dueCalendar.getTime()));
            }
        };

        mDueDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BillEditorActivity.this,
                        dueDateSetListener,
                        dueCalendar.get(Calendar.YEAR),
                        dueCalendar.get(Calendar.MONTH),
                        dueCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void displayBillFromFirebase(final String utilityId, final String billId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(BILLS_PATH)
                .child(billId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mBill = dataSnapshot.getValue(Bill.class);
                        if (mBill != null) {
                            displayBill();
                        } else {
                            Timber.d("Error retrieving bill with id %s", billId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void displayBill() {
        mIssueDateEditText.setText(getReadableFormatFromDateString(mBill.issue_date), TextView.BufferType.EDITABLE);
        mDueDateEditText.setText(getReadableFormatFromDateString(mBill.due_date), TextView.BufferType.EDITABLE);
        mBillValueEditText.setText(String.format(Locale.US, "%.2f", mBill.value), TextView.BufferType.EDITABLE);
        mIsPayedCheckBox.setChecked(mBill.paid);
    }

    private void saveCurrentBillToFirebase() {
        String issueDateString = getIsoFormatFromDateString(mIssueDateEditText.getText().toString());
        String dueDateString = getIsoFormatFromDateString(mDueDateEditText.getText().toString());
        String billValueString = mBillValueEditText.getText().toString();

        if (TextUtils.isEmpty(issueDateString) || TextUtils.isEmpty(dueDateString) ||
                TextUtils.isEmpty(billValueString)) {
            Toast.makeText(BillEditorActivity.this, R.string.toast_data_is_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        mBill.utility_id = mUtilityId;
        mBill.value = Double.parseDouble(billValueString);
        mBill.issue_date = issueDateString;
        mBill.due_date = dueDateString;
        mBill.paid = mIsPayedCheckBox.isChecked();

        DatabaseReference billReference = null;
        if (!TextUtils.isEmpty(mBillId)) {
            billReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(BILLS_PATH)
                    .child(mBillId);
        } else {
            billReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(BILLS_PATH)
                    .push();

            FirebaseDatabase.getInstance().getReference()
                    .child(UTILITIES_PATH)
                    .child(mUtilityId)
                    .child(BILLS_PATH)
                    .child(billReference.getKey())
                    .setValue(true);
        }

        billReference.setValue(mBill);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bill_editor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (TextUtils.isEmpty(mBillId)) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCurrentBillToFirebase();
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
                        deleteBill(mBillId);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_bill)
                .setPositiveButton(R.string.button_yes, dialogClickListener)
                .setNegativeButton(R.string.button_no, dialogClickListener)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mBill.utility_id = mUtilityId;
        mBill.due_date = getIsoFormatFromDateString(mDueDateEditText.getText().toString());
        mBill.issue_date = getIsoFormatFromDateString(mIssueDateEditText.getText().toString());

        String billValueString = mBillValueEditText.getText().toString();
        if (!TextUtils.isEmpty(billValueString)) {
            mBill.value = Double.parseDouble(billValueString);
        } else {
            mBill.value = 0.0;
        }

        mBill.paid = mIsPayedCheckBox.isChecked();

        outState.putParcelable(BILL_KEY, mBill);
    }
}
