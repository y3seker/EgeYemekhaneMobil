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
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.activities.CancelActivity;
import com.y3seker.egeyemekhanemobil.models.CancelItem;

import java.util.List;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 */
public class CancelRVAdapter extends RecyclerView.Adapter<CancelRVAdapter.CancelHolder> implements CompoundButton.OnCheckedChangeListener {

    private List<CancelItem> items;
    private final int itemLayoutID;
    private int lastPosition = -1;
    private final Context mContext;
    private CancelActivity.CheckedChangeListener checkedChangeListener;

    public CancelRVAdapter(Context c, int itemLayoutID, List<CancelItem> items) {
        this.mContext = c;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.checkedChangeListener = null;
    }

    public void setCheckedChangeListener(CancelActivity.CheckedChangeListener checkedChangeListener) {
        this.checkedChangeListener = checkedChangeListener;
    }

    public void changeList(List<CancelItem> items) {
        this.items = items;
        lastPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public CancelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false);
        final CancelHolder h = new CancelHolder(v);
        h.checkBox.setTag(h);
        if (checkedChangeListener == null)
            throw new IllegalStateException("set checkerListener first!");
        h.checkBox.setOnCheckedChangeListener(this);
        return h;
    }

    @Override
    public void onBindViewHolder(CancelHolder holder, int position) {
        holder.checkBox.setText(items.get(position).text);
        setAnimation(holder.card, position);
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
        CancelHolder h = (CancelHolder) buttonView.getTag();
        int pos = h.getAdapterPosition();
        CancelItem i = items.get(pos);
        i.isChecked = isChecked;
        checkedChangeListener.onChange(i, pos, isChecked);
    }

    public static class CancelHolder extends RecyclerView.ViewHolder {

        final AppCompatCheckBox checkBox;
        final CardView card;

        public CancelHolder(View itemView) {
            super(itemView);
            this.card = (CardView) itemView.findViewById(R.id.cancel_row_card);
            this.checkBox = (AppCompatCheckBox) itemView.findViewById(R.id.cancel_row_checkbox);
        }
    }

}
