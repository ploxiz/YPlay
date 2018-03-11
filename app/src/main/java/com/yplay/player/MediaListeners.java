package com.yplay.player;

import com.yplay.search.MediaObject;

public interface MediaListeners {

    void onMediaStarted(MediaObject object);

    void onMediaCompleted(MediaObject object);

}
