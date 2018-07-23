package com.bogdanorzea.happyhome.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import timber.log.Timber;

public class FirebaseUtils {

    public static final String MEMBERS_PATH = "members";
    public static final String HOMES_PATH = "homes";
    public static final String UTILITIES_PATH = "utilities";
    public static final String METERS_PATH = "meters";
    public static final String REPAIRS_PATH = "repairs";
    public static final String REPAIR_PHOTOS_PATH = "repair_photos";
    public static final String BILLS_PATH = "bills";
    public static final String UTILITIES_KEY = "utilities";

    public static class Repair {

        public static void deleteRepair(final String repairId) {
            final DatabaseReference repairReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(REPAIRS_PATH)
                    .child(repairId);

            repairReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    com.bogdanorzea.happyhome.data.Repair repair = dataSnapshot.getValue(com.bogdanorzea.happyhome.data.Repair.class);

                    if (repair == null) {
                        Timber.d("Failed to get the repair object from reference %s", repairId);
                        return;
                    }

                    deleteRepairImage(repair.image_uri);
                    repairReference.removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private static void deleteRepairImage(final String imageUrl) {
            if (TextUtils.isEmpty(imageUrl)) {
                return;
            }

            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Timber.d("Successfully deleted file %s", imageUrl);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Timber.d("Failed to delete file %s", imageUrl);
                }
            });

        }
    }

    public static class Utility {

        public static void deleteUtility(final String utilityId) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(UTILITIES_PATH)
                    .child(utilityId);

            databaseReference.removeValue();
        }

        public static void deleteBill(final String billId) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(BILLS_PATH)
                    .child(billId);

            databaseReference.removeValue();
        }
    }

}
