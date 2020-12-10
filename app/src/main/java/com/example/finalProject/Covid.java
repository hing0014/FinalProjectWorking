/* Course Name: CST2335_021
 * Class name: CST2335 Graphical Interface Programming
 * Covid19 Case Data
 * Date: December 7th, 2020
 * Student Name : Jihyun Park as author
 * purpose: This is the final project with Teammates
 * This is about the Covid-19 research results
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

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

/**
 * This is the database for the covid-19 cases
 * This is the main class of Covid, extends AppCompatActivity.
 * this class implements NavigationView.OnNavigationItemSelectedListener
 * This class contains clicklistener, fragments, toolbar and navigation menu
 * @ author Jihyun Park
 **/
public class Covid extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
/*This is the variable for the all methods
*@author Jihyun Park
*/
    public static boolean isTablet;
    ArrayList<CovidEvent> list = new ArrayList<>();
    MyListAdapter myAdapter;

    ArrayList<CovidEvent> repoList = new ArrayList<>();
    RepoListAdapter repoAdapter;

    ImageButton searchButton;
    EditText searchText, fromText, toText;

    Button helpButton, repositButton;
    ProgressBar progressBar;
    String country, countryCode, province, status;
    int cases;
    SQLiteDatabase covidDB;
    CovidOpener covidOpener;
    String pattern = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    public final static String ITEM_COUNTRY = "COUNTRY";
    public final static String ITEM_CODE = "CODE";
    public final static String ITEM_PROVINCE = "PROVINCE";
    public final static String ITEM_CASE = "CASES";
    public final static String ITEM_STATUS = "STATUS";
    public final static String ITEM_ID = "ID";
    public final static String SAVE_COUNTRY = "country";
    public final static String SAVE_FROM = "from";
    public final static String SAVE_TO = "to";
    public final static String ITEM_ISSTORED = "isStored";
    private static SharedPreferences sharedPref;
    private FragmentManager fm;

    /**
     * When the button, Covid-19, of the main page, connected with this page.
     * When the search button is clicked the search button, stored data is showed the Covid table which in contained all information that user wants.
     * Also recorded text will be stored on SharedPreferences as 'CovidFile' include  Country and date those are searched and period
     * This activity creates for the cases of  Covid-19 the confirm cases in world around by Province and country
     * @author Jihyun Park
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid);

        ListView searchView = findViewById(R.id.listView);
        myAdapter = new MyListAdapter();
        searchView.setAdapter(myAdapter);


        ListView repoView = findViewById(R.id.repositoryView);
        repoAdapter = new RepoListAdapter();
        repoView.setAdapter(repoAdapter);

        boolean isTablet = findViewById(R.id.fragmentLocation) != null; //check if the FrameLayout is loaded

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        CovidRequest req = new CovidRequest();

        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        searchText = findViewById(R.id.searchText);
        // save date and country name
        sharedPref = getSharedPreferences("CovidFile", Context.MODE_PRIVATE);

        searchText.setText(sharedPref.getString(SAVE_COUNTRY, ""));
        fromText.setText(sharedPref.getString(SAVE_FROM, ""));
        toText.setText(sharedPref.getString(SAVE_TO, ""));

        searchButton = findViewById(R.id.magnify);
        searchButton.setOnClickListener((clk) ->
        {
            String searchWord = searchText.getText().toString();
            String from = fromText.getText().toString();
            String to = toText.getText().toString();

            if (!(from.equals(to))) {
                req.execute("https://api.covid19api.com/country/" + searchWord + "/status/confirmed/live?from=" + from + "T00:00:00Z&to=" + to + "T00:00:00Z");
            } else {
                Toast.makeText(this, R.string.searchText1, Toast.LENGTH_SHORT).show();
            }
            EditText fromText = findViewById(R.id.fromText);
            String edFrom = fromText.getText().toString();
            saveToSharedPreference(edFrom, SAVE_FROM);

            EditText toText = findViewById(R.id.toText);
            String edTo = toText.getText().toString();
            saveToSharedPreference(edTo, SAVE_TO);

            EditText searchText = findViewById(R.id.searchText);
            String edSearchText = searchText.getText().toString();
            saveToSharedPreference(edSearchText, SAVE_COUNTRY);

            myAdapter.notifyDataSetChanged();
        });

        repoView.setOnItemLongClickListener((p, b, pos, id) -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.attention)).setMessage(R.string.deletSure)
                    .setPositiveButton(R.string.yes, (click, arg) ->
                    {
                        covidDB.delete(CovidOpener.TABLE_NAME, CovidOpener.COL_ID + "=?", new String[]{Integer.toString(repoList.get((int)id).id)});
                        repoList.remove(pos);
                        repoAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (click, arg) -> {
                    })
                   .create().show();
            return true;
        });

        searchView.setOnItemClickListener((lv, item, position, id) -> {
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ITEM_COUNTRY, list.get(position).country);
            dataToPass.putString(ITEM_CODE, list.get(position).countryCode);
            dataToPass.putString(ITEM_PROVINCE, list.get(position).province);
            dataToPass.putInt(ITEM_CASE, list.get(position).cases);
            dataToPass.putString(ITEM_STATUS, list.get(position).status);
            dataToPass.putLong(ITEM_ID, id);//
            dataToPass.putBoolean(ITEM_ISSTORED, false);
            // Is tablet
            if (isTablet) {
                FragmentCovidDetails newFragment = new FragmentCovidDetails();
                fm = getFragmentManager();
                newFragment.setArguments(dataToPass);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLocation, new Fragment()).commit();
                myAdapter.notifyDataSetChanged();

            } else {  // isPhone
                Intent goToActivity = new Intent(this, EmptyCovid.class);
                goToActivity.putExtras(dataToPass); //send data to next activity
                startActivity(goToActivity);
            }
        });

        helpButton = findViewById(R.id.help);
        helpButton.setOnClickListener((click) -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle(R.string.helpTitle).setMessage(R.string.covidHelp)
                    .setPositiveButton(R.string.confirm, (cl, arg) -> { }).create().show();
        });

        repositButton = findViewById(R.id.repository);
        repositButton.setOnClickListener( clickto ->{
            loadDataFromDatabase();
            repoAdapter.notifyDataSetChanged();
        });

        repoView.setOnItemClickListener((lv, item, position, id) -> {
            Bundle dataToPass = new Bundle();
            dataToPass.putString(ITEM_COUNTRY, repoList.get(position).country);
            dataToPass.putString(ITEM_CODE, repoList.get(position).countryCode);
            dataToPass.putString(ITEM_PROVINCE, repoList.get(position).province);
            dataToPass.putInt(ITEM_CASE, repoList.get(position).cases);
            dataToPass.putString(ITEM_STATUS, repoList.get(position).status);
            dataToPass.putLong(ITEM_ID, id);
            dataToPass.putBoolean(ITEM_ISSTORED, true);

            Intent goToActivity = new Intent(this, EmptyCovid.class);
            goToActivity.putExtras(dataToPass); //send data to next activity
            startActivity(goToActivity);

        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent pageChange;

        switch (item.getItemId()) {
            case (R.id.home):
                pageChange = new Intent(Covid.this, MainActivity.class);
                startActivity(pageChange);
                break;

            case (R.id.ticket):
                pageChange = new Intent(Covid.this, TicketMaster.class);
                startActivity(pageChange);
                break;

            case (R.id.food):
                pageChange = new Intent(Covid.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;

            case (R.id.audio):
                pageChange = new Intent(Covid.this, AudioActivity.class);
                startActivity(pageChange);
                break;

            case (R.id.bacteria):
                finish();
                break;

            case (R.id.search_item):
                break;
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    /* Extended BaseAdapter that is the bridge between a ListView and the data that backs the list.
     * ListView can display any data provided that it is wrapped in a MyListAdapter.
     * all the methods are override from BaseAdapter
     * This is explin what is inside of the list, number of items, rows.
     * Row-layout that will be positioned at the specified row in the list.
     * @author Jihyun Park
     */
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
/*
* This is the Adapter extends BaseAdapter for the Load Repository
* The function is the same as MyListAdapter
* @author Jihyun Park
* */
    private class RepoListAdapter extends BaseAdapter {

        /*number of items in the repoList
         * Override
         * Return size of the list
         * @author Jihyun Park
         * */
        @Override
        public int getCount() {
            return repoList.size();
        }

        /* Objects go at row in the repoList
         * Override
         * @param int position
         * @author Jihyun Park
         * */
        @Override
        public Object getItem(int position) {
            return repoList.get(position);
        }

        /* database id at row
         * Override
         * @author Jihyun Park
         * */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /* this returns the layout that will be positioned at the specified row in the repoList.
         * this view is make a new row, set the text by row, and returns information to the table
         * Override
         * @author Jihyun Park
         * */
        @Override
        public View getView(int position, View v, ViewGroup parent) {
            CovidEvent covidResult = repoList.get(position);
            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.covid_row_layout, parent, false);
            TextView coronaView = newView.findViewById(R.id.covidCases);
            coronaView.setText("province: " + covidResult.getProvince() + " , cases: " + covidResult.getCases());
            repoAdapter.notifyDataSetChanged();

            return newView;
        }
    }

    /* this class has 3 important functions: doInBackground, onProgressUpdate, onPostExecute
     * In order for the interface to be responsive to user input, any long running tasks must be run
     * To start the thread, create an object and call execute()
     * This class takes JSON array from the URL for the Covid-19 confirmed cases
     *@author: Jihyun Park
     */
    private class CovidRequest extends AsyncTask<String, Integer, String> {

        /* This is contains internet website which is contains information that users want
         * connection with website, bring the data and set the information to the table
         * doInBackground() parameter is array type of String
         * Override method
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

                    list.add(new CovidEvent(country, countryCode, province, cases, status, -1));
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

        /* incoming parameter is the exact same object that was returned by doInBackground.
         * @param String fromDoInBackground
         * @Author Jihyun Park
         */
        protected void onPostExecute(String fromDoInBackground) {
            myAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

/* This is retrieve any privious data that already stored in SQL databse
     * create table, put data into table.
     * This database will load repoList by order
     * @author Jihyun Park
     * */
    private void loadDataFromDatabase() {
        covidOpener = new CovidOpener(this);
        covidDB = covidOpener.getWritableDatabase();

        repoList.clear();
        String[] columns = {covidOpener.COL_ID, covidOpener.COL_COUNTRY, covidOpener.COL_CODE, covidOpener.COL_PROVINCE, covidOpener.COL_CASES, covidOpener.COL_STATUS};

        Cursor results = covidDB.query(false, covidOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        int countryColumnIndex = results.getColumnIndex(covidOpener.COL_COUNTRY);
        int countryCodeColumnIndex = results.getColumnIndex(covidOpener.COL_CODE);
        int provinceColumnIndex = results.getColumnIndex(covidOpener.COL_PROVINCE);
        int caseColumnIndex = results.getColumnIndex(covidOpener.COL_CASES);
        int statusColumnIndex = results.getColumnIndex(covidOpener.COL_STATUS);
        int idColumnIndex = results.getColumnIndex(covidOpener.COL_ID);

        while (results.moveToNext()) {
            String country = results.getString(countryColumnIndex);
            String countryCode = results.getString(countryCodeColumnIndex);
            String province = results.getString(provinceColumnIndex);
            int cases = results.getInt(caseColumnIndex);
            String status = results.getString(statusColumnIndex);
            int id = results.getInt(idColumnIndex);

            repoList.add(new CovidEvent(country, countryCode, province, cases, status, id));
        }
        results.close();
    }

    /* Stores history of the searched information at SharedPreferences
     * @param stringToSave will be stored as String value
     * @param key, stringToSave.
     * @author Jihyun Park
     */
    public void saveToSharedPreference(String stringToSave, String key) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString( key, stringToSave);
        editor.commit();
        myAdapter.notifyDataSetChanged();
    }

    /* reference  professor's lecture
     * Inflate the menu items for use in the action bar
     * Manages the search function in the action bar.
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
     *  Inflate the menu items for use in the action bar
     * @param item The menu used in the action bar.
     */
    @SuppressLint("NonConstantResourceId")
    // @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent pageChange;
        switch (item.getItemId()) {
            case R.id.home:
                pageChange = new Intent(Covid.this, MainActivity.class);
                startActivity(pageChange);
                break;

            case R.id.ticket:
                pageChange = new Intent(Covid.this, TicketMaster.class);
                startActivity(pageChange);
                break;

            case R.id.food:
                pageChange = new Intent(Covid.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;

            case R.id.audio:
                pageChange = new Intent(Covid.this, AudioActivity.class);
                startActivity(pageChange);
                break;

            case R.id.search_item:
                break;

        }
        return false;
    }

    /*
     * this is the class about the basic information of the related Covid Event
     * @author: Jihyun Park
     * */
     class CovidEvent {
        public int id;
        public String country;
        public String countryCode;
        public String province;
        public int cases;
        public String status;

        /* This is the class of the covid event
         * @param String type is country, countryCode, province, status and double typs is cases
         * @Author Jihyun Park*/
        public CovidEvent(String country, String countryCode, String province, int cases, String status, int id) {
            this.country = country;
            this.countryCode = countryCode;
            this.province = province;
            this.cases = cases;
            this.status = status;
            this.id = id;
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
}