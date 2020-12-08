package com.example.finalProject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.drm.DrmStore;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is the main activity for the Recipe Search Page, it manipulates the listview via various methods and classes seen below
 * @author Kasia Kuzma
 * @version 1.0
 * Course CST2335
 * Lab Section 021
 * RecipeSearchPage Class
 */
public class RecipeSearchPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     *Where the user will search for recipes
     */
    EditText searchBar;
    /**
     * The listview that will show the recipes that match the value in the edittext
     */
    ListView recipeList;
    /**
     * Will activate the database and asynctask to search for matching recipes
     */
    Button searchRecipes;
    /**
     * Will show the loading progress of the recipe page
     */
    ProgressBar loadingBar;
    /**
     *An array of recipes; where the recipes will be stored once retrieved from database/website
     */
    private ArrayList<RecipeGetters> recipes = new ArrayList<>();
    /**
     * Instance of the adapter class used to inflate the listview with rows of found recipes
     */
    private RecipePageAdapter theAdapter;
    /**
     * Instance of the database class to store and load the database of recipes
     */
    private static SQLiteDatabase recipeDB;
    /**
     * Instance of the AsyncTask class that runs a background thread to retrieve the recipes from the website
     */
    //ViewRecipesFromURL seeRecipes = new ViewRecipesFromURL();
    FrameLayout fragmentFrame;
    FragmentRecipeDetails recipeFrag = null;
    static boolean isTablet = false;
    SharedPreferences recipePrefs;
    int recipesArrayLength;
    long newId = 0;
    String recipeTitle = "title";
    String title;
    String href;
    String ingredients;

    public static final String RECIPE_TITLE = "TITLE";
    public static final String RECIPE_HREF = "HREF";
    public static final String RECIPE_INGREDIENTS = "INGREDIENTS";
    public static final String RECIPE_ID = "_id";


    /**
     * Initializes the variables above, loads the database, and calls click listeners for the search button and listview
     * @param savedInstanceState Pre-made bundle with every activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_search_page);

        searchBar = findViewById(R.id.searchRecipe); //the edit text
        searchRecipes = findViewById(R.id.searchButtonRecipe);
        if(recipes.size() != 0){
            theAdapter.notifyDataSetChanged();
        }

        fragmentFrame = findViewById(R.id.recipeFrame);
        if(fragmentFrame != null){
            isTablet = true;
        }

        Toolbar recipeToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(recipeToolbar);
        DrawerLayout theDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggleDrawer = new ActionBarDrawerToggle(this, theDrawer, recipeToolbar, R.string.open, R.string.close);
        theDrawer.addDrawerListener(toggleDrawer);
        toggleDrawer.syncState();

        loadingBar = findViewById(R.id.recipeProgress);
        loadingBar.setVisibility(View.VISIBLE);
        recipeList = findViewById(R.id.recipesList);

        loadFromDatabase();

        recipePrefs = getSharedPreferences("file", Context.MODE_PRIVATE);

        String preferredRecipe = recipePrefs.getString(recipeTitle, "");
        searchBar.setText(preferredRecipe);

        theAdapter = new RecipePageAdapter();
        recipeList.setAdapter(theAdapter);
        //Add help button
        //Add favourite button
        //Add snackbar

        AtomicReference<ViewRecipesFromURL> recipeUrlQuery = new AtomicReference<>(new ViewRecipesFromURL());
        searchRecipes.setOnClickListener( click -> {
            title = searchBar.getText().toString();

            if(URLUtil.isValidUrl("http://www.recipepuppy.com/api/?q="+ title +"&p=3&format=xml")){
                loadingBar.setProgress((0));
                loadingBar.setVisibility(View.VISIBLE);
                recipes.clear();
                recipeUrlQuery.get().execute("http://www.recipepuppy.com/api/?q="+ title +"&p=3&format=xml");
                recipeUrlQuery.set(new ViewRecipesFromURL());
            }
            else{
                Toast.makeText(getApplicationContext(), "Recipes not found", Toast.LENGTH_SHORT).show();

            }
        });

        recipeList.setOnItemClickListener((parent, view, pos, id) -> {
            Bundle setData = new Bundle();
            RecipeGetters getRecipes = recipes.get(pos);
            setData.putString(RECIPE_TITLE, getRecipes.getTitle());
            setData.putString(RECIPE_HREF, getRecipes.getHrefURL());
            setData.putString(RECIPE_INGREDIENTS, getRecipes.getIngredients());

            if(isTablet){
                recipeFrag = new FragmentRecipeDetails();
                recipeFrag.setArguments(setData);
                getSupportFragmentManager().beginTransaction().replace(R.id.recipeFrame, recipeFrag).commit();
            }
            else{
                Intent phoneFragment = new Intent(RecipeSearchPage.this, EmptyRecipe.class);
                phoneFragment.putExtras(setData);
                startActivity(phoneFragment);
            }
        });

        recipeList.setOnItemLongClickListener( (parent, view, pos, id) -> {
            AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
            deleteBuilder.setTitle(getResources().getString(R.string.deleteRecipe));
            deleteBuilder.setMessage(getResources().getString(R.string.deleteRecipeConfirmation));
            deleteBuilder.setPositiveButton((R.string.yes), (click, arg) -> {
                RecipeGetters selectedRecipe = recipes.get(pos);
                recipeDB.delete(TicketMasterOpener.TABLE_NAME, RecipePageOpener.COL_ID + "= ?", new String[] {Long.toString(selectedRecipe.getRecipeID())});
            });
            deleteBuilder.setNegativeButton(R.string.no, (click, arg) -> { });
            deleteBuilder.create().show();
            return true;
        });

        //ViewRecipesFromURL viewRecipes = new ViewRecipesFromURL();
        //viewRecipes.execute("http://www.recipepuppy.com/api/?i=onions,garlic&q=omelet&p=3&format=xml");//should I split this url?
    }

    /**
     * The Adapter class for the listview in order to inflate the listview with rows from another xml file
     */
    private class RecipePageAdapter extends BaseAdapter{

        /**
         * retrieves the amount of rows present
         * @return the size of the recipe array
         */
        @Override
        public int getCount() {
            if(recipes == null){
                return 0;
            }
            return recipes.size();
        }

        /**
         * retrieves the contents of a specific row, determined by its position
         * @param position row position
         * @return row position
         */
        @Override
        public Object getItem(int position) {
            return position;
        }

        /**
         * meant for using databases for the row contents, also determined by position
         * @param position numbered position of the row in the listview
         * @return row position
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * determines what is to be shown for each row of the listview
         * @param position row position
         * @param view old view
         * @param parent parent of the viewgroup
         * @return the new view that is to be inflated
         */
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            RecipeGetters getRecipe = recipes.get(position);
            LayoutInflater inflater = getLayoutInflater();
            if(view==null) view = inflater.inflate(R.layout.recipe_row_layout, parent, false);
            TextView rowText = view.findViewById(R.id.recipeTextRow);
            rowText.setText(getRecipe.getTitle());
            //ImageView recipeImage = view.findViewById(R.id.recipeIcon);

            return view;
        }
    }

    /**
     * Creates a new instance of the Opener class to retrieve a database to fill it with the contents of the recipes
     */
    private void loadFromDatabase(){
        RecipePageOpener recipeOpener = new RecipePageOpener(this);
        recipeDB = recipeOpener.getWritableDatabase();
        String[] columns = {RecipePageOpener.COL_ID,
                RecipePageOpener.COL_TITLE,
                RecipePageOpener.COL_HREF,
                RecipePageOpener.COL_INGREDIENTS} ;
        @SuppressLint("Recycle") Cursor results = recipeDB.query(false, RecipePageOpener.TABLE_NAME, columns, null, null, null, null, null, null);
        int titleColumnIndex = results.getColumnIndex(RecipePageOpener.COL_TITLE);
        int hrefColumnIndex = results.getColumnIndex(RecipePageOpener.COL_HREF);
        int ingredientsColumnIndex = results.getColumnIndex(RecipePageOpener.COL_INGREDIENTS);
        int idColumnIndex = results.getColumnIndex(RecipePageOpener.COL_ID);
        while(results.moveToNext())
        {
            String title = results.getString(titleColumnIndex);
            String href = results.getString(hrefColumnIndex);
            String ingredients = results.getString(ingredientsColumnIndex);
            long id = results.getLong(idColumnIndex);

            recipes.add(new RecipeGetters(title, href, ingredients, id));
        }
        results.close();
    }

    /**
     * Shows an alert dialog that describes the recipe once clicked from the listview, allowing the browser to open or for the recipe to be favourited
     * @param position position of the recipe in the array of recipes
     */
   /* protected void showRecipe(int position){
        RecipeGetters recipe = recipes.get(position);
        //View recipeAlert_view = getLayoutInflater().inflate(R.layout.recipe_alertdialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(recipe.getTitle());
        builder.setMessage(recipe.getHrefURL() +"/n" + recipe.getIngredients());
       // builder.setView(recipeAlert_view);
        builder.setPositiveButton("Favourite", (click, b) -> Toast.makeText(getApplicationContext(),"Recipe has been favourited", Toast.LENGTH_SHORT).show());
        builder.setNegativeButton("Close", (click, b) -> { });
        builder.setNeutralButton("Open in Browser", (click, b) -> {

        });
    } */

    /**
     * Creates a background thread to retrieve external data and store it internally, all while allowing the progress to be tracked
     */
    @SuppressLint("StaticFieldLeak")
    private class ViewRecipesFromURL extends AsyncTask< String, Integer, String> {

        /**
         * Retrieves the recipes from the url and uses an xml parser to read the website's data and initialize the variables below
         * @param strings pass in when the execute method is invoked, in this case it is a url
         * @return a string, in this case a string to indicate the completion of the method
         */
        @Override
        protected String doInBackground(String... strings) {
            String title = null;
            String href = null;
            String ingredients = null;
            try {
                String encode = URLEncoder.encode(strings[0], "UTF-8"); //fix
                URL url = new URL(encode);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput( response  , "UTF-8");

                int eventType = xpp.getEventType(); //The parser is currently at START_DOCUMENT
                publishProgress(50);
                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG){
                        if(xpp.getName().equals("title")){
                            xpp.next();
                            title = xpp.getText();
                        }
                        else if (xpp.getName().equals("href")){
                            xpp.next();
                            href = xpp.getText();
                        }
                        else if (xpp.getName().equals("ingredients")){
                            xpp.next();
                            ingredients = xpp.getText();
                        }
                    }
                    eventType = xpp.next();
                }
                ContentValues newRows = new ContentValues();
                newRows.put(RecipePageOpener.COL_TITLE, title);
                newRows.put(RecipePageOpener.COL_HREF, href);
                newRows.put(RecipePageOpener.COL_INGREDIENTS, ingredients);
                long newRowID = recipeDB.insert(TicketMasterOpener.TABLE_NAME, null, newRows);
                recipes.add(new RecipeGetters(title, href, ingredients, newRowID));

            }catch (Exception ignored){

            }
            publishProgress(100);
            return "Done";
        }

        /**
         * Allows for actions to be completed once the progress of the thread has been updated
         * @param args array of integers
         */
        public void onProgressUpdate(Integer ... args) {
            loadingBar.setProgress(args[0]);
            Log.i("Loading Bar", "Loading..."+args[0]+"% complete");
        }

        /**
         * Allows for actions once the thread has finished
         * @param fromDoInBackground the string originally passed to the doInBackground method, a url in this case
         */
        public void onPostExecute(String fromDoInBackground) {
            Log.i("Loading Bar", "Loading Complete");
            loadingBar.setVisibility(View.INVISIBLE);
            theAdapter.notifyDataSetChanged();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        EditText retrieveRecipeSearch = findViewById(R.id.searchRecipe);
        String retrieveRecipe = retrieveRecipeSearch.getText().toString();
        SharedPreferences.Editor editRecipes = recipePrefs.edit();
        editRecipes.putString(retrieveRecipe, recipeTitle);
        editRecipes.apply();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.recipe_menu, menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent nextActivity;
        switch(item.getItemId()){
            case R.id.homeItem:
                finish();
                break;
            case R.id.recipeItem:
                nextActivity = new Intent(RecipeSearchPage.this, RecipeSearchPage.class);
                startActivity(nextActivity);
                break;
            case R.id.helpItem:
                AlertDialog.Builder helpDialog = new AlertDialog.Builder(this);
                helpDialog.setTitle(R.string.helpTitle);
                helpDialog.setMessage(R.string.helpMessage);
                helpDialog.create().show();
                helpDialog.setNeutralButton(R.string.helpOk, (click, arg)->{ });
                break;
            case R.id.ticketItem:
                nextActivity = new Intent(RecipeSearchPage.this, TicketMaster.class);
                startActivity(nextActivity);
                break;
            case R.id.covidItem:
                nextActivity = new Intent(RecipeSearchPage.this, Covid.class);
                startActivity(nextActivity);
                break;
            case R.id.audioItem:
                nextActivity = new Intent(RecipeSearchPage.this, AudioActivity.class);
                startActivity(nextActivity);
                break;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){

        Intent nextPage;
        switch(item.getItemId()){
            case R.id.homeItem:
                finish();
                break;
            case R.id.recipeItem:
                nextPage = new Intent(RecipeSearchPage.this, RecipeSearchPage.class);
                startActivity(nextPage);
                break;
            case R.id.helpItem:
                AlertDialog.Builder helpDialog = new AlertDialog.Builder(this);
                helpDialog.setTitle(R.string.helpTitle);
                helpDialog.setMessage(R.string.helpMessage);
                helpDialog.create().show();
                helpDialog.setNeutralButton(R.string.helpOk, (click, arg)->{ });
                break;
            case R.id.ticketItem:
                nextPage = new Intent(RecipeSearchPage.this, TicketMaster.class);
                startActivity(nextPage);
                break;
            case R.id.covidItem:
                nextPage = new Intent(RecipeSearchPage.this, Covid.class);
                startActivity(nextPage);
                break;
            case R.id.audioItem:
                nextPage = new Intent(RecipeSearchPage.this, AudioActivity.class);
                startActivity(nextPage);
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}