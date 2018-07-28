package com.bogdanorzea.happyhome.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.data.Repair;
import com.bogdanorzea.happyhome.ui.MainActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.bogdanorzea.happyhome.utils.FirebaseUtils.HOMES_PATH;
import static com.bogdanorzea.happyhome.utils.FirebaseUtils.REPAIRS_PATH;


public class RepairsWidget extends AppWidgetProvider {

    private static DatabaseReference databaseReference;

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferences_name), Context.MODE_PRIVATE);
        final String homeId = sharedPref.getString(context.getString(R.string.current_home_id), "");
        if (TextUtils.isEmpty(homeId)){
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.repairs_widget);

            // Update TextViews
            views.setTextViewText(R.id.widget_details, context.getText(R.string.chose_current_home));

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_details, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(HOMES_PATH)
                .child(homeId)
                .child(REPAIRS_PATH);

        final int[] numberOfRepairsNotFixed = {0};

        final int[] currentItem = {0};
        final long[] childrenCount = new long[1];
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                childrenCount[0] = dataSnapshot.getChildrenCount();
                currentItem[0]++;
                String snapshotKey = dataSnapshot.getKey();

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(REPAIRS_PATH)
                        .child(snapshotKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Repair repair = dataSnapshot.getValue(Repair.class);

                                if (!repair.fixed) {
                                    numberOfRepairsNotFixed[0]++;
                                }

                                if (currentItem[0] >= childrenCount[0]) {
                                    updateWidget(context, appWidgetManager, appWidgetId, numberOfRepairsNotFixed[0]);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseReference.addChildEventListener(childEventListener);

    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int repairsNotFixed) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.repairs_widget);

        // Update TextViews
        String widgetText = context.getString(R.string.appwidget_text);
        views.setTextViewText(R.id.widget_title, widgetText);

        String repairStatement;
        if (repairsNotFixed == 0) {
            repairStatement = context.getString(R.string.no_repairs);
        } else {
            repairStatement = context.getResources()
                    .getQuantityString(R.plurals.numberOfRepairs, repairsNotFixed, repairsNotFixed);
        }
        views.setTextViewText(R.id.widget_details, repairStatement);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("nav_location", "repairs");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_details, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

