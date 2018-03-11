package com.yplay.playlists;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.yplay.BaseActivity;
import com.yplay.BuildConfig;
import com.yplay.DatabaseHandler;
import com.yplay.R;

import java.util.List;

public class PlaylistsActivity extends BaseActivity {

    private List<PlaylistObject> playlists;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_playlists);
        recyclerView = findViewById(R.id.playlists_recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initPlaylists();
        initRecyclerView();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_playlists;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.navigation_playlists;
    }

    private void initPlaylists() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this, BuildConfig.DATABASE_NAME);
        databaseHandler.open();
        playlists = databaseHandler.loadPlaylists();
        databaseHandler.close();
    }

    private void initRecyclerView() {
        recyclerView.setAdapter(new PlaylistsAdapter(PlaylistsActivity.this, playlists));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
