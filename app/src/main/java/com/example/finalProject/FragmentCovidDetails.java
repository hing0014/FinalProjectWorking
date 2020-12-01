package com.example.finalProject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FragmentCovidDetails extends Fragment {

    Bundle dataToPass;
    AppCompatActivity parentActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity=(AppCompatActivity) context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dataToPass = getArguments(); // bundle back

        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.activity_covid_details, container, false);

        TextView country = (TextView) result.findViewById(R.id.country);
        country.setText("Country : " + (dataToPass.getString(Covid.ITEM_COUNTRY)));

        TextView conCode = (TextView) result.findViewById(R.id.conCode);
        conCode.setText("Country Code : " + (dataToPass.getString(Covid.ITEM_CONCODE)));

        TextView province = (TextView) result.findViewById(R.id.province);
        province.setText("Province : " + (dataToPass.getString(Covid.ITEM_PROVINCE)));

        TextView cases = (TextView) result.findViewById(R.id.cases);
        cases.setText("Cases : " + (dataToPass.getString(Covid.ITEM_CASE)));

        TextView status = (TextView) result.findViewById(R.id.status);
        status.setText("Status : " + (dataToPass.getString(Covid.ITEM_STATUS)));


//        Button back = result.findViewById(R.id.back);
//        back.setOnClickListener( clk ->
//        {
//            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
//            if(!TicketMaster.isTablet)
//            {
//                parentActivity.onBackPressed();
//            }
//            else{
//                EmptyCovid parent = (EmptyCovid) getActivity();
//                Intent backToCovid = new Intent();
//                parent.setResult(Activity.RESULT_OK, backToCovid); //send data back to FragmentExample in onActivityResult()
//            }
//
//        });
         return result;
    }


}

