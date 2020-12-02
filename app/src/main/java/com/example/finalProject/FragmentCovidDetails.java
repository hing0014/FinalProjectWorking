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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FragmentCovidDetails extends Fragment {

    SQLiteDatabase covidDB;
    CovidOpener covidOpener;
    long id;
    Bundle dataToPass;
    AppCompatActivity parentActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        covidOpener = new CovidOpener(getContext());
        covidDB = covidOpener.getWritableDatabase();

        dataToPass = getArguments(); // bundle back
        id = dataToPass.getLong(Covid.ITEM_ID);
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.activity_covid_details, container, false);

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


        Button backButton = result.findViewById(R.id.back1);
        backButton.setOnClickListener(click ->
        {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            if (!Covid.isTablet) {
                parentActivity.onBackPressed();
            }
        });

        Button storeButton = result.findViewById(R.id.store);
        storeButton.setOnClickListener(click -> {

            CovidOpener dbHelper = new CovidOpener(getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues newRowValues = new ContentValues();
            newRowValues.put(CovidOpener.COL_ID, dataToPass.getString(Covid.ITEM_ID));
            newRowValues.put(CovidOpener.COL_COUNTRY, dataToPass.getString(Covid.ITEM_COUNTRY));
            newRowValues.put(CovidOpener.COL_CODE, dataToPass.getString(Covid.ITEM_CODE));
            newRowValues.put(CovidOpener.COL_PROVINCE, dataToPass.getString(Covid.ITEM_PROVINCE));
            newRowValues.put(CovidOpener.COL_CASES, dataToPass.getInt(Covid.ITEM_CASE));
            newRowValues.put(CovidOpener.COL_STATUS,dataToPass.getString(Covid.ITEM_STATUS));
            long newRowId = db.insert(CovidOpener.TABLE_NAME, null, newRowValues);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle(R.string.attention)
                    .setPositiveButton(getResources().getString(R.string.ok), (clk, arg) -> { });
            alertDialogBuilder.setMessage(R.string.stored)
                    .create().show();
        });

        Button deleteButton = result.findViewById(R.id.delete);
        deleteButton.setOnClickListener(click -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle(getResources().getString(R.string.delete)).setMessage(R.string.deletSure)
                    .setPositiveButton(getResources().getString(R.string.yes), (clicked, arg) ->
                    {
                       // deleteFromDatabase(CovidOpener.TABLE_NAME, id,  );
                    })
                    .setNegativeButton(getResources().getString(R.string.no), (clicked, arg) -> {  })
                    .create().show();

            });
        return result;
    }
    private void deleteFromDatabase(long id){

       // covidDB.delete(id);

    }
}

