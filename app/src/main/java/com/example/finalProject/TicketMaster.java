package com.example.finalProject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Links between the XML and ticketmaster database.
 *  <p>
 * Course Name: CST8288_010
 * Class name: TicketMaster
 * Date: November 19, 2020
 *
 * @version 1.0
 * @author Chris HIng
 */
public class TicketMaster extends AppCompatActivity
{
    /**
     * Fields for storing the database information for use throughout the class.
     */
    private static SQLiteDatabase dataBase;
    private static SharedPreferences prefs;
    FragmentTicketDetails dFragment = null;
    private ArrayList<TicketEvent> events = new ArrayList<>();
    private TicketMasterListAdapter myAdapter;
    FrameLayout frame;
    private ProgressBar theBar;
    String city;
    String cityKey = "city";
    String radiusKey = "radius";
    String eventName;
    String startDate;
    double ticketPriceMin;
    double ticketPriceMax;
    String eventUrl;
    String imageName;
    String radius;
    String imageString;
    int eventArrayLength;
    Bitmap image;
    static boolean dataNotFound = false;
    public static boolean isTablet = false;
    long newId = 0;

    public final static String ITEM_CITY = "CITY";
    public final static String ITEM_NAME = "EVENT NAME";
    public final static String ITEM_START_DATE = "START DATE";
    public final static String ITEM_MIN_PRICE = "MIN PRICE";
    public final static String ITEM_MAX_PRICE = "MAX PRICE";
    public final static String ITEM_URL = "URL";
    public final static String ITEM_IMAGE_STRING = "IMAGE";
    public final static String ITEM_ID = "_id";

    /**
     * Creates and manages the click listeners of the button.
     * <p>
     * When the search button is clicked the editText fields stored data is passed to the TicketMasterQuery in the form of the data source URL.
     * That information is then displayed on screen.
     *
     * @param savedInstanceState Bundle object used in the super call of onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_master);

        frame = findViewById(R.id.frame);
        if(frame != null)isTablet = true;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        theBar = findViewById(R.id.loadBar);
        theBar.setVisibility(View.INVISIBLE);
        ListView myList = findViewById(R.id.theListView);
        loadDataFromDatabase();

        EditText cityText = findViewById(R.id.citySearch);
        prefs = getSharedPreferences("file", Context.MODE_PRIVATE);
        String prefCity = prefs.getString(cityKey, "");
        cityText.setText(prefCity);

        EditText radiusText =  findViewById(R.id.radius);
        prefs = getSharedPreferences("file", Context.MODE_PRIVATE);
        String radText = prefs.getString(radiusKey, "");
        radiusText.setText(radText);

        myList.setAdapter(myAdapter = new TicketMasterListAdapter());
        if(events.size() != 0)
        {
            myAdapter.notifyDataSetChanged();
        }

        Button helpButton = findViewById(R.id.help);
        helpButton.setOnClickListener(click ->
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.dialogWrap);
            alertDialogBuilder.setTitle(R.string.ticketMasterHelpTitle)
                    .setPositiveButton(getResources().getString(R.string.ok), (clk, arg) -> { });
            alertDialogBuilder.setMessage(R.string.ticketMasterHelp)
                    .create().show();
        });

        Button favbutton = findViewById(R.id.favorites);
        favbutton.setOnClickListener(click ->
        {
            events.clear();
            loadDataFromDatabase();
            myAdapter.notifyDataSetChanged();
        });

        Button searchButton = findViewById(R.id.searchButton);
        AtomicReference<TicketMasterQuery> tickQuer = new AtomicReference<>(new TicketMasterQuery());
        searchButton.setOnClickListener(click ->
        {
            city = cityText.getText().toString();
            radius = radiusText.getText().toString();
            boolean isInt = true;
            try {
                int num = Integer.parseInt(radius);
            } catch (NumberFormatException e) {
                isInt = false;
            }
            if(isInt)
            {
                if(URLUtil.isValidUrl("https://app.ticketmaster.com/discovery/v2/events.json?apikey=9xSSOAi25vaqiTP1UGfMa1fxycNnJPpd&city=" + city + "&radius=" + radius))
                {
                    theBar.setProgress(0);
                    theBar.setVisibility(View.VISIBLE);
                    events.clear();
                    tickQuer.get().execute("https://app.ticketmaster.com/discovery/v2/events.json?apikey=9xSSOAi25vaqiTP1UGfMa1fxycNnJPpd&city=" + city + "&radius=" + radius, city);
                    tickQuer.set(new TicketMasterQuery());
                }
            }
            else Snackbar.make(click, R.string.InvalidRadious,Snackbar.LENGTH_SHORT).show();
        });

        myList.setOnItemClickListener( (parent, view, pos, id) -> {

            Bundle dataToPass = new Bundle();
            TicketEvent temp = events.get(pos);
            dataToPass.putString(ITEM_CITY, temp.getCity() );
            dataToPass.putString(ITEM_NAME, temp.getEventName() );
            dataToPass.putString(ITEM_START_DATE, temp.getStartDate() );
            dataToPass.putDouble(ITEM_MIN_PRICE, temp.getTicketPriceMin() );
            dataToPass.putDouble(ITEM_MAX_PRICE, temp.getTicketPriceMax() );
            dataToPass.putString(ITEM_URL, temp.getUrl() );
            dataToPass.putString(ITEM_IMAGE_STRING, encodeTobase64(temp.getImage()));

            if(isTablet)
            {
                dFragment = new FragmentTicketDetails(); //add a DetailFragment
                dFragment.setArguments( dataToPass ); //pass it a bundle for information
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, dFragment) //Add the fragment in FrameLayout
                        .commit(); //actually load the fragment. Calls onCreate() in DetailFragment
            }
            else //isPhone
            {
                Intent nextActivity = new Intent(TicketMaster.this, EmptyTicket.class);
                nextActivity.putExtras(dataToPass); //send data to next activity
                startActivity(nextActivity); //make the transition
            }
        });

        myList.setOnItemLongClickListener( (parent, view, pos, id) -> {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.attention)).setMessage(R.string.deletSure)
                    .setPositiveButton("Yes", (click, arg) ->
                    {
                        TicketEvent selectedEvent = events.get(pos);
                        deleteEvent(selectedEvent);
                        events.remove(pos);
                        if(isTablet)
                        {
                            frame.removeAllViews();
                        }
                        myAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (click, arg) -> {  })
                    .create().show();
            return true;
        });
    }

    /**
     * Handles the link to the data and creation fo the database.
     *  <p>
     * Course Name: CST8288_010
     * Class name: TicketMasterQuery
     * Date: November 19, 2020
     *
     * @version 1.0
     * @author Chris HIng
     */
    @SuppressLint("StaticFieldLeak")
    private class TicketMasterQuery extends AsyncTask<String, Integer, String>
    {
        String city;

