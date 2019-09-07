package com.brownstone.agelessgrace;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MusicSelectorActivity extends AppCompatActivity {

    ArrayList<MediaFileInfo> selectedMusic = new ArrayList<>();
    ArrayList<Integer> selectedMusicLocations = new ArrayList<>();
    public static ArrayList<MediaFileInfo> resultantMusicSelection;

    ArrayList<String> audioList = new ArrayList<>();
    MusicListItemAdapter itemAdapter;
    ArrayList<MediaFileInfo> availableMusic = new ArrayList<>();
    ListView musicList;
    Boolean permissionsGranted = false;
    String toolSelectionType = "";
    Integer tool1Index = -1;
    Integer tool2Index = -1;
    Integer tool3Index = -1;
    Boolean showAcceptItem = false;
    Boolean repeating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_selector);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.select_music_title);

        Intent in = getIntent();
        Bundle bundle = getIntent().getExtras();
        toolSelectionType = bundle.getString("selectionType");
        tool1Index = bundle.getInt("tool1Index");
        tool2Index = bundle.getInt("tool2Index");
        tool3Index = bundle.getInt("tool3Index");
        repeating = bundle.getBoolean("repeating");

        musicList = findViewById(R.id.music_list_view);

        if (checkPermission()) {
            parseAllAudio();
        } else {
            requestPermission();
        }
   }

//    @Override
//    public boolean onSupportNavigateUp(){
//        finish();
//        return true;
//    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_selection, menu);
        MenuItem accept_item = menu.findItem(R.id.accept_selections);
        if (showAcceptItem) {
            accept_item.setVisible(true);
        } else {
            accept_item.setVisible(false);
        }
        return true;
    }

    public static ArrayList<MediaFileInfo> getData() {
        return resultantMusicSelection;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.accept_selections) {
            resultantMusicSelection = selectedMusic;
            Intent exerciseActivity = new Intent(getApplicationContext(), ExerciseActivity.class);
            exerciseActivity.putExtra("selectionType", toolSelectionType);
            exerciseActivity.putExtra("tool1Index", tool1Index);
            exerciseActivity.putExtra("tool2Index", tool2Index);
            exerciseActivity.putExtra("tool3Index", tool3Index);
            exerciseActivity.putExtra("repeating", repeating);
            startActivity(exerciseActivity);
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    private void parseAllAudio() {
        try {
            String TAG = "Audio";
            availableMusic = getAvailableMusic(this);
            if (availableMusic.size() > 0) {
                itemAdapter = new MusicListItemAdapter(this, availableMusic);
                musicList.setAdapter(itemAdapter);

                musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CheckBox cb = view.findViewById(R.id.checkBox);
                        cb.setChecked(!cb.isChecked());
                        if (cb.isChecked()) {
                            selectedMusic.add(availableMusic.get(position));
                            selectedMusicLocations.add(position);
                        } else if (selectedMusic.contains(availableMusic.get(position))) {
                            selectedMusic.remove(availableMusic.get(position));
                            selectedMusicLocations.remove(position);
                            showAcceptItem = false;
                        }
                        if (selectedMusic.size() == 3) {
                            showAcceptItem = true;
                        }
                        invalidateOptionsMenu();
                    }
                });
            } else {
                final boolean isEmulator = isEmulator();
//                if (isEmulator) {
                    LayoutInflater inflater = this.getLayoutInflater();

                    android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this, R.style.AlertDialogTheme);
                    View view = inflater.inflate(R.layout.centered_image_alert, null);
                    alertDialog.setView(view);
                    TextView theMessage = view.findViewById((R.id.alertMessage));
                    TextView title = view.findViewById((R.id.alertTitle));
                    title.setText(R.string.alert_title);
                    String message = getString(R.string.no_music_available);
                    if (isEmulator) {
                        message = getString(R.string.no_music_available_on_emulator);
                    } else {
                        message = getString(R.string.no_music_available_on_this_device_continuing_anyway);
                    }

                    theMessage.setText(message);
                    // add the buttons
                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (isEmulator) {
                                        returnToMainActivity();
                                    } else {

//                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                        setResult(RESULT_CANCELED, intent);
//                                        startActivity(intent);
                                        resultantMusicSelection = new ArrayList<>();
                                        Intent exerciseActivity = new Intent(getApplicationContext(), ExerciseActivity.class);
                                        exerciseActivity.putExtra("selectionType", toolSelectionType);
                                        exerciseActivity.putExtra("tool1Index", tool1Index);
                                        exerciseActivity.putExtra("tool2Index", tool2Index);
                                        exerciseActivity.putExtra("tool3Index", tool3Index);
                                        exerciseActivity.putExtra("repeating", repeating);
                                        startActivity(exerciseActivity);
                                    }
                                }
                            });
                    alertDialog.setNegativeButton(R.string.action_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    // create and show the alert dialog
                    alertDialog.show();

//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void returnToMainActivity() {
        //need to set start and end dates if not set
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
        intent.putExtra("repeating", repeating);
        intent.putStringArrayListExtra("tool_id_nos", toolIds);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String todaysDate = formatter.format(new Date());
        SharedPref.write(Constants.LAST_EXERCISE_DATE, todaysDate);
        intent.putStringArrayListExtra("tool_ids",toolIds);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }

    private Boolean checkTotalMusicTimeIsEnough() {
        Integer totalDuration = 0;
        for (int i = 0; i < selectedMusic.size(); i++) {
            MediaFileInfo song = selectedMusic.get(i);
            totalDuration += Integer.parseInt((song.getDuration()));
        }
        return totalDuration >= Constants.DAILY_EXERCISE_TIME && selectedMusic.size() >= 3;
    }

    public static Bitmap getAlbumart(Context context, Long album_id) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
                pfd = null;
                fd = null;
            }
        } catch (Error ee) {

        } catch (Exception e) {

        }

        if (null != albumArtBitMap) {
            return albumArtBitMap;
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.album_cover);
    }

    public static ArrayList<MediaFileInfo> getAvailableMusic(Context c) {
        ArrayList<MediaFileInfo> availableMusic = new ArrayList<MediaFileInfo>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        //Some audio may be explicitly marked as not being music
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";


        Cursor cursor = c.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder);
        try {
            cursor.moveToFirst();
            do{
                int duration = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                if (duration >= 1000) {
                    String artist = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String album = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String track = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    Long albumId = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    MediaFileInfo song = new MediaFileInfo();

                    Bitmap bitmap = getAlbumart(c, albumId);

                    song.setArtist(artist);
                    song.setAlbumName(album);
                    song.setSongTitle(track);
                    song.setAlbumId(albumId);
                    song.setBitmap(bitmap);
                    song.setDuration(String.valueOf(duration));
                    song.setFilePath(filePath);

                    availableMusic.add(song);
                }
            } while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableMusic;
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            permissionsGranted = true;
            return true;

        } else {
            permissionsGranted = false;
            return false;
        }
    }

    private void requestPermission(){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //PERMISSION_REQUEST_CODE:
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = true;
                parseAllAudio();

            } else {
                permissionsGranted = false;
//                    Permission Denied

            }
        }
    }
}
