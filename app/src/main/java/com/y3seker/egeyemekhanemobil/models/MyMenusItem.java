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

import org.jsoup.select.Elements;

import java.util.Calendar;

/**
 * Created by Yunus Emre Şeker on 19.10.2015.
 * -
 */
public class MyMenusItem implements Parcelable, Comparable {

    public String dateString, from, balance;
    public boolean breakfast = false, lunch = false, dinner = false, iftar = false;
    public Calendar date;

    public MyMenusItem(Elements elements) {
        this.from = elements.get(0).text();
        this.balance = elements.get(1).text();
        this.dateString = elements.get(2).text();
        setMeals(elements.get(3).text());
    }

    public MyMenusItem(Calendar date, Elements elements) {
        this(elements);
        this.date = date;
    }

    private MyMenusItem(Parcel in) {
        dateString = in.readString();
        from = in.readString();
        balance = in.readString();
        breakfast = in.readByte() != 0;
        lunch = in.readByte() != 0;
        dinner = in.readByte() != 0;
        iftar = in.readByte() != 0;
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

    public MyMenusItem(String string) {
        this.dateString = string;
    }


    public void setMeals(String s) {
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
            case "iftar":
                iftar = true;
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
        dest.writeByte((byte) (iftar ? 1 : 0));
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.date.compareTo(((MyMenusItem) another).date);
    }

}
