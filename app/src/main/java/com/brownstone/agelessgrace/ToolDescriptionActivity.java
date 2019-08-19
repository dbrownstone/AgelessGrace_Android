package com.brownstone.agelessgrace;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ToolDescriptionActivity extends AppCompatActivity {

    int index;
    List<String> selections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_description);

        Intent in = getIntent();
        Bundle bundle = getIntent().getExtras();
        index = bundle.getInt("theIndex", -1);
        selections = bundle.getStringArrayList("selections");

        if (index > -1) {
            Resources res = getResources();
            String tool = String.format("Tool #%d: %s", (index + 1), (res.getStringArray(R.array.tools)[index]));
            String descrText = (res.getStringArray(R.array.full_descriptions)[index]);
            getSupportActionBar().setTitle(tool);
            TextView description = (TextView) this.findViewById(R.id.toolDescription);
            description.setText(descrText);
            description.setMovementMethod(new ScrollingMovementMethod());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.NOTES_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onSupportNavigateUp();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent intent = new Intent();
        intent.putExtra("from_description", true);
        intent.putStringArrayListExtra("selections",new ArrayList<String>());
        setResult(RESULT_CANCELED, intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tool_description, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                setResult(RESULT_CANCELED, intent);
                startActivity(intent);
                return true;
            case R.id.action_next:
                // User chose the "Next" action
                Intent showNextActivity = new Intent(getApplicationContext(), NotesActivity.class);
                showNextActivity.putExtra("theIndex", index);
                showNextActivity.putStringArrayListExtra("selections", (ArrayList<String>)selections);
                startActivityForResult(showNextActivity, Constants.NOTES_ACTIVITY_REQUEST_CODE);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
