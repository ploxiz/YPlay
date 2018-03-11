package com.yplay.playlists;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.yplay.BaseActivity;
import com.yplay.BuildConfig;
import com.yplay.DatabaseHandler;
import com.yplay.R;
import com.yplay.player.PlayerActivity;
import com.yplay.search.MediaObject;

import java.util.ArrayList;

public class ExpandedPlaylistActivity extends BaseActivity {

    private PlaylistObject playlist;
    private ArrayList<MediaObject> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlist = getIntent().getParcelableExtra("PLAYLIST");

        fetchPlaylistMedia(playlist);
        initFloatingButton();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        playlist = getIntent().getParcelableExtra("PLAYLIST");

        fetchPlaylistMedia(playlist);
        initFloatingButton();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_expanded_playlist;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.navigation_playlists;
    }

    private void fetchPlaylistMedia(PlaylistObject playlist) {
        DatabaseHandler databaseHandler = new DatabaseHandler(this, BuildConfig.DATABASE_NAME);
        databaseHandler.open();
        getSupportActionBar().setTitle(databaseHandler.retrievePlaylistName(playlist.getId()));
        mediaList = databaseHandler.loadMediaFromPlaylist(String.valueOf(playlist.getId()));
        databaseHandler.close();

        RecyclerView recyclerView = findViewById(R.id.expanded_playlist_recyclerView);
        recyclerView.setAdapter(new ExpandedPlaylistAdapter(ExpandedPlaylistActivity.this, mediaList, playlist));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.expanded_playlist_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpandedPlaylistActivity.this, PlayerActivity.class);
                intent.putParcelableArrayListExtra("MEDIA_QUEUE", mediaList);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                ExpandedPlaylistActivity.this.startActivityIfNeeded(intent, 0);
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ExpandedPlaylistActivity.this, "Play all", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

}
