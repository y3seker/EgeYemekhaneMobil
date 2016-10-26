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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;
import com.y3seker.egeyemekhanemobil.utils.Utils;

import java.util.List;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class MenuRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MyMenusItem> items;
    private final int itemLayoutID;

    private final int grey;
    private final int accent;

    public MenuRVAdapter(Context c, int itemLayoutID, List<MyMenusItem> items) {
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        grey = ContextCompat.getColor(c, R.color.grey_400);
        accent = ContextCompat.getColor(c, R.color.colorAccent);
    }


    public void changeList(List<MyMenusItem> items) {
        this.items = items;
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
            holder1.date.setText(Utils.myMenusDateStringFormat.format(myMenusItem.date.getTime()));

            holder1.dinner.setTextColor(myMenusItem.dinner ? accent : grey);
            holder1.lunch.setTextColor(myMenusItem.lunch ? accent : grey);
            holder1.breakfast.setTextColor(myMenusItem.breakfast ? accent : grey);
            holder1.iftar.setTextColor(myMenusItem.iftar ? accent : grey);

            /*
            // Toogles the visibility of menus
            holder1.dinner.setVisibility(myMenusItem.dinner ? View.VISIBLE : View.GONE);
            holder1.lunch.setVisibility(myMenusItem.lunch ? View.VISIBLE : View.GONE);
            holder1.breakfast.setVisibility(myMenusItem.breakfast ? View.VISIBLE : View.GONE);
            holder1.iftar.setVisibility(myMenusItem.iftar ? View.VISIBLE : View.GONE);
            */
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class Menu_Holder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView breakfast;
        final TextView lunch;
        final TextView dinner;
        final TextView iftar;
        final CardView card;

        Menu_Holder(View itemView) {
            super(itemView);
            this.date = (TextView) itemView.findViewById(R.id.mymenus_row_date);
            this.iftar = (TextView) itemView.findViewById(R.id.mymenus_row_iftar);
            this.card = (CardView) itemView.findViewById(R.id.mymenus_row_card);
            this.lunch = (TextView) itemView.findViewById(R.id.mymenus_row_lunch);
            this.dinner = (TextView) itemView.findViewById(R.id.mymenus_row_dinner);
            this.breakfast = (TextView) itemView.findViewById(R.id.mymenus_row_breakfast);
        }
    }
}
