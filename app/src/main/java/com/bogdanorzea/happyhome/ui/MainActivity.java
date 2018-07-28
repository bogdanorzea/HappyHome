package com.bogdanorzea.happyhome.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bogdanorzea.happyhome.R;
import com.bogdanorzea.happyhome.ui.home.HomesFragment;
import com.bogdanorzea.happyhome.ui.meters.MetersFragment;
import com.bogdanorzea.happyhome.ui.repairs.RepairsFragment;
import com.bogdanorzea.happyhome.ui.utilities.UtilitiesFragment;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    onSignInInitialize(user.getDisplayName(), user.getPhotoUrl(), user.getEmail());
                    if (getSupportFragmentManager().getFragments().isEmpty()) {
                        mNavigationView.setCheckedItem(R.id.nav_homes);
                        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_homes));
                    }
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.happy_home_logo)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if (user != null && intent != null) {
            if (intent.hasExtra("nav_location")) {
                String location = intent.getStringExtra("nav_location");

                int locationId;
                switch (location) {
                    case "repairs": {
                        locationId = R.id.nav_repairs;
                        break;
                    }
                    default:
                        return;
                }

                mNavigationView.setCheckedItem(locationId);
                onNavigationItemSelected(mNavigationView.getMenu().findItem(locationId));
            }
        }
    }

    private void onSignInInitialize(String displayName, Uri photoUrl, String userEmail) {
        View headerView = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);

        TextView userNameTextView = headerView.findViewById(R.id.user_name);
        userNameTextView.setText(displayName);

        ImageView userPhotoImageView = headerView.findViewById(R.id.user_photo);
        Glide.with(this).load(photoUrl).into(userPhotoImageView);

        TextView userEmailTextView = headerView.findViewById(R.id.user_email);
        userEmailTextView.setText(userEmail);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (id) {
            case R.id.nav_homes:
                HomesFragment homesFragment = new HomesFragment();

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_placeholder, homesFragment)
                        .commit();
                break;
            case R.id.nav_bills:
                UtilitiesFragment utilityBillsFragment = new UtilitiesFragment();

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_placeholder, utilityBillsFragment)
                        .commit();
                break;
            case R.id.nav_readings:
                MetersFragment metersFragment = new MetersFragment();

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_placeholder, metersFragment)
                        .commit();
                break;
            case R.id.nav_repairs:
                RepairsFragment repairsFragment = new RepairsFragment();

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_placeholder, repairsFragment)
                        .commit();
                break;
            case R.id.nav_logout:
                AuthUI.getInstance().signOut(this);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
