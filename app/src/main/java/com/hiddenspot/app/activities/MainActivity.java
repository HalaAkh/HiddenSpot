package com.hiddenspot.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hiddenspot.app.R;
import com.hiddenspot.app.fragments.FavoritesFragment;
import com.hiddenspot.app.fragments.HomeFragment;
import com.hiddenspot.app.fragments.ProfileFragment;
import com.hiddenspot.app.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)      { loadFragment(new HomeFragment());      return true; }
            if (id == R.id.nav_search)    { loadFragment(new SearchFragment());    return true; }
            if (id == R.id.nav_favorites) { loadFragment(new FavoritesFragment()); return true; }
            if (id == R.id.nav_profile)   { loadFragment(new ProfileFragment());   return true; }
            if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddPlaceActivity.class));
                return false;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }
}
