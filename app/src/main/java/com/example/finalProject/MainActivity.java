package com.example.finalProject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button covid = (Button) findViewById(R.id.covid);
        covid.setOnClickListener( (click) ->
        {
            Intent covidActivity = new Intent(MainActivity.this, Covid.class);
            startActivity(covidActivity);
        });

        Button ticketMaster = (Button) findViewById(R.id.ticketMaster);
        ticketMaster.setOnClickListener( (clicker) ->
        {
            Intent ticketMasterActivity = new Intent(MainActivity.this, TicketMaster.class);
            startActivity(ticketMasterActivity);
        });

        Button recipe = (Button) findViewById(R.id.recipe);
        recipe.setOnClickListener( (clicker) ->
        {
            Intent recipeActivity = new Intent(MainActivity.this, RecipeSearchPage.class);
            startActivity(recipeActivity);
        });

        Button audio = (Button) findViewById(R.id.audioData);
        audio.setOnClickListener( (clicker) ->
        {
            Intent audioActivity = new Intent(MainActivity.this, AudioActivity.class);
            startActivity(audioActivity);
        });
    }
    /**
     * Inflate the menu items for use in the action bar
     * <p>
     * Manages the search function in the action bar.
     * @param menu The menu used in the action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        /* slide 15 material:*/
        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView sView = (SearchView)searchItem.getActionView();
        sView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });

        return true;
    }
    /**
     * Navigates to the selected activity.
     * <p>
     * Based on the menu item click.
     * @param item The menu used in the action bar.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent pageChange;
        switch(item.getItemId())
        {
            case R.id.home:
                finish();
                break;
            case R.id.ticket:
                pageChange = new Intent(MainActivity.this, TicketMaster.class);
                startActivity(pageChange);
                break;
            case R.id.food:
                pageChange = new Intent(MainActivity.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;
            case R.id.audio:
                pageChange = new Intent(MainActivity.this, AudioActivity.class);
                startActivity(pageChange);
                break;
            case R.id.bacteria:
                pageChange = new Intent(MainActivity.this, Covid.class);
                startActivity(pageChange);
                break;
            case R.id.search_item:


                break;
        }
        return false;
    }
    /**
     * Navigates to the selected activity.
     * <p>
     * Based on the menu item click.
     * @param item The menu used in the action bar.
     */

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        Intent pageChange;
        switch(item.getItemId())
        {
            case R.id.home:
                finish();
                break;
            case R.id.ticket:
                pageChange = new Intent(MainActivity.this, TicketMaster.class);
                startActivity(pageChange);
                break;
            case R.id.food:
                pageChange = new Intent(MainActivity.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;
            case R.id.audio:
                pageChange = new Intent(MainActivity.this, AudioActivity.class);
                startActivity(pageChange);
                break;
            case R.id.bacteria:
                pageChange = new Intent(MainActivity.this, Covid.class);
                startActivity(pageChange);
                break;
            case R.id.search_item:
                break;
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
}
