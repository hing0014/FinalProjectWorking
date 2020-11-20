package com.example.finalProject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CovidDetails extends AppCompatActivity {
    Bundle dataFromActivity;
    long id;
    Bundle dataToPass;
    AppCompatActivity parentActivity;

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dataToPass=  getIntent().getExtras();

        View result = inflater.inflate(R.layout.activity_covid_details, container, false);

        TextView country = (TextView) result.findViewById(R.id.country);
        country.setText(dataFromActivity.getString(Covid.ITEM_COUNTRY));

        TextView conCode = (TextView) result.findViewById(R.id.conCode);
        conCode.setText(dataFromActivity.getString(Covid.ITEM_CONCODE));

        TextView province = (TextView) result.findViewById(R.id.province);
        province.setText(dataFromActivity.getString(Covid.ITEM_PROVINCE));

        TextView cases = (TextView) result.findViewById(R.id.cases);
        cases.setText(dataFromActivity.getString(Covid.ITEM_CASE));

        TextView status = (TextView) result.findViewById(R.id.status);
        status.setText(dataFromActivity.getString(Covid.ITEM_COUNTRY));

        return result;
    }
}