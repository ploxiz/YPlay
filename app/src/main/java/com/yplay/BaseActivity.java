package com.yplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.yplay.player.PlayerActivity;
import com.yplay.playlists.PlaylistsActivity;
import com.yplay.search.SearchActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());

        navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_player:
                        startActivity(new Intent(BaseActivity.this, PlayerActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        return true;
                    case R.id.navigation_playlists:
                        startActivity(new Intent(BaseActivity.this, PlaylistsActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        return true;
                    case R.id.navigation_search:
                        startActivity(new Intent(BaseActivity.this, SearchActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // update which item on the navigation view should be selected
        Menu menu = navigation.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() == getNavigationItemId()) {
                item.setChecked(true);
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0); // remove the animation when changing activities
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, 0); // remove the animation when changing activities
    }

    protected abstract int getContentView();

    protected abstract int getNavigationItemId();

}
