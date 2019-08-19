package com.brownstone.agelessgrace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ToolFragment extends Fragment {

    View thisView;

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
    ArrayList<Integer> selectedTools;
    ArrayList<String> completedToolSets;
    ArrayList<Integer> lastCompletedToolIds;
    public String lastCompletedToolSet;
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
        thisView = inflater.inflate(R.layout.tools_tab_fragment, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.tools_title);

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
            ArrayList<Integer> arr = new ArrayList<Integer>();
            for(int j=0; j<numbersStr.length; j++) {
                selectedTools.add(i,Integer.parseInt(numbersStr[j]));
            }
        }

        String nextToolSet = "";
        Integer whichDay = 1;
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
            if (selectedToolSets.size() > 0 && completedToolSets != null) {
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
        tools = new String[3];
        toolsNo = new Integer[3];
        String[] toolIds = nextToolSet.split(",");
        for (int i = 0; i < 3; i++) {
            Integer toolId = Integer.parseInt(toolIds[i]);
            tools[i] = allTools[toolId];
            toolsNo[i] = toolId;
        }

    }

    void displayTheAppropriateButtons(Integer displayedToolsCount) {
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
                    // allows selection of ttools mnaually, randomly(either adding to list of creating a list)
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
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh fragment
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    Integer[] resetToolsArray() {
        Integer[] array = new Integer[21];
        for (int i = 0; i < 21; i++) array[i] = i;
        return array;
    }

    void getSharedPreferences() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String todaysDate = formatter.format(new Date());
        lastCompletedToolIds = SharedPref.getLastCompletedToolIds();
        String completedDate = SharedPref.read("Date_of_last_exercise", "");
        lastExerciseWasCompletedToday = (completedDate.equals(todaysDate));
        completedToolIds = SharedPref.getCompletedToolIds();
        setupCompletedSets();
        selectedToolSets = SharedPref.getAllSelectedToolSets();

        for (int i = 0; i < completedToolSets.size(); i++) {
            String set = completedToolSets.get(i);
            if (!selectedToolSets.contains(set)) {
                selectedToolSets.add(set);
            }
        }

        pauseBetweenTools = !SharedPref.read(Constants.PAUSE_BETWEEN_TOOLS, false);
        exercise_automatically = SharedPref.read(Constants.START_EXERCISE_IMMEDIATELY, false);
        exerciseDaily = SharedPref.read(Constants.EXERCISE_DAILY, true);
        if (exerciseDaily) {
            startingDate = SharedPref.read(Constants.STARTING_DATE, todaysDate);
        }
    }

    void setupCompletedSets() {
        String aSet = "";
        completedToolSets = new ArrayList<>();
        if (completedToolIds == null) {
            return;
        }
        for (int i = 0; i <  completedToolIds.size(); i = i += 3) {
            aSet = String.valueOf(completedToolIds.get(i)) + "," + String.valueOf(completedToolIds.get(i + 1)) + "," + String.valueOf(completedToolIds.get(i + 2));
            if (completedToolSets == null) {
                completedToolSets = new ArrayList<>();
            }
            completedToolSets.add(aSet);
            aSet = "";
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                String selectedToolSet = "";
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
                if (exerciseDaily && !lastExerciseWasCompletedToday) {
                    continueToNextItem();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getContext(), R.style.AGAlertDialog);
                    alertDialog.setTitle(R.string.alert_done_header);
                    String message = getString(R.string.alert_exercise_done);
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
                    break;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    void prepareToolForDisplay(String toolSet) {
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
        if (completedToolSets.size() == 7) {
            completedToolSets = new ArrayList<>();
            completedToolIds = new ArrayList<>();
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.tools_title);
            showSelectButtonOnly();
            mainActivity.invalidateOptionsMenu();
            itemAdapter = new ItemAdapter(this, tools, toolsNo,  descriptions, true);
            toolList.setAdapter(itemAdapter);
        } else {
            Integer dayOfExercise = 1;
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
        String selectedToolSet = "";
        show_continue_button = false;
        selectedToolSet = "";
        if (completedToolSets == null || completedToolSets.size() == 0) {
            selectedToolSet = selectedToolSets.get(0);
        } else  if (completedToolSets.size() < 7) {
            selectedToolSet = selectedToolSets.get(completedToolSets.size());
        }
        if (selectedToolSet != "") {
            prepareToolForDisplay(selectedToolSet);
            completeSelection();
        } else {
            // have completed all 7 and not repeating the last one any lomger
            // therefore restart
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
            completeSelection();
        }
    }

    void selectTheAppropriateTitle(Integer whichDay) {
        String exercise_title = String.format(getResources().getString(R.string.exercise_title),whichDay);
        if (!exerciseDaily) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(exercise_title);
            toolSelectionType = exercise_title;
            return;
        }
        switch (whichDay) {
            case 1:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.first_day_title);
                toolSelectionType = String.valueOf(R.string.first_day_title);
                break;
            case 2:
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.second_day_title);
                toolSelectionType = String.valueOf(R.string.second_day_title);
                break;
            case 3:
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.third_day_title);
                toolSelectionType = String.valueOf(R.string.third_day_title);
                break;
            case 4:
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.fourth_day_title);
                toolSelectionType = String.valueOf(R.string.fourth_day_title);
                break;
            case 5:
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.fifth_day_title);
                toolSelectionType = String.valueOf(R.string.fifth_day_title);
                break;
            case 6:
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.sixth_day_title);
                toolSelectionType = String.valueOf(R.string.sixth_day_title);
                break;
            case 7:
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.seventh_day_title);
                toolSelectionType = String.valueOf(R.string.seventh_day_title);
                break;
        }
    }

    Integer[] getSelectedToolNos() {
        Integer[] toolIds = new Integer[3];
        String[] allTools = getResources().getStringArray(R.array.tools);
        int i = 0;
        for (String aTool : tools) {
            int pos = new ArrayList<String>(Arrays.asList(allTools)).indexOf(aTool);
            toolIds[i++] = pos;
        }
        return toolIds;
    }


    void addToCurrentlySelectedTools(String toolName, Integer whichOne) {
        currentSelection.add(toolName);
        currentSelectionIds.add(whichOne);
    }

    void removeFromCurrentlySelectedTools(String toolName, Integer whichOne) {
        currentSelection.remove(toolName);
        currentSelectionIds.remove(Integer.valueOf(whichOne));
    }

    public void showCommitButton() {
        show_commit_button = true;
        mainActivity.invalidateOptionsMenu();
    }

    public void showContinueButton() {
        if (lastExerciseWasCompletedToday && exerciseDaily) {
            setRefresh();
        } else {
            show_continue_button = true;
        }
        mainActivity.invalidateOptionsMenu();
    }

    public void showSelectButtonOnly() {
        show_select_button = true;
        show_music_icon = false;
        show_commit_button = false;
        show_continue_button = false;
        show_refresh_button = false;
        show_repeat_continue_buttons = false;
        mainActivity.invalidateOptionsMenu();
    }

    public void goToDescriptionView(String[] toolList, Integer[] toolNoList,int position) {
        Intent showToolDescriptionActivity = new Intent(getActivity().getApplication(), ToolDescriptionActivity.class);
        tools = toolList;
        toolsNo = toolNoList;
        if (tools.length == 3) {
            theIndex = toolsNo[position];
        } else {
            theIndex = position;
        }
        showToolDescriptionActivity.putExtra("theIndex", theIndex);
        showToolDescriptionActivity.putStringArrayListExtra("selections", (ArrayList<String>)currentSelection);
        startActivity(showToolDescriptionActivity);
    }

    void setRefresh() {
        show_music_icon = false;
        show_refresh_button = true;
        this.mainActivity.invalidateOptionsMenu();
    }

    void setRepeat() {
        show_repeat_button = true;
        show_select_button = true;
        this.mainActivity.invalidateOptionsMenu();
    }

    public void showSelectAlert(ToolFragment aContext) {
        // setup the alert builder
        int numberOfToolsToSelectRandomly = 7 - selectedToolSets.size();
        Context context = this.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.product_logo_small);
        if (selectedToolSets.size() > 0 && selectedToolSets.size() < 7) {
            builder.setMessage(String.format((getResources().getString(R.string.alert_select_more)), numberOfToolsToSelectRandomly));
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
            builder.setMessage(R.string.alert_select);
            builder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            selectRandomToolSets(true);
                        }
                    });
        }
        builder.setTitle(R.string.select_the_tools);

        builder.setNeutralButton(R.string.action_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    void selectRandomToolSets(Boolean all) {
        ArrayList<Integer> randomNumbers = new ArrayList<>();
        Integer randCnt = 21;
        if (all) {
            selectedTools = new ArrayList<Integer>();
            selectedToolSets = new ArrayList<>();
            completedToolSets = new ArrayList<>();
            SharedPref.remove(Constants.SELECTED_TOOL_IDS);
        } else {
            if (selectedToolSets.size() > 0 && selectedTools.size() == 0) {
                selectedTools = new ArrayList<>(selectedToolSets.size() * 3);
                for (int i = 0; i < selectedToolSets.size(); i++) {
                    String toolSet = selectedToolSets.get(i);
                    String[] numbersStr = toolSet.split(",");
                    for(int j=0; j<numbersStr.length; j++) {
                        selectedTools.add(Integer.parseInt(numbersStr[j]));
//                        randomNumbers.add(Integer.parseInt(numbersStr[j]));
                    }
                }
            }
        }
        for (int i = 0; i <= 20; i++) {
            if (!all) {
                if(!selectedTools.contains(i)) {
                    randomNumbers.add(i);
                }
            } else {
                randomNumbers.add(i);
            }
        }

        randCnt = randomNumbers.size();
        Collections.shuffle(randomNumbers);
        for (int j = 0; j < randCnt; j += 3) {
            String toolSet =  Integer.toString(randomNumbers.get(j)) + "," +
                    Integer.toString(randomNumbers.get(j + 1)) + "," +
                    Integer.toString(randomNumbers.get(j + 2));
            selectedToolSets.add(toolSet);
            if (!all) {
                selectedTools.add(randomNumbers.get(j));
                selectedTools.add(randomNumbers.get(j + 1));
                selectedTools.add(randomNumbers.get(j + 2));
            }
        }
        if (all) {
            selectedTools = randomNumbers;
        }
        SharedPref.saveToSelectedToolIds(Constants.SELECTED_TOOL_IDS,selectedTools);
        prepareDisplay();
    }

    void prepareDisplay() {
        String nextToolSet = "";
        if (completedToolSets == null || completedToolSets.size() == 0) {
            selectTheAppropriateTitle(1);
            nextToolSet = selectedToolSets.get(0);
        } else {
            selectTheAppropriateTitle(completedToolSets.size() + 1);
            nextToolSet = selectedToolSets.get(completedToolSets.size());
        }
        Set<String> sets = new LinkedHashSet<>(selectedToolSets);
        SharedPref.addSelectedToolSets(sets);
        ArrayList<Integer> selectedToolNos = SharedPref.getSelectedToolIds();
        int[] numbers = new int[selectedToolNos.size()];
        for (int i = 0; i < selectedToolNos.size(); i++) {
            toolsNo[i] = selectedToolNos.get(i);
        }
        prepareToolForDisplay(nextToolSet);
        itemAdapter.updateData(tools, toolsNo);
        show_music_icon = true;
        mainActivity.invalidateOptionsMenu();
    }

    public void selectMusicView() {
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
