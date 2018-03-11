package com.yplay.search;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Duration;

public class MediaObject implements Parcelable {

    private String id;
    private String title;
    private int duration;
    private String views;

    public static final Parcelable.Creator<MediaObject> CREATOR
            = new Parcelable.Creator<MediaObject>() {
        @Override
        public MediaObject createFromParcel(Parcel in) {
            return new MediaObject(in);
        }

        @Override
        public MediaObject[] newArray(int size) {
            return new MediaObject[size];
        }
    };

    public MediaObject(String id, String title, int duration, String views) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.views = views;
    }

    private MediaObject(Parcel in) {
        Object[] data = in.readArray(getClass().getClassLoader());

        id = (String) data[0];
        title = (String) data[1];
        duration = (int) data[2];
        views = (String) data[3];
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public String getViews() {
        return views;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(new Object[] {
                this.id,
                this.title,
                this.duration,
                this.views
        });
    }

}
