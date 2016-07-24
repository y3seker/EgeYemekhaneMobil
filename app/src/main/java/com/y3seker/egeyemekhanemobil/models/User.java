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

package com.y3seker.egeyemekhanemobil.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.y3seker.egeyemekhanemobil.retrofit.SerializableHttpCookie;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;

import java.net.HttpCookie;
import java.util.HashMap;

/**
 * Created by Yunus Emre Şeker on 7.10.2015.
 * -
 */
public class User implements Parcelable {

    private String username, password, name;
    private int cafeteriaNumber;

    private String baseUrl;

    private boolean isLoggedIn;
    private HashMap<String, String> viewStates;

    private HttpCookie cookie;

    public User() {
        this("", "", "", 0);
    }

    public User(String name, String username, String password, int caf) {
        this.name = name;
        this.password = password;
        this.username = username;
        this.cafeteriaNumber = caf;
        this.isLoggedIn = false;
        this.viewStates = new HashMap<>();
        this.cookie = null;
        baseUrl = ConnectionUtils.findBaseUrl(cafeteriaNumber);
    }

    protected User(Parcel in) {
        viewStates = new HashMap<>();
        username = in.readString();
        password = in.readString();
        name = in.readString();
        cafeteriaNumber = in.readInt();
        isLoggedIn = in.readByte() != 0;
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            viewStates.put(in.readString(), in.readString());
        }
        baseUrl = in.readString();
        if (in.readByte() != 0)
            cookie = new SerializableHttpCookie().decode(in.readString());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getMenuLabel() {
        return getUsername() + " #" + (cafeteriaNumber == 0 ? "P" : getCafeteriaNumber());
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getCafeteriaNumber() {
        return cafeteriaNumber;
    }

    public HashMap<String, String> getViewStates() {
        return viewStates;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public void setViewStates(HashMap<String, String> viewStates) {
        if (viewStates != null)
            this.viewStates = viewStates;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCafeteriaNumber(int cafeteriaNumber) {
        this.cafeteriaNumber = cafeteriaNumber;
        baseUrl = ConnectionUtils.findBaseUrl(cafeteriaNumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(name);
        dest.writeInt(cafeteriaNumber);
        dest.writeByte((byte) (isLoggedIn ? 1 : 0));
        dest.writeInt(viewStates.size());
        for (String s : viewStates.keySet()) {
            dest.writeString(s);
            dest.writeString(viewStates.get(s));
        }
        dest.writeString(baseUrl);
        dest.writeByte((byte) (cookie != null ? 1 : 0));
        if (cookie != null)
            dest.writeString(new SerializableHttpCookie().encode(cookie));
    }

    public long getUniqeID() {
        return Long.parseLong(username + cafeteriaNumber);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User && this.getUniqeID() == ((User) o).getUniqeID();
    }

    @Override
    public String toString() {
        return getName() + "\n" + getUsername() + "\n" +
                (cafeteriaNumber != 0 ? getCafeteriaNumber() + " Nolu Yemekhane" : "Personel Yemekhanesi");

    }

    public HttpCookie getCookie() {
        return cookie;
    }

    public void setCookie(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public String getCookieKey() {
        return "c" + getUniqeID();
    }
}
