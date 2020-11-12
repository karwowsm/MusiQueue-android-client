package pl.com.karwowsm.musiqueue.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import pl.com.karwowsm.musiqueue.Constants;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.TokenHolder;
import pl.com.karwowsm.musiqueue.api.controller.BaseController;
import pl.com.karwowsm.musiqueue.api.dto.UserAccount;

public abstract class NavigationViewActivity extends AbstractActivity implements NavigationView.OnNavigationItemSelectedListener {

    UserAccount me;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBarsColors();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.tracks_nav_menu_item && !(this instanceof TracksActivity)) {
            Intent intent = new Intent(this, TracksActivity.class);
            intent.putExtra("me", me);
            startActivity(intent);
        } else if (id == R.id.share_nav_menu_item) {
            showToast(R.string.not_developed_yet);
        } else if (id == R.id.send_nav_menu_item) {
            showToast(R.string.not_developed_yet);
        } else if (id == R.id.logout_nav_menu_item) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    void initNavigationView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.username_tv);
        TextView emailTextView = headerView.findViewById(R.id.email_tv);
        usernameTextView.setText(me.getUsername());
        emailTextView.setText(me.getEmail());
    }

    void logout() {
        clearToken();
        BaseController.setBaseErrorResponseListener(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void clearToken() {
        getSharedPreferences(Constants.AUTH_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.PREF_TOKEN, null)
            .apply();
        TokenHolder.setToken(null);
    }
}
