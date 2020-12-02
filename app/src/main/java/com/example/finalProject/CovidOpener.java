/* Course Name: CST2335_021
 * Class name: CST2335 Graphical Interface Programming
 * Covid19 Case Data
 * Date: November 19, 2020
 * Student Name : Jihyun Park
 * purpose: This is the final project with Teammates
 * This is the SQLite database
 */
package com.example.finalProject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* This is the class of the covidOpener extends by SQliteHelper.
 * This class makes table in order to store information of the Covid cases
 * @author: jihyun Park
 */
public class CovidOpener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "CovidDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "Covid";
    public final static String COL_TITLE = "Covid19Case";
    public final static String COL_ID = "_id";
    public final static String COL_COUNTRY = "Country";
    public final static String COL_CODE = "Code";
    public final static String COL_PROVINCE = "Province";
    public final static String COL_CASES = "Cases";
    public final static String COL_STATUS = "Status";

    /*@param Context ctx
     * @author Jihyun Park
     * this is the inherited constructor
     * */
    public CovidOpener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    /* this is onCreate function for the creating the tavle
     * @param SQLiteDatabase db
     * @ Author: Jihyun Park
     * */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT,"
                + COL_COUNTRY + " TEXT,"
                + COL_CODE + " TEXT,"
                + COL_PROVINCE + " TEXT,"
                + COL_CASES +  " int,"
                + COL_STATUS + " TEXT);");
    }

    /*this is for using upgrading of the table
    * it is Overrided
     * @param SQLiteDatabase db, int oldVersion, int newVersion
     * @ahuthor Jihyun Park
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /*this is for using downgrading of the table
     * it is Overrided
     * @param SQLiteDatabase db, int oldVersion, int newVersion
     * @author Jihyun Park
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
