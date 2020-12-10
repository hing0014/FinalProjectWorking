package com.example.finalProject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class is meant to be a database for the recipes, setting up the structure for the database to be used later
 * @author Kasia Kuzma
 * @version 1.0
 * Course CST2335
 * Lab Section 021
 * RecipeSearchPage Class
 */
public class RecipePageOpener extends SQLiteOpenHelper{

    /**
     * The name of the database
     */
    protected final static String DATABASE_NAME = "RecipesDB";
    /**
     * The version number of the database, increasing or decreasing the version number calls an upgrade
     * or downgrade method to change the database
     */
    protected final static int VERSION_NUM = 1;
    /**
     * The table name for the database to create and reference
     */
    public final static String TABLE_NAME = "RECIPES";
    /**
     * The title column stores the name of the recipe in the title column
     */
    public final static String COL_TITLE = "TITLE";
    /**
     * The Href column stores the recipe's url
     */
    public final static String COL_HREF = "HREF";
    /**
     * The string of ingredients stored in the Ingredients column
     */
    public final static String COL_INGREDIENTS = "INGREDIENTS";
    /**
     * The row id of the recipe in the database
     */
    public final static String COL_ID = "_id";

    /**
     * Creating a new instance of the database
     * @param ctx context of the database
     */
    public RecipePageOpener(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    /**
     * Creates the database and sets the attribute types
     * @param db the database to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_HREF + " text,"
                + COL_INGREDIENTS + " text);");
    }

    /**
     * triggers if changes are made to the structure of the database, specifically when the version number increases
     * @param db database in question
     * @param oldVersion old version number of the database
     * @param newVersion new version number of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * triggers if the version number decreases and downgrades the database structure according to the changes
     * @param db database in question
     * @param oldVersion old version of the database
     * @param newVersion new version of the database, which is less than the old version
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
