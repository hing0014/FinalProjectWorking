package com.example.finalProject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

public class CovidDetails extends AppCompatActivity {
    Bundle dataFromActivity;
    long id;
    Bundle dataToPass;
    AppCompatActivity parentActivity;

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dataToPass=  getIntent().getExtras();

        View result = inflater.inflate(R.layout.activity_covid_details, container, false);

        return result;
    }
}