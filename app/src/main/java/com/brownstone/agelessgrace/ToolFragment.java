package com.brownstone.agelessgrace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static com.brownstone.agelessgrace.MusicSelectorActivity.isEmulator;

public class ToolFragment extends Fragment {

    String FTAG = "ToolFragment()";
    View thisView;
    final ViewGroup nullParent = null;

    Boolean clear_all_buttons = false;
    Boolean show_refresh_button = false;
    Boolean show_select_button = false;
    Boolean show_repeat_button = false;
    public Boolean show_music_icon = false;
    public Boolean show_commit_button = false;
    public Boolean show_continue_button = false;
    public Boolean show_reselect_repeat_buttons = false;
    public Boolean show_repeat_continue_buttons = false;

    private static final int EMPTY = -1;
    String toolSelectionType = "";
    ListView toolList;
    String[] tools;
    String[] allTools;
    Integer[] toolsNo = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    ArrayList<String>  selectedToolSets;
    ArrayList<String> selectedTools;
    ArrayList<String> completedToolSets;
    String completedSets;
    ArrayList<Integer> lastCompletedToolIds;
    public String lastCompletedToolSet;
    public String lastCompletedSet;
    public ArrayList<Integer> completedToolIds;
    Integer[][] selectedToolsArray;
    ArrayList<String> currentSelection;
    ArrayList<Integer> currentSelectionIds;
    String[] descriptions;
    public ItemAdapter itemAdapter;
    Integer theIndex;
    public String startingDate;
    String[] startAndEndDates;
    Boolean repeating = false;

    public boolean pauseBetweenTools = true;
    public boolean exerciseDaily = true;
    public boolean exercise_automatically = false;

    Boolean lastExerciseWasCompletedToday = false;

    MainActivity mainActivity;
    String titleBar = "";


    public ToolFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MainActivity.displayProcedureName(FTAG);

        thisView = inflater.inflate(R.layout.tools_tab_fragment, container, false);
        String theTitle = getString(R.string.tools_title);
        AppCompatActivity theActivity = (AppCompatActivity) getActivity();
        if ((theActivity.getSupportActionBar()) != null) {
            theActivity.getSupportActionBar().setTitle(theTitle);
        }
        mainActivity = (MainActivity) getActivity();
        tools = getResources().getStringArray(R.array.tools);
        allTools = tools;
        toolsNo = resetToolsArray();
        descriptions = getResources().getStringArray(R.array.primary_benefits);
        currentSelection = new ArrayList<>();
        currentSelectionIds = new ArrayList<>();

        setHasOptionsMenu(true);
        getSharedPreferences();
        selectedTools = new ArrayList<>(selectedToolSets.size() * 3);
        for (int i = 0; i < selectedToolSets.size(); i++) {
            String toolSet = selectedToolSets.get(i);
            String[] numbersStr = toolSet.split(",");
            ArrayList<Integer> arr = new ArrayList<>();
            for (String s : numbersStr) {
                selectedTools.add(i, String.valueOf(s));
            }
        }

