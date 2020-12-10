package com.example.finalProject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class EmptyRecipe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_recipe);

        Bundle setData = getIntent().getExtras();

        RecipeFragmentDetails recipeFrag = new RecipeFragmentDetails();
        recipeFrag.setArguments( setData ); //pass data to the the fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.emptyRecipeFragment, recipeFrag).commit();
    }
}