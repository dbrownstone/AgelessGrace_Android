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
import android.text.TextUtils;

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
    ArrayList<String> selectedToolNos = new ArrayList<>();
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
        completedToolIds = context.completedToolIds;
        selectedToolSets = context.selectedToolSets;
        selectedToolNos = new ArrayList<>();
        for (int k = 0; k < selectedToolSets.size(); k++) {
            String selectedToolSet = selectedToolSets.get(k);
            String[] idsStr = selectedToolSet.split(",");
            for (int j = 0; j<3; j++) {
                selectedToolNos.add(idsStr[j]);
            }
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

        int i = position;
        int pos = new ArrayList<String>(Arrays.asList(context.allTools)).indexOf(adapterTools[position]);
        String name = String.format("Tool #%d: %s", pos + 1, adapterTools[position]);
        String desc = descriptions[position];
        final Integer toolNo = position;

        nameTextView.setText(name);
        descriptionTextView.setText(desc);

        if (selectedToolNos == null) {
            selectedToolNos = new ArrayList<>();
        }
        if (adapterTools.length > 3) {
            if (selectedToolNos != null && selectedToolNos.contains(String.valueOf(position))) {
                if (completedToolIds != null && completedToolIds.contains(position + 1)) {
                    selectButton.setVisibility(View.INVISIBLE);
                } else{
                    selectButton.setBackgroundResource(R.mipmap.selected);
                }
            } else {
                selectButton.setVisibility(View.VISIBLE);
            }
            if ((completedToolIds != null && completedToolIds.size() > 0 ) || (selectedToolNos != null && selectedToolNos.size()  > 0)) {
                if (completedToolIds != null && completedToolIds.size() < 7 && completedToolIds.contains(position + 1)) {
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
                    if (selectedToolNos.contains(String.valueOf(toolNo))) {
                        //remove this selection
                        v.setBackgroundResource(R.mipmap.selector);
                        selectedToolNos.remove(String.valueOf(toolNo));
                        toolCount -= 1;
                        context.showSelectButtonOnly();
                    } else {
                        //add this selection
                        v.setBackgroundResource(R.mipmap.selected);
                        selectedToolNos.add(String.valueOf(toolNo));
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
                                toolSet = "";
                            } else {
                                delim = ",";
                            }
                        }
                        context.selectedToolSets = selectedToolSets;
                        context.showCommitButton();
                        String sets = TextUtils.join(" ", selectedToolSets);
                        SharedPref.addSelectedToolSets(sets);
                    }
                }
            });
        } else {
            selectButton.setVisibility(View.GONE);
        }
        return v;
    }

    public void updateData(String[] tools, Integer[] toolNos) {
        adapterToolNos = toolNos;
        adapterTools = tools;

        notifyDataSetChanged();
    }
}