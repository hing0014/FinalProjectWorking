/* Course Name: CST2335_021
 * Class name: CST2335 Graphical Interface Programming
 * Covid19 Case Data
 * Date: November 19, 2020
 * Student Name : Jihyun Park
 * purpose: This is the final project with Teammates
 * This is the SQLite database
 */
package com.example.finalProject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
public class Covid extends AppCompatActivity {

    ArrayList<CovidEvent> list = new ArrayList<>();
    MyListAdapter myAdapter;
    ImageButton searchButton;
    EditText searchText, fromText, toText;
    TextView countryDisp, countryCodeDisp, provinceDisp, casesDisp, statusDisp;
    ProgressBar progressBar;
    String country, countryCode, province, status;
    double cases;
    SQLiteDatabase CovidDB;
    CovidOpener covidOpener;
    String pattern = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    String from = simpleDateFormat.format(new Date());
    String to = simpleDateFormat.format(new Date());

    public final static String ITEM_COUNTRY = "COUNTRY";
    public final static String ITEM_CONCODE = "COUNTRY CODE";
    public final static String ITEM_PROVINCE = "PROVINCE";
    public final static String ITEM_CASE = "CASES";
    public final static String ITEM_STATUS = "STATUS";

    /**
     * When the button, Covid-19, of the main page, connected with this page. gdfdfdfhddffdfdf
     * When the search button is clicked the search button, stored data is showed the covid table which in contained all information that user wants.
     *
     * @param savedInstanceState Bundle object used in the super call of onCreate.
     * @author Jihyun Park
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid);

        ListView myList = findViewById(R.id.listView);
        myAdapter = new MyListAdapter();
        myList.setAdapter(myAdapter);

      //  loadDataFromDatabase();

        progressBar = findViewById(R.id.progressBar);

        CovidRequest req = new CovidRequest();
        fromText = findViewById(R.id.fromText);
        toText= findViewById(R.id.toText);
        searchText = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.magnify);
        searchButton.setOnClickListener((clk) ->
        {
            String searchWord = searchText.getText().toString();
            String from= fromText.getText().toString();
            String to=toText.getText().toString();

            if (! (from.equals(to))) {
                req.execute("https://api.covid19api.com/country/" + searchWord + "/status/confirmed/live?from=" + from + "T00:00:00Z&to=" + to + "T00:00:00Z");
            }
            else {
                Toast.makeText(this, R.string.searchText1, Toast.LENGTH_SHORT).show();
            }

        });
        myList.setOnItemClickListener( (lv, item, position, id) -> {

            Bundle dataToPass = new Bundle();
            dataToPass.putString(ITEM_COUNTRY, list.get(position).country);
            dataToPass.putString(ITEM_CONCODE, list.get(position).countryCode);
            dataToPass.putString(ITEM_PROVINCE, list.get(position).province);
            dataToPass.putDouble(ITEM_CASE, list.get(position).cases);
            dataToPass.putString(ITEM_STATUS, list.get(position).status);

            Intent nextActivity = new Intent(this, CovidDetails.class);
            nextActivity.putExtras(dataToPass); //send data to next activi
            startActivity(nextActivity);

        });
        myList.setOnItemLongClickListener((p, b, pos, id) -> {
            Log.e("long clicked", "pos: " + pos);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.warning).setMessage((getResources().getString(R.string.delete))+"\n" + (getResources().getString(R.string.norecover)))
                    .setPositiveButton(R.string.yes, (click, arg) -> {
                        list.remove(pos);
                        CovidDB.delete(covidOpener.TABLE_NAME, CovidOpener.COL_CASE + "= ?", new String[]{Long.toString(id)});
                        myAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton(R.string.no, (click, arg) -> {
                    })
                    .create().show();

            return true;
        });
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

            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.covid_row_layout, parent, false);

            TextView tView = newView.findViewById(R.id.searchText);
            tView.setText(getItem(position).toString());
            return newView;
        }
    }

    //        protected void updateContext(Context c) {
//            //Create a ContentValues object to represent a database row:
//            ContentValues updatedValues = new ContentValues();
//            updatedValues.put(CovidOpener.COL_CASE, c.getCases());
//
//            //now call the update function:
//            CovidDB.update(CovidOpener.TABLE_NAME, updatedValues, CovidOpener.COL_CASE + "= ?", new String[]{Double.toString(c.getCases())});
//        }
//
//        protected void deleteContext(Context c) {
//            CovidDB.delete(CovidOpener.TABLE_NAME, CovidOpener.COL_CASE + "= ?", new String[]{Double.toString(c.getCases())});
//        }


    /* this class has 3 important functions: doInBackground, onProgressUpdate, onPostExecute
     * In order for the interface to be responsive to user input, any long running tasks must be run
     * To start the thread, create an object and call execute()
     *@author: Jihyun Park
     */

