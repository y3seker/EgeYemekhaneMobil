/*
 * Copyright 2015 Yunus Emre Şeker. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.y3seker.egeyemekhanemobil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.y3seker.egeyemekhanemobil.models.User;

import java.util.ArrayList;

/**
 * Created by Yunus Emre Şeker on 8.10.2015.
 * -
 */
public class Database extends SQLiteOpenHelper {

    public static final String USER_UNIQUEID = "Uniqueid";
    public static final String USER_NAME = "Name";
    public static final String USER_USERNAME = "Username";
    public static final String USER_PASSWORD = "Password";
    public static final String USER_CAFNUMBER = "Cafnumber";

    public static final String[] ALL_USER_KEYS = new String[]{USER_UNIQUEID,
            USER_NAME,
            USER_USERNAME,
            USER_PASSWORD,
            USER_CAFNUMBER};

    public static final String TABLE_USER = "user_table";

    public static final String DATABASE_NAME = "YEMEKHANEDB";
    public static final int DATABASE_VERSION = 5;

    private static final String DB_CREATE_USER_TABLE =
            "create table " + TABLE_USER
                    + " (" + USER_UNIQUEID + " integer primary key, "

                    + USER_CAFNUMBER + " INTEGER not null, "
                    + USER_NAME + " text not null, "
                    + USER_USERNAME + " text not null, "
                    + USER_PASSWORD + " text not null "

                    + ");";


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public long insertUser(User user) throws SQLiteException {

        ContentValues initialValues = new ContentValues();
        initialValues.put(USER_UNIQUEID, user.getUniqeID());
        initialValues.put(USER_CAFNUMBER, user.getCafeteriaNumber());
        initialValues.put(USER_NAME, user.getName());
        initialValues.put(USER_USERNAME, user.getUsername());
        initialValues.put(USER_PASSWORD, user.getPassword());

        // Insert it into the database.
        return getWritableDatabase().insert(TABLE_USER, null, initialValues);
    }

    public long updateUser(User user) throws SQLiteException {

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(USER_UNIQUEID, user.getUniqeID());
        updatedValues.put(USER_CAFNUMBER, user.getCafeteriaNumber());
        updatedValues.put(USER_NAME, user.getName());
        updatedValues.put(USER_USERNAME, user.getUsername());
        updatedValues.put(USER_PASSWORD, user.getPassword());

        // Update it into the database.
        return getWritableDatabase().update(TABLE_USER, updatedValues, USER_UNIQUEID + "=" + user.getUniqeID(), null);
    }


    // Return all data in the database.
    private Cursor _getAllUsers() {
        Cursor c = getReadableDatabase().query(true, TABLE_USER, ALL_USER_KEYS,
                null, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> result = new ArrayList<>();
        Cursor cursor = _getAllUsers();
        if (cursor.moveToFirst()) {
            do {
                result.add(new User(cursor.getString(cursor.getColumnIndex(USER_NAME)),
                                cursor.getString(cursor.getColumnIndex(USER_USERNAME)),
                                cursor.getString(cursor.getColumnIndex(USER_PASSWORD)),
                                cursor.getInt(cursor.getColumnIndex(USER_CAFNUMBER))
                        )
                );
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public boolean deleteUser(long uniqueID) {
        return getWritableDatabase().delete(TABLE_USER, USER_UNIQUEID + "=" + uniqueID, null) > 0;
    }

    public User getUser(long uniqueID) {
        User user = null;
        Cursor cursor = getWritableDatabase().query(true, TABLE_USER, ALL_USER_KEYS,
                USER_UNIQUEID + "=" + uniqueID, null, null, null, null, null);
        if (cursor.moveToFirst())
            user = new User(cursor.getString(cursor.getColumnIndex(USER_NAME)),
                    cursor.getString(cursor.getColumnIndex(USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(USER_PASSWORD)),
                    cursor.getInt(cursor.getColumnIndex(USER_CAFNUMBER)));
        cursor.close();
        return user;
    }
}
