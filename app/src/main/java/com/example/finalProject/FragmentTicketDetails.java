package com.example.finalProject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FragmentTicketDetails extends Fragment
{
    private AppCompatActivity parentActivity;

    public final static String ITEM_CITY = "CITY";
    public final static String ITEM_NAME = "EVENT NAME";
    public final static String ITEM_START_DATE = "START DATE";
    public final static String ITEM_MIN_PRICE = "MIN PRICE";
    public final static String ITEM_MAX_PRICE = "MAX PRICE";
    public final static String ITEM_URL = "URL";
    public final static String ITEM_IMAGE_STRING = "IMAGE";
    Bitmap image;
    String city;
    String eventName;
    String startDate;
    double ticketPriceMin;
    double ticketPriceMax;
    String eventUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle dataToSet = getArguments();
        View result =  inflater.inflate(R.layout.fragment_ticket_details, container, false);

        ImageView imagev = result.findViewById(R.id.eventImage);
        TextView cityv = result.findViewById(R.id.city);
        TextView namev = result.findViewById(R.id.name);
        TextView startDatev = result.findViewById(R.id.startDate);
        TextView ticketPriceMinv = result.findViewById(R.id.ticketPriceMin);
        TextView ticketPriceMaxv = result.findViewById(R.id.ticketPriceMax);

        image = TicketMaster.decodeBase64(dataToSet.getString(ITEM_IMAGE_STRING));
        city = dataToSet.getString(ITEM_CITY);
        eventName = dataToSet.getString(ITEM_NAME);
        startDate = dataToSet.getString(ITEM_START_DATE);
        ticketPriceMin = dataToSet.getDouble(ITEM_MIN_PRICE);
        ticketPriceMax = dataToSet.getDouble(ITEM_MAX_PRICE);
        eventUrl = dataToSet.getString(ITEM_URL);

        imagev.setImageBitmap(image);
        cityv.setText(city);
        namev.setText(eventName);
        startDatev.setText(startDate);
        ticketPriceMinv.setText(String.valueOf(ticketPriceMin));
        ticketPriceMaxv.setText(String.valueOf(ticketPriceMax));

        Button back = result.findViewById(R.id.back);
        back.setOnClickListener( clk ->
        {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            if(!TicketMaster.isTablet)
            {
                parentActivity.onBackPressed();
            }
        });

        Button urlButton = result.findViewById(R.id.urlButton);
        urlButton.setOnClickListener( clk ->
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parentActivity);
            alertDialogBuilder.setTitle(getResources().getString(R.string.urlChoice))
                    .setPositiveButton(getResources().getString(R.string.yes), (click, arg) ->
                    {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventUrl));
                        startActivity(browserIntent);
                    }).setNegativeButton("No", (click, arg) -> {  }).create().show();
        });
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //context will either be FragmentExample for a tablet, or EmptyActivity for phone
        parentActivity = (AppCompatActivity)context;
    }
}
