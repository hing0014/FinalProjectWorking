/* Course Name: CST2335_021
 * Class name: CST2335 Graphical Interface Programming
 * Covid19 Case Data
 * Date: November 19, 2020
 * Student Name : Jihyun Park
 * purpose: This is the final project with Teammates
 * This is the SQLite database
 */
package com.example.finalProject;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is the database for the covid-19 cases
 * This is the main class of Covid, extends AppCompatActivity.
 *
 * @ author Jihyun Park
 **/
public class Covid extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean isTablet;
    ArrayList<CovidEvent> list = new ArrayList<>();
    MyListAdapter myAdapter;
    ImageButton searchButton;
    EditText searchText, fromText, toText;
    ProgressBar progressBar;
    String country, countryCode, province, status;
    int cases;
    SQLiteDatabase CovidDB;
    CovidOpener covidOpener;
    String pattern = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    String from = simpleDateFormat.format(new Date());
    String to = simpleDateFormat.format(new Date());

    String fromKey = simpleDateFormat.format(new Date());
    String toKey = simpleDateFormat.format(new Date());
    public final static String ITEM_COUNTRY = "COUNTRY";
    public final static String ITEM_CODE = "CODE";
    public final static String ITEM_PROVINCE = "PROVINCE";
    public final static String ITEM_CASE = "CASES";
    public final static String ITEM_STATUS = "STATUS";
    private SharedPreferences sharedPref;
    private FragmentManager fm;

    /**
     * When the button, Covid-19, of the main page, connected with this page.
     * When the search button is clicked the search button, stored data is showed the covid table which in contained all information that user wants.
     * Also recorded text will be stored on SharedPreferences as 'CovidFile' include  Country and date
     *
     * @param savedInstanceState Bundle object used in the super call of onCreate.
     * @author Jihyun Park
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid);

        loadDataFromDatabase();

        ListView myList = findViewById(R.id.listView);
        myAdapter = new MyListAdapter();
        myList.setAdapter(myAdapter);

        boolean isTablet = findViewById(R.id.fragmentLocation) != null; //check if the FrameLayout is loaded

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        loadDataFromDatabase();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        CovidRequest req = new CovidRequest();

        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        searchText = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.magnify);
        searchButton.setOnClickListener((clk) ->
        {
            String searchWord = searchText.getText().toString();
            String from = fromText.getText().toString();
            String to = toText.getText().toString();

            if (!(from.equals(to))) {
                req.execute("https://api.covid19api.com/country/" + searchWord + "/status/confirmed/live?from=" + from + "T00:00:00Z&to=" + to + "T00:00:00Z");
            }
            else {
                Toast.makeText(this, R.string.searchText1, Toast.LENGTH_SHORT).show();
            }
            saveToSharedPreference(searchWord,searchText.getText().toString());
            saveToSharedPreference(from,fromText.getText().toString());
            saveToSharedPreference(to,toText.getText().toString());

        });

        myList.setOnItemClickListener((lv, item, position, id) -> {
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ITEM_COUNTRY, list.get(position).country);
            dataToPass.putString(ITEM_CODE, list.get(position).countryCode);
            dataToPass.putString(ITEM_PROVINCE, list.get(position).province);
            dataToPass.putInt(ITEM_CASE, list.get(position).cases);
            dataToPass.putString(ITEM_STATUS, list.get(position).status);//
            // Is tablet
            if (isTablet) {
                FragmentCovidDetails newFragment = new FragmentCovidDetails();
                fm = getFragmentManager();
                newFragment.setArguments(dataToPass);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLocation, newFragment).commit(); // remove, delete...etc
            } else {  // isPhone
                Intent goToActivity = new Intent(this, EmptyCovid.class);
                goToActivity.putExtras(dataToPass); //send data to next activi
                startActivity(goToActivity);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.home):
                Intent pageChange = new Intent(Covid.this, MainActivity.class);
                startActivity(pageChange);
                break;

            case (R.id.ticket):
                // message=" You clicked to go to the TicketMaster";
                Intent pageChange2 = new Intent(Covid.this, TicketMaster.class);
                startActivity(pageChange2);
                break;

            case (R.id.food):
                //message=" You clicked to find the Recipe";
                Intent pageChange3 = new Intent(Covid.this, RecipeSearchPage.class);
                startActivity(pageChange3);
                break;

            case (R.id.audio):
                //message=" You clicked to find to music";
                Intent pageChange4 = new Intent(Covid.this, AudioActivity.class);
                startActivity(pageChange4);
                break;

            case (R.id.bacteria):
                //message=" You clicked to get the information about status of the Covid-19";

                break;

            case (R.id.search_item):
                //message=" You clicked to find information";
                break;
        }
        return false;
    }

    /*
     * Extended BaseAdapter that is the bridge between a ListView and the data that backs the list.
     * ListView can display any data provided that it is wrapped in a MyListAdapter.
     * all the methods are override from BaseAdapter
     * This is explin what is inside of the list, number of items, rows.
     * Row-layout that will be positioned at the specified row in the list.
     * @author Jihyun Park
     *
     * */
    private class MyListAdapter extends BaseAdapter {

        /*number of items in the list
         * Override
         * Return size of the list
         * @author Jihyun Park
         * */
        @Override
        public int getCount() {
            return list.size();
        }

        /* Objects go at row in the list
         * Override
         * @param int position
         * @author Jihyun Park
         * */

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        /* database id at row
         * Override
         * @author Jihyun Park
         * */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /* this returns the layout that will be positioned at the specified row in the list.
         * this view is make a new row, set the text by row, and returns information to the table
         * Override
         * @author Jihyun Park
         * */
        @Override
        public View getView(int position, View v, ViewGroup parent) {
            CovidEvent covidResult = list.get(position);
            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.covid_row_layout, parent, false);
            TextView coronaView = newView.findViewById(R.id.covidCases);
            coronaView.setText("province: " + covidResult.getProvince() + " , cases: " + covidResult.getCases());
            myAdapter.notifyDataSetChanged();
            return newView;
        }
    }

