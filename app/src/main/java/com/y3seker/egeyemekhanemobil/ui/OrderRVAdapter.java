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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.activities.OrderActivity;
import com.y3seker.egeyemekhanemobil.models.OrderItem;

import java.util.List;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class OrderRVAdapter extends RecyclerView.Adapter<OrderRVAdapter.OrderHolder> implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    List<OrderItem> items;
    int itemLayoutID;
    int lastPosition = -1;
    Context mContext;
    OrderActivity.CheckerListener checkerListener;

    public OrderRVAdapter(Context c, int itemLayoutID, List<OrderItem> items) {
        this.mContext = c;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.checkerListener = null;
    }

    public void setCheckerListener(OrderActivity.CheckerListener checkerListener) {
        this.checkerListener = checkerListener;
    }

    public void changeList(List<OrderItem> items) {
        this.items = items;
        lastPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false);
        final OrderHolder h = new OrderHolder(v);
        h.card.setTag(h);

        if (checkerListener == null)
            throw new IllegalStateException("set checkerListener first!");
        h.card.setOnClickListener(this);
        return h;
    }

    @Override
    public void onBindViewHolder(OrderHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.text.setText(item.text);
        holder.checker.setBackgroundResource(item.isChecked ? R.drawable.round_accent : R.drawable.round_grey);
        holder.checker.setVisibility(item.isInProgress ? View.GONE : View.VISIBLE);
        holder.progress.setVisibility(item.isInProgress ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        OrderHolder h = (OrderHolder) buttonView.getTag();
        int pos = h.getAdapterPosition();
        OrderItem i = items.get(pos);
        i.isChecked = isChecked;
        checkerListener.onChange(i, pos, isChecked);
    }

    @Override
    public void onClick(View v) {
        OrderHolder holder = (OrderHolder) v.getTag();
        int pos = holder.getAdapterPosition();
        OrderItem item = items.get(pos);
        item.isInProgress = !item.isInProgress;
        item.isChecked = !item.isChecked;
        notifyItemChanged(pos);
        checkerListener.onChange(item, pos, item.isChecked);
    }

    public void progressDone(boolean succeed, int pos) {
        items.get(pos).isChecked = succeed && items.get(pos).isChecked;
        items.get(pos).isInProgress = false;
        notifyItemChanged(pos);
    }

    public class OrderHolder extends RecyclerView.ViewHolder {

        TextView text, checker;
        ProgressBar progress;

        CardView card;

        public OrderHolder(View itemView) {
            super(itemView);
            this.card = (CardView) itemView.findViewById(R.id.order_row_card);
            this.text = (TextView) itemView.findViewById(R.id.order_row_text);
            this.checker = (TextView) itemView.findViewById(R.id.order_row_checker);
            this.progress = (ProgressBar) itemView.findViewById(R.id.order_row_progress);
        }
    }

}
