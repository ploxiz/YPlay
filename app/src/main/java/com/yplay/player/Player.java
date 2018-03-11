package com.yplay.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import com.yplay.search.MediaObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Besides some trivial features that the Android MediaPlayer doesn't have (repeat, shuffle etc.),
 * Player also implements features such as fetching and managing any necessary media files by
 * interacting with a MediaManager.
 */
class Player extends MediaPlayer {

    public enum RepeatMode {
        NONE, ONE, ALL;

        public RepeatMode next() {
            switch (this) {
                case ONE:
                    return ALL;

                case ALL:
                    return NONE;

                default: // NONE
                    return ONE;
            }
        }
    }

    private Context context;
    private List<MediaListeners> observers;

    private List<MediaObject> media;
    private int index;

    private boolean shuffleFlag;
    private RepeatMode repeatMode;
    private boolean paused;

    Player(Context context) {
        this.context = context;
        shuffleFlag = false;
        repeatMode = RepeatMode.NONE;

        setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                for (MediaListeners observer: observers) {
                    observer.onMediaCompleted(media.get(index));
                }

                switch (repeatMode) {
                    case NONE:
                        break;

                    case ONE:
                        playNext();
                        break;

                    case ALL:
                        if (shuffleFlag) {
                            // TODO:
                        } else {
                            index = (index + 1) % media.size();
                            playNext();
                        }

                        break;
                }
            }
        });
    }

    Player(Context context, boolean shuffleFlag, RepeatMode repeatMode) {
        this(context);
        this.shuffleFlag = shuffleFlag;
        this.repeatMode = repeatMode;
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        paused = true;
    }

    public boolean isPaused() {
        return paused;
    }

    void addObserver(MediaListeners observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }

        observers.add(observer);
    }

    void setMedia(List<MediaObject> media) {
        this.media = media;
    }

    RepeatMode getRepeatMode() {
        return repeatMode;
    }

    void setNextRepeatMode() {
        repeatMode = repeatMode.next();
    }

    boolean getShuffleFlag() {
        return shuffleFlag;
    }

    void switchShuffleFlag() {
        shuffleFlag = !shuffleFlag;
    }

    void play() throws Exception {
        if (media == null || media.size() == 0) {
            throw new Exception("No media to be played.");
        }

        if (shuffleFlag) {
            playNextShuffle();
        } else {
            index = 0;
            playNext();
        }
    }

    /**
     * If this is called, shuffleFlag is off.
     */
    private void playNext() {
        new PlayNextAsyncTask().execute();
    }

    private void playNextShuffle() {
        // TODO:
    }

    private class PlayNextAsyncTask extends AsyncTask<Void, Void, Void> {

        private MediaObject mediaObject;
        private String path;

        @Override
        protected void onPreExecute() {
            mediaObject = media.get(index);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                path = MediaManager.fetchMedia(context, mediaObject);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                reset();
                setDataSource(path);
                prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            start();

            for (MediaListeners observer : observers) {
                observer.onMediaStarted(mediaObject);
            }
        }
    }
}
