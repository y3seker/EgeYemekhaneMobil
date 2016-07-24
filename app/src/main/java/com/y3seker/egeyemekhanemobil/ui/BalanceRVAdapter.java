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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.models.BalanceItem;

import java.util.List;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class BalanceRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    int itemLayoutID;
    int footerLayoutID;
    int lastPosition = -1;
    boolean hasPages;
    boolean isLoading = false;
    boolean isLoadingFailed = false;
    boolean footerRemoved = false;
    List<BalanceItem> items;
    View.OnClickListener moreButtonListener;
    Context mContext;

    public int RED, REDD, GREEN, GREEND;

    public BalanceRVAdapter(Context mContext, int itemLayoutID, List<BalanceItem> items) {
        this.mContext = mContext;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.hasPages = false;

        RED = ContextCompat.getColor(mContext, R.color.red_600);
        REDD = ContextCompat.getColor(mContext, R.color.red_700);
        GREEN = ContextCompat.getColor(mContext, R.color.green_600);
        GREEND = ContextCompat.getColor(mContext, R.color.green_700);
    }

    public BalanceRVAdapter(Context mContext, int itemLayoutID, int footerLayoutID, List<BalanceItem> items, boolean hasPages, View.OnClickListener moreButtonListener) {
        this(mContext, itemLayoutID, items);
        this.hasPages = hasPages;
        this.footerLayoutID = footerLayoutID;
        this.moreButtonListener = moreButtonListener;
    }

    public void setupForPages(int footerLayoutID, View.OnClickListener moreButtonListener) {
        if (footerRemoved)
            return;
        this.hasPages = true;
        this.footerRemoved = false;
        this.footerLayoutID = footerLayoutID;
        this.moreButtonListener = moreButtonListener;
    }

    public void removeFooter() {
        if (!hasPages || footerRemoved)
            return;
        int footerPos = getItemCount();
        hasPages = false;
        footerRemoved = true;
        notifyItemRemoved(footerPos);
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false);
            return new BalanceHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(footerLayoutID, parent, false);
            FooterHolder holder = new FooterHolder(v);
            holder.card.setOnClickListener(moreButtonListener);
            return holder;
        }
    }

    private void setAnimation(View viewToAnimate) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.balance_item_anim);
        viewToAnimate.startAnimation(animation);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BalanceHolder) {
            BalanceHolder holder1 = (BalanceHolder) holder;
            BalanceItem balanceItem = items.get(position);
            holder1.yer.setText(balanceItem.yer);
            holder1.tarih.setText(balanceItem.tarih);
            holder1.time.setText(balanceItem.zaman);
            holder1.sonBakiye.setText(balanceItem.sonBakiye);
            holder1.islemTutari.setText(balanceItem.islemTutari);
            if (balanceItem.isCashOutflow()) {
                holder1.islemTutari.setTextColor(RED);
                holder1.divider.setBackgroundColor(RED);
            } else {
                holder1.islemTutari.setTextColor(GREEN);
                holder1.divider.setBackgroundColor(GREEN);
            }
        } else {
            FooterHolder footerHolder = (FooterHolder) holder;
            if (isLoading) {
                footerHolder.loadingProgress.setVisibility(View.VISIBLE);
                footerHolder.errorText.setVisibility(View.GONE);
                footerHolder.text.setVisibility(View.GONE);
            } else {
                if (isLoadingFailed) {
                    footerHolder.errorText.setVisibility(View.VISIBLE);
                    footerHolder.loadingProgress.setVisibility(View.GONE);
                    footerHolder.text.setVisibility(View.GONE);
                } else {
                    footerHolder.text.setVisibility(View.VISIBLE);
                    footerHolder.loadingProgress.setVisibility(View.GONE);
                    footerHolder.errorText.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (hasPages && position == items.size()) return 1;
        else return 0;
    }

    @Override
    public int getItemCount() {
        return hasPages && !footerRemoved ? items.size() + 1 : items.size();
    }

    public void showLoadingFooter(boolean isLoading) {
        this.isLoading = isLoading;
        this.isLoadingFailed = false;
        notifyItemChanged(getItemCount() - 1);
    }

    public void showErrorFooter() {
        this.isLoading = false;
        this.isLoadingFailed = true;
        notifyItemChanged(getItemCount() - 1);
    }

    public static class BalanceHolder extends RecyclerView.ViewHolder {
        TextView tarih;
        TextView time;
        TextView yer;
        TextView islemTutari;
        TextView sonBakiye;
        View divider;
        CardView card;

        public BalanceHolder(View itemView) {
            super(itemView);
            this.tarih = (TextView) itemView.findViewById(R.id.balance_row_tarih);
            this.time = (TextView) itemView.findViewById(R.id.balance_row_time);
            this.yer = (TextView) itemView.findViewById(R.id.balance_row_yer);
            this.islemTutari = (TextView) itemView.findViewById(R.id.balance_row_it);
            this.sonBakiye = (TextView) itemView.findViewById(R.id.balance_row_sont);
            this.divider = itemView.findViewById(R.id.balance_row_divider);
            this.card = (CardView) itemView.findViewById(R.id.row_card);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        TextView text;
        TextView errorText;
        ProgressBar loadingProgress;
        CardView card;

        public FooterHolder(View itemView) {
            super(itemView);
            this.text = (TextView) itemView.findViewById(R.id.footer_more_text);
            this.card = (CardView) itemView.findViewById(R.id.footer_more_card);
            this.loadingProgress = (ProgressBar) itemView.findViewById(R.id.footer_loading_progress);
            this.errorText = (TextView) itemView.findViewById(R.id.footer_error_text);
        }
    }
}
