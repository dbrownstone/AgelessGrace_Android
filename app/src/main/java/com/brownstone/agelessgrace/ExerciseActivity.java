package com.brownstone.agelessgrace;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.brownstone.agelessgrace.Hourglass;

import static java.lang.Math.PI;
import static java.lang.Math.toIntExact;

public class ExerciseActivity extends AppCompatActivity {

    public static final String TAG = "ExerciseActivity";
    DateManager dataMgr;
    Integer individualToolPeriod = (Constants.DAILY_EXERCISE_TIME) / 3;

    public static boolean active = false;

    int tool1Index, tool2Index, tool3Index, currentIndex;
    String tool1Name;
    String tool2Name;
    String tool3Name;
    TextView scrollingText;
    String bodyPartsText;
    String waysToMoveText;
    ArrayList<MediaFileInfo> selectedMusic = new ArrayList<MediaFileInfo>();
    MediaPlayer mp;
    String toolSelectionType = "";
    MediaFileInfo firstSong;
    MediaFileInfo secondSong;
    MediaFileInfo thirdSong;
    MediaFileInfo currentSelection;
    ImageView recordCover;
    Boolean isActive = false;
    Boolean startExerciseImmediately = false;
    Boolean shouldPlayMusicItem = true;
    Boolean resumeMusic = false;
    Boolean nextToolSelected = false;
    Boolean pauseBetweenTools = true;
    Boolean allCompleted = false;
    TextView totalTimeRemainingTV;
    long remainingTime;
    Boolean didStartCountDown = false;
    Integer durationInt;
    TextView songTitle;
    TextView artist;
    TextView timeRemaining;

    Hourglass hourglass;

    ToolFragment toolFragment;
    MainActivity mainActivity;

