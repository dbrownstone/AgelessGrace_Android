package com.brownstone.agelessgrace;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingsFragment extends Fragment {

    Boolean showSaveSettingsButton;
    Switch freq_sw, pause_sw, exercise_start_sw, restart_sw;
    TextView freq_result, pause_result, exercise_start_result, restart_result, dateSelectorTitle;
    DatePicker datePicker;

    Calendar currentDate;
    Date theDate;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_tab_fragment, container, false);
        setHasOptionsMenu(true);

        datePicker = view.findViewById(R.id.datePicker);
        dateSelectorTitle = view.findViewById(R.id.startingDateTitle);

        freq_sw = view.findViewById(R.id.exercise_frequency_sw);
        freq_result = view.findViewById(R.id.freq_result);

        pause_sw = view.findViewById(R.id.pause_sw);
        pause_result = view.findViewById(R.id.pause_result);

        restart_sw = view.findViewById(R.id.restart_sw);
        restart_result = view.findViewById(R.id.restart_result);

        exercise_start_sw = view.findViewById(R.id.exercise_start_sw);
        exercise_start_result = view.findViewById(R.id.exercise_start_result);

        showSaveSettingsButton = false;
        if (!SharedPref.keyExists(Constants.STARTING_DATE)) {
            showSaveSettingsButton =true;
            getActivity().invalidateOptionsMenu();
        }
        if (SharedPref.keyExists(Constants.EXERCISE_DAILY)) {
            Boolean daily = SharedPref.read(Constants.EXERCISE_DAILY, true);
            freq_sw.setChecked(daily);
            if (freq_sw.isChecked()) {
                freq_result.setText(getString(R.string.daily));
                datePicker.setVisibility(View.VISIBLE);
                dateSelectorTitle.setVisibility(View.VISIBLE);
            } else {
                freq_result.setText(getString(R.string.intermittently));
                datePicker.setVisibility(View.GONE);
                dateSelectorTitle.setVisibility(View.INVISIBLE);
            }
        }

        freq_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    freq_result.setText(getString(R.string.daily));
                    datePicker.setVisibility(View.VISIBLE);
                    dateSelectorTitle.setVisibility(View.VISIBLE);
                } else {
                    freq_result.setText(getString(R.string.intermittently));
                    datePicker.setVisibility(View.GONE);
                    dateSelectorTitle.setVisibility(View.INVISIBLE);
                }
                showSaveSettingsButton = true;
                getActivity().invalidateOptionsMenu();
                SharedPref.clearAllPreferences();
            }
        });

        if (SharedPref.keyExists(Constants.RESTART_CURRENTLY_SELECTED_MUSIC)) {
            Boolean restart = SharedPref.read(Constants.RESTART_CURRENTLY_SELECTED_MUSIC, false);
            restart_sw.setChecked(restart);
            if (restart) {
                restart_result.setText(getString(R.string.yes));
            } else {
                restart_result.setText(getString(R.string.no));
            }
        }
        restart_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    restart_result.setText(getString(R.string.yes));
                } else {
                    restart_result.setText(getString(R.string.no));
                }
                showSaveSettingsButton = true;
                getActivity().invalidateOptionsMenu();
            }
        });

        if (SharedPref.keyExists("Start_exercise_automatically")) {
            exercise_start_sw.setChecked(SharedPref.read("Start_exercise_automatically", false));
        }
        exercise_start_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    exercise_start_result.setText(getString(R.string.yes));
                } else {
                    exercise_start_result.setText(getString(R.string.no));
                }
                showSaveSettingsButton = true;
                getActivity().invalidateOptionsMenu();
            }
        });

        if (SharedPref.keyExists(Constants.PAUSE_BETWEEN_TOOLS)) {
            Boolean shouldPause = SharedPref.read(Constants.PAUSE_BETWEEN_TOOLS, false);
            pause_sw.setChecked(shouldPause);
            if (shouldPause) {
                pause_result.setText(getString(R.string.yes));
                exercise_start_sw.setChecked(false);
            } else {
                pause_result.setText(getString(R.string.no));
            }
        }
        pause_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pause_result.setText(getString(R.string.yes));
                    exercise_start_sw.setChecked(false);
                } else {
                    pause_result.setText(getString(R.string.no));
                }
                showSaveSettingsButton = true;
                getActivity().invalidateOptionsMenu();
            }
        });

        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMdd");
        String startDate = DateManager.getStartingDate();
        Calendar currentDate = Calendar.getInstance();
        try {
            Date thisDate = simpleFormatter.parse(startDate);
            currentDate.setTime(thisDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return view;

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    Date prepareTheDate() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings_tab);

        inflater.inflate(R.menu.menu_settingsfragment, menu);
        if (showSaveSettingsButton) {
            menu.findItem(R.id.action_save_settings).setVisible(true);
        } else {
            menu.findItem(R.id.action_save_settings).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_settings) {
            if (theDate == null) {
                theDate = prepareTheDate();
            }
            saveSettings(theDate);
            showSaveSettingsButton = false;
            getActivity().invalidateOptionsMenu();
            TabLayout toolsList = (TabLayout) getActivity().findViewById(R.id.tab_layout);
            toolsList.getTabAt(0).select();
            return true;
        }

        return false;
    }

    public void saveSettings(Date theDate ) {
        SharedPref.write(Constants.PAUSE_BETWEEN_TOOLS, pause_sw.isChecked());
        SharedPref.write(Constants.EXERCISE_DAILY, freq_sw.isChecked());
        SharedPref.write(Constants.START_EXERCISE_IMMEDIATELY, exercise_start_sw.isChecked());
        SharedPref.write(Constants.RESTART_CURRENTLY_SELECTED_MUSIC,restart_sw.isChecked());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        SharedPref.write("StartingDate", formatter.format(theDate));
        if (freq_sw.isChecked()) {
            DateManager.setStartToEndDates();
        }
    }
}
