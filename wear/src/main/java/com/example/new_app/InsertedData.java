package com.example.new_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InsertedData extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Inserted.db";
    public static final String TABLE_NAME = "inserted_table";
    public static final String COL_1 = "TIME";
    public static final String COL_2 = "DATE";
    public static final String COL_3 = "VALUE";//goal or steps walked
    public static final String COL_4 = "STATE";

    public InsertedData(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,TIME TEXT,DATE TEXT,VALUE INTEGER, STATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String time, String date, int value, String state) {
        System.out.println("Inserted Data in the InsertData Database");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, time);
        contentValues.put(COL_2, date);
        contentValues.put(COL_3, value);
        contentValues.put(COL_4, state);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            System.out.println("Inserted");
        return true;
    }

    //Get latest goal set
    public Cursor getAllData1() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from inserted_table where STATE IS 'Goal' ", null);
        res.moveToLast();
        return res;
    }

    //Get latest steps recorded (these are the official subtracted steps)
    public Cursor getAllData2() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from inserted_table where STATE IS 'Start of Walk' ", null);
        res.moveToLast();
        return res;
    }

    /*public Cursor getDataToSendToPhone() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from inserted_table where BOOLEANS =0", null);//where bool=0
        res.moveToLast();
        return res;
    }

    public boolean updateData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        /*ContentValues contentValues = new ContentValues();
        contentValues.put(COL_4, 1);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }*/

}