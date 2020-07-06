package com.example.firebase;
/*
This is the core of the SQLite database
It sets up how each instance is laid out.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME ="Database.db";
    // Database Version
    private static final int DATABASE_VERSION = 5;
    public static final String TABLE_SENSOR_BOARD  = "Sensor_board_table";
    public static final String TABLE_FILTERED = "Filtered_table";
    public static final String TABLE_LOG = "Log_table";
    public static final String TABLE_RAW = "Raw_table";
    public static final String TABLE_DOWNLOADED = "Downloaded_table";

    public static final String TABLE_FILTERED_DAY_1 = "Filtered_table_day_1";
    public static final String TABLE_FILTERED_DAY_2 = "Filtered_table_day_2";
    public static final String TABLE_FILTERED_DAY_3 = "Filtered_table_day_3";

    public static final String TABLE_FILTERED_LOG = "Filtered_Log_table";
    public static final String TABLE_FILTERED_LIVE = "Filtered_Live_table";
    public static final String TABLE_FILTERED_DOWNLOADED = "Filtered_Downloaded_table";

    public static final String TABLE_DATA_SCIENCE = "Filtered_Data_science";


    public static final String COL1 = "ID";
    public static final String COL2 = "T1";
    public static final String COL3 = "T2";
    public static final String COL4 = "T3";
    public static final String COL5 = "T4";
    public static final String COL6 = "Pdiff";
    public static final String COL7 = "time";
    public static final String RAW_DATA = "Raw_data";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        String Created_TABLE_SENSOR_BOARD = "CREATE TABLE " + TABLE_SENSOR_BOARD+ " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_FILTERED = "CREATE TABLE " + TABLE_FILTERED + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_LOG = "CREATE TABLE " + TABLE_LOG + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_DOWNLOADED = "CREATE TABLE " + TABLE_DOWNLOADED + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_RAW = "CREATE TABLE " + TABLE_RAW + " (ID INTEGER PRIMARY KEY, " +
                "RAW_DATA TEXT)";

        //Days

        String Created_TABLE_FILTERED_DAY_1 = "CREATE TABLE " + TABLE_FILTERED_DAY_1 + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_FILTERED_DAY_2 = "CREATE TABLE " + TABLE_FILTERED_DAY_2 + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_FILTERED_DAY_3 = "CREATE TABLE " + TABLE_FILTERED_DAY_3 + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";


        String Created_TABLE_FILTERED_LOG = "CREATE TABLE " + TABLE_FILTERED_LOG + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_FILTERED_LIVE = "CREATE TABLE " + TABLE_FILTERED_LIVE + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";
        String Created_TABLE_FILTERED_DOWNLOADED = "CREATE TABLE " + TABLE_FILTERED_DOWNLOADED + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";

        String CREATED_TABLE_DATA_SCIENCE = "CREATE TABLE " + TABLE_DATA_SCIENCE + " (ID INTEGER PRIMARY KEY, " +
                "T1 TEXT, T2, TEXT, T3 TEXT, T4 TEXT, Pdiff TEXT, TIME TEXT)";


        db.execSQL(Created_TABLE_SENSOR_BOARD);
        db.execSQL(Created_TABLE_FILTERED);
        db.execSQL(Created_TABLE_LOG);
        db.execSQL(Created_TABLE_DOWNLOADED);
        db.execSQL(Created_TABLE_RAW);
        db.execSQL(Created_TABLE_FILTERED_DAY_1);
        db.execSQL(Created_TABLE_FILTERED_DAY_2);
        db.execSQL(Created_TABLE_FILTERED_DAY_3);

        db.execSQL(Created_TABLE_FILTERED_LOG);
        db.execSQL(Created_TABLE_FILTERED_LIVE);
        db.execSQL(Created_TABLE_FILTERED_DOWNLOADED);

        db.execSQL(CREATED_TABLE_DATA_SCIENCE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_BOARD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTERED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RAW);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTERED_DAY_1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTERED_DAY_2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTERED_DAY_3);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTERED_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTERED_LIVE);

        db.execSQL("DROP TABLE IF EXISTS "  + TABLE_DATA_SCIENCE);
        // create new tables
        onCreate(db);
    }
    /*
    Function for adding data to the specified database. It needs the data to be passed into it so that the data can be added successsfully
     */

    public boolean addData_Sensor_Board(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_SENSOR_BOARD,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Log(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_LOG,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Filtered(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Downloaded(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_DOWNLOADED,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Raw(String Raw){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(RAW_DATA,Raw);//8


        long result = db.insert(TABLE_RAW,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }

    //Days

    public boolean addData_Filtered_Day_1(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED_DAY_1,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Filtered_Day_2(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED_DAY_2,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Filtered_Day_3(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED_DAY_3,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }

    public boolean addData_Filtered_Log(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED_LOG,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }
    public boolean addData_Filtered_Live(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED_LIVE,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }

    public boolean addData_Filtered_Downloaded(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_FILTERED_DOWNLOADED,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }


    public boolean addData_Data_Science(String t1, String t2, String t3, String t4, String pdiff, String time){//Adds data to the database when data has been passed into

        SQLiteDatabase db = this.getWritableDatabase();//Enable the database to be written to
        ContentValues contentValues = new ContentValues();//Setup the data input note COL1 is for the ID
        contentValues.put(COL2,t1);//2
        contentValues.put(COL3,t2);//3
        contentValues.put(COL4,t3);//4
        contentValues.put(COL5,t4);//5
        contentValues.put(COL6,pdiff);//6
        contentValues.put(COL7,time);//7

        long result = db.insert(TABLE_DATA_SCIENCE,null,contentValues);//Check to see if pass in has been successful and pass in the data on this line via the pass in of content values
        if(result == -1){//Returned false when inputting data to the database
            return false;
        }
        else{
            return true;//No error
        }
    }

    public Cursor showData_Sensor_Board(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR_BOARD;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Filtered(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Raw(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_RAW;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Log(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_LOG;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Downloaded(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_DOWNLOADED;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    //Days

    public Cursor showData_Filtered_Day_1(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED_DAY_1;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Filtered_Day_2(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED_DAY_2;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Filtered_Day_3(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED_DAY_3;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    public Cursor showData_Filtered_Log(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED_LOG;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Filtered_Live(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED_LIVE;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }
    public Cursor showData_Filtered_Downloaded(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FILTERED_DOWNLOADED;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    public Cursor showData_Data_Science(){//Used to locate the data inside the database
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_DATA_SCIENCE;

        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    public void deleteAll_Log(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_LOG;
        db.execSQL(clearDB);
    }
    public void deleteAll_Raw(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_RAW;
        db.execSQL(clearDB);
    }
    public void deleteAll_Sensor_Board(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_SENSOR_BOARD;
        db.execSQL(clearDB);
    }
    public void deleteAll_Filtered(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED;
        db.execSQL(clearDB);
    }


    public void deleteAll_Downloaded(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_DOWNLOADED;
        db.execSQL(clearDB);
    }

    public void deleteAll_Filtered_Day_1(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_DAY_1;
        db.execSQL(clearDB);
    }
    public void deleteAll_Filtered_Day_2(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_DAY_2;
        db.execSQL(clearDB);
    }
    public void deleteAll_Filtered_Day_3(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_DAY_3;
        db.execSQL(clearDB);
    }

    public void deleteAll_Filtered_Log(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_LOG;
        db.execSQL(clearDB);
    }
    public void deleteAll_Filtered_Live(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_LIVE;
        db.execSQL(clearDB);
    }

    public void deleteAll_Filtered_Donwloaded(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_DOWNLOADED;
        db.execSQL(clearDB);
    }
    public void deleteAll_Data_Science(){//Clears the entire database
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_DOWNLOADED;
        db.execSQL(clearDB);
    }
    public void delete_all_tables()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDB = "DELETE FROM "+ TABLE_FILTERED_DOWNLOADED;
        clearDB = "DELETE FROM " + TABLE_FILTERED;
        clearDB = "DELETE FROM " + TABLE_FILTERED_LIVE;
        clearDB = "DELETE FROM " + TABLE_FILTERED_LOG;
        clearDB = "DELETE FROM " + TABLE_FILTERED_DAY_1;
        clearDB = "DELETE FROM " + TABLE_FILTERED_DAY_2;
        clearDB = "DELETE FROM " + TABLE_FILTERED_DAY_3;
        clearDB = "DELETE FROM " + TABLE_DOWNLOADED;
        clearDB = "DELETE FROM " + TABLE_RAW;
        clearDB = "DELETE FROM " + TABLE_SENSOR_BOARD;
        clearDB = "DELETE FROM " + TABLE_LOG;
        clearDB = "DELETE FROM " + TABLE_DATA_SCIENCE;
        db.execSQL(clearDB);
    }
}
