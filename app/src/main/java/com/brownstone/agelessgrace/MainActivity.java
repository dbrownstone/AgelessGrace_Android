package com.brownstone.agelessgrace;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private final AppLifecycleCallbacks mCallbacks = new AppLifecycleCallbacks();

    private static final int EMPTY = -1;

    String startingDate;
    String toolSelectionType = "";
    Integer[] toolsNo = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
    String[] tools;

    public ArrayList<Integer> completedToolsArray = new ArrayList<>();
    String[] descriptions;

    public Toolbar toolbar;

    ToolFragment toolsFragment;
    static ExerciseActivity exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getApplication().registerActivityLifecycleCallbacks(mCallbacks);

        SharedPref.init(getApplicationContext());
        DateManager.init(getApplicationContext());

        Resources res = getResources();
        tools = res.getStringArray(R.array.tools);
        descriptions = res.getStringArray(R.array.primary_benefits);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tools_tab)).setIcon(R.mipmap.tools));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.settings_tab)).setIcon(R.mipmap.settings));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.about_tab)).setIcon(R.mipmap.about));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        toolsFragment = (ToolFragment)adapter.getItem(0);

        if (!SharedPref.keyExists(Constants.STARTING_DATE)) {
            // running the app for the first time since installation
            startingDate = SharedPref.read(Constants.STARTING_DATE, "");
            if (startingDate.length() == 0) {
                // display the Settings tab
                TabLayout.Tab settings = tabLayout.getTabAt(1);
                settings.select();
            }
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if (bundle.getBoolean("from_exercise")) {
                if (!bundle.getBoolean("repeating")) {
//                    addToCompletedTools();
                    toolsFragment.repeating = false;
                } else {
                    toolsFragment.repeating = true;
                }
                ArrayList<String> toolIds = bundle.getStringArrayList("tool_ids");
                SharedPref.saveToSelectedToolIds(Constants.SELECTED_TOOL_IDS, toolIds);
                if (!completedToolsArray.contains(Integer.parseInt(toolIds.get(0)))) {
                    completedToolsArray.add(Integer.parseInt(toolIds.get(0)));
                    completedToolsArray.add(Integer.parseInt(toolIds.get(1)));
                    completedToolsArray.add(Integer.parseInt(toolIds.get(2)));
                }
                SharedPref.saveToCompletedToolIds(Constants.COMPLETED_TOOL_IDS,completedToolsArray);
                toolSelectionType = bundle.getString("selection_type");
                toolsFragment.selectedToolSets = SharedPref.getAllSelectedToolSets();
                if (toolsFragment.selectedToolSets.size() < 7) {
                    toolsFragment.show_music_icon = false;
                    showTheCongratulationsDialogs("All Tools");
                    returnToToolsLayout();
                } else {
                    String key = "";
                    toolSelectionType = bundle.getString("selection_type");
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
                    getSupportActionBar().setTitle(toolSelectionType);
                }
            }
        }
    }

    public void returnToToolsLayout() {
        toolsFragment.show_reselect_repeat_buttons = true;
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.select();
    }

    void addToCompletedTools() {
        ArrayList<Integer> lastCompletedToolIds = SharedPref.getLastCompletedToolIds();
        SharedPref.saveToCompletedToolIds(Constants.COMPLETED_TOOL_IDS, lastCompletedToolIds);
    }

    void showTheCongratulationsDialogs(String toolSelectionType) {
        String message = "";
        if (toolSelectionType.equals(getString(R.string.seventh_day_title)) || toolSelectionType.equals(R.string.exercise_title)) {
            message = getString(R.string.congrats_seven_day_completion);
        } else if (toolSelectionType.equals("21 Days")) {
            message = getString(R.string.congrats_twenty_one_days_completed);
        } else {
            String nextTime = "next time";
            if (SharedPref.read(Constants.EXERCISE_DAILY, true)) nextTime = "tomorrow";
            message = getString(R.string.congrats_ten_minute_completion, nextTime);
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.AGAlertDialog);
        alertDialog.setTitle(R.string.congrats);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public static class AppLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(activity.getClass().getSimpleName(), "onCreate(Bundle)");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.i(activity.getClass().getSimpleName(), "onStop()");
            String className = activity.getClass().getSimpleName();
            if (className == "ExercisActivity") {
                exercise.resumeMusic = true;
                exercise.playMusic();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.i(activity.getClass().getSimpleName(), "onResume()");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.i(activity.getClass().getSimpleName(), "onPause()");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            Log.i(activity.getClass().getSimpleName(), "onSaveInstanceState(Bundle)");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.i(activity.getClass().getSimpleName(), "onStop()");
            String className = activity.getClass().getSimpleName();
            if (className == "ExercisActivity") {
                exercise.pauseSong();
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(activity.getClass().getSimpleName(), "onDestroy()");
        }
    }
}
