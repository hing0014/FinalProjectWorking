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
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;
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
    private final ArrayList<RecipeGetters> recipes = new ArrayList<>();
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
     * This uses AtomicReference which
     */
    AtomicReference<ViewRecipesFromURL> recipeUrlQuery;
    /**
     * New instance of the frame layout for the tablet
     */
    FrameLayout frame;
    /**
     * New instance of the recipe fragment details
     */
    RecipeFragmentDetails recipeFrag = null;
    /**
     * Boolean to help check whether the emulator is a tablet or not
     */
    static boolean isTablet = false;
    /**
     * The shared preferences variable to store the recipe titles in
     */
    SharedPreferences recipePrefs;
    /**
     * Allows for new row ids for the different recipes to be created and tracked
     */
    long newRecipeId = 0;
    /**
     * Meant to store the titles of the saved recipes on shared prefs
     */
    String recipeTitle = "title";
    /**
     * Meant to store the title of the recipe from the xml file
     */
    String title;
    /**
     * Meant to store the url of the xml when read from the parser
     */
    String href;
    /**
     * Meant to store the ingredients of the read and parsed xml recipe file
     */
    String ingredients;
    /**
     * The button to go to the favourite recipes activity
     */
    Button favouriteButton;
    /**
     * The button for the help alert dialog
     */
    Button helpButton;
    /**
     * Meant to retrieve the title column in the database
     */
    public static final String RECIPE_TITLE = "TITLE";
    /**
     * Meant to retrieve the href column in the database
     */
    public static final String RECIPE_HREF = "HREF";
    /**
     * Meant to retrieve the ingredients column in the database
     */
    public static final String RECIPE_INGREDIENTS = "INGREDIENTS";


    /**
     * Initializes the variables above, loads the database, and calls click listeners for the search
     * button, shows a dialog for help, retrieves saved facourites and loads a listview of recipes
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

//        fragmentFrame = findViewById(R.id.recipeFrame);
//        isTablet = findViewById(R.id.fragmentLocation) != null;
        frame = findViewById(R.id.frame);
        if(frame != null)isTablet = true;

        Toolbar recipeToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(recipeToolbar);
        DrawerLayout theDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggleDrawer = new ActionBarDrawerToggle(this, theDrawer, recipeToolbar, R.string.open, R.string.close);
        theDrawer.addDrawerListener(toggleDrawer);
        toggleDrawer.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        recipeUrlQuery = new AtomicReference<>(new ViewRecipesFromURL());
        searchRecipes.setOnClickListener( click -> {
            title = searchBar.getText().toString();

            if(URLUtil.isValidUrl("http://www.recipepuppy.com/api/?q="+ title +"&p=3&format=xml")){
                loadingBar.setProgress((0));
                loadingBar.setVisibility(View.VISIBLE);
                recipes.clear();
                recipeUrlQuery.get().execute("http://www.recipepuppy.com/api/?q="+ title +"&p=3&format=xml");
                recipeUrlQuery.set(new ViewRecipesFromURL());
                Toast.makeText(this, R.string.recipesFound, Toast.LENGTH_SHORT).show();
            }

        });

        recipeList.setOnItemClickListener((parent, view, pos, id) -> {
            Bundle setData = new Bundle();
            RecipeGetters getRecipes = recipes.get(pos);
            setData.putString(RECIPE_TITLE, getRecipes.getTitle());
            setData.putString(RECIPE_HREF, getRecipes.getHrefURL());
            setData.putString(RECIPE_INGREDIENTS, getRecipes.getIngredients());

            if(isTablet){
                recipeFrag = new RecipeFragmentDetails();
                recipeFrag.setArguments(setData);
                getSupportFragmentManager().beginTransaction().replace(R.id.recipeFrame, recipeFrag).commit();
                theAdapter.notifyDataSetChanged();
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
            deleteBuilder.setMessage(getResources().getString(R.string.confirmDeleteRecipe));
            deleteBuilder.setPositiveButton((R.string.yes), (click, arg) -> {
                RecipeGetters selectedRecipe = recipes.get(pos);
                recipeDB.delete(RecipePageOpener.TABLE_NAME, RecipePageOpener.COL_ID + "= ?", new String[] {Long.toString(selectedRecipe.getRecipeID())});
                theAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), R.string.okRecipeDeleted, Toast.LENGTH_SHORT).show();
            });
            deleteBuilder.setNegativeButton(R.string.no, (click, arg) -> { });
            deleteBuilder.create().show();
            return true;
        });

        helpButton = findViewById(R.id.recipeHelp);
        helpButton.setOnClickListener( click -> {
            AlertDialog.Builder helpDialog = new AlertDialog.Builder(this);
            helpDialog.setTitle(R.string.helpTitleRecipe);
            helpDialog.setMessage(R.string.helpMessage);
            helpDialog.create().show();
            helpDialog.setNeutralButton(R.string.helpOk, (clicks, arg)->{ });
        });

        TextView snackBar = findViewById(R.id.snackBarRecipe);
        favouriteButton = findViewById(R.id.recipeFavourite);
        favouriteButton.setOnClickListener(click -> {
            recipes.clear();
            loadFromDatabase();
            theAdapter.notifyDataSetChanged();
                Snackbar.make(snackBar, R.string.retrievedRecipes, Snackbar.LENGTH_SHORT).show();
        });
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
     * A helper method that allows for variables to be saved and stored in shared preferences
     * @param value the value of the key to be saved
     * @param key the name which the value will be found under
     */
    public void saveSharedPrefs(String value, String key){
        SharedPreferences.Editor editRecipes = recipePrefs.edit();
        editRecipes.putString(key, value);
        editRecipes.apply();
        theAdapter.notifyDataSetChanged();
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
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput( response  , "UTF-8");
                int eventType = xpp.getEventType();
                publishProgress(50);
                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG ){
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
                    publishProgress(75);
                    recipes.add(new RecipeGetters(title, href, ingredients, ++newRecipeId));
                }
                publishProgress(100);
            }catch (Exception e){
                Log.e("tag", e.getMessage(), e);
            }
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

    /**
     * What happens to this activity while another activity is being launched, in this case recipe
     * titles are saved as well as what was left in the edit text
     */
    @Override
    protected void onPause() {
        super.onPause();
        EditText retrieveRecipeSearch = findViewById(R.id.searchRecipe);
        String retrieveRecipe = retrieveRecipeSearch.getText().toString();
        saveSharedPrefs(retrieveRecipe, recipeTitle);
    }

    /**
     * Inflates the toolbar created by the example menu
     * @param menu the menu which contains the toolbar
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    /**
     * Determines what each item does, which in this case, these items go to different activites when clicked
     * @param item the different options in the toolbar
     * @return false
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent nextActivity;
        switch(item.getItemId()){
            case R.id.home:
                finish();
                break;
            case (R.id.ticket):
                nextActivity = new Intent(RecipeSearchPage.this, TicketMaster.class);
                startActivity(nextActivity);
                break;

            case (R.id.food):
                nextActivity = new Intent(RecipeSearchPage.this, RecipeSearchPage.class);
                startActivity(nextActivity);
                break;

            case (R.id.audio):
                nextActivity = new Intent(RecipeSearchPage.this, AudioActivity.class);
                startActivity(nextActivity);
                break;

            case (R.id.bacteria):
                nextActivity = new Intent(RecipeSearchPage.this, Covid.class);
                startActivity(nextActivity);
                break;

            case (R.id.search_item):
                break;
        }
        return false;
    }

    /**
     * Determines what happens for each item in the navigation drawer, in this case, it leads to the other activities
     * @param item each option in the navigation drawer
     * @return false
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){

        Intent nextPage;
        switch(item.getItemId()){
            case R.id.home:
                nextPage = new Intent(RecipeSearchPage.this, MainActivity.class);
                startActivity(nextPage);
                break;
            case (R.id.ticket):
                nextPage = new Intent(RecipeSearchPage.this, TicketMaster.class);
                startActivity(nextPage);
                break;

            case (R.id.food):
                nextPage = new Intent(RecipeSearchPage.this, RecipeSearchPage.class);
                startActivity(nextPage);
                break;

            case (R.id.audio):
                nextPage = new Intent(RecipeSearchPage.this, AudioActivity.class);
                startActivity(nextPage);
                break;

            case (R.id.bacteria):
                nextPage = new Intent(RecipeSearchPage.this, Covid.class);
                startActivity(nextPage);
                break;

            case (R.id.search_item):
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * Allows for the database in this activity to be retrieves
     * @return the database full of the favourited recipes
     */
    public static SQLiteDatabase getRecipeDB(){
        return recipeDB;
    }
}