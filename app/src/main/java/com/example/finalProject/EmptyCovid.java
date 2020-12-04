/* Course Name: CST2335_021
 * Class name: CST2335 Graphical Interface Programming
 * Covid-19 Case Data
 * Date: December 7th, 2020
 * Student Name : Jihyun Park as author
 * purpose: This is the final project with Teammates
 */
package com.example.finalProject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
/*
* this is the empty class that only create new fragmentCovidDetails to define its Layout
*@author Jihyun Park
* */
 public class EmptyCovid extends AppCompatActivity {

     /*Override Class
    * This class comes from list of the covidEvent by clicking the elements
    * If Emulator is not tablet, it works
     *@param Bundle savedInstanceState,
     *@author Jihyun Park
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emptycovid);

        Bundle dataToPass = getIntent().getExtras(); //get the data that was passed from Covid.class
        // This is the same as fragmentCovidDetails java file.
        FragmentCovidDetails dFragment = new FragmentCovidDetails();
        dFragment.setArguments( dataToPass ); //pass data to the the fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLocation, dFragment)
                .commit();
    }
}
