package com.example.new_app;

//3) Local Βάση δεδομένων (δημιουργείται και βρίσκεται στο τηλέφωνο) για να αποθηκεύουμε τα detected activities

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Stance_Database extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Stance_Database.db";
    public static final String TABLE_NAME = "Stance_Database_table";
    public static final String COL_1 = "TIME";//στήλη για να αποθηκεύουμε την ώρα που γίνεται detect ένα activity
    public static final String COL_2 = "DATE";//στήλη για να αποθηκεύουμε την ημερομηνία που γίνεται detect ένα activity
    public static final String COL_3 = "CONFIDENCE";//στήλη για να αποθηκεύουμε την σιγουριά που γίνεται detect ένα activity (π.χ., ο χρήστης περπατά με 30% σιγουριά ή ο χρήστης είναι ακίνητος με 100% σιγουριά)
    public static final String COL_4 = "SENSOR";//στήλη για να αποθηκεύουμε το detected activity (έχουμε 8 activities στο σύνολο).
    public static final String COL_5 = "STOOD";//στήλη για να αποθηκεύουμε κατάσταση true ή false. True αν οι χρήστες βρίσκονται σε κατάσταση κίνησης ή false αν είναι σε κατάσταση ακινησίας
    public static final String COL_6 = "NOTIFICATION";//στήλη για να αποθηκεύουμε την κατάσταση: οι χρήστες λαμβάνουν notifications ναι η όχι
    public static final String COL_7 = "TIME_MODIFIED";//στήλη που μας βοηθά να εντοπίσουμε πιο εύκολα δεδομένα απο την βάση

    public Stance_Database(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIME TEXT, DATE TEXT, CONFIDENCE INTEGER, SENSOR TEXT, STOOD TEXT, NOTIFICATION TEXT, TIME_MODIFIED TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String time, String date, int value, String value2, String stood, String notification, String timeModified) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, time);
        contentValues.put(COL_2, date);
        contentValues.put(COL_3, value);
        contentValues.put(COL_4, value2);
        contentValues.put(COL_5, stood);
        contentValues.put(COL_6, notification);
        contentValues.put(COL_7, timeModified);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            System.out.println("Inserted");
        return true;
    }

    //Μέθοδος για να δούμε πόσες φορές ο χρήστης ήταν active μέσα στην μέρα - πχ. αν ο χρήστης απο τις 2 μεχρι τις 3 σηκώστηκε 1000 ή 1 ή 503 ή 38 φορές, εμείς το μετρούμε πάντα σαν 1. Δεν μας ενδιαφέρει πόσες φορές σηκώστηκε σε μια ώρα αλλά αν έσπασε καθιστικότητα σε μια ώρα)
    //Άυτή την μέθοδο θα την χρησιμοποιήσουμε για να δούμε αν ο χρήστης έσπασε καθιστικότητα τουλάχιστον 1 φορά για 12 ώρες την ημέρα. Αν ο χρήστης δεν έσπασε καθιστικότητα σε μια ώρα, το μετρούμε σαν 0.
    public Cursor getActivitiesStood(String dateInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select DISTINCT TIME_MODIFIED from Stance_Database_table where DATE is @dateInput and STOOD is 'true'", new String[]{ dateInput });
        res.moveToLast();
        return res;
    }

    public Cursor getActivitiesStoodHour(String dateInput, String hourInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput and STOOD is 'true' and TIME_MODIFIED is @hourinput ", new String[]{ dateInput, hourInput });
        res.moveToLast();
        return res;
    }

    //get steps from current date and hour (for 2 mins of consecutive walking)
    public Cursor getWalking2(String dateInput, String hourInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput and TIME is @hourinput and STOOD is 'true' ", new String[]{ dateInput, hourInput });
        return res;
    }

    /*public Cursor getActivitiesStood(String dateInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput and STOOD is 'true'", new String[]{ dateInput });
        res.moveToLast();
        return res;
    }

    public Cursor getAllData(String dateInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput and ((SENSOR  NOT LIKE 'Still') and  (SENSOR  NOT LIKE 'In Vehicle') and (SENSOR  NOT LIKE 'Tilting') and (SENSOR  NOT LIKE 'Unknown') ) ", new String[]{ dateInput });
        res.moveToLast();
        return res;
    }

    public Cursor getOneData(String dateInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput and STOOD = 1 and ((SENSOR  NOT LIKE 'Still') and  (SENSOR  NOT LIKE 'In Vehicle') and (SENSOR  NOT LIKE 'Tilting') and (SENSOR  NOT LIKE 'Unknown') ) ", new String[]{ dateInput });
        res.moveToLast();
        return res;
    }*/

    /*public Cursor getOneDataEXPERIMENTAL(String dateInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput ", new String[]{ dateInput });
        res.moveToLast();
        return res;
    }

    public Cursor getOneDataEXPERIMENTAL(String dateInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Stance_Database_table where STOOD = 1 ", null);
        res.moveToLast();
        return res;
    }*/

    //Cursor res = db.rawQuery("select * from Stance_Database_table where DATE is @dateInput and NOT (SENSOR = 'Still')  ", new String[]{ dateInput }); or SENSOR NOT LIKE '' or SENSOR NOT LIKE 'Unknown'  or SENSOR NOT LIKE 'In Vehicle'

    public boolean updateData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, 1);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }

}
