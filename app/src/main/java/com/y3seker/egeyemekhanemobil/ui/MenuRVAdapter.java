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

package com.y3seker.egeyemekhanemobil.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.activities.MyMenusActivity;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class MenuRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<MyMenusItem> items;
    int itemLayoutID;
    int lastPosition = -1;

    int grey, accent;

    public MenuRVAdapter(Context c, int itemLayoutID, List<MyMenusItem> items) {
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        grey = ContextCompat.getColor(c, R.color.grey_400);
        accent = ContextCompat.getColor(c, R.color.colorAccent);
    }


    public void changeList(List<MyMenusItem> items) {
        this.items = items;
        lastPosition = -1;
        notifyDataSetChanged();
    }

    public void clearList() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false);
        return new Menu_Holder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof Menu_Holder) {
            Menu_Holder holder1 = (Menu_Holder) holder;
            MyMenusItem myMenusItem = items.get(position);
            holder1.date.setText(myMenusItem.dateString);
            holder1.balance.setText(myMenusItem.balance);
            holder1.dinner.setTextColor(myMenusItem.dinner ? accent : grey);
            holder1.lunch.setTextColor(myMenusItem.lunch ? accent : grey);
            holder1.breakfast.setTextColor(myMenusItem.breakfast ? accent : grey);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Menu_Holder extends RecyclerView.ViewHolder {
        public TextView date, breakfast, lunch, dinner, balance;
        public CardView card;

        public Menu_Holder(View itemView) {
            super(itemView);
            this.date = (TextView) itemView.findViewById(R.id.mymenus_row_date);
            this.balance = (TextView) itemView.findViewById(R.id.mymenus_row_balance);
            this.card = (CardView) itemView.findViewById(R.id.mymenus_row_card);
            this.lunch = (TextView) itemView.findViewById(R.id.mymenus_row_lunch);
            this.dinner = (TextView) itemView.findViewById(R.id.mymenus_row_dinner);
            this.breakfast = (TextView) itemView.findViewById(R.id.mymenus_row_breakfast);
        }
    }
}
