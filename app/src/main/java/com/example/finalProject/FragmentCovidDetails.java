/* Course Name: CST2335_021
 * Class name: CST2335 Graphical Interface Programming
 * Covid-19 Case Data
 * Date: December 7th, 2020
 * Student Name : Jihyun Park as author
 * purpose: This is the final project with Teammates
 * This is about the Covid-19 research results as a fragment elements
 */
package com.example.finalProject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

/*This class is for the FragmentCovidDetails.
 * It extends fragment for the specific item as row in a table
 *@Author Jihyun Park
 * */
public class FragmentCovidDetails extends Fragment {
    long id;
    Bundle dataToPass;
    AppCompatActivity parentActivity;


    /*This methods is Override
     * It is defined the AppCompatActivity
     * This is used for tablet or phone
     *@param context
     *@author Jihyun Park
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity = (AppCompatActivity) context;
    }

    /*
     * This is the similar patter as MyListAdapter and repoAdapter
     * This shows database as bundle, which contains id, country, country code, province, cases, status and Id
     * This methods will save data to the database if user wants
     * @param LayoutInflater inflater used to get the view
     * @param ViewGroup container
     * @param  Bundle savedInstanceState
     * @author Jihyun Park
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dataToPass = getArguments(); // bundle back
        id = dataToPass.getLong(Covid.ITEM_ID);
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_covid_details, container, false);

        TextView idView = (TextView) result.findViewById(R.id.idText);
        idView.setText("ID : " + id);

        TextView country = (TextView) result.findViewById(R.id.country);
        country.setText("Country : " + (dataToPass.getString(Covid.ITEM_COUNTRY)));

        TextView conCode = (TextView) result.findViewById(R.id.conCode);
        conCode.setText("Country Code : " + (dataToPass.getString(Covid.ITEM_CODE)));

        TextView province = (TextView) result.findViewById(R.id.province);
        province.setText("Province : " + (dataToPass.getString(Covid.ITEM_PROVINCE)));

        TextView cases = (TextView) result.findViewById(R.id.cases);
        cases.setText("Cases : " + (dataToPass.getInt(Covid.ITEM_CASE)));

        TextView status = (TextView) result.findViewById(R.id.status);
        status.setText("Status : " + (dataToPass.getString(Covid.ITEM_STATUS)));

        // This button used to go back mainActivity of the Covid class
        Button backButton = result.findViewById(R.id.back1);
        backButton.setOnClickListener(click ->
        {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            parentActivity.onBackPressed();

        });

        Button storeButton = result.findViewById(R.id.store);
        // this is the data from searchview
        if (dataToPass.getBoolean(Covid.ITEM_ISSTORED) == false) {
            storeButton.setOnClickListener(click -> {
                CovidOpener dbHelper = new CovidOpener(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues newRowValues = new ContentValues();
                newRowValues.put(CovidOpener.COL_ID, dataToPass.getString(Covid.ITEM_ID));
                newRowValues.put(CovidOpener.COL_COUNTRY, dataToPass.getString(Covid.ITEM_COUNTRY));
                newRowValues.put(CovidOpener.COL_CODE, dataToPass.getString(Covid.ITEM_CODE));
                newRowValues.put(CovidOpener.COL_PROVINCE, dataToPass.getString(Covid.ITEM_PROVINCE));
                newRowValues.put(CovidOpener.COL_CASES, dataToPass.getInt(Covid.ITEM_CASE));
                newRowValues.put(CovidOpener.COL_STATUS, dataToPass.getString(Covid.ITEM_STATUS));
                long newRowId = db.insert(CovidOpener.TABLE_NAME, null, newRowValues);

                // This snack bar will show the user that data stored database
                Snackbar snackBar = Snackbar.make(getView(), R.string.stored, Snackbar.LENGTH_SHORT);
                snackBar.setAction(R.string.confirm1, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
            });
        }
        // this is the data from the repository
        else {
            storeButton.setVisibility(View.INVISIBLE);
        }
        return result;
    }
}