        /*
         * Real Red (Mar 3 '09 at 9:37). Get file name from URL [Webpage]. Retrieved from
         * https://stackoverflow.com/questions/605696/get-file-name-from-url
         */
        /**
         * Gathers the TicketMaster data and builds the database.
         * <p>
         * Connects to the passed in URL.
         * Converts websites JSON file to a string.
         * Extracts the required information out of the string and into and array of TicketEvent objects.
         *
         * @param debates An array of Strings, the first string being the TicketMaster URL, and the second being the city name.
         */
        @Override
        protected String doInBackground(String... debates)
        {
            try
            {
                image = null;
                city = debates[1];
                URL url = new URL(debates[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
                String result = sb.toString();
                JSONObject jObject = new JSONObject(result);
                JSONObject testLinks = jObject.getJSONObject("page");
                if(testLinks.getInt("totalElements") == 0)
                {
                    dataNotFound = true;
                    return "City Not Found";
                }
                JSONObject jObjEmbed = jObject.getJSONObject("_embedded");
                JSONArray jsonEventArray = jObjEmbed.getJSONArray("events");
                eventArrayLength = jsonEventArray.length();
                for(int i = 0; i < eventArrayLength; i++)
                {
                    JSONObject jsonEvent = jsonEventArray.getJSONObject(i);
                    eventName= jsonEvent.getString("name");

                    JSONObject jsonEventDates = jsonEvent.getJSONObject("dates");
                    JSONObject jsonEventStart = jsonEventDates.getJSONObject("start");
                    startDate = jsonEventStart.getString("localDate");

                    JSONArray jsonEventPriceRanges = jsonEvent.getJSONArray("priceRanges");
                    JSONObject jsonPricees = jsonEventPriceRanges.getJSONObject(0);
                    ticketPriceMin = jsonPricees.getDouble("min");
                    ticketPriceMax = jsonPricees.getDouble("max");

                    eventUrl = jsonEvent.getString("url");

                    String imageURLString = "";
                    JSONArray jsonImages = jsonEvent.getJSONArray("images");
                    for(int ii = 0; ii < jsonImages.length(); ii++)
                    {
                        JSONObject jasonImageDetails = jsonImages.getJSONObject(ii);

                        if(jasonImageDetails.getString("ratio").equals("4_3"))
                        {
                            imageURLString = jasonImageDetails.getString("url");
                            break;
                        }
                    }

                    if(!(imageURLString.equals("")))
                    {
                        URL imageUrlObject = new URL(imageURLString);
                        HttpURLConnection connection = (HttpURLConnection) imageUrlObject.openConnection();
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200)
                        {
                            image = BitmapFactory.decodeStream(connection.getInputStream());
                        }

                        imageName = imageURLString.substring( imageURLString.lastIndexOf('/')+1);

                        FileOutputStream outputStream = openFileOutput( imageName, Context.MODE_PRIVATE);

                        image.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        FileInputStream fis = null;
                        try {    fis = openFileInput(String.valueOf(imageName));   }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        Bitmap bm = BitmapFactory.decodeStream(fis);
                    }
                    ContentValues newRowValues = new ContentValues();
                    newRowValues.put(TicketMasterOpener.COL_CITY, city);
                    newRowValues.put(TicketMasterOpener.COL_EVENT_NAME, eventName);
                    newRowValues.put(TicketMasterOpener.COL_START_DATE, startDate);
                    newRowValues.put(TicketMasterOpener.COL_MIN_PRICE, ticketPriceMin);
                    newRowValues.put(TicketMasterOpener.COL_MAX_PRICE, ticketPriceMax);
                    imageString = encodeTobase64(image);
                    newRowValues.put(TicketMasterOpener.COL_IMAGE_STRING, imageString);
                    newRowValues.put(TicketMasterOpener.COL_URL, eventUrl);

                    events.add(new TicketEvent(city, eventName, startDate, ticketPriceMin, ticketPriceMax, eventUrl, image, ++newId));
                    Log.i("Event Created", "Event name: " + eventName);
                    int inpars = ((i+1)*100)/eventArrayLength;
                    publishProgress(inpars);
                }
                newId = 0;
            }
            catch (Exception ignored)
            {

            }
            publishProgress(100);
            return "Completed Success";
        }

        /**
         * Gathers the TicketMaster data and builds the database.
         * <p>
         * Connects to the passed in URL.
         * Converts websites JSON file to a string.
         * Extracts the required information out of the string and into and array of TicketEvent objects.
         *
         * @param value An array of Integers used to store in the log and update the on screen progress bar.
         */
        public void onProgressUpdate(Integer...value)
        {
            theBar.setProgress(value[0]);
            Log.i("Progress", "Progress is :" + value[0] + "%");
        }

        /**
         * Runs after the database and ArrayList is populated.
         * <p>
         * Sets the progress bar to INVISIBLE.
         * updates the screen.
         *
         * @param fromDoInBackground Final string returned from doInBackground.
         */
        public void onPostExecute(String fromDoInBackground)
        {
            if(dataNotFound)
            {
                Toast.makeText(getApplicationContext(),R.string.datNotFound, Toast.LENGTH_SHORT).show();
                dataNotFound = false;
            }
            theBar.setVisibility(View.INVISIBLE);
            myAdapter.notifyDataSetChanged();
            Log.i("Finalized", fromDoInBackground);
        }

    }

