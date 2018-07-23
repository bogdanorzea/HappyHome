package com.bogdanorzea.happyhome.ui.utilities;

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

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.BILLS_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.UTILITIES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.Utility.deleteBill;

public class BillEditorActivity extends AppCompatActivity {

    private static final String BILL_KEY = "bill_object";
    private String mUserUid;
    private String mHomeId;
    private String mUtilityId;
    private String mBillId;
    private Bill mBill;
    private EditText mIssueDateEditText;
    private EditText mDueDateEditText;
    private EditText mBillValueEditText;

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

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId") && intent.hasExtra("utilityId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
                mUtilityId = intent.getStringExtra("utilityId");
            }

            if (intent.hasExtra("billId")) {
                setTitle("Edit bill");
                mBillId = intent.getStringExtra("billId");
            } else {
                setTitle("Add bill");
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
    }

    private void displayBillFromFirebase(final String utilityId, String billId) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(BILLS_PATH)
                .child(billId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mBill = dataSnapshot.getValue(Bill.class);
                        mBill.utility_id = utilityId;

                        displayBill();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void displayBill() {
        mBillValueEditText.setText(mBill.value.toString(), TextView.BufferType.EDITABLE);
        mIssueDateEditText.setText(mBill.issue_date, TextView.BufferType.EDITABLE);
        mDueDateEditText.setText(mBill.due_date, TextView.BufferType.EDITABLE);
    }

    private void saveCurrentBillToFirebase() {
        String issueDateString = mIssueDateEditText.getText().toString();
        String dueDateString = mDueDateEditText.getText().toString();
        String billValueString = mBillValueEditText.getText().toString();

        if (TextUtils.isEmpty(issueDateString) || TextUtils.isEmpty(dueDateString) ||
                TextUtils.isEmpty(billValueString)) {
            Toast.makeText(BillEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        mBill.utility_id = mUtilityId;
        mBill.value = Double.parseDouble(billValueString);
        mBill.issue_date = issueDateString;
        mBill.due_date = dueDateString;

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
        builder.setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BILL_KEY, mBill);
    }
}