    int length;// where in the music file, the music was paused

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Resources res = getResources();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.exercise_screen_title);

        startExerciseImmediately = SharedPref.read(Constants.START_EXERCISE_IMMEDIATELY, true);
        pauseBetweenTools = SharedPref.read(Constants.PAUSE_BETWEEN_TOOLS, false);
        Bundle bundle = getIntent().getExtras();
        toolSelectionType = bundle.getString("selectionType");
        tool1Index = bundle.getInt("tool1Index") + 1;
        tool2Index = bundle.getInt("tool2Index") + 1;
        tool3Index = bundle.getInt("tool3Index") + 1;
        tool1Name = (res.getStringArray(R.array.tools))[tool1Index - 1];
        tool2Name = (res.getStringArray(R.array.tools))[tool2Index - 1];
        tool3Name = (res.getStringArray(R.array.tools))[tool3Index - 1];
        selectedMusic = MusicSelectorActivity.getData();
        scrollingText = findViewById(R.id.scrollingTextView);
        bodyPartsText = (res.getStringArray(R.array.body_parts_to_move)[tool1Index]);
        waysToMoveText = (res.getStringArray(R.array.ways_to_move_them)[tool1Index]);
        TextView tool1 = findViewById(R.id.tool1);
        tool1.setText(res.getString(R.string.formatted_tool_title, tool1Index, (res.getStringArray(R.array.tools))[tool1Index - 1]));
        TextView tool2 = findViewById(R.id.tool2);
        tool2.setText(res.getString(R.string.formatted_tool_title, tool2Index, (res.getStringArray(R.array.tools))[tool2Index - 1]));
        TextView tool3 = findViewById(R.id.tool3);
        tool3.setText(res.getString(R.string.formatted_tool_title, tool3Index, (res.getStringArray(R.array.tools))[tool3Index - 1]));
        songTitle = findViewById(R.id.song_title);
        currentIndex = 0;
        firstSong = selectedMusic.get(0);
        currentSelection = firstSong;
        secondSong = selectedMusic.get(1);
        thirdSong = selectedMusic.get(2);
        songTitle.setText(firstSong.getSongTitle());
        artist = findViewById(R.id.artist);
        artist.setText(firstSong.getArtist());
        durationInt = Integer.parseInt((firstSong.getDuration()));
        timeRemaining = findViewById(R.id.song_time_remaining);
        timeRemaining.setText(formatSongTime(durationInt));
        recordCover = findViewById(R.id.imageView);
        recordCover.setImageBitmap(firstSong.getBitmap());
        totalTimeRemainingTV = findViewById(R.id.total_time_remaining);
        totalTimeRemainingTV.setText(formatSongTime(Constants.DAILY_EXERCISE_TIME));
        hourglass = new Hourglass(Constants.DAILY_EXERCISE_TIME, 1000) {
            @Override
            public void onTimerTick(long remainingTime) {
                totalTimeRemainingTV.setText(hourglass.RemainingTimeString());
                individualToolPeriod -= 1000;
                durationInt -= 1000;
                //if tool has been completed
                if (individualToolPeriod <= 0) {
                    currentIndex += 1;
                    individualToolPeriod = (Constants.DAILY_EXERCISE_TIME)/3;
                    if (remainingTime >= 1000) {
                        changeTools(currentIndex);
                    }
                } else {
                    if (!mp.isPlaying()) {
                        mp.start();
//                        MediaFileInfo theSong = firstSong;
                        switch (currentIndex) {
                            case 1:
                                currentSelection = secondSong;
                                break;
                            case 2:
                                currentSelection = thirdSong;
                                break;
                        }
                        durationInt = Integer.parseInt((currentSelection.getDuration()));
                    }
                }
                timeRemaining.setText(formatSongTime(durationInt));
            }

            @Override
            public void onTimerFinish() {
                if (mp.isPlaying()) {
                    mp.stop();
                }
                mp.release();
                returnToMainActivity();
            }
        };
    }

    void returnToMainActivity() {
        //need to set start and end dates if not set
        allCompleted = true;
        Resources res = getResources();
        if ((DateManager.getStartToEndDates()).size() == 0) {
            DateManager.setStartToEndDates();
        }

        ArrayList<String> toolIds = new ArrayList<String>(3);
        toolIds.add(String.valueOf(tool1Index));
        toolIds.add(String.valueOf(tool2Index));
        toolIds.add(String.valueOf(tool3Index));

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("selection_type", toolSelectionType);
        intent.putExtra("from_exercise", true);
//        intent.putExtra("repeating", repeating);
        intent.putStringArrayListExtra("tool_id_nos", toolIds);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String todaysDate = formatter.format(new Date());
        SharedPref.write(Constants.LAST_EXERCISE_DATE, todaysDate);
        intent.putStringArrayListExtra("tool_ids",toolIds);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }

    private String formatSongTime(Integer durationInt) {
        return String.format(Locale.getDefault(),"%2d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationInt) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(durationInt)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(durationInt) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInt)));
    }

    private void changeTools(int nextTool) {
        TextView tool1 = findViewById(R.id.tool1);
        tool1.setTextColor(ContextCompat.getColor(this,R.color.AG_blue));
        TextView tool2 = findViewById(R.id.tool2);
        tool2.setTextColor(ContextCompat.getColor(this,R.color.AG_blue));
        TextView tool3 = findViewById(R.id.tool3);
        tool3.setTextColor(ContextCompat.getColor(this,R.color.AG_blue));
        String scrollingContent = "";
        if (pauseBetweenTools) {
            pauseSong();
        } else {
            mp.stop();
        }
        Resources res = getResources();
        String toolName = tool1Name + " ";
        switch (nextTool) {
            case 1:
                mp = MediaPlayer.create(this,Uri.parse(secondSong.getFilePath()));
                recordCover.setImageBitmap(secondSong.getBitmap());
                songTitle.setText(secondSong.getSongTitle());
                artist.setText(secondSong.getArtist());
                durationInt = Integer.parseInt((secondSong.getDuration()));
                timeRemaining.setText(formatSongTime(durationInt));
                tool2.setTextColor(ContextCompat.getColor(this,R.color.colorAccent));
                toolName = tool2Name + " ";
                bodyPartsText = (res.getStringArray(R.array.body_parts_to_move)[tool2Index - 1]);
                waysToMoveText = (res.getStringArray(R.array.ways_to_move_them)[tool2Index - 1]);
                break;
            case 2:
                mp.stop();
                mp = MediaPlayer.create(this,Uri.parse(thirdSong.getFilePath()));
                recordCover.setImageBitmap(thirdSong.getBitmap());
                songTitle.setText(thirdSong.getSongTitle());
                artist.setText(thirdSong.getArtist());
                durationInt = Integer.parseInt((thirdSong.getDuration()));
                timeRemaining.setText(formatSongTime(durationInt));
                tool3.setTextColor(ContextCompat.getColor(this,R.color.colorAccent));
                toolName = tool3Name + " ";
                bodyPartsText = (res.getStringArray(R.array.body_parts_to_move)[tool3Index - 1]);
                waysToMoveText = (res.getStringArray(R.array.ways_to_move_them)[tool3Index - 1]);
                break;
        }
        if (!pauseBetweenTools) {
            scrollingContent = res.getString(R.string.scrolling_content,toolName,bodyPartsText,waysToMoveText);
            scrollingText.setText(scrollingContent);
            scrollingText.setSelected(true);// starts the scroll
            playMusic();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exercise_activity, menu);
        menu.findItem(R.id.play_music).setVisible(false);
        menu.findItem(R.id.pause_music).setVisible(false);
        if (resumeMusic && startExerciseImmediately) {//if in backround and paused
            resume();
            return true;
        }
        if (startExerciseImmediately) {
            menu.findItem(R.id.play_music).setVisible(false);
            menu.findItem(R.id.pause_music).setVisible(false);
            mp = MediaPlayer.create(this, Uri.parse(currentSelection.getFilePath()));
            Resources res = getResources();
            String toolName = tool1Name + " ";
            String scrollingContent = res.getString(R.string.scrolling_content,toolName,bodyPartsText,waysToMoveText);
            scrollingText.setText(scrollingContent);
            scrollingText.setSelected(true);// starts the scroll
            playMusic();
        } else {
            if (shouldPlayMusicItem) {
                menu.findItem(R.id.play_music).setVisible(true);
                menu.findItem(R.id.pause_music).setVisible(false);
            } else {
                menu.findItem(R.id.play_music).setVisible(false);
                menu.findItem(R.id.pause_music).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources res = getResources();
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(Activity.RESULT_OK, intent);
                startActivity(intent);
                break;
            case R.id.play_music:
                remainingTime = Constants.DAILY_EXERCISE_TIME;
                if (!resumeMusic) {
                    mp = MediaPlayer.create(this, Uri.parse(currentSelection.getFilePath()));
                }
                String scrollingContent = res.getString(R.string.scrolling_content,tool1Name,bodyPartsText,waysToMoveText);
                scrollingText.setText(scrollingContent);
                scrollingText.setSelected(true);// starts the scroll
                playMusic();
                shouldPlayMusicItem = false;
                resumeMusic = false;
                invalidateOptionsMenu();
                break;
            case R.id.pause_music:
                pauseSong();
                break;
          default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    public void playMusic() {
        Resources res = getResources();
        String theTitle = (res.getStringArray(R.array.tools))[tool1Index - 1];//String.format(res.getString(R.string.tool_title),tool1Index);
        switch (currentIndex) {
            case 1:
                theTitle = (res.getStringArray(R.array.tools))[tool2Index - 1];//String.format(res.getString(R.string.tool_title),tool2Index);
                break;
            case 2:
                theTitle = (res.getStringArray(R.array.tools))[tool3Index - 1];//String.format(res.getString(R.string.tool_title),tool3Index);
                break;
        }
        getSupportActionBar().setTitle(theTitle);
        if (nextToolSelected) {
            hourglass.stopTimer();
            hourglass.setTime(remainingTime);//new Hourglass(remainingTime, 1000);
            didStartCountDown = true;
            hourglass.startTimer();
            mp.start();
        } else {
            Boolean isPaused = !mp.isPlaying() && length > 1;

            if (isPaused) return;
            hourglass.startTimer();
            mp.start();
        }

    }

    public void pause() {
        pauseSong();
    }

    public void resume() {
        hourglass.startTimer();
        if (mp != null) {
            mp.start();
            mp.seekTo(length);
            hourglass.resumeTimer();
            scrollingText.setSelected(true);
        }
    }

    public void pauseSong() {
        if (mp.isPlaying()) {
            mp.pause();
            length = mp.getCurrentPosition();
        }
        totalTimeRemainingTV.setText(hourglass.RemainingTimeString());
        shouldPlayMusicItem = true;
        hourglass.pauseTimer();
        scrollingText.stopNestedScroll();
        invalidateOptionsMenu();
    }
}
