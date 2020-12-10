package com.example.finalProject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

/**
 * A fragment class that shows the details of the recipes from the listview and allows the user to
 * save the recipe or to look at the recipe in a browser
 * @author Kasia Kuzma
 *  * @version 1.0
 *  * Course CST2335
 *  * Lab Section 021
 *  * RecipeSearchPage Class
 */
public class RecipeFragmentDetails extends Fragment {

    /**
     * The parent activity of this fragment, that being the recipe search page
     */
    private AppCompatActivity parentActivity;
    /**
     * Meant to reference the title column in the database
     */
    public static final String RECIPE_TITLE = "TITLE";
    /**
     * References the href column in the database
     */
    public static final String RECIPE_HREF = "HREF";
    /**
     * References the ingredients column in the database
     */
    public static final String RECIPE_INGREDIENTS = "INGREDIENTS";
    /**
     * The variable to set the recipe title to, retrieved from the database
     */
    String recipeTitle;
    /**
     * The variable to set the recipe ingredients to, retrieved from the database
     */
    String ingredientsList;
    /**
     * The variable to set the recipe href to, retrieved from the database
     */
    String recipeUrl;

    /**
     * Creates a view for the recipe details fragment to show the details of a recipe and allow the
     * user to save the recipe in favourites or to open a browser for the recipe
     * @param inflater inflates the fragment layout
     * @param group ViewGroup
     * @param savedInstanceState saved instance state from the previous bundle
     * @return the new view that is the inflated fragment
     */
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
        Button saveToFavourites = recipeResult.findViewById(R.id.addFavouriteRecipe);
        TextView detailsSnackbar = recipeResult.findViewById(R.id.detailsSnackbar);

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
                startActivity(goToBrowserIntent);
            });
            alertGoToBrowser.setNegativeButton(getResources().getString(R.string.no), (onClick, arg) ->{ });
            alertGoToBrowser.create().show();
        });

        saveToFavourites.setOnClickListener(click -> {
            SQLiteDatabase favouritesDB = RecipeSearchPage.getRecipeDB();
            ContentValues newRecipeValues = new ContentValues();
            newRecipeValues.put(RecipePageOpener.COL_TITLE, recipeTitle);
            newRecipeValues.put(RecipePageOpener.COL_HREF, recipeUrl);
            newRecipeValues.put(RecipePageOpener.COL_INGREDIENTS, ingredientsList);
            favouritesDB.insert(RecipePageOpener.TABLE_NAME, null, newRecipeValues);
            Snackbar.make(detailsSnackbar, R.string.confirmSaveRecipe, Snackbar.LENGTH_SHORT).show();
        });

        return recipeResult;
    }

    /**
     * Attaches a context to the fragment, in this case it's the recipe search page's context
     * @param context the parent activity's context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        parentActivity = (AppCompatActivity) context;
    }
}