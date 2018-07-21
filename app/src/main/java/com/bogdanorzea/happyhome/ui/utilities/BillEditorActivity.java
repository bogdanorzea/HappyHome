package com.bogdanorzea.happyhome.ui.utilities;

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
import com.bogdanorzea.happyhome.data.Bill;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BillEditorActivity extends AppCompatActivity {

    private String mUserUid;
    private String mHomeId;
    private String mUtilityId;
    private String mBillId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText issueDate = findViewById(R.id.issue_date);
        final EditText dueDate = findViewById(R.id.due_date);
        final EditText billValue = findViewById(R.id.bill_value);
        Button addButton = findViewById(R.id.add_button);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("userUid") && intent.hasExtra("homeId") && intent.hasExtra("utilityId")) {
                mUserUid = intent.getStringExtra("userUid");
                mHomeId = intent.getStringExtra("homeId");
                mUtilityId = intent.getStringExtra("utilityId");
            }

            if (intent.hasExtra("billId")) {
                mBillId = intent.getStringExtra("billId");

                addButton.setText("Update");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("bills")
                        .child(mBillId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Bill bill = dataSnapshot.getValue(Bill.class);

                                billValue.setText(bill.value.toString(), TextView.BufferType.EDITABLE);
                                issueDate.setText(bill.issue_date, TextView.BufferType.EDITABLE);
                                dueDate.setText(bill.due_date, TextView.BufferType.EDITABLE);
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
                String issueDateString = issueDate.getText().toString();
                String dueDateString = dueDate.getText().toString();
                String billValueString = billValue.getText().toString();

                if (TextUtils.isEmpty(issueDateString) || TextUtils.isEmpty(dueDateString) ||
                        TextUtils.isEmpty(billValueString)) {
                    Toast.makeText(BillEditorActivity.this, "Data is missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bill bill = new Bill();
                bill.utility_id = mUtilityId;
                bill.value = Double.parseDouble(billValueString);
                bill.issue_date = issueDateString;
                bill.due_date = dueDateString;

                DatabaseReference billReference = null;
                if (!TextUtils.isEmpty(mBillId)) {
                    billReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("bills")
                            .child(mBillId);
                } else {
                    billReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("bills")
                            .push();

                    FirebaseDatabase.getInstance().getReference().child("utilities")
                            .child(mUtilityId)
                            .child("bills")
                            .child(billReference.getKey())
                            .setValue(true);
                }

                billReference.setValue(bill);

                finish();
            }
        });
    }
}
