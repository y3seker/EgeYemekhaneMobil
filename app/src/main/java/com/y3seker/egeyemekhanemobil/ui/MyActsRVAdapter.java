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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.activities.MyActsActivity;
import com.y3seker.egeyemekhanemobil.models.MyActsItem;

import java.util.List;

/**
 * Created by Yunus Emre Şeker on 24.10.2015.
 * -
 */
public class MyActsRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<MyActsItem> items;
    Context mContext;
    int itemLayoutID;
    int footerLayoutID;
    int lastPosition = -1;
    boolean hasPages;
    boolean isLoading = false;
    boolean isLoadingFailed = false;
    View.OnClickListener moreButtonListener;
    private boolean footerRemoved = false;

    public MyActsRVAdapter(Context mContext, int itemLayoutID, List<MyActsItem> items) {
        this.mContext = mContext;
        this.itemLayoutID = itemLayoutID;
        this.items = items;
        this.hasPages = false;
    }

    public MyActsRVAdapter(Context mContext, int itemLayoutID, int footerLayoutID, List<MyActsItem> items, boolean hasPages, View.OnClickListener moreButtonListener) {
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

    public void setMoreButtonListener(View.OnClickListener l) {
        moreButtonListener = l;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false);
            return new MyActsHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(footerLayoutID, parent, false);
            FooterHolder holder = new FooterHolder(v);
            if (moreButtonListener == null)
                throw new IllegalStateException("set onClickListener first!");
            holder.card.setOnClickListener(moreButtonListener);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyActsHolder) {
            MyActsHolder holder1 = (MyActsHolder) holder;
            MyActsItem myActsItem_ = items.get(position);
            holder1.date.setText(myActsItem_.date_time);
            holder1.caf.setText(myActsItem_.caf);
            holder1.menu_type.setText(myActsItem_.menu_type);
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

    @Override
    public int getItemViewType(int position) {
        if (hasPages && position == items.size()) return 1;
        else return 0;
    }

    @Override
    public int getItemCount() {
        return hasPages ? items.size() + 1 : items.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public class MyActsHolder extends RecyclerView.ViewHolder {
        TextView date, caf, menu_type;
        CardView card;

        public MyActsHolder(View itemView) {
            super(itemView);
            this.date = (TextView) itemView.findViewById(R.id.myacts_row_datetime);
            this.caf = (TextView) itemView.findViewById(R.id.myacts_row_caf);
            this.card = (CardView) itemView.findViewById(R.id.myacts_row_card);
            this.menu_type = (TextView) itemView.findViewById(R.id.myacts_row_menutype);
        }
    }

    public class FooterHolder extends RecyclerView.ViewHolder {
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
