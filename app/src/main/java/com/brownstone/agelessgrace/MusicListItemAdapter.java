package com.brownstone.agelessgrace;

import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicListItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    ArrayList<MediaFileInfo> availableMusic = new ArrayList<MediaFileInfo>();
    ContentResolver cr;

    public MusicListItemAdapter(Context c, ArrayList<MediaFileInfo> availableMusic) {
        cr = c.getContentResolver();
        this.availableMusic = availableMusic;
        mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return availableMusic.size();
    }

    @Override
    public Object getItem(int position) {

        return availableMusic.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.music_row_item,null);
        CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkBox);
        checkbox.setTag(100 + position);
        TextView artist = (TextView) v.findViewById(R.id.artistView);
        MediaFileInfo song = new MediaFileInfo();
        song = this.availableMusic.get(position);
        artist.setText(song.getArtist());
        TextView songTitle = (TextView) v.findViewById(R.id.song_title);
        songTitle.setText(song.getSongTitle());
        TextView duration = (TextView) v.findViewById(R.id.duration);
        Integer durationInt = Integer.parseInt((song.getDuration()));
        duration.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationInt) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(durationInt)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(durationInt) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInt))));
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        imageView.setImageBitmap(song.getBitmap());
        return v;
    }}
