package com.yplay.playlists;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yplay.BuildConfig;
import com.yplay.DatabaseHandler;
import com.yplay.R;

import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.MyViewHolder> {

    private Context context;
    private List<PlaylistObject> playlists;

    PlaylistsAdapter(Context context, List<PlaylistObject> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @Override
    public PlaylistsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlists, parent, false);

        return new PlaylistsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PlaylistsAdapter.MyViewHolder holder, final int position) {
        final PlaylistObject playlist = playlists.get(position);

        holder.title.setText(playlist.getTitle());

        holder.count.setText(playlist.getCount() + " videos");

        holder.duration.setText(playlist.getDurationFormatted());

        holder.rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(context);
                final AlertDialog inputDialog = new AlertDialog.Builder(context)
                        .setTitle("Type a new name")
                        .setView(input)
                        .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (input.getText().toString().equals("")) {
                                    Toast.makeText(context,
                                            "Please enter a valid name for the playlist",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    DatabaseHandler databaseHandler = new DatabaseHandler(context,
                                            BuildConfig.DATABASE_NAME);
                                    databaseHandler.open();
                                    databaseHandler.renamePlaylist(String.valueOf(playlist.getId()),
                                            input.getText().toString());
                                    databaseHandler.close();

                                    Toast.makeText(context, "Playlist renamed", Toast.LENGTH_SHORT).show();

                                    Intent intent = ((PlaylistsActivity) context).getIntent();
                                    ((PlaylistsActivity) context).finish();
                                    context.startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create();
                inputDialog.show();
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // TODO:
                Intent intent = new Intent(context, ExpandedPlaylistActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("PLAYLIST", playlist);
                context.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (holder.options.getVisibility() == View.GONE) {
                    holder.options.setVisibility(View.VISIBLE);
                } else {
                    holder.options.setVisibility(View.GONE);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, count, duration;
        private LinearLayout options;
        private Button rename, delete;
        private CardView cardView;

        private MyViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.playlists_title_textView);
            count = view.findViewById(R.id.playlists_count_textView);
            duration = view.findViewById(R.id.playlists_duration_textView);
            options = view.findViewById(R.id.playlists_options_layout);
            rename = view.findViewById(R.id.playlists_rename_button);
            delete = view.findViewById(R.id.playlists_delete_button);
            cardView = view.findViewById(R.id.playlists_card_view);
        }
    }

}
