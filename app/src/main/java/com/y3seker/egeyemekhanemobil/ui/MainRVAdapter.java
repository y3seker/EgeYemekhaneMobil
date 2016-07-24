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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.models.BalanceItem;
import com.y3seker.egeyemekhanemobil.models.MyActsItem;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;

import java.util.List;

/**
 * Created by Yunus Emre Şeker on 26.10.2015.
 * -
 */
public class MainRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Object> list;

    private int grey, accent;


    public MainRVAdapter(Context mContext, List<Object> list) {
        this.mContext = mContext;
        this.list = list;
        grey = mContext.getResources().getColor(R.color.grey_400);
        TypedValue typedValue = new TypedValue();
        TypedArray a = mContext.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        accent = a.getColor(0, 0);
        a.recycle();

    }

    @Override
    public int getItemViewType(int position) {
        Object o = list.get(position);
        if (o instanceof BalanceItem) {
            return 1;
        } else if (o instanceof MyMenusItem) {
            return 2;
        } else if (o instanceof MyActsItem) {
            return 3;
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_balance, parent, false);
                return new BalanceRVAdapter.BalanceHolder(v1);
            case 2:
                View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_mymenus, parent, false);
                return new MyMenusHolder(v2);
            case 3:
                View v3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_myacts, parent, false);
                return new LastActHolder(v3);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyMenusHolder) {
            MyMenusHolder holder1 = (MyMenusHolder) holder;
            MyMenusItem myMenusItem = (MyMenusItem) list.get(position);
            holder1.text.setText(myMenusItem.dateString);
            holder1.dinner.setTextColor(myMenusItem.dinner ? accent : grey);
            holder1.lunch.setTextColor(myMenusItem.lunch ? accent : grey);
            holder1.breakfast.setTextColor(myMenusItem.breakfast ? accent : grey);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyMenusHolder extends RecyclerView.ViewHolder {

        public TextView text, breakfast, lunch, dinner;

        public MyMenusHolder(View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.mymenus_row_date);
            this.lunch = (TextView) itemView.findViewById(R.id.mymenus_row_lunch);
            this.dinner = (TextView) itemView.findViewById(R.id.mymenus_row_dinner);
            this.breakfast = (TextView) itemView.findViewById(R.id.mymenus_row_breakfast);
        }
    }

    public class LastActHolder extends RecyclerView.ViewHolder {
        TextView date, caf, menu_type;
        CardView card;

        public LastActHolder(View itemView) {
            super(itemView);
            this.date = (TextView) itemView.findViewById(R.id.myacts_row_datetime);
            this.caf = (TextView) itemView.findViewById(R.id.myacts_row_caf);
            this.card = (CardView) itemView.findViewById(R.id.myacts_row_card);
            this.menu_type = (TextView) itemView.findViewById(R.id.myacts_row_menutype);
        }
    }
}
