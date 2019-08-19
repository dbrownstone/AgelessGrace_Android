package com.brownstone.agelessgrace;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class MediaFileInfo {
    private String title;
    private String artist;
    private String filePath;
    private String album;
    private String duration;
    private Bitmap imageBitmap;
    private String albumArtPath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }

    public void setBitmap(Bitmap bitmap) {
        this.imageBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return this.imageBitmap;
    }

    public String getSongTitle() {

        return title;
    }

    public void setSongTitle(String songTitle) {
        this.title = songTitle;
    }

    public String getAlbumName() {
        return album;
    }

    public void setAlbumName(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setAlbumArtPath(String albumArtPath) {
        this.albumArtPath = albumArtPath;
    }

    public String getAlbumArtPath() {
        return albumArtPath;
    }

    public void setAlbumId(long albumId) {
        long albumId1 = albumId;
    }
}
