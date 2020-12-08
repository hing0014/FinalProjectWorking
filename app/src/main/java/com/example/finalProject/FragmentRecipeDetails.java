package com.example.finalProject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentRecipeDetails extends Fragment {

    private AppCompatActivity parentActivity;
    public static final String RECIPE_TITLE = "TITLE";
    public static final String RECIPE_HREF = "HREF";
    public static final String RECIPE_INGREDIENTS = "INGREDIENTS";
    public static final String RECIPE_ID = "_id";

    String recipeTitle;
    String ingredientsList;
    String recipeUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle setData = getArguments();
        View recipeResult = inflater.inflate(R.layout.activity_fragment_recipe_details, group, false);

        //ImageView recipeIcon = recipeResult.findViewById(R.id.fragmentRecipeIcon);
        TextView recipeName = recipeResult.findViewById(R.id.recipeTitleFragment);
        TextView recipeIngredients = recipeResult.findViewById(R.id.ingredientsFragment);
        recipeTitle = setData.getString(RECIPE_TITLE);
        ingredientsList = setData.getString(RECIPE_INGREDIENTS);
        recipeUrl = setData.getString(RECIPE_HREF);

        recipeName.setText(recipeTitle);
        recipeIngredients.setText(ingredientsList);
        Button backButton = recipeResult.findViewById(R.id.recipeFragmentBackButton);
        Button openBrowser = recipeResult.findViewById(R.id.goToBrowserButtonFragment);

        backButton.setOnClickListener( click ->
        {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            if(!RecipeSearchPage.isTablet)
            {
                parentActivity.onBackPressed();
            }
        });

        openBrowser.setOnClickListener( click -> {
            AlertDialog.Builder alertGoToBrowser = new AlertDialog.Builder(parentActivity);
            alertGoToBrowser.setTitle(getResources().getString(R.string.alertToRecipeBrowser));
            alertGoToBrowser.setPositiveButton(getResources().getString(R.string.yes), (onClick, arg) ->{
                Intent goToBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipeUrl));
            });
            alertGoToBrowser.setNegativeButton(getResources().getString(R.string.no), (onClick, arg) ->{
            alertGoToBrowser.create().show();
            });
        });
        return recipeResult;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        parentActivity = (AppCompatActivity) context;
    }
}