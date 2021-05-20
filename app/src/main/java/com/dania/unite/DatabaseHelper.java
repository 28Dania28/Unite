package com.dania.unite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Blob;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String TABLE_NAME = "ChatNew";
    private static final String COL1 = "ID";
    private static final String COL2 = "SEEN";
    private static final String COL3 = "ChatMsg";
    private static final String COL4 = "Time";
    private static final String COL5 = "Date";
    private static final String COL6 = "Status";
    private static final String COL7 = "TYPE";
    private static final String COL8 = "S_OR_R";
    private static final String COL9 = "Push_Key";
    private static final String FRIENDCOL1 = "ID";
    private static final String FRIENDCOL2 = "UserId";
    private static final String FRIENDCOL3 = "UserNamw";
    private static final String FRIENDCOL4 = "ProfilePic";
    private static final String LOVECOL1 = "ID";
    private static final String LOVECOL2 = "UserId";
    private static final String LOVECOL3 = "UserName";
    private static final String LOVECOL4 = "ProfilePic";


    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addData(String name, String seen, String msg, String time, String date, String status, String type, String s_or_r,String push_key){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ name + " ("+COL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COL2+" TEXT, "+COL3+" TEXT, "+COL4+" TEXT, "+COL5+" TEXT, "+COL6+" TEXT, "+COL7+" TEXT, "+COL8+" TEXT, "+COL9+" TEXT)");
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, seen);
        contentValues.put(COL3, msg);
        contentValues.put(COL4, time);
        contentValues.put(COL5, date);
        contentValues.put(COL6, status);
        contentValues.put(COL7, type);
        contentValues.put(COL8, s_or_r);
        contentValues.put(COL9, push_key);
        long result = db.insert(name, null, contentValues);
        if (result == -1){
            return false;
        }else {
            return true;
        }
    }


    public Cursor getData(String table1){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ table1 + " ("+COL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COL2+" TEXT, "+COL3+" TEXT, "+COL4+" TEXT, "+COL5+" TEXT, "+COL6+" TEXT, "+COL7+" TEXT, "+COL8+" TEXT, "+COL9+" TEXT)");
        Cursor c = db.rawQuery("select * from "+table1,null);
        return c;
    }

    public boolean addFriendData(String userid, String username, String dp){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ "FriendsList" + " ("+FRIENDCOL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+FRIENDCOL2+" TEXT, "+FRIENDCOL3+" TEXT,"+FRIENDCOL4+" TEXT )");
        ContentValues contentValues = new ContentValues();
        contentValues.put(FRIENDCOL2, userid);
        contentValues.put(FRIENDCOL3, username);
        contentValues.put(FRIENDCOL4, dp);
        long result = db.insert("FriendsList", null, contentValues);
        if (result == -1){
            return false;
        }else {
            return true;
        }
    }

    public Cursor getFriendData(String table1){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ "FriendsList" + " ("+FRIENDCOL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+FRIENDCOL2+" TEXT, "+FRIENDCOL3+" TEXT,"+FRIENDCOL4+" TEXT )");
        Cursor c = db.rawQuery("select * from "+table1,null);
        return c;
    }
    public void deleteAllFriends(String table_name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+table_name);
        db.close();
    }

    public boolean addLoveData(String userid, String username, String dp){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ "LoveList" + " ("+LOVECOL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+LOVECOL2+" TEXT, "+LOVECOL3+" TEXT, "+LOVECOL4+" TEXT)");
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOVECOL2, userid);
        contentValues.put(LOVECOL3, username);
        contentValues.put(LOVECOL4, dp);
        long result = db.insert("LoveList", null, contentValues);
        if (result == -1){
            return false;
        }else {
            return true;
        }
    }
    public Cursor getLoveData(String table1){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ "LoveList" + " ("+LOVECOL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+LOVECOL2+" TEXT, "+LOVECOL3+" TEXT, "+LOVECOL4+" TEXT)");
        Cursor c = db.rawQuery("select * from "+table1,null);
        return c;
    }
    public void deleteAllLove(String table_name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+table_name);
        db.close();
    }



}
