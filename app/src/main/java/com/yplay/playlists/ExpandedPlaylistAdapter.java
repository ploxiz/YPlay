package com.yplay.playlists;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yplay.BuildConfig;
import com.yplay.DatabaseHandler;
import com.yplay.R;
import com.yplay.player.PlayerActivity;
import com.yplay.search.MediaObject;

import java.util.ArrayList;
import java.util.List;

public class ExpandedPlaylistAdapter extends RecyclerView.Adapter<ExpandedPlaylistAdapter.MyViewHolder> {


    private Context context;
    private List<MediaObject> mediaList;
    private PlaylistObject playlist;

    public ExpandedPlaylistAdapter(Context context, List<MediaObject> mediaList, PlaylistObject playlist) {
        this.context = context;
        this.mediaList = mediaList;
        this.playlist = playlist;
    }

    @Override
    public ExpandedPlaylistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expanded_playlist, parent, false);

        return new ExpandedPlaylistAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final MediaObject media = mediaList.get(position);

        holder.title.setText(media.getTitle());

        holder.duration.setText(String.valueOf(media.getDurationFormatted()));

        holder.rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // TODO: remove this button
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler databaseHandler = new DatabaseHandler(context, BuildConfig.DATABASE_NAME);
                databaseHandler.open();
                databaseHandler.deleteVideo(media.getId(), playlist.getId());
                databaseHandler.close();

                Intent intent = ((ExpandedPlaylistActivity) context).getIntent();
                ((ExpandedPlaylistActivity) context).finish();
                context.startActivity(intent);

            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);

                ArrayList<MediaObject> mediaQueue = new ArrayList<>();
                mediaQueue.add(media);

                intent.putParcelableArrayListExtra("MEDIA_QUEUE", mediaQueue);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                ((AppCompatActivity) context).startActivityIfNeeded(intent, 0);
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
        return mediaList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, duration;
        private LinearLayout options;
        private Button rename, delete;
        private CardView cardView;

        private MyViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.expanded_playlist_title_textView);
            duration = view.findViewById(R.id.expanded_playlist_duration_textView);
            options = view.findViewById(R.id.expanded_playlist_options_layout);
            rename = view.findViewById(R.id.expanded_playlist_rename_button);
            delete = view.findViewById(R.id.expanded_playlist_delete_button);
            cardView = view.findViewById(R.id.expanded_playlist_card_view);
        }
    }
}
