package com.example.finalProject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button covid = (Button) findViewById(R.id.covid);
        covid.setOnClickListener( (click) ->
        {
            Intent covidActivity = new Intent(MainActivity.this, Covid.class);
            startActivity(covidActivity);
        });

        Button ticketMaster = (Button) findViewById(R.id.ticketMaster);
        ticketMaster.setOnClickListener( (clicker) ->
        {
            Intent ticketMasterActivity = new Intent(MainActivity.this, TicketMaster.class);
            startActivity(ticketMasterActivity);
        });

        Button recipe = (Button) findViewById(R.id.recipe);
        recipe.setOnClickListener( (clicker) ->
        {
            Intent recipeActivity = new Intent(MainActivity.this, RecipeSearchPage.class);
            startActivity(recipeActivity);
        });

        Button audio = (Button) findViewById(R.id.audioData);
        audio.setOnClickListener( (clicker) ->
        {
            Intent audioActivity = new Intent(MainActivity.this, AudioActivity.class);
            startActivity(audioActivity);
        });
    }

}