    /**
     * Adds the rows to the screen.
     *  <p>
     * Course Name: CST8288_010
     * Class name: TicketMasterListAdapter
     * Date: November 19, 2020
     *
     * @version 1.0
     * @author Chris HIng
     */
    private class TicketMasterListAdapter extends BaseAdapter
    {
        /**
         * Returns the size of the events ArrayList.
         * <p>
         * If events is null, return 0.
         * Else return the size of events.
         */
        public int getCount()
        {
            if(events == null) return 0;
            return events.size();
        }
        /**
         * Returns the Object position of the passed in int position.
         * <p>
         * Converts the int to an object.
         */
        public Object getItem(int position){return position;}
        /**
         * Returns the long position of the passed in int position.
         * <p>
         * Converts the int to a long.
         */
        public long getItemId(int position) { return position; }
        /**
         * Returns the view to be added into the ListView.
         * <p>
         * Adds the data into each view, then passes the view to be added to the ListView.
         */
        public View getView(int position, View view, ViewGroup parent)
        {
            TicketEvent arEl = events.get(position);
            LayoutInflater inflater = getLayoutInflater();
            if(view == null) view = inflater.inflate(R.layout.row_ticket_master_event, parent, false);

            TextView messageText = view.findViewById(R.id.eventRowName);
            messageText.setText(arEl.getEventName());

            ImageView gotImage = view.findViewById(R.id.eventImage);
            gotImage.setImageBitmap(arEl.getImage());

            return view;
        }
    }
    /**
     * Loads Items from the database into the ArrayList.
     * <p>
     * Pull each line out of the database and build a new TicketEvent based on the data.
     * Adds the new TicketEvent to the ArrayList of TicketEvents.
     *
     */
    private void loadDataFromDatabase()
    {
        TicketMasterOpener dbOpener = new TicketMasterOpener(this);
        dataBase = dbOpener.getWritableDatabase();
        String [] columns = {
                TicketMasterOpener.COL_ID,
                TicketMasterOpener.COL_CITY,
                TicketMasterOpener.COL_EVENT_NAME,
                TicketMasterOpener.COL_START_DATE,
                TicketMasterOpener.COL_MIN_PRICE,
                TicketMasterOpener.COL_MAX_PRICE,
                TicketMasterOpener.COL_IMAGE_STRING,
                TicketMasterOpener.COL_URL};
        Cursor results = dataBase.query(false, TicketMasterOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        int cityColumnIndex = results.getColumnIndex(TicketMasterOpener.COL_CITY);
        int eventNameColIndex = results.getColumnIndex(TicketMasterOpener.COL_EVENT_NAME);
        int startDateColIndex = results.getColumnIndex(TicketMasterOpener.COL_START_DATE);
        int minPriceColIndex = results.getColumnIndex(TicketMasterOpener.COL_MIN_PRICE);
        int maxPriceColIndex = results.getColumnIndex(TicketMasterOpener.COL_MAX_PRICE);
        int urlColIndex = results.getColumnIndex(TicketMasterOpener.COL_URL);
        int imageStringColIndex = results.getColumnIndex(TicketMasterOpener.COL_IMAGE_STRING);
        int idColIndex = results.getColumnIndex(TicketMasterOpener.COL_ID);
        while(results.moveToNext())
        {
            String city = results.getString(cityColumnIndex);
            String eventName = results.getString(eventNameColIndex);
            String startDate = results.getString(startDateColIndex);
            double minPrice = Double.parseDouble(results.getString(minPriceColIndex));
            double maxPrice = Double.parseDouble(results.getString(maxPriceColIndex));
            String url = results.getString(urlColIndex);
            Bitmap imageDecoded = decodeBase64(results.getString(imageStringColIndex));
            long id = results.getLong(idColIndex);
            events.add(new TicketEvent(city, eventName, startDate, minPrice, maxPrice, url, imageDecoded, id));
        }
        results.close();
    }
    /**
     * Removes an item from the database.
     * <p>
     * Removes the passed in ticket event item from the database.
     *
     * @param c TicketEvent object to be removed from the database.
     */
    protected void deleteEvent(TicketEvent c)
    {
        dataBase.delete(TicketMasterOpener.TABLE_NAME, TicketMasterOpener.COL_ID + "= ?", new String[] {Long.toString(c.getId())});
    }
    /**
     * TicketEvent holds the data for a single TicketMaster event.
     *  <p>
     * Course Name: CST8288_010
     * Class name: TicketEvent
     * Date: November 25, 2020
     *
     * @version 1.0
     * @author Chris HIng
     */
    private static class TicketEvent
    {
        String city;
        String eventName;
        String startDate;
        double ticketPriceMin;
        double ticketPriceMax;
        String url;
        long index;
        Bitmap image;
        /**
         * Constructor for the TicketEvent.
         * <p>
         * uses the passed in values to instantiate a new TicketEvent object.
         */
        private TicketEvent(String city, String eventName, String startDate, double ticketPriceMin, double ticketPriceMax, String url, Bitmap image, long index)
        {
            this.city = city;
            this.eventName = eventName;
            this.startDate = startDate;
            this.ticketPriceMin = ticketPriceMin;
            this.ticketPriceMax = ticketPriceMax;
            this.url = url;
            this.index = index;
            this.image = image;
        }
        /**
         * Returns the city name.
         * <p>
         * Returns the city name.
         */
        public String getCity()
        {
            return city;
        }
        /**
         * Returns the event Name.
         * <p>
         * Returns the event Name.
         */
        public String getEventName()
        {
            return eventName;
        }
        /**
         * Returns the start date.
         * <p>
         * Returns the start date.
         */
        public String getStartDate(){ return startDate; }
        /**
         * Returns the minimum ticket price.
         * <p>
         * Returns the minimum ticket price.
         */
        public double getTicketPriceMin(){ return ticketPriceMin; }
        /**
         * Returns the maximum ticket price.
         * <p>
         * Returns the maximum ticket price.
         */
        public double getTicketPriceMax(){ return ticketPriceMax; }
        /**
         * Returns the url.
         * <p>
         * Returns the url.
         */
        public String getUrl(){ return url; }
        /**
         * Returns the image.
         * <p>
         * Returns the image.
         */
        public Bitmap getImage() {return image;}
        /**
         * Returns the index.
         * <p>
         * Returns the index.
         */
        public long getId() { return index; }
    }

    /*
     * Amol Suryawanshi (Apr 22 '16 at 10:28). converting Java bitmap to byte array [Webpage]. Retrieved from
     * https://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
     */
    /**
     * Converts the passed in Bitmap to a String.
     * <p>
     * Converts the passed in Bitmap to a String.
     * @param image Bitmap image to be converted.
     */
    public static String encodeTobase64(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /*
     * Amol Suryawanshi (Apr 22 '16 at 10:28). converting Java bitmap to byte array [Webpage]. Retrieved from
     * https://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
     */
    /**
     * Converts the passed in String to a Bitmap.
     * <p>
     * Converts the passed in String to a Bitmap.
     * @param input String image to be converted.
     */
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
    /**
     * Gets the EditText data from the screen and calls saveSharedPrefs.
     * <p>
     * Gets the EditText data from the screen and calls saveSharedPrefs.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        EditText citTex =  findViewById(R.id.citySearch);
        String cit = citTex.getText().toString();
        saveSharedPrefs(cit, cityKey);

        EditText radTex =   findViewById(R.id.radius);
        String rad = radTex.getText().toString();
        saveSharedPrefs(rad, radiusKey);
    }
    /**
     * Saves the String into SharedPreferences
     * <p>
     * Saves the String into SharedPreferences
     * @param stringToSave The string to be saved into SharedPreferences.
     * @param key The key to wrap the stringToSave.
     */
    public void saveSharedPrefs(String stringToSave, String key)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, stringToSave);
        editor.apply();
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
                pageChange = new Intent(TicketMaster.this, TicketMaster.class);
                startActivity(pageChange);
                break;
            case R.id.food:
                pageChange = new Intent(TicketMaster.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;
            case R.id.audio:
                pageChange = new Intent(TicketMaster.this, AudioActivity.class);
                startActivity(pageChange);
                break;
            case R.id.bacteria:
                pageChange = new Intent(TicketMaster.this, Covid.class);
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
    // Needed for the OnNavigationItemSelected interface:
    @SuppressLint("NonConstantResourceId")

    public boolean onNavigationItemSelected( MenuItem item) {

        Intent pageChange;
        switch(item.getItemId())
        {
            case R.id.home:
                finish();
                break;
            case R.id.ticket:
                pageChange = new Intent(TicketMaster.this, TicketMaster.class);
                startActivity(pageChange);
                break;
            case R.id.food:
                pageChange = new Intent(TicketMaster.this, RecipeSearchPage.class);
                startActivity(pageChange);
                break;
            case R.id.audio:
                pageChange = new Intent(TicketMaster.this, AudioActivity.class);
                startActivity(pageChange);
                break;
            case R.id.bacteria:
                pageChange = new Intent(TicketMaster.this, Covid.class);
                startActivity(pageChange);
                break;
            case R.id.search_item:
                break;
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
    /**
     * Get the Database.
     * <p>
     * Get the Database.
     */
    public static SQLiteDatabase getDatabase(){return dataBase;}

    /**
     * Get the SharedPreferences.
     * <p>
     * Get the SharedPreferences.
     */
    public static SharedPreferences getSharedPreferences(){return prefs;}
}
