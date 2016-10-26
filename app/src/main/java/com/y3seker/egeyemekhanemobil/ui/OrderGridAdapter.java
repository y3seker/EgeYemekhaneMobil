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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.activities.OrderActivity;
import com.y3seker.egeyemekhanemobil.models.OrderItem;

import java.util.List;
import java.util.Random;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class OrderGridAdapter extends RecyclerView.Adapter<OrderGridAdapter.OrderHolder> implements View.OnClickListener, View.OnLongClickListener {

    private final Context mContext;
    private final List<OrderItem> items;
    private final int itemLayoutID;
    private int lastPosition = -1;
    // COLORS
    private final int grey500;
    private final int grey400;
    private final int white;
    private final int black;
    private final int colorPrimary;
    private final String[] days;
    private final int daysLength;
    private OrderActivity.CheckerListener checkerListener;

    public OrderGridAdapter(Context c, int itemLayoutID, List<OrderItem> items) {
        this.mContext = c;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.checkerListener = null;
        days = new String[]{"P", "S", "Ç", "P", "C"};
        daysLength = days.length;
        grey500 = ContextCompat.getColor(mContext, R.color.grey_500);
        grey400 = ContextCompat.getColor(mContext, R.color.grey_400);
        white = ContextCompat.getColor(mContext, R.color.white);
        black = ContextCompat.getColor(mContext, R.color.black);
        colorPrimary = ContextCompat.getColor(mContext, R.color.colorPrimary);
    }

    public void setCheckerListener(OrderActivity.CheckerListener checkerListener) {
        this.checkerListener = checkerListener;
    }

    public void notifyItemRangeChanged() {
        lastPosition = 4;
        notifyDataSetChanged();
    }

    public void clearList() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false);
        final OrderHolder h = new OrderHolder(v);
        h.checker.setTag(h);
        h.checker.setOnClickListener(this);
        h.checker.setOnLongClickListener(this);
        return h;
    }

    @Override
    public void onBindViewHolder(OrderHolder holder, int position) {
        bind(holder, position);
    }

    private void bind(OrderHolder holder, int position) {
        int checkerV, progressV;
        int checkerColor = 0, bgColor = 0;
        String text = "";
        if (position < daysLength) {
            text = days[position];
            checkerColor = colorPrimary;
            bgColor = 0;
            progressV = View.GONE;
            checkerV = View.VISIBLE;
        } else {
            OrderItem item = getItem(position);
            if (item.isInProgress) {
                checkerV = View.GONE;
                progressV = View.VISIBLE;
            } else {
                progressV = View.GONE;
                checkerV = View.VISIBLE;
                text = item.dayNumber;

                if (item.isChecked) {
                    checkerColor = white;
                    bgColor = R.drawable.round_accent;
                } else if (item.isOrderedBefore) {
                    bgColor = R.drawable.round_grey;
                    checkerColor = white;
                } else if (item.isDisabled) {
                    checkerColor = grey400;
                    bgColor = 0;
                } else {
                    bgColor = 0;
                    checkerColor = black;
                }
            }
        }
        holder.progress.setVisibility(progressV);
        holder.checker.setVisibility(checkerV);
        holder.checker.setTextColor(checkerColor);
        holder.checker.setBackgroundResource(bgColor);
        holder.checker.setText(text);
    }

    private void setAnimation(OrderHolder holder, int anim, boolean randomStartOff) {
        if (lastPosition >= holder.getAdapterPosition())
            return;
        Animation animation = AnimationUtils.loadAnimation(mContext, anim);
        if (randomStartOff) animation.setStartOffset((new Random().nextLong() % 300) + 100);
        holder.itemView.startAnimation(animation);
        lastPosition = holder.getAdapterPosition();
    }

    @Override
    public int getItemCount() {
        return items.size() + daysLength;
    }

    @Override
    public void onClick(View v) {
        OrderHolder holder = (OrderHolder) v.getTag();
        int pos = holder.getAdapterPosition();
        if (pos < daysLength)
            return;
        OrderItem item = getItem(pos);

        if (!item.isDisabled) {
            item.isInProgress = !item.isInProgress;
            item.isChecked = !item.isChecked;
            notifyItemChanged(pos);
            checkerListener.onChange(item, pos, item.isChecked);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        OrderHolder holder = (OrderHolder) v.getTag();
        int pos = holder.getAdapterPosition();
        if (pos < daysLength)
            return true;
        OrderItem item = getItem(pos);
        checkerListener.onLongClick(item, pos);
        return true;
    }

    public void progressDone(boolean succeed, int pos) {
        OrderItem item = getItem(pos);
        item.isChecked = succeed && item.isChecked;
        item.isInProgress = false;
        notifyItemChanged(pos);
    }

    private OrderItem getItem(int pos) {
        return items.get(pos - daysLength);
    }


    public class OrderHolder extends RecyclerView.ViewHolder {

        final TextView checker;
        final ProgressBar progress;

        public OrderHolder(View itemView) {
            super(itemView);
            this.checker = (TextView) itemView.findViewById(R.id.order_row_checker);
            this.progress = (ProgressBar) itemView.findViewById(R.id.order_row_progress);
        }
    }

}
