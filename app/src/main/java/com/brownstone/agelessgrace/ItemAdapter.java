package com.brownstone.agelessgrace;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class ItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    String[] adapterTools;
    Integer[] adapterToolNos = new Integer[3];
    ArrayList<Integer> completedToolIds = SharedPref.getCompletedToolIds();
    ArrayList<String> selectedTools = new ArrayList<String>();
    List<Integer> selectedToolNos = new ArrayList<Integer>();
    ArrayList<String> selectedToolSets;
//    String[] selectedToolSets;
//    Set<String> toolSet = new HashSet<String>();
    String toolSet = "";
    Map<String, Integer> currentSelections = new HashMap<String, Integer>();

    Integer numberOfChoices;
    ToolFragment context;

    String[] descriptions;
    Boolean resetState = false;


    public ItemAdapter(ToolFragment context, String[] i, Integer[] ids, String[] d, Boolean reset) {
        resetState = reset;
        if (resetState == false) {
            createSelectedToolsArrayFromPreferences();
        } else {
            completedToolIds = new ArrayList<>();
        }
        int l = 0;
        adapterTools = i;
        Integer j = 0;
        adapterToolNos = ids;
        descriptions = d;
        this.context = context;
        numberOfChoices = 0;
        resetState = reset;
        mInflater = context.getActivity().getLayoutInflater();//(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return adapterTools.length;
    }

    @Override
    public Object getItem(int position) {
        return adapterTools[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Integer index = position;
        View v = mInflater.inflate(R.layout.row_item,null);
        TextView nameTextView = v.findViewById(R.id.nameTextView);
        TextView descriptionTextView = v.findViewById(R.id.toolDescriptionTextView);
        final Button selectButton = v.findViewById(R.id.selectButton);
        selectButton.setBackgroundResource(R.mipmap.selector);

        ImageButton forwardBtn = v.findViewById(R.id.forwardButton);
        forwardBtn.setTag(position);

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.goToDescriptionView(adapterTools, adapterToolNos, index);
            }});

        if (adapterTools.length > 3) {
            List<Integer> completedToolIds = context.completedToolIds;
            if ((completedToolIds != null && completedToolIds.size() > 0 && completedToolIds.size() < 7) && selectedToolNos.size() == completedToolIds.size()) {
                for (int i = 0; i < completedToolIds.size(); i++) {
                    if (!selectedToolNos.contains(completedToolIds.get(i))) {
                        selectedToolNos.add(completedToolIds.get(i) - 1);
                    }
                }
            }

        }
        int i = position;
        int pos = new ArrayList<String>(Arrays.asList(context.allTools)).indexOf(adapterTools[position]);
        String name = String.format("Tool #%d: %s", pos + 1, adapterTools[position]);
        String desc = descriptions[position];
        final Integer toolNo = position;

        nameTextView.setText(name);
        descriptionTextView.setText(desc);

        if (selectedToolNos == null) {
            selectedToolNos = new ArrayList<Integer>();
        }
        if (adapterTools.length > 3) {
            selectButton.setVisibility(View.VISIBLE);
            if ((completedToolIds != null && completedToolIds.size() > 0 ) || (selectedToolNos != null && selectedToolNos.size()  > 0)) {
                if (completedToolIds != null && completedToolIds.size() < 7 && completedToolIds.contains(position)) {
                    selectButton.setVisibility(View.GONE);
                } else if (selectedToolNos.contains(position)) {
                    selectButton.setBackgroundResource(R.mipmap.selected);
                }
                if (completedToolIds == null || completedToolIds.size() == 7) {
                    completedToolIds = new ArrayList<Integer>();
                }
            }
            if (completedToolIds != null && this.selectedTools.size() >= completedToolIds.size() + 3) {
                context.showContinueButton();
            } else if (resetState) {
                context.showSelectButtonOnly();
                resetState = false;
            }
            if (numberOfChoices == 0) {
                toolSet = "";
            }

            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer toolCount = selectedToolNos.size();
                    if (selectedToolNos.contains(toolNo)) {
                        //remove this selection
                        v.setBackgroundResource(R.mipmap.selector);
                        int locOfToolNo = selectedToolNos.indexOf(toolNo);
                        selectedToolNos.remove(toolNo);
                        toolCount -= 1;
                        context.showSelectButtonOnly();
                    } else {
                        //add this selection
                        v.setBackgroundResource(R.mipmap.selected);
                        selectedToolNos.add(toolNo);
                        toolCount++;
                        context.showSelectButtonOnly();
                    }
                    if (toolCount > 0 && toolCount % 3 == 0) {
                        toolSet = "";
                        selectedToolSets = new ArrayList<>();
                        StringBuilder sb = new StringBuilder();
                        String delim = "";
                        for (int i = 0; i < selectedToolNos.size(); i++) {
                            String s = String.valueOf(selectedToolNos.get(i));
                            sb.append(delim);
                            sb.append(s);
                            if (i > 0 && (i + 1) % 3 == 0) {
                                toolSet = sb.toString();
                                sb = new StringBuilder();
                                delim = "";
                                selectedToolSets.add(toolSet);
//                                add(selectedToolSets,toolSet);
                                toolSet = "";
                            } else {
                                delim = ",";
                            }
                        }
                        context.selectedToolSets = selectedToolSets;
                        context.showCommitButton();
                        Set<String> sets = new LinkedHashSet<>(selectedToolSets);
                        SharedPref.addSelectedToolSets(sets);
                    }
                }
            });
        } else {
            selectButton.setVisibility(View.GONE);
        }
        return v;
    }

    /* create a list of tools that have already been selected */
    void createSelectedToolsArrayFromPreferences() {
        selectedToolSets = SharedPref.getAllSelectedToolSets();
        selectedToolNos = new ArrayList<>(selectedToolSets.size() * 3);
        for (int i = 0; i < selectedToolSets.size(); i++) {
            String toolSet = selectedToolSets.get(i);
            String[] numbersStr = toolSet.split(",");
            for(int j=0; j<numbersStr.length; j++) {
                selectedToolNos.add(Integer.parseInt(numbersStr[j]));
            }
        }
    }

    void getSelectedToolNos(ToolFragment context) {
        for (String aTool : selectedTools) {
            int pos = new ArrayList<String>(Arrays.asList(context.allTools)).indexOf(aTool);
            selectedToolNos.add(pos);
        }
    }

    public void updateData(String[] tools, Integer[] toolNos) {
        adapterToolNos = toolNos;
        adapterTools = tools;

        notifyDataSetChanged();
    }
}
