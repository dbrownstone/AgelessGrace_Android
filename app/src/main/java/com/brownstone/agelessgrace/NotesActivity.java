package com.brownstone.agelessgrace;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    List<String> selections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Intent in = getIntent();
        Bundle bundle = getIntent().getExtras();
        int index = bundle.getInt("theIndex", -1);
        selections = bundle.getStringArrayList("selections");

        if (index > -1) {
            Resources res = getResources();
            String tool = String.format("Tool #%d: %s", (index + 1), (res.getStringArray(R.array.tools)[index]));
            getSupportActionBar().setTitle(tool);
            String descrText = (res.getStringArray(R.array.body_parts_to_move)[index]);
            TextView description = (TextView) this.findViewById(R.id.parts_description);
            description.setText(descrText);
            descrText = (res.getStringArray(R.array.ways_to_move_them)[index]);
            TextView ways_to_move = (TextView) this.findViewById(R.id.was_to_moveTextView);
            ways_to_move.setText(descrText);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent intent = new Intent();
        intent.putStringArrayListExtra("selections",(ArrayList<String>)selections);
        setResult(RESULT_OK, intent);
        finish();
        return true;
    }
}
