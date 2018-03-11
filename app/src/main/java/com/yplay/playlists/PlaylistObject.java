package com.yplay.playlists;

import android.os.Parcel;
import android.os.Parcelable;

public class PlaylistObject implements Parcelable {

    private String title;
    private int id;
    private int count;
    private int duration;

    public static final Parcelable.Creator<PlaylistObject> CREATOR
            = new Parcelable.Creator<PlaylistObject>() {
        @Override
        public PlaylistObject createFromParcel(Parcel in) {
            return new PlaylistObject(in);
        }

        @Override
        public PlaylistObject[] newArray(int size) {
            return new PlaylistObject[size];
        }
    };

    public PlaylistObject(String title, int id, int count, int duration) {
        this.title = title;
        this.id = id;
        this.count = count;
        this.duration = duration;
    }

    private PlaylistObject(Parcel in) {
        Object[] data = in.readArray(getClass().getClassLoader());

        title = (String) data[0];
        id = (int) data[1];
        count = (int) data[2];
        duration = (int) data[3];
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public int getDuration() {
        return duration;
    }

    public String getDurationFormatted() {
        int duration = this.duration;

        StringBuilder durationString = new StringBuilder("");

        if (duration >= 3600 ) { // at least an hour
            int hours = duration / 3600;
            int minutes = (duration % 3600) / 60;
            int seconds = (duration % 3600) % 60;

            durationString.append(hours);

            if (minutes < 10) {
                durationString.append(":0").append(minutes);
            } else {
                durationString.append(":").append(minutes);
            }

            if (seconds < 10) {
                durationString.append(":0").append(seconds);
            } else {
                durationString.append(":").append(seconds);
            }
        } else if (duration >= 60) { // at least a minute
            int minutes = duration / 60;
            int seconds = duration % 60;

            durationString.append(minutes);

            if (seconds < 10) {
                durationString.append(":0").append(seconds);
            } else {
                durationString.append(":").append(seconds);
            }
        } else { // only seconds
            durationString.append(duration);
        }

        return durationString.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(new Object[] {
                this.title,
                this.id,
                this.count,
                this.duration
        });
    }
}