    class CovidRequest extends AsyncTask<String, Integer, String> {

        /* This is contains internet website which is contains information that users want
         * connection with website, bring the data and set the informatino to the table
         * doInBackground() – parameter is array type of String
         * Override mothod
         * @ Author: Jihyun Park
         */
        @Override
        public String doInBackground(String... args) {
            String searchWord= searchText.getText().toString();
            String from= fromText.getText().toString();
            String to=toText.getText().toString();

            try {
                URL url = new URL("https://api.covid19api.com/country/"+searchWord+"/status/confirmed/live?from="+from+"T00:00:00Z&to="+to);
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

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject covidObject = jArray.getJSONObject(i);
                    country = covidObject.getString("Country");
                    countryCode = covidObject.getString("CountryCode");
                    province = covidObject.getString("Province");
                    cases = covidObject.getDouble("Cases");
                    status = covidObject.getString("Status");

                    ContentValues newRowValues = new ContentValues();

                    newRowValues.put(covidOpener.COL_COUNTRY, country);
                    newRowValues.put(covidOpener.COL_CONCODE, countryCode);
                    newRowValues.put(covidOpener.COL_PROVINCE, province);
                    newRowValues.put(covidOpener.COL_CASE, cases);
                    newRowValues.put(covidOpener.COL_STATUS, status);

                    list.add(new CovidEvent(country, countryCode, province, cases, status));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            publishProgress(100);
            return "Search Complete";
        }

        /*
         * this is the response from a server
         * it shows computation progresses, update GUI
         * @author Jihyin Park
         */
        @Override //Type 2
        public void onProgressUpdate(Integer... args) {

            progressBar.setVisibility(View.VISIBLE);

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

//        CovidDB.execSQL("CREATE TABLE " + covidOpener.TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + covidOpener.COL_TITLE + "  text," + covidOpener.COL_COUNTRY + " text," + covidOpener.COL_CONCODE + " TEXT," + covidOpener.COL_PROVINCE + " text,"
//                + covidOpener.COL_CASE + " double," + covidOpener.COL_STATUS + " text);");

        String[] columns = {covidOpener.COL_TITLE, covidOpener.COL_COUNTRY, covidOpener.COL_CONCODE, covidOpener.COL_PROVINCE,
                covidOpener.COL_CASE, covidOpener.COL_STATUS};

        Cursor results = CovidDB.query(false, covidOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        int countryColumnIndex = results.getColumnIndex(covidOpener.COL_COUNTRY);
        int countryCodeColumnIndex = results.getColumnIndex(covidOpener.COL_CONCODE);
        int provinceColumnIndex = results.getColumnIndex(covidOpener.COL_PROVINCE);
        int caseColumnIndex = results.getColumnIndex(covidOpener.COL_CASE);
        int statusColumnIndex = results.getColumnIndex(covidOpener.COL_STATUS);

        while (results.moveToNext()) {
            String country = results.getString(countryColumnIndex);
            String countryCode = results.getString(countryCodeColumnIndex);
            String province = results.getString(provinceColumnIndex);
            Double cases = results.getDouble(caseColumnIndex);
            String status = results.getString(statusColumnIndex);

            list.add(new CovidEvent(country, countryCode, province, cases, status));
        }
    }
}
/*
 * this is the class of the basic information of the related Covid Event
 * @author: Jihyun Park
 * */

class CovidEvent {
    String country;
    String countryCode;
    String province;
    double cases;
    String status;

    /* This is the class of the covid event
     * @param String type is country, countryCode, province, status and double typs is cases
     * @Author Jihyun Park*/
    public CovidEvent(String country, String countryCode, String province, double cases, String status) {
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
    public double getCases() {
        return cases;
    }
    public String getStatus() {
        return status;
    }
}
