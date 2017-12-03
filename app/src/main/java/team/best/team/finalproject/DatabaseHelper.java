package team.best.team.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joel on 27/11/2017.
 * <p>
 * When adding a new table, please follow similar style to the way Thermostat is used
 * ie New tables need constants for table name, columns, and array of columns
 * New tables need to be added to onCreate()
 * New tables need a public get___DataFromDB, add___DataToDB, ?remove___DataToDB
 * This could become a superclass/interface then ThermostatDatabaseHelper would implement DatabaseHelper
 * I think keeping it as 1 class works since we'll only be using 1 static instance of the database throughout the app
 * ie Main Activity instantiates a new DatabaseHelper and it is used everywhere.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static SQLiteDatabase database;
    
    private static final String ACTIVITY_NAME = "DatabaseHelper";
    
    private static final String DATABASE_NAME = "BEST_DATABASE.db";
    private static final int VERSION_NUM = 3;
    
    private static final String KEY_ID = "_ID"; // _ID is used by all tables
    
    private static final String THERMOSTAT_TABLE_NAME = "THERMOSTAT_TABLE";
    private static final String KEY_THERMOSTAT_DAY = "DAY";
    private static final String KEY_THERMOSTAT_TIME = "HOUR";
    private static final String KEY_THERMOSTAT_TEMPERATURE = "TEMPERATURE";
    private static final String[] THERMOSTAT_COLUMNS = {KEY_THERMOSTAT_DAY, KEY_THERMOSTAT_TIME, KEY_THERMOSTAT_TEMPERATURE}; // columns does not include KEY_ID
    
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUM);
        Log.i(ACTIVITY_NAME, "-- In Constructor");
        database = getWritableDatabase();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(ACTIVITY_NAME, "-- In onCreate(), creating a database");
        
        // CREATE TABLE THERMOSTAT_TABLE (_ID INTEGER PK AUTO, DAY TEXT, HOUR TEXT, MINUTE TEXT, TEMPERATURE TEXT);
        db.execSQL("CREATE TABLE " + THERMOSTAT_TABLE_NAME
                + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_THERMOSTAT_DAY + " TEXT, "
                + KEY_THERMOSTAT_TIME + " TEXT, "
                + KEY_THERMOSTAT_TEMPERATURE + " TEXT"
                + " );");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        Log.i(ACTIVITY_NAME, "-- In onUpgrade(), upgrading a database");
        
        // DROP TABLE IF EXISTS THERMOSTAT_TABLE
        db.execSQL("DROP TABLE IF EXISTS " + THERMOSTAT_TABLE_NAME);
        
        // recreate db using onCreate(db)
        onCreate(db);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        Log.i(ACTIVITY_NAME, "-- In onDowngrade(), downgrading a database");
        
        // DROP TABLE IF EXISTS THERMOSTAT_TABLE
        db.execSQL("DROP TABLE IF EXISTS " + THERMOSTAT_TABLE_NAME);
        
        // recreate db using onCreate(db)
        onCreate(db);
    }
    
    
    // -------------
    //  GET ITEM ID
    // -------------
    
    public int getThermostatItemID(int position) {
        return getItemID(position, THERMOSTAT_TABLE_NAME);
    }
    
    /**
     * Get id from database given position.
     * Removing entries from db will make id != position, so getItemID is needed
     * This is for internal use only. public get___ItemID will call this private method.
     *
     * @param position  Position of entry in database that you want the ID of
     * @param tableName Table that will be queried. Must be one of the class constants listed
     * @return id of database entry at given position
     */
    private int getItemID(int position, String tableName) {
        Log.i(ACTIVITY_NAME, "-- In getItemID()");
        
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
        
        if (cursor.moveToPosition(position)) {
            return Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID)));
        }
        else {
            Log.i(ACTIVITY_NAME, "-- In getItemID(), no data in position " + position);
            return -1;
        }
    }
    
    
    // --------------------
    //  ADD DATABASE ENTRY
    // --------------------
    
    public void addThermostatDBEntry(ArrayList<String> dataToDB) {
        Log.i(ACTIVITY_NAME, "-- In addThermostatDBEntry()");
        
        addDBEntry(dataToDB, THERMOSTAT_TABLE_NAME, THERMOSTAT_COLUMNS);
    }
    
    /**
     * Adds the ArrayList of Strings into the given database.
     * This is for internal use only. public add___DBEntry will call this private method.
     *
     * @param dataToDB  ArrayList of Strings that will be inserted into database
     * @param tableName Table that data will be inserted into
     * @param columns   Columns that data will be inserted into
     */
    private void addDBEntry(ArrayList<String> dataToDB, String tableName, String[] columns) {
        Log.i(ACTIVITY_NAME, "-- In addDBEntry()");
        
        ContentValues values = new ContentValues();
        
        for (int i = 0; i < columns.length; i++) {
            values.put(columns[i], dataToDB.get(i));
        }
        
        database.insertWithOnConflict(tableName, "NULL FIELD", values, SQLiteDatabase.CONFLICT_IGNORE);
    }
    
    
    // -------------------
    //  GET DATABASE DATA
    // -------------------
    
    public ArrayList<ArrayList<String>> getThermostatDBData() {
        Log.i(ACTIVITY_NAME, "-- In getThermostatDBData()");
        
        return getDBData(THERMOSTAT_TABLE_NAME);
    }
    
    /**
     * Goes through the specified table in the database and returns a 2D ArrayList of the data.
     * This is for internal use only. public get___DBData will call this private method.
     *
     * @param tableName Table that will be queried. Must be one of the class constants listed
     * @return 2D ArrayList of Strings with all of the data from the given table
     */
    private ArrayList<ArrayList<String>> getDBData(String tableName) {
        Log.i(ACTIVITY_NAME, "-- In getDBData()");
        
        ArrayList<ArrayList<String>> dataFromDB = new ArrayList<>();
        
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
        Log.i(ACTIVITY_NAME, "-- In getDBData(), queried database successfully. # of rows: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            for (int row = 0; row < cursor.getCount(); row++) {
                
                dataFromDB.add(new ArrayList<String>());
                
                // add all the values of each column to ArrayList that will be returned
                for (int col = 0; col < cursor.getColumnCount(); col++) {
                    String cellRetrieved = cursor.getString(col);
                    //Log.i(ACTIVITY_NAME, "-- -- Got: " + cellRetrieved);
                    dataFromDB.get(row).add(cellRetrieved);
                }
                
                cursor.moveToNext();
            }
        }
    
        cursor.close();
        return dataFromDB;
    }
    
    
    // -----------------------
    //  DELETE DATABASE ENTRY
    // -----------------------
    
    public void deleteThermostatDBEntry(int ID) {
        Log.i(ACTIVITY_NAME, "-- In deleteThermostatDBEntry()");
        
        deleteDBEntry(ID, THERMOSTAT_TABLE_NAME);
    }
    
    /**
     * Deletes the row associated to the given ID.
     * This is for internal use only. public delete___DBEntry will call this private method.
     *
     * @param ID        Primary Key ID that will be deleted from table
     * @param tableName Table that will be queried. Must be one of the class constants listed
     */
    private void deleteDBEntry(int ID, String tableName) {
        Log.i(ACTIVITY_NAME, "-- In deleteDBEntry()");
        
        //database.execSQL("DELETE FROM " + tableName + " WHERE " + KEY_ID + " = " + ID);
        database.delete(tableName, KEY_ID + " = " + ID, null);
    }
    
    
    // -----------------------
    //  UPDATE DATABASE ENTRY
    // -----------------------
    
    public void updateThermostatDBEntry(int ID, ArrayList<String> newData) {
        Log.i(ACTIVITY_NAME, "-- In updateThermostatDBEntry()");
        
        updateDBEntry(ID, newData, THERMOSTAT_TABLE_NAME, THERMOSTAT_COLUMNS);
    }
    
    /**
     * Updates the row with the given ID with the ArrayList of new data
     * This is for internal use only. public update___DBEntry will call this private method.
     *
     * @param ID        Primary Key ID associated to row being updated.
     * @param newData   ArrayList of Strings of new entries that will replace old row
     * @param tableName Table that will be queried. Must be one of the class constants listed
     * @param columns   Columns that data will be replaced
     */
    private void updateDBEntry(int ID, ArrayList<String> newData, String tableName, String[] columns) {
        Log.i(ACTIVITY_NAME, "-- In updateDBEntry()");
        
        ContentValues values = new ContentValues();
        
        for (int i = 0; i < columns.length; i++) {
            values.put(columns[i], newData.get(i));
        }
        
        database.updateWithOnConflict(tableName, values, KEY_ID + " = " + ID, null, SQLiteDatabase.CONFLICT_IGNORE);
    }
}



