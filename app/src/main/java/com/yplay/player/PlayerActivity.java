package com.yplay.player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yplay.BaseActivity;
import com.yplay.R;
import com.yplay.search.MediaObject;

import java.util.List;
import java.util.Locale;

public class PlayerActivity extends BaseActivity implements MediaListeners {

    private Player player;
    private TextView titleTextView;
    private TextView playedTimeTextView;
    private TextView durationTextView;
    private SeekBar seekBar;
    private ImageButton playButton;
    private ImageButton repeatImageButton;
    private ImageButton shuffleImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.player_toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences settings = getSharedPreferences("com.yplay", MODE_PRIVATE);
        if (settings.getBoolean("first_run", true)) {
            settings.edit().putBoolean("first_run", false).apply();
            // TODO:
        }

        player = new Player(this);
        player.addObserver(this);

        titleTextView = findViewById(R.id.player_title_textView);
        playedTimeTextView = findViewById(R.id.player_played_time_textView);
        durationTextView = findViewById(R.id.player_duration_textView);
        seekBar = findViewById(R.id.player_seekBar);
        playButton = findViewById(R.id.player_play_imageButton);
        repeatImageButton = findViewById(R.id.player_repeat_imageButton);
        shuffleImageButton = findViewById(R.id.player_shuffle_imageButton);

        initPlayButton();
        initRepeatImageButton();
        initShuffleImageButton();
        initSeekBar();
        openUpdateThread(); // update the played time and the seek bar every second
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // TODO:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_player;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.navigation_player;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        List<MediaObject> media = intent.getParcelableArrayListExtra("MEDIA_QUEUE");
        player.setMedia(media);
        try {
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMediaStarted(MediaObject mediaObject) {
        titleTextView.setText(mediaObject.getTitle());
        durationTextView.setText(mediaObject.getDurationFormatted());
        seekBar.setProgress(0);
        seekBar.setMax(player.getDuration());

        playButton.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_pause_black_120dp));
    }

    @Override
    public void onMediaCompleted(MediaObject mediaObject) {
        playedTimeTextView.setText(mediaObject.getDurationFormatted());
        playButton.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_play_arrow_black_120dp));
    }

    private void initPlayButton() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    playButton.setImageDrawable(
                            getResources().getDrawable(R.drawable.ic_play_arrow_black_120dp));
                } else if (player.isPaused()) {
                    player.start();
                    playButton.setImageDrawable(
                            getResources().getDrawable(R.drawable.ic_pause_black_120dp));
                } else {
                    // TODO: for example when a media has finished and repeat is set to none
                }
            }
        });
    }

    private void initRepeatImageButton() {
        repeatImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.setNextRepeatMode();

                switch (player.getRepeatMode()) {
                    case NONE:
                        repeatImageButton.setImageDrawable(getResources()
                                .getDrawable(R.drawable.ic_repeat_none_black_24dp));
                        repeatImageButton.setColorFilter(ContextCompat.getColor(PlayerActivity.this,
                                R.color.colorDisabled), android.graphics.PorterDuff.Mode.SRC_IN);
                        Toast.makeText(PlayerActivity.this, "Repeat none", Toast.LENGTH_SHORT).show();
                        break;

                    case ONE:
                        repeatImageButton.setImageDrawable(getResources()
                                .getDrawable(R.drawable.ic_repeat_one_black_24dp));
                        repeatImageButton.setColorFilter(ContextCompat.getColor(PlayerActivity.this,
                                R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                        Toast.makeText(PlayerActivity.this, "Repeat one", Toast.LENGTH_SHORT).show();
                        break;

                    case ALL:
                        repeatImageButton.setImageDrawable(getResources()
                                .getDrawable(R.drawable.ic_repeat_all_black_24dp));
                        repeatImageButton.setColorFilter(ContextCompat.getColor(PlayerActivity.this,
                                R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                        Toast.makeText(PlayerActivity.this, "Repeat all", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void initShuffleImageButton() {
        shuffleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.switchShuffleFlag();

                if (player.getShuffleFlag()) {
                    shuffleImageButton.setImageDrawable(getResources()
                            .getDrawable(R.drawable.ic_shuffle_black_24dp));
                    Toast.makeText(PlayerActivity.this, "Shuffle on", Toast.LENGTH_SHORT).show();
                } else {
                    shuffleImageButton.setImageDrawable(getResources()
                            .getDrawable(R.drawable.ic_shuffle_black_24dp)); // TODO: https://github.com/Templarian/MaterialDesign/issues/2658
                    Toast.makeText(PlayerActivity.this, "Shuffle on", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updatePlayedTime(progress);
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void openUpdateThread() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    updatePlayedTime(player.getCurrentPosition());
                    seekBar.setProgress(player.getCurrentPosition());
                }

                handler.postDelayed(this, 1000);
            }
        }).start();
    }

    private void updatePlayedTime (int progress) {
        int minutes = (progress / (1000 * 60)) % 60;
        int seconds = (progress / 1000) % 60 ;

        playedTimeTextView.setText(String.format(Locale.ENGLISH, "%01d:%02d", minutes, seconds));
    }

}
