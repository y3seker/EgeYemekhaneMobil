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

import com.y3seker.egeyemekhanemobil.utils.Utils;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class OrderItem implements Comparable<OrderItem> {
    public String text, name, dayNumber;
    public boolean isDisabled;
    public boolean isChecked;
    public boolean isInProgress;
    public boolean isOrderedBefore;
    public String date;

    public OrderItem(String text, String name) {
        this.text = text.replace("(menü)", "");
        this.name = name;
        this.isChecked = false;
    }

    public OrderItem(String text, String name, String date) {
        this.date = date;
        this.text = text.replace("(menü)", "");
        this.name = name;
        this.isChecked = false;
    }

    public OrderItem(String text, String name, String date, boolean isDisabled) {
        this.date = date;
        this.isDisabled = isDisabled;
        this.text = text.replace("(menü)", "");
        this.name = name;
        this.isChecked = false;
        this.isInProgress = false;
    }

    public OrderItem(String text, String name, String date, boolean isDisabled, boolean isOrderedBefore) {
        this.text = text;
        this.name = name;
        this.date = date;
        this.dayNumber = text.replaceAll("\\D+", "");
        this.isDisabled = isDisabled;
        this.isOrderedBefore = isOrderedBefore;
        this.isInProgress = false;
        this.isChecked = false;
    }

    public void reset() {
        this.isDisabled = false;
        this.isOrderedBefore = false;
        this.isChecked = false;
        this.isInProgress = false;
    }

    public void setIsDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public void setIsOrderedBefore(boolean isOrderedBefore) {
        this.isOrderedBefore = isOrderedBefore;
    }

    @Override
    public int compareTo(OrderItem another) {
        return Utils.getReverseDateString2(this.date).compareTo(Utils.getReverseDateString2(another.date));
    }
}
