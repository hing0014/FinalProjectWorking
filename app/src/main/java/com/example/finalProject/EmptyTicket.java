package com.example.finalProject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * An empty activity that only create's a new FragmentTicketDetails to define its FrameLayout.
 *  <p>
 * Course Name: CST8288_010
 * Class name: EmptyTicket
 * Date: November 19, 2020
 *
 * @version 1.0
 * @author Chris HIng
 */
public class EmptyTicket extends AppCompatActivity {
    /**
     * Create's a new FragmentTicketDetails to define the FrameLayout.
     * <p>
     * Passes the bundle to the FragmentTicketDetails.
     *
     * @param savedInstanceState Bundle object used in the super call of onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_ticket);

        Bundle dataToPass = getIntent().getExtras(); //get the data that was passed from FragmentExample

        //This is copied directly from FragmentExample.java lines 47-54
        FragmentTicketDetails dFragment = new FragmentTicketDetails();
        dFragment.setArguments( dataToPass ); //pass data to the the fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLocation, dFragment)
                .commit();
    }
}
