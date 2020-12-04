package com.example.finalProject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class EmptyCovid extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emptycovid);

        Bundle dataToPass = getIntent().getExtras(); //get the data that was passed from Covid.class

        FragmentCovidDetails dFragment = new FragmentCovidDetails();
        dFragment.setArguments( dataToPass ); //pass data to the the fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLocation, dFragment)
                .commit();
    }
}
