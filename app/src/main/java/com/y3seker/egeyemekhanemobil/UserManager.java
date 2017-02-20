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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.SerializableHttpCookie;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;

import org.jsoup.nodes.Document;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Yunus on 13.12.2016.
 * -
 */
public class UserManager {
    private static UserManager mInstance = new UserManager();

    User currentUser = null;
    SharedPreferences cookiesPrefs;
    List<User> users = new ArrayList<>();
    Database database;
    SharedPreferences appPrefs;

    private UserManager() {
    }

    public static UserManager getInstance() {
        return mInstance;
    }

    public void init(Context context) {
        appPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        cookiesPrefs = context.getSharedPreferences(PrefConstants.COOKIE_STORE_PREF, MODE_PRIVATE);
        database = new Database(context);
        users = database.getAllUsers();
        long defaultUserID = appPrefs.getLong(PrefConstants.DEFAULT_USER, 0);
        if (!users.isEmpty()) {
            for (User user : users) {
                String cookie = cookiesPrefs.getString(user.getCookieKey(), "");
                if (!cookie.isEmpty())
                    user.setCookie(new SerializableHttpCookie().decode(cookie));
                if (user.getUniqeID() == defaultUserID)
                    currentUser = user;
            }
            if (currentUser == null) {
                currentUser = users.get(0);
                appPrefs.edit().putLong(PrefConstants.DEFAULT_USER, currentUser.getUniqeID()).apply();
            }
        } else {
            // no user found, maybe throw an exception?
        }
    }

    public Subscription login(int id, RxAppCompatActivity context, final Subscriber<Document> caller) {
        User selectedUser = getUserByHashcode(id);
        if (selectedUser == null) {
            caller.onError(new IllegalStateException("User has not found with id:" + id));
            return null;
        }
        return login(selectedUser, context, caller);
    }

    public Subscription login(RxAppCompatActivity context, final Subscriber<Document> caller) {
        if (currentUser == null) {
            caller.onError(new IllegalStateException("No user found!"));
            return null;
        }
        return login(currentUser, context, caller);
    }

    public Subscription login(@NonNull final User user, RxAppCompatActivity context, final Subscriber<Document> caller) {
        if (user.isLoggedIn()) {
            currentUser = user;
            caller.onNext(null);
            caller.onCompleted();
            return null;
        }

        return ConnectionUtils.loginObservable(user)
                .compose(context.bindToLifecycle())
                .cast(Document.class)
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {
                        caller.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        caller.onError(e);
                    }

                    @Override
                    public void onNext(Document document) {
                        if (ParseUtils.isLoginPage(document))
                            onError(new InvalidCredentialException("Credentials invalid for user: " + user.getUniqeID()));
                        else {
                            HttpCookie cookie = RetrofitManager.getCookie();
                            if (cookie != null) {
                                cookiesPrefs.edit()
                                        .putString(user.getCookieKey(), new SerializableHttpCookie().encode(cookie))
                                        .apply();
                            }
                            user.setViewStates(ParseUtils.extractViewState(document));
                            if (user.getName().isEmpty()) {
                                user.setName(ParseUtils.getUserName(document));
                                database.updateUser(user);
                            }
                            user.setCookie(cookie);
                            user.setIsLoggedIn(true);
                            currentUser = user;
                            caller.onNext(document);
                        }
                    }
                });
    }

    public boolean isUserExist(User u) {
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                if (user.getUniqeID() == u.getUniqeID())
                    return true;
            }
            return false;
        } else
            return false;
    }

    public UserManager addUser(User newUser) {
        // If this is first ever user, make it default
        if (users.isEmpty()) {
            appPrefs.edit()
                    .putLong(PrefConstants.DEFAULT_USER, newUser.getUniqeID()).apply();
        }
        users.add(newUser);
        database.insertUser(newUser);
        return mInstance;
    }

    public boolean hasUser() {
        return !users.isEmpty();
    }

    public List<User> getUsers() {
        return users;
    }

    public User getUserByHashcode(int id) {
        User selectedUser = null;
        for (User user : users) {
            if (user.hashCode() == id)
                selectedUser = user;
        }
        return selectedUser;
    }

    public boolean deleteCurrentUser() {
        if (database.deleteUser(currentUser.getUniqeID())) {
            users.remove(currentUser);
            if (users.isEmpty()) {
                setCurrentUser(null);
            } else {
                setCurrentUser(users.get(0));
            }
            return true;
        }
        return false;
    }

    public void destroy() {
        database.close();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public UserManager setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        return mInstance;
    }

}
