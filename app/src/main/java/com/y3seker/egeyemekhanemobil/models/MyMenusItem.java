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
import android.support.annotation.NonNull;

import com.squareup.okhttp.internal.Util;
import com.y3seker.egeyemekhanemobil.utils.Utils;

import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Yunus Emre Şeker on 19.10.2015.
 * -
 */
public class MyMenusItem implements Parcelable {

    public String dateString, from, balance;
    public boolean breakfast, lunch, dinner;

    public MyMenusItem(Elements elements) {
        this.from = elements.get(0).text();
        this.balance = elements.get(1).text();
        this.dateString = elements.get(2).text();
        String mType = elements.get(3).text();
        updateMeals(mType);
    }

    public MyMenusItem(String dateString, boolean dinner, boolean lunch, boolean breakfast) {
        this.dateString = dateString;
        this.dinner = dinner;
        this.lunch = lunch;
        this.breakfast = breakfast;
    }

    protected MyMenusItem(Parcel in) {
        dateString = in.readString();
        from = in.readString();
        balance = in.readString();
        breakfast = in.readByte() != 0;
        lunch = in.readByte() != 0;
        dinner = in.readByte() != 0;
    }

    public static final Creator<MyMenusItem> CREATOR = new Creator<MyMenusItem>() {
        @Override
        public MyMenusItem createFromParcel(Parcel in) {
            return new MyMenusItem(in);
        }

        @Override
        public MyMenusItem[] newArray(int size) {
            return new MyMenusItem[size];
        }
    };

    public void updateMeals(String s) {
        switch (s) {
            case "Kahvaltı":
                breakfast = true;
                break;
            case "Öğlen":
                lunch = true;
                break;
            case "Akşam":
                dinner = true;
                break;
            default:
                break;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dateString);
        dest.writeString(from);
        dest.writeString(balance);
        dest.writeByte((byte) (breakfast ? 1 : 0));
        dest.writeByte((byte) (lunch ? 1 : 0));
        dest.writeByte((byte) (dinner ? 1 : 0));
    }

}
