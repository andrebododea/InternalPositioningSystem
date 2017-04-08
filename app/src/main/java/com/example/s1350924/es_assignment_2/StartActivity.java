package com.example.s1350924.es_assignment_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Internal Positioning System");

        final Activity activity = this;

        Context mycontext = (Context)activity;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(activity, MapsActivity.class);

                // Add extras to the Intent to pass to the next activity
                /*
                myIntent.putExtra("image",byteArray);

                // Add the two arrays with points
                myIntent.putExtra("Xpoints",xCoords);
                myIntent.putExtra("Ypoints",yCoords);
                */

                // Start new activity with this new intent
                activity.startActivity(myIntent);
            }
        });
    }




}
