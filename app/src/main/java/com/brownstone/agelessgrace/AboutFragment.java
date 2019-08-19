package com.brownstone.agelessgrace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.about_tab_fragment, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);

        String about = (getResources().getStringArray(R.array.about_agelessgrace)[0]);
        TextView aboutAg = (TextView) view.findViewById(R.id.about_ag);
        aboutAg.setText(about);

        String whoDoesIt = (getResources().getStringArray(R.array.about_agelessgrace)[1]);
        TextView whoAg = (TextView) view.findViewById(R.id.ag_who);
        whoAg.setText(whoDoesIt);

        String whatIsIt = (getResources().getStringArray(R.array.about_agelessgrace)[2]);
        TextView whatAg = (TextView) view.findViewById(R.id.ag_what);
        whatAg.setText(whatIsIt);

        String whyIsIt = (getResources().getStringArray(R.array.about_agelessgrace)[3]);
        TextView whyAg = (TextView) view.findViewById(R.id.ag_why);
        whyAg.setText(whyIsIt);

        String whereToDoIt = (getResources().getStringArray(R.array.about_agelessgrace)[4]);
        TextView whereAg = (TextView) view.findViewById(R.id.ag_where);
        whereAg.setText(whereToDoIt);

        String howToDoIt = (getResources().getStringArray(R.array.about_agelessgrace)[5]);
        TextView howAg = (TextView) view.findViewById(R.id.ag_how);
        howAg.setText(howToDoIt);
        return view;
    }
}