//    protected void deleteContext(Context c) {
//        CovidDB.delete(CovidOpener.TABLE_NAME, CovidOpener.COL_CASE + "= ?", new String[]{Double.toString(c.getId())});
//    }

    /* this class has 3 important functions: doInBackground, onProgressUpdate, onPostExecute
     * In order for the interface to be responsive to user input, any long running tasks must be run
     * To start the thread, create an object and call execute()
     *@author: Jihyun Park
     */
    private class CovidRequest extends AsyncTask<String, Integer, String> {

        /* This is contains internet website which is contains information that users want
         * connection with website, bring the data and set the informatino to the table
         * doInBackground() – parameter is array type of String
         * Override mothod
         * @ Author: Jihyun Park
         */
        @Override
        public String doInBackground(String... args) {
            String searchWord = searchText.getText().toString();
            String from = fromText.getText().toString();
            String to = toText.getText().toString();

            try {
                URL url = new URL("https://api.covid19api.com/country/" + searchWord + "/status/confirmed/live?from=" + from + "T00:00:00Z&to=" + to);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream response = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                String result = sb.toString();

                JSONArray jArray = new JSONArray(result);

                for (int i = 1; i < jArray.length(); i++) {
                    JSONObject covidObject = jArray.getJSONObject(i);
                    country = covidObject.getString("Country");
                    countryCode = covidObject.getString("CountryCode");
                    province = covidObject.getString("Province");
                    cases = covidObject.getInt("Cases");
                    status = covidObject.getString("Status");

                    ContentValues newRowValues = new ContentValues();

                    newRowValues.put(covidOpener.COL_COUNTRY, country);
                    newRowValues.put(covidOpener.COL_CODE, countryCode);
                    newRowValues.put(covidOpener.COL_PROVINCE, province);
                    newRowValues.put(covidOpener.COL_CASES, cases);
                    newRowValues.put(covidOpener.COL_STATUS, status);

                    list.add(new CovidEvent(country, countryCode, province, cases, status));
                }
                publishProgress(20);
                publishProgress(50);
                publishProgress(80);


            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Search Complete";
        }

        /*
         * this is the response from a server
         * it shows computation progresses, update GUI
         * @author Jihyun Park
         */
        @Override //Type 2
        public void onProgressUpdate(Integer... value) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(value[0]);
            Log.i("setProgress:", "" + value[0]);
        }

        /*
         * incoming parameter is the exact same object that was returned by doInBackground.
         * @param String fromDoInBackground
         * @Author Jihyun Park
         */
        protected void onPostExecute(String fromDoInBackground) {
            myAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /*
     * This is retrieve any privious data
     * create table, put data into table
     * @author Jihyun Park
     * */
    private void loadDataFromDatabase() {

        covidOpener = new CovidOpener(this);

        CovidDB = covidOpener.getWritableDatabase();

        String[] columns = {covidOpener.COL_COUNTRY, covidOpener.COL_CODE, covidOpener.COL_PROVINCE, covidOpener.COL_CASES, covidOpener.COL_STATUS};

        Cursor results = CovidDB.query(false, covidOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        int countryColumnIndex = results.getColumnIndex(covidOpener.COL_COUNTRY);
        int countryCodeColumnIndex = results.getColumnIndex(covidOpener.COL_CODE);
        int provinceColumnIndex = results.getColumnIndex(covidOpener.COL_PROVINCE);
        int caseColumnIndex = results.getColumnIndex(covidOpener.COL_CASES);
        int statusColumnIndex = results.getColumnIndex(covidOpener.COL_STATUS);

        while (results.moveToNext()) {
            String country = results.getString(countryColumnIndex);
            String countryCode = results.getString(countryCodeColumnIndex);
            String province = results.getString(provinceColumnIndex);
            int cases = results.getInt(caseColumnIndex);
            String status = results.getString(statusColumnIndex);

            list.add(new CovidEvent(country, countryCode, province, cases, status));
        }
        results.close();
    }


    /**Stores at SharedPreferences
     * @param stringToSave will be stored as String value
     * @param key          stringToSave.
     */
    public void saveToSharedPreference(String stringToSave, String key) {
        sharedPref=getSharedPreferences("CovidFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, stringToSave);
        editor.commit();
    }
    public SharedPreferences getDatabase(){
        return getDatabase();
    }
/*
//*this is the toolbar activity which is located upper part of main Avtivity
// * this is include navigationMenu item as well
// */
//    public class TestToolbar extends AppCompatActivity {
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_covid);

//            Toolbar tBar =  findViewById(R.id.toolbar);
//            setSupportActionBar(tBar);
//            DrawerLayout drawer = findViewById(R.id.drawer_layout);
//            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
//            drawer.addDrawerListener(toggle);
//            toggle.syncState();

    /**
     * reference  professor's lecture
     * Inflate the menu items for use in the action bar     *
     * Manages the search function in the action bar.
     *
     * @param menu The menu used in the action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView sView = (SearchView) searchItem.getActionView();
        sView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    /**
     * this is the shared code as team
     * Navigates to the selected activity.
     * Based on the menu item click.
     *
     * @param item The menu used in the action bar.
     */
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionItemSelected(MenuItem item) {
        String message = null;
        switch (item.getItemId()) {
            case R.id.home:
                message = " You click to go Main page.";
                Intent pageChange = new Intent(Covid.this, MainActivity.class);
                startActivity(pageChange);
                break;

            case R.id.ticket:
                message = " You clicked to go to the TicketMaster";
                pageChange = new Intent(Covid.this, TicketMaster.class);
                startActivity(pageChange);
                break;

            case R.id.food:
                message = " You clicked to find the Recipy";
                pageChange = new Intent(Covid.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;

            case R.id.audio:
                message = " You clicked to find to music";
                pageChange = new Intent(Covid.this, AudioActivity.class);
                startActivity(pageChange);
                break;

            case R.id.search_item:
                message = " You clicked to find information";
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        return true;
    }

//    /*
//     */
//    public static SQLiteDatabase getDatabase() {
//        return sqLiteDatabase;
//    }
//
//    /**
//     * Get the SharedPreferences.
//     */
//    public static SharedPreferences getSharedPreferences() {
//        return sharedPref;
//    }
}
/*
 * this is the class of the basic information of the related Covid Event
 * @author: Jihyun Park
 * */

class CovidEvent {
    String country;
    String countryCode;
    String province;
    int cases;
    String status;

    /* This is the class of the covid event
     * @param String type is country, countryCode, province, status and double typs is cases
     * @Author Jihyun Park*/
    public CovidEvent(String country, String countryCode, String province, int cases, String status) {
        this.country = country;
        this.countryCode = countryCode;
        this.province = province;
        this.cases = cases;
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getProvince() {
        return province;
    }

    public int getCases() {
        return cases;
    }

    public String getStatus() {
        return status;
    }
}
