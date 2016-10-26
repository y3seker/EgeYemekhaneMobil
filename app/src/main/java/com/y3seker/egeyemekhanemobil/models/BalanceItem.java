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

import com.y3seker.egeyemekhanemobil.utils.Utils;

import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class BalanceItem implements Parcelable {

    public final String tarih;
    public final String zaman;
    public final String yer;
    private String islemOncesiBakiye;
    public String islemTutari;
    public final String sonBakiye;
    private String tarih_zaman;
    private Date date;

    public BalanceItem(Elements elements) {
        this.tarih = elements.get(0).text();
        this.zaman = elements.get(1).text();
        this.yer = elements.get(2).text();
        this.islemOncesiBakiye = elements.get(3).text();
        this.islemTutari = elements.get(4).text() + " TL";
        this.sonBakiye = elements.get(5).text() + " TL";
        this.tarih_zaman = tarih + "-" + zaman;
        try {
            this.date = Utils.balanceDateFormat.parse(tarih_zaman);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!isCashOutflow()) islemTutari = "+" + islemTutari;
    }

    public BalanceItem(String tarih, String zaman, String yer, String islemTutari, String sonBakiye) {
        this.tarih = tarih;
        this.zaman = zaman;
        this.yer = yer;
        this.islemTutari = islemTutari;
        this.sonBakiye = sonBakiye;
    }

    private BalanceItem(Parcel in) {
        tarih = in.readString();
        zaman = in.readString();
        yer = in.readString();
        islemOncesiBakiye = in.readString();
        islemTutari = in.readString();
        sonBakiye = in.readString();
        tarih_zaman = in.readString();
    }

    public static final Creator<BalanceItem> CREATOR = new Creator<BalanceItem>() {
        @Override
        public BalanceItem createFromParcel(Parcel in) {
            return new BalanceItem(in);
        }

        @Override
        public BalanceItem[] newArray(int size) {
            return new BalanceItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tarih);
        dest.writeString(zaman);
        dest.writeString(yer);
        dest.writeString(islemOncesiBakiye);
        dest.writeString(islemTutari);
        dest.writeString(sonBakiye);
        dest.writeString(tarih_zaman);
    }

    public boolean isCashOutflow() {
        return islemTutari.contains("-");
    }
}