        String nextToolSet;
        int whichDay = 1;
        if (selectedToolSets.size() == 7) {
            Integer theTitleId;
            if (completedToolSets == null || completedToolSets.size() == 0) {
                nextToolSet = selectedToolSets.get(0);
            } else {
                if (exerciseDaily) {
                    if (lastExerciseWasCompletedToday) {
                        nextToolSet = selectedToolSets.get(completedToolSets.size() - 1);
                    } else {
                        nextToolSet = selectedToolSets.get(completedToolSets.size());
                    }
                } else {
                    nextToolSet = selectedToolSets.get(completedToolSets.size() - 1);
                }
            }
            setUpToolsToBeDisplayed(nextToolSet);
            if (completedToolSets != null && completedToolSets.size() != 0) {
                whichDay = completedToolSets.size();
            }
            selectTheAppropriateTitle(whichDay);
        } else {
            if (selectedToolSets.size() > 0 && (completedToolSets != null && completedToolSets.size() > 0)) {
                nextToolSet = selectedToolSets.get(completedToolSets.size() - 1);
                setUpToolsToBeDisplayed(nextToolSet);
                whichDay = completedToolSets.size();
                selectTheAppropriateTitle(whichDay);
            } else {
                tools = allTools;
            }
        }
        displayTheAppropriateButtons(tools.length);
        toolList = thisView.findViewById(R.id.tool_list);
        itemAdapter = new ItemAdapter(this, tools, toolsNo,  descriptions, false);
        toolList.setAdapter(itemAdapter);
        mainActivity.invalidateOptionsMenu();
        return thisView;
    }


    void setUpToolsToBeDisplayed(String nextToolSet) {
        MainActivity.displayProcedureName(FTAG);

        tools = new String[3];
        toolsNo = new Integer[3];
        String[] toolIds = nextToolSet.split(",");
        List<String> toolIdsList = Arrays.asList(toolIds);
        for (int j = 0; j < toolIdsList.size(); j++) {
            Integer toolId = Integer.parseInt(toolIds[j]);
            tools[j] = allTools[toolId];
            toolsNo[j] = toolId;
        }
    }

    void displayTheAppropriateButtons(Integer displayedToolsCount) {
        MainActivity.displayProcedureName(FTAG);

        show_music_icon = false;
        show_commit_button = false;
        show_continue_button = false;
        show_refresh_button = false;
        show_select_button = false;

        if (exerciseDaily) {
            if (lastExerciseWasCompletedToday) {
                setRefresh();
            } else  if (selectedToolSets.size() < 7) {
                showSelectButtonOnly();
            } else {
                show_commit_button = true;
            }
        } else if (selectedToolSets.size() < 7) {
            if (completedToolSets != null && completedToolSets.size() > 0) {
                show_reselect_repeat_buttons = true;
            } else {
                if (selectedToolSets.size() > 0 && tools.length % 3 == 0) {
                    show_commit_button = true;
                } else {
                    // allows selection of tools manually, randomly(either adding to list of creating a list)
                    showSelectButtonOnly();
                }
            }
            mainActivity.invalidateOptionsMenu();
        } else {
            if (completedToolSets == null || completedToolSets.size() == 0) {
                show_music_icon = true;
            } else {
                show_repeat_continue_buttons = true;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        MainActivity.displayProcedureName(FTAG);

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh fragment
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction().detach(this).attach(this).commit();
            }
        }
    }

    Integer[] resetToolsArray() {
        MainActivity.displayProcedureName(FTAG);

        Integer[] array = new Integer[21];
        for (int i = 0; i < 21; i++) array[i] = i;
        return array;
    }

    void getSharedPreferences() {
        MainActivity.displayProcedureName(FTAG);

        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyyMMdd");
        String todaysDate = formatter.format(new Date());
        String completedDate = SharedPref.read("Date_of_last_exercise", "");
        lastExerciseWasCompletedToday = (completedDate.equals(todaysDate));
        lastCompletedToolIds = SharedPref.getLastCompletedToolIds();
        completedToolIds = SharedPref.getCompletedToolIds();
        selectedToolSets = SharedPref.getAllSelectedSets();
        if (completedToolIds != null && completedToolIds.size() > 0) {
            setupCompletedSets();
        }

        pauseBetweenTools = !SharedPref.read(Constants.PAUSE_BETWEEN_TOOLS, false);
        exercise_automatically = SharedPref.read(Constants.START_EXERCISE_IMMEDIATELY, false);
        exerciseDaily = SharedPref.read(Constants.EXERCISE_DAILY, true);
        if (exerciseDaily) {
            startingDate = SharedPref.read(Constants.STARTING_DATE, todaysDate);
        }
    }

    void setupCompletedSets() {
        MainActivity.displayProcedureName(FTAG);

        String aSet;
        if (completedToolIds != null) {
            completedToolSets = new ArrayList<>();
            for (int i = 0; i < completedToolIds.size(); i += 3) {
                aSet = completedToolIds.get(i) + "," + completedToolIds.get(i + 1) + "," + completedToolIds.get(i + 2);
                if (completedToolSets == null) {
                    completedToolSets = new ArrayList<>();
                }
                if (!completedToolSets.contains(aSet)) {
                    completedToolSets.add(aSet);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MainActivity.displayProcedureName(FTAG);

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tool_fragment, menu);
        menu.findItem(R.id.action_select).setVisible(false);
        menu.findItem(R.id.action_reselect).setVisible(false);
        menu.findItem(R.id.action_select_music).setVisible(false);
        menu.findItem(R.id.action_commit).setVisible(false);
        menu.findItem(R.id.action_continue).setVisible(false);
        menu.findItem(R.id.action_refresh_view).setVisible(false);
        menu.findItem(R.id.action_repeat).setVisible(false);
        if (show_music_icon){
            menu.findItem(R.id.action_select_music).setVisible(true);
            menu.findItem(R.id.action_reselect).setVisible(true);
        } else if (show_commit_button) {
            menu.findItem(R.id.action_commit).setVisible(true);
            menu.findItem(R.id.action_reselect).setVisible(true);
        } else if (show_continue_button) {
            menu.findItem(R.id.action_select).setVisible(true);
            menu.findItem(R.id.action_continue).setVisible(true);
        } else if (show_repeat_continue_buttons) {
            menu.findItem(R.id.action_continue).setVisible(true);
            menu.findItem(R.id.action_repeat).setVisible(true);
        } else if (show_refresh_button) {
            menu.findItem(R.id.action_refresh_view).setVisible(true);
        } else if (show_select_button) {
            menu.findItem(R.id.action_select).setVisible(true);
        } else if (show_reselect_repeat_buttons) {
            menu.findItem(R.id.action_repeat).setVisible(true);
            menu.findItem(R.id.action_reselect).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.displayProcedureName(FTAG);

        int id = item.getItemId();
        switch (id) {
            case R.id.action_select:
                showSelectAlert(this);
                break;
            case R.id.action_reselect:
                tools = allTools;
                // reset toolsNo
                toolsNo = resetToolsArray();
                if (selectedToolSets == null) {
                    selectedToolSets = new ArrayList<>();
                } else if (selectedToolSets.size() < itemAdapter.selectedToolSets.size()) {
                    selectedToolSets = itemAdapter.selectedToolSets;
                } else {
                    itemAdapter.selectedToolSets = selectedToolSets;
                    itemAdapter.selectedToolNos = selectedTools;
                }
                itemAdapter.updateData(tools, toolsNo);
                showSelectButtonOnly();
                break;
            case R.id.action_commit:
                show_commit_button = false;
                String selectedToolSet;
                if (completedToolSets == null || completedToolSets.size() == 0) {
                    selectedToolSet = selectedToolSets.get(0);
                } else {
                    selectedToolSet = selectedToolSets.get(completedToolSets.size());
                }
                prepareToolForDisplay(selectedToolSet);
                completeSelection();
                break;
            case R.id.action_continue:
                continueToNextItem();
                break;
            case R.id.action_repeat:
                repeating = true;
            case R.id.action_select_music:
                selectMusicView();
                break;
            default: // action_refresh_view
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                String todaysDate = formatter.format(new Date());
                String completedDate = SharedPref.read("Date_of_last_exercise", "");
                lastExerciseWasCompletedToday = (completedDate.equals(todaysDate));

                if ((exerciseDaily && !lastExerciseWasCompletedToday) || isEmulator())  {
                    if (isEmulator() && getContext() != null ) {
                        LayoutInflater inflater;
                        inflater = getActivity().getLayoutInflater();


                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
                        View view = inflater.inflate(R.layout.centered_image_alert, nullParent);
                        alertDialog.setView(view);

                        alertDialog.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setNeutralButton("Bypass",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        continueToNextItem();
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        break;
                    } else {
                        continueToNextItem();
                    }
                } else {
                    if (getContext() != null) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);

                        View view = inflater.inflate(R.layout.centered_image_alert, nullParent);
                        alertDialog.setView(view);

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
                    break;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void prepareToolForDisplay(String toolSet) {
        MainActivity.displayProcedureName(FTAG);

        String[] idsStr = toolSet.split(",");
        toolsNo = new Integer[3];
        tools = new String[3];
        toolsNo[0] = Integer.parseInt(idsStr[0]);
        tools[0] = allTools[toolsNo[0]];
        toolsNo[1] = Integer.parseInt(idsStr[1]);
        tools[1] = allTools[toolsNo[1]];
        toolsNo[2] = Integer.parseInt(idsStr[2]);
        tools[2] = allTools[toolsNo[2]];
    }

    void completeSelection() {
        MainActivity.displayProcedureName(FTAG);

        if (completedToolSets != null && completedToolSets.size() == 7) {
            completedToolSets = new ArrayList<>();
            completedToolIds = new ArrayList<>();
            if (getActivity() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.tools_title));
                showSelectButtonOnly();
                mainActivity.invalidateOptionsMenu();
                itemAdapter = new ItemAdapter(this, tools, toolsNo, descriptions, true);
                toolList.setAdapter(itemAdapter);
            }
        } else {
            int dayOfExercise = 1;
            if (completedToolSets != null) {
                dayOfExercise = completedToolSets.size() + 1;
            }
            selectTheAppropriateTitle(dayOfExercise);
            show_music_icon = true;
            itemAdapter.updateData(tools, toolsNo);
            mainActivity.invalidateOptionsMenu();
        }
    }

    void continueToNextItem() {
        MainActivity.displayProcedureName(FTAG);

        String selectedToolSet;
        show_continue_button = false;
        selectedToolSet = "";
        if (completedToolSets == null || completedToolSets.size() == 0) {
            selectedToolSet = selectedToolSets.get(0);
        } else  if (completedToolSets.size() < 7) {
            selectedToolSet = selectedToolSets.get(completedToolSets.size());
        }
        if (!selectedToolSet.equals("")) {
            prepareToolForDisplay(selectedToolSet);
            completeSelection();
        } else {
            // have completed all 7 and not repeating the last one any lomger
            // therefore restart
            if (getActivity() != null) {
                selectedToolSets = new ArrayList<>();
                itemAdapter.selectedToolSets = selectedToolSets;
                SharedPref.remove(Constants.SELECTED_TOOL_SETS);
                selectedTools = new ArrayList<>();
                SharedPref.remove(Constants.SELECTED_TOOL_IDS);
                itemAdapter.selectedToolNos = selectedTools;
                completedToolSets = new ArrayList<>();
                SharedPref.remove(Constants.COMPLETED_TOOL_SETS);
                completedToolIds = new ArrayList<>();
                itemAdapter.completedToolIds = completedToolIds;
                SharedPref.remove(Constants.COMPLETED_TOOL_IDS);
                tools = allTools;
                toolsNo = resetToolsArray();
                showSelectButtonOnly();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.tools_title);
                mainActivity.invalidateOptionsMenu();
                itemAdapter = new ItemAdapter(this, tools, toolsNo, descriptions, true);
                toolList.setAdapter(itemAdapter);
            }}
    }

    void selectTheAppropriateTitle(Integer whichDay) {
        MainActivity.displayProcedureName(FTAG);

        if (getActivity() != null){
            String exercise_title = String.format(getResources().getString(R.string.exercise_title), whichDay);
            if (!exerciseDaily) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(exercise_title);
                toolSelectionType = exercise_title;
                return;
            }
            switch (whichDay) {
                case 1:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.first_day_title));
                    toolSelectionType = getString(R.string.first_day_title);
                    break;
                case 2:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.second_day_title));
                    toolSelectionType = getString(R.string.second_day_title);
                    break;
                case 3:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.third_day_title));
                    toolSelectionType = getString(R.string.third_day_title);
                    break;
                case 4:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.fourth_day_title));
                    toolSelectionType = getString(R.string.fourth_day_title);
                    break;
                case 5:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.fifth_day_title));
                    toolSelectionType = getString(R.string.fifth_day_title);
                    break;
                case 6:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.sixth_day_title));
                    toolSelectionType = getString(R.string.sixth_day_title);
                    break;
                case 7:
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.seventh_day_title);
                    toolSelectionType = getString(R.string.seventh_day_title);
                    break;
            }
        }
    }

    Integer[] getSelectedToolNos() {
        MainActivity.displayProcedureName(FTAG);

        Integer[] toolIds = new Integer[3];
        String[] allTools = getResources().getStringArray(R.array.tools);
        int i = 0;
        for (String aTool : tools) {
            int pos = new ArrayList<>(Arrays.asList(allTools)).indexOf(aTool);
            toolIds[i++] = pos;
        }
        return toolIds;
    }

    void addToCurrentlySelectedTools(String toolName, Integer whichOne) {
        MainActivity.displayProcedureName(FTAG);

        currentSelection.add(toolName);
        currentSelectionIds.add(whichOne);
    }

    void removeFromCurrentlySelectedTools(String toolName, Integer whichOne) {
        MainActivity.displayProcedureName(FTAG);

        currentSelection.remove(toolName);
        currentSelectionIds.remove(Integer.valueOf(whichOne));
    }

    public void showCommitButton() {
        MainActivity.displayProcedureName(FTAG);

        show_commit_button = true;
        mainActivity.invalidateOptionsMenu();
    }

    public void showContinueButton() {
        MainActivity.displayProcedureName(FTAG);

        if (lastExerciseWasCompletedToday && exerciseDaily) {
            setRefresh();
        } else {
            show_continue_button = true;
        }
        mainActivity.invalidateOptionsMenu();
    }

    public void showSelectButtonOnly() {
        MainActivity.displayProcedureName(FTAG);

        show_select_button = true;
        show_music_icon = false;
        show_commit_button = false;
        show_continue_button = false;
        show_refresh_button = false;
        show_repeat_continue_buttons = false;
        mainActivity.invalidateOptionsMenu();
    }

    public void goToDescriptionView(String[] toolList, Integer[] toolNoList,int position) {
        MainActivity.displayProcedureName(FTAG);

        if (getActivity() != null) {
            Intent showToolDescriptionActivity = new Intent(getActivity().getApplication(), ToolDescriptionActivity.class);
            tools = toolList;
            toolsNo = toolNoList;
            if (tools.length == 3) {
                theIndex = toolsNo[position];
            } else {
                theIndex = position;
            }
            showToolDescriptionActivity.putExtra("theIndex", theIndex);
            showToolDescriptionActivity.putStringArrayListExtra("selections", currentSelection);
            startActivity(showToolDescriptionActivity);
        }
    }

    void setRefresh() {
        MainActivity.displayProcedureName(FTAG);

        show_music_icon = false;
        show_refresh_button = true;
        this.mainActivity.invalidateOptionsMenu();
    }

    void setRepeat() {
        MainActivity.displayProcedureName(FTAG);

        show_repeat_button = true;
        show_select_button = true;
        this.mainActivity.invalidateOptionsMenu();
    }

    public void showSelectAlert(ToolFragment aContext) {
        MainActivity.displayProcedureName(FTAG);

        // setup the alert builder
        int numberOfToolsToSelectRandomly = 7 - selectedToolSets.size();
        if (getActivity() != null && getContext() != null) {
            LayoutInflater inflater = this.getActivity().getLayoutInflater();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);

            View view = inflater.inflate(R.layout.centered_image_alert,nullParent);

            builder.setView(view);

            TextView message = view.findViewById((R.id.alertMessage));
            TextView title = view.findViewById((R.id.alertTitle));
            if (selectedToolSets.size() > 0 && selectedToolSets.size() < 7) {
                message.setText(String.format((getResources().getString(R.string.alert_select_more)), numberOfToolsToSelectRandomly));
                builder.setPositiveButton(R.string.all,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                selectRandomToolSets(true);
                            }
                        });

                builder.setNegativeButton(R.string.more,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                selectRandomToolSets(false);
                            }
                        });
            } else {
                message.setText(R.string.alert_select);
                builder.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                selectRandomToolSets(true);
                            }
                        });
            }
            title.setText(R.string.select_the_tools);

            builder.setNeutralButton(R.string.action_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }
    }

    void selectRandomToolSets(Boolean all) {
        MainActivity.displayProcedureName(FTAG);

        ArrayList<Integer> randomNumbers = new ArrayList<>();
        int randCnt;
        if (all) {
            selectedTools = new ArrayList<>();
            selectedToolSets = new ArrayList<>();
            completedToolSets = new ArrayList<>();
            SharedPref.remove(Constants.SELECTED_TOOL_IDS);
        } else {
            if (selectedToolSets.size() > 0 && selectedTools.size() % 3 == 0) {
                selectedTools = new ArrayList<>(selectedToolSets.size() * 3);
                for (int i = 0; i < selectedToolSets.size(); i++) {
                    String toolSet = selectedToolSets.get(i);
                    String[] numbersStr = toolSet.split(",");
                    for (String s : numbersStr) {
                        selectedTools.add(String.valueOf(s));
                    }
                }
            }
        }
        // create an integerized version of selected tools
        ArrayList<Integer>selectedToolsInt = new ArrayList<>();
        for (int j = 0; j < selectedTools.size(); j++) {
            selectedToolsInt.add(Integer.parseInt(selectedTools.get(j)));
        }
        for (int i = 0; i <= 20; i++) {
            if (!all) {
                if(!selectedToolsInt.contains(i)) {
                    randomNumbers.add(i);
                }
            } else {
                randomNumbers.add(i);
            }
        }

        randCnt = randomNumbers.size();
        Collections.shuffle(randomNumbers);

        for (int i = 0; i < randCnt; i++) {
            selectedTools.add(Integer.toString(randomNumbers.get(i)));
        }
        selectedToolSets = new ArrayList<>();
        for (int j = 0; j < 21; j = j + 3) {
            selectedToolSets.add(selectedTools.get(j) + "," +
                    selectedTools.get(j + 1) + "," +
                    selectedTools.get(j + 2));
        }

        String sets = TextUtils.join(" ", selectedToolSets);
        SharedPref.saveToSelectedToolIds(Constants.SELECTED_TOOL_IDS,selectedTools);
        SharedPref.addSelectedToolSets(sets);

        prepareDisplay();
    }

    void prepareDisplay() {
        MainActivity.displayProcedureName(FTAG);

        String nextToolSet;
        if (completedToolSets == null || completedToolSets.size() == 0) {
            selectTheAppropriateTitle(1);
            nextToolSet = selectedToolSets.get(0);
        } else {
            selectTheAppropriateTitle(completedToolSets.size() + 1);
            nextToolSet = selectedToolSets.get(completedToolSets.size());
        }
        String sets = TextUtils.join(" ", selectedToolSets);
        SharedPref.addSelectedToolSets(sets);
        ArrayList<String> selectedToolNos = SharedPref.getSelectedToolIds();
        int[] numbers = new int[selectedToolNos.size()];
        for (int i = 0; i < selectedToolNos.size(); i++) {
            toolsNo[i] = Integer.parseInt(selectedToolNos.get(i));
        }
        prepareToolForDisplay(nextToolSet);
        itemAdapter.updateData(tools, toolsNo);
        show_music_icon = true;
        mainActivity.invalidateOptionsMenu();
    }

    public void selectMusicView() {
        MainActivity.displayProcedureName(FTAG);

        if (getActivity() != null) {
            Intent selectMusicActivity = new Intent(getActivity().getApplication(), MusicSelectorActivity.class);
            selectMusicActivity.putExtra("selectionType", toolSelectionType);
            selectMusicActivity.putExtra("tool1Name", tools[0]);
            selectMusicActivity.putExtra("tool2Name", tools[1]);
            selectMusicActivity.putExtra("tool3Name", tools[2]);
            selectMusicActivity.putExtra("tool1Index", toolsNo[0]);
            selectMusicActivity.putExtra("tool2Index", toolsNo[1]);
            selectMusicActivity.putExtra("tool3Index", toolsNo[2]);
            selectMusicActivity.putExtra("repeating", repeating);
            repeating = false;
            startActivity(selectMusicActivity);
        }
    }
}
