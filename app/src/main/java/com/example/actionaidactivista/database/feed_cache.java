package com.example.actionaidactivista.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.actionaidactivista.models.feed;

public class feed_cache extends SQLiteOpenHelper {
    public static final String FEEDS = "Feeds_table";

    //initial db version = 1
    public static final int DB_VERSION = 1;

    public feed_cache(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, "feed_cache.db", factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("create table " + FEEDS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,FEEDID TEXT,DES TEXT,DATEPOSTED TEXT,FILETYPE TEXT,INTTYPE TEXT,MIMETYPE TEXT,PATH TEXT,URL TEXT,CONTENT TEXT,USERID TEXT," +
                    "USERACCOUNT TEXT,ISAPPROVED TEXT,LOCATION TEXT,GEOLOCATION TEXT)");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //INSERTING MESSAGE
    public boolean insertFeed(feed feed) {
        //KEEP THE NUMBER OF FEEDS IN CACHE @20 AND LESS
        boolean result;
        long ins;
        try {
            //delete the current feeds
            //deleteAllRecordsFromTable("Feeds_table");
            //first check existence
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor res = db.rawQuery("SELECT * FROM '" + FEEDS + "' WHERE FEEDID ='" + feed.getmId() + "'", null);
            if (res.moveToNext()) {
                result = false;
            } else {
                ContentValues contentValues = new ContentValues();

                contentValues.put("FEEDID", feed.getmId());
                contentValues.put("DES", feed.getmDescription());
                contentValues.put("DATEPOSTED", feed.getmDate());
                contentValues.put("FILETYPE", feed.getmFileType());
                contentValues.put("INTTYPE", feed.getmIntType());
                contentValues.put("MIMETYPE", feed.getmMimeType());
                contentValues.put("PATH", feed.getmPath());
                contentValues.put("URL", feed.getmUrl());
                contentValues.put("CONTENT", feed.getmContent());
                contentValues.put("USERID", feed.getmUploaderId());
                contentValues.put("LOCATION", feed.getmLocation());
                contentValues.put("GEOLOCATION", feed.getmGeoLocation());

                ins = db.insert(FEEDS, null, contentValues);
                db.close();
                if (ins == -1) {
                    result = false;
                } else {
                    result = true;
                }
            }

        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    //GET CACHED FEEDS
    public Cursor getFeeds() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM Feeds_table";
            cursor = db.rawQuery(query, null);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return cursor;
    }

    /*
     * performs deletion of all entries of a given table
     */
    public void deleteAllRecordsFromTable(String table_name){
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(table_name,null,null);
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
