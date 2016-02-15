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

import org.jsoup.select.Elements;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class MyActsItem implements Parcelable {

    public String date, time, caf, menu_type;
    public String date_time;

    public MyActsItem(Elements elements) {
        this.date = elements.get(0).text();
        this.time = elements.get(1).text();
        this.menu_type = elements.get(2).text();
        this.caf = elements.get(3).text();
        this.date_time = date + " " + time;
    }

    public MyActsItem(String date_time, String menu_type, String caf) {
        this.date_time = date_time;
        this.menu_type = menu_type;
        this.caf = caf;
    }

    protected MyActsItem(Parcel in) {
        date = in.readString();
        time = in.readString();
        caf = in.readString();
        menu_type = in.readString();
        date_time = in.readString();
    }

    public static final Creator<MyActsItem> CREATOR = new Creator<MyActsItem>() {
        @Override
        public MyActsItem createFromParcel(Parcel in) {
            return new MyActsItem(in);
        }

        @Override
        public MyActsItem[] newArray(int size) {
            return new MyActsItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(caf);
        dest.writeString(menu_type);
        dest.writeString(date_time);
    }
}
