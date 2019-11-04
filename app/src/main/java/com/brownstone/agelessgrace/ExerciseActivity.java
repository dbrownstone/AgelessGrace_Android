package com.brownstone.agelessgrace;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ConfigurationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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

import static com.brownstone.agelessgrace.BuildConfig.DEBUG;
import static com.brownstone.agelessgrace.Constants.DAILY_EXERCISE_TIME;
import static com.brownstone.agelessgrace.BuildConfig.BUILD_TYPE;



public class ExerciseActivity extends AppCompatActivity {

    public static final String TAG = "ExerciseActivity()";
    DateManager dataMgr;

    Integer totalExercisePeriod =  DAILY_EXERCISE_TIME;
    Integer individualToolPeriod = (totalExercisePeriod) / 3;
    public static boolean active = false;
    boolean restartExercise = false;

    int tool1Index, tool2Index, tool3Index, currentIndex;
    String tool1Name;
    String tool2Name;
    String tool3Name;
    TextView scrollingText;
    String bodyPartsText;
    String waysToMoveText;
    ArrayList<MediaFileInfo> selectedMusic = new ArrayList<>();
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
    Boolean restartCurrentlySelectedMusic = true;
    Boolean allCompleted = false;
    TextView totalTimeRemainingTV;
    long remainingTime;
    Boolean didStartCountDown = false;
    Integer durationInt;
    TextView songTitle;
    TextView artist;
    TextView timeRemaining;
    Locale currentLocale;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        currentLocale = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);

        Resources res = getResources();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.exercise_screen_title);
        }


        startExerciseImmediately = SharedPref.read(Constants.START_EXERCISE_IMMEDIATELY, true);
        pauseBetweenTools = SharedPref.read(Constants.PAUSE_BETWEEN_TOOLS, false);
        if (SharedPref.keyExists(Constants.RESTART_CURRENTLY_SELECTED_MUSIC)) {
            restartCurrentlySelectedMusic = SharedPref.read(Constants.RESTART_CURRENTLY_SELECTED_MUSIC, false);
        } else {
            restartCurrentlySelectedMusic = true;
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            toolSelectionType = bundle.getString("selectionType","");
            if (bundle.containsKey("tool1Index")) {
                tool1Index = bundle.getInt("tool1Index");
            }
            if (bundle.containsKey("tool2Index")) {
                tool2Index = bundle.getInt("tool2Index");
            }
            if (bundle.containsKey("tool3Index")) {
                tool3Index = bundle.getInt("tool3Index");
            }
            tool1Name = (res.getStringArray(R.array.tools))[tool1Index];
            tool2Name = (res.getStringArray(R.array.tools))[tool2Index];
            tool3Name = (res.getStringArray(R.array.tools))[tool3Index];
        }
        selectedMusic = MusicSelectorActivity.getData();

        scrollingText = findViewById(R.id.scrollingTextView);
        String bpText = (res.getStringArray(R.array.body_parts_to_move)[tool1Index]);
        bodyPartsText = bpText.replace("\n", "; ");
        String wmText = (res.getStringArray(R.array.ways_to_move_them)[tool1Index]);
        waysToMoveText = wmText.replace("\n", "; ");
        TextView tool1 = findViewById(R.id.tool1);
        tool1.setText(res.getString(R.string.formatted_tool_title, tool1Index + 1, (res.getStringArray(R.array.tools))[tool1Index]));
        TextView tool2 = findViewById(R.id.tool2);
        tool2.setText(res.getString(R.string.formatted_tool_title, tool2Index + 1, (res.getStringArray(R.array.tools))[tool2Index]));
        TextView tool3 = findViewById(R.id.tool3);
        tool3.setText(res.getString(R.string.formatted_tool_title, tool3Index + 1, (res.getStringArray(R.array.tools))[tool3Index]));
        songTitle = findViewById(R.id.song_title);
        currentIndex = 0;
        // Notes about exercise times
        // generally each exercise lasts for 1/3 of the total exercise period
        // i.e. in release mode, each exercise lasts for 10/3 minutes - 3.333 mins
        if (selectedMusic.size() > 0) {
            firstSong = selectedMusic.get(0);
            currentSelection = firstSong;
            secondSong = selectedMusic.get(1);
            thirdSong = selectedMusic.get(2);
            songTitle.setText(firstSong.getSongTitle());
            artist = findViewById(R.id.artist);
            artist.setText(firstSong.getArtist());
            durationInt = Integer.parseInt((firstSong.getDuration()));
            if (durationInt < individualToolPeriod) {
                restartExercise = restartCurrentlySelectedMusic;
            }
            timeRemaining = findViewById(R.id.song_time_remaining);
            timeRemaining.setText(formatSongTime(durationInt));
            recordCover = findViewById(R.id.imageView);
            recordCover.setImageBitmap(firstSong.getBitmap());
        } else {
            artist = findViewById(R.id.artist);
            artist.setText(" ");
            durationInt = individualToolPeriod;
            timeRemaining = findViewById(R.id.song_time_remaining);
            timeRemaining.setText(formatSongTime(durationInt));
            recordCover = findViewById(R.id.imageView);
        }
        totalTimeRemainingTV = findViewById(R.id.total_time_remaining);
        totalTimeRemainingTV.setText(formatSongTime(totalExercisePeriod));

        final int minInterval = 1000;
        hourglass = new Hourglass(totalExercisePeriod) {
            @Override
            public void onTimerTick(long theRemainingTime) {
                remainingTime = theRemainingTime;
                totalTimeRemainingTV.setText(RemainingTimeString(remainingTime));
                durationInt -= minInterval;
                individualToolPeriod -= minInterval;
                timeRemaining.setText(formatSongTime(durationInt));
                //if a tool has been completed
                if (durationInt <= minInterval|| individualToolPeriod <= minInterval) {
                    if (restartCurrentlySelectedMusic && durationInt <= minInterval && individualToolPeriod > (minInterval * 15)) { //restartExercise) {
                        // restart the same music to continue this exercise unless there
                        // are less than 15 seconds remaining in this interval
                        mp.stop();
                        changeTools(currentIndex);
                    } else {
                        currentIndex+=1;
                        if (mp != null) {
                            mp.stop();
                            try {
                                mp.prepare();
                                changeTools(currentIndex);
                            } catch (IOException e) {
                                Log.e(TAG, "IOException during prepare after stop! mp value: " + mp);
                            }
                        }
                        individualToolPeriod = (totalExercisePeriod) / 3;
                        if (currentIndex == 3) {
                            stopTimer();
                        }
                    }
                } else {
                    if (selectedMusic.size() > 0 && (mp != null && !mp.isPlaying())) {
                        if (currentIndex < 3) {
                            mp.start();
                        }
                        switch (currentIndex) {
                            case 0:
                                currentSelection = firstSong;
                                break;

                            case 1:
                                currentSelection = secondSong;
                                break;

                            case 2:
                                currentSelection = thirdSong;
                                break;

                        }
                    }
                }
            }

            @Override
            public void onTimerFinish() {


            }
        };
    }

    public String RemainingTimeString(long remainingTime) {
        long seconds = remainingTime/1000;//convert to seconds
        long minutes = seconds / 60;//convert to minutes

        if(minutes > 0) { //if we have minutes, then there might be some remainder seconds
            seconds = seconds % 60;//seconds can be between 0-60, so we use the % operator to get the remainder
        }
        return String.format(currentLocale,"%d:%02d", minutes, seconds);
    }


    @Override
    public boolean onSupportNavigateUp(){
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.release();
            hourglass.stopTimer();
        }
        finish();
        return true;
    }

    void returnToMainActivity() {
        //need to set start and end dates if not set
        allCompleted = true;
        Resources res = getResources();
        if ((DateManager.getStartToEndDates()).size() == 0) {
            DateManager.setStartToEndDates();
        }

        ArrayList<String> toolIds = new ArrayList<>(3);
        toolIds.add(String.valueOf(tool1Index));
        toolIds.add(String.valueOf(tool2Index));
        toolIds.add(String.valueOf(tool3Index));

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("selection_type", toolSelectionType);
        intent.putExtra("from_exercise", true);
        intent.putStringArrayListExtra("tool_id_nos", toolIds);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", currentLocale);
        String todaysDate = formatter.format(new Date());
        SharedPref.write(Constants.LAST_EXERCISE_DATE, todaysDate);
        intent.putStringArrayListExtra("tool_ids",toolIds);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }

    private String formatSongTime(Integer durationInt) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInt) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(durationInt));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInt) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInt));
        if (seconds < 0) {
            seconds = 0;
        }
        return String.format(Locale.getDefault(),"%2d:%02d",minutes, seconds); // The change is in this line);
    }

    private void changeTools(int nextTool) {
        TextView tool1 = findViewById(R.id.tool1);
        tool1.setTextColor(ContextCompat.getColor(this,R.color.AG_blue));
        TextView tool2 = findViewById(R.id.tool2);
        tool2.setTextColor(ContextCompat.getColor(this,R.color.AG_blue));
        TextView tool3 = findViewById(R.id.tool3);
        tool3.setTextColor(ContextCompat.getColor(this,R.color.AG_blue));
        String scrollingContent;
        if (pauseBetweenTools) {
            pauseSong();
        }
        Resources res = getResources();
        String toolName = tool1Name + " ";
        switch (nextTool) {
            case 0:
                mp = MediaPlayer.create(this, Uri.parse(firstSong.getFilePath()));
                durationInt = Integer.parseInt((firstSong.getDuration()));
                timeRemaining.setText(formatSongTime(durationInt));
                restartExercise = true;
                tool1.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                break;
            case 1:
                if (selectedMusic.size() > 0) {
                    mp = MediaPlayer.create(this, Uri.parse(secondSong.getFilePath()));
                    recordCover.setImageBitmap(secondSong.getBitmap());
                    songTitle.setText(secondSong.getSongTitle());
                    artist.setText(secondSong.getArtist());
                    durationInt = Integer.parseInt((secondSong.getDuration()));
                    restartExercise = (durationInt < individualToolPeriod);
                } else {
                    durationInt = individualToolPeriod;
                }
                timeRemaining.setText(formatSongTime(durationInt));
                tool2.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                toolName = tool2Name + " ";
                String bpText = (res.getStringArray(R.array.body_parts_to_move)[tool2Index]);
                bodyPartsText = bpText.replace("\n", "; ");
                String wmText = (res.getStringArray(R.array.ways_to_move_them)[tool2Index]);
                waysToMoveText = wmText.replace("\n", "; ");
                break;
            case 2:
                if (selectedMusic.size() > 0) {
                    mp = MediaPlayer.create(this, Uri.parse(thirdSong.getFilePath()));
                    recordCover.setImageBitmap(thirdSong.getBitmap());
                    songTitle.setText(thirdSong.getSongTitle());
                    artist.setText(thirdSong.getArtist());
                    durationInt = Integer.parseInt((thirdSong.getDuration()));
                    restartExercise = (durationInt < individualToolPeriod);
                } else {
                    durationInt = individualToolPeriod;
                }
                timeRemaining.setText(formatSongTime(durationInt));
                tool3.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                toolName = tool3Name + " ";
                bpText = (res.getStringArray(R.array.body_parts_to_move)[tool3Index]);
                bodyPartsText = bpText.replace("\n", "; ");
                wmText = (res.getStringArray(R.array.ways_to_move_them)[tool3Index]);
                waysToMoveText = wmText.replace("\n", "; ");
                break;
            case 3:
                ArrayList<String> selectedToolSets = SharedPref.getAllSelectedToolSets();
                if (selectedToolSets.size() < 7) {
                    showTheCongratulationsDialogs("All Tools");
                } else {
                    String key = "";
                    showTheCongratulationsDialogs(toolSelectionType);
                    if (toolSelectionType.equals(getString(R.string.seventh_day_title))) {
                        if (SharedPref.read(Constants.END_OF_FIRST_WEEK, false)) {
                            if (SharedPref.read(Constants.END_OF_SECOND_WEEK, false)) {
                                showTheCongratulationsDialogs("21 Days");
                            } else {
                                SharedPref.read(Constants.END_OF_SECOND_WEEK, true);
                            }
                        } else {
                            SharedPref.write(Constants.END_OF_FIRST_WEEK, true);
                        }
                    }
                }
                break;
        }
        if (!pauseBetweenTools) {
            scrollingContent = res.getString(R.string.scrolling_content,toolName,bodyPartsText,waysToMoveText);
            scrollingText.setText(scrollingContent);
            scrollingText.setSelected(true);// starts the scroll
            nextToolSelected = !restartExercise;
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
            if (selectedMusic.size() > 0) {
                mp = MediaPlayer.create(this, Uri.parse(currentSelection.getFilePath()));
            }
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
                remainingTime = totalExercisePeriod;
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
        String theTitle = (res.getStringArray(R.array.tools))[tool1Index];//String.format(res.getString(R.string.tool_title),tool1Index);
        switch (currentIndex) {
            case 1:
                theTitle = (res.getStringArray(R.array.tools))[tool2Index];//String.format(res.getString(R.string.tool_title),tool2Index);
                break;
            case 2:
                theTitle = (res.getStringArray(R.array.tools))[tool3Index];//String.format(res.getString(R.string.tool_title),tool3Index);
                break;
        }
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(theTitle);
        }
        if (nextToolSelected) {
            if (currentIndex < 3) {
                if (mp != null) {
                    mp.start();
                    hourglass.startTimer();
                    scrollingText.setSelected(true);
                }
            }
        } else {
            Boolean isPaused;

            if (selectedMusic.size() > 0) {
                isPaused = !mp.isPlaying() && length > 1;

                if (isPaused) return;
                hourglass.startTimer();
                mp.start();
            }
        }
    }

    public void pause() {
        pauseSong();
    }

    public void resume() {
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
        totalTimeRemainingTV.setText(RemainingTimeString(remainingTime));
        shouldPlayMusicItem = true;
        hourglass.pauseTimer();
        scrollingText.stopNestedScroll();
        invalidateOptionsMenu();
    }

    void showTheCongratulationsDialogs(String toolSelectionType) {
        String message;
        if (toolSelectionType.equals(getString(R.string.seventh_day_title)) || toolSelectionType.equals(getString(R.string.exercise_title))) {
            message = getString(R.string.congrats_seven_day_completion);
        } else if (toolSelectionType.equals("21 Days")) {
            message = getString(R.string.congrats_twenty_one_days_completed);
        } else {
            String nextTime = "next time";
            if (SharedPref.read(Constants.EXERCISE_DAILY, true)) nextTime = "tomorrow";
            message = getString(R.string.congrats_ten_minute_completion, nextTime);
        }
        LayoutInflater inflater = this.getLayoutInflater();

        android.support.v7.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
        View view = inflater.inflate(R.layout.centered_image_alert, null);
        alertDialog.setView(view);

        TextView theMessage = view.findViewById((R.id.alertMessage));
        TextView title = view.findViewById((R.id.alertTitle));
        title.setText(R.string.congrats);
        theMessage.setText(message);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                        returnToMainActivity();
                    }
                });
        alertDialog.show();
    }
}
