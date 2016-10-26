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

package com.y3seker.egeyemekhanemobil.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.Database;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.constants.RequestCodes;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.SerializableHttpCookie;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;

import org.jsoup.nodes.Document;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

/**
 * Created by Yunus Emre Şeker on 7.10.2015.
 * -
 */
public class LoginActivity extends RxAppCompatActivity {

    public static final String ADD_USER_ACTION = "add_user";
    public static final String LOGIN_FAILED_ACTION = "login_failed";
    private static final String LOGIN_SUCCEED_ACTION = "login_succeed";
    private static final String TAG = LoginActivity.class.getSimpleName();

    private List<User> users;
    private User currentUser = null;
    private Database db;
    private SharedPreferences cookiesPrefs;
    private SharedPreferences appPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);
        users = db.getAllUsers();
        cookiesPrefs = this.getSharedPreferences(PrefConstants.COOKIE_STORE_PREF, MODE_PRIVATE);
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        long defaultUserID = appPrefs.getLong(PrefConstants.DEFAULT_USER, 0);
        if (users.size() == 0) {
            startAddUserActivity();
            return;
        }

        for (User user : users) {
            String cookie = cookiesPrefs.getString(user.getCookieKey(), "");
            if (!cookie.equals(""))
                user.setCookie(new SerializableHttpCookie().decode(cookie));
            if (user.getUniqeID() == defaultUserID)
                currentUser = user;
        }

        if (currentUser == null) {
            currentUser = users.get(0);
            appPrefs.edit().putLong(PrefConstants.DEFAULT_USER, currentUser.getUniqeID()).apply();
        }

        ConnectionUtils.loginObservable(currentUser)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Login Failed");
                        startActivity(LOGIN_FAILED_ACTION);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Document document) {
                        Log.d(TAG, "Login Succeed");
                        HttpCookie cookie = RetrofitManager.getCookie();
                        cookiesPrefs.edit()
                                .putString(currentUser.getCookieKey(), new SerializableHttpCookie().encode(cookie))
                                .apply();
                        currentUser.setCookie(cookie);
                        currentUser.setIsLoggedIn(true);
                        startActivity(LOGIN_SUCCEED_ACTION);
                    }
                });

    }

    private void startAddUserActivity() {
        Intent l = new Intent(getApplicationContext(), AddUserActivity.class);
        l.setAction(LoginActivity.ADD_USER_ACTION);
        l.putParcelableArrayListExtra(ParseConstants.USERS, (ArrayList<? extends Parcelable>) users);
        startActivityForResult(l, RequestCodes.LOGIN_REQ_CODE);
    }

    @Override
    protected void onStop() {
        db.close();
        super.onStop();
    }

    private void startActivity(String action) {
        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        mainIntent.setAction(action);
        mainIntent.putParcelableArrayListExtra(ParseConstants.USERS, (ArrayList<? extends Parcelable>) users);
        startActivity(mainIntent);
        finish();
        db.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.LOGIN_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                User user = data.getParcelableExtra(ParseConstants.USER);
                users.add(user);
                db.insertUser(user);
                startActivity(LOGIN_SUCCEED_ACTION);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.add_account_to_use, Toast.LENGTH_SHORT).show();
                //finish();
            }
        }

    }

}
