package com.yplay.search;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yplay.DatabaseHandler;
import com.yplay.R;
import com.yplay.player.PlayerActivity;
import com.yplay.playlists.PlaylistObject;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private Context context;
    private List<MediaObject> searchResults;

    SearchAdapter(Context context, List<MediaObject> searchResults) {
        this.context = context;
        this.searchResults = searchResults;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final MediaObject searchResult = searchResults.get(position);

        holder.title.setText(searchResult.getTitle());

        holder.duration.setText(searchResult.getDurationFormatted());

        holder.views.setText(searchResult.getViews() + " views");

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHandler databaseHandler = new DatabaseHandler(context, "local.db");
                databaseHandler.open();
                List<PlaylistObject> playlists = databaseHandler.loadPlaylists();
                databaseHandler.close();

                CharSequence[] playlistsArray = new CharSequence[playlists.size()];
                for (int i = 0; i < playlistsArray.length; i++) {
                    playlistsArray[i] = playlists.get(i).getTitle();
                }

                final List<Integer> selected = new ArrayList<>();

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Choose playlists")
                        .setMultiChoiceItems(playlistsArray, null,
                                new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    selected.add(which + 1);
                                } else {
                                    selected.remove(Integer.valueOf(which + 1));
                                }
                            }
                        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseHandler databaseHandler = new DatabaseHandler(context, "local.db");
                                databaseHandler.open();

                                for (int pId : selected) {
                                    databaseHandler.insertVideo(pId, searchResult.getId(),
                                            searchResult.getTitle(), searchResult.getDuration());
                                }

                                databaseHandler.close();

                                if (selected.size() != 0) {
                                    Toast.makeText(context, "Item added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).setNeutralButton("Create new", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                final EditText input = new EditText(context);

                                final AlertDialog inputDialog = new AlertDialog.Builder(context)
                                        .setTitle("Type a name")
                                        .setView(input)
                                        .setPositiveButton("Create",
                                                new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (input.getText().toString().equals("")) {
                                                    Toast.makeText(context,
                                                            "Please enter the name for a new playlist",
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    DatabaseHandler databaseHandler =
                                                            new DatabaseHandler(context,
                                                                    "local.db");
                                                    databaseHandler.open();
                                                    databaseHandler.addPlaylist(
                                                            input.getText().toString());
                                                    databaseHandler.close();

                                                    Toast.makeText(context, "Item added",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel",
                                                new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        }).create();
                                inputDialog.show();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);

                ArrayList<MediaObject> mediaQueue = new ArrayList<>();
                mediaQueue.add(searchResult);

                intent.putParcelableArrayListExtra("MEDIA_QUEUE", mediaQueue);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                ((AppCompatActivity) context).startActivityIfNeeded(intent, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, duration, views;
        private ImageButton addButton;
        private CardView cardView;

        private MyViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.search_title_textView);
            duration = view.findViewById(R.id.search_duration_textView);
            views = view.findViewById(R.id.search_views_textView);
            addButton = view.findViewById(R.id.search_add_button);
            cardView = view.findViewById(R.id.search_card_view);
        }
    }
}
