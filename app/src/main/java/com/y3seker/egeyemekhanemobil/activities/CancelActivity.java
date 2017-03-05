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

package com.y3seker.egeyemekhanemobil.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.models.CancelItem;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.ui.CancelRVAdapter;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus Emre Şeker on 9.10.2015.
 * -
 */
public class CancelActivity extends BaseActivity {

    private static final String TAG = CancelActivity.class.getSimpleName();
    @BindView(R.id.root_layout)
    CoordinatorLayout coLayout;
    @BindView(R.id.cancel_toolbar)
    Toolbar toolbar;
    @BindView(R.id.cancel_fab)
    FloatingActionButton fab;
    @BindView(R.id.cancel_rv)
    RecyclerView recyclerView;
    @BindView(R.id.cancel_no_item)
    TextView noItemInfo;
    private String BASE_URL = "";
    private String URL = "";
    private List<CancelItem> cancelItems;
    private CancelRVAdapter rvAdapter;
    private int checkedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel);
        ButterKnife.bind(this);
        setupToolbar(toolbar, getString(R.string.cancel_menu));
        cancelItems = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rvAdapter = new CancelRVAdapter(this, R.layout.row_cancel, cancelItems);
        recyclerView.setAdapter(rvAdapter);
        rvAdapter.setCheckedChangeListener(new CheckedChangeListener() {
            @Override
            public void onChange(CancelItem cancelItem, int pos, boolean isChecked) {
                checkedCount += isChecked ? 1 : -1;
                if (checkedCount > 0)
                    fab.show();
                else
                    fab.hide();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postTheCancel();
            }
        });
        getFirstPage();
    }

    private void getFirstPage() {
        showProgressBar();
        RetrofitManager.api().getCancel().cache()
                .retry(2)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        onFailed(R.string.connection_error, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getFirstPage();
                            }
                        });
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        changeItemsUI(document);
                    }
                });
    }

    private void changeItemsUI(Document doc) throws NullPointerException {
        cancelItems.clear();
        Elements rows = doc.getElementById("ctl00_ContentPlaceHolder1_Table1").select("tbody").first().children();

        rows.remove(0);
        for (Element row : rows) {
            String text = row.text();
            cancelItems.add(new CancelItem(text, row.select("input").attr("name")));
        }
        hideProgressBar();
        fab.hide();
        rvAdapter.changeList(cancelItems);
        if (cancelItems.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noItemInfo.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noItemInfo.setVisibility(View.GONE);
        }
    }

    private void postTheCancel() {
        fab.hide();
        showProgressBar();
        FormBody.Builder feb = ConnectionUtils.febWithViewStates(user.getViewStates());
        for (CancelItem cancelItem : cancelItems) {
            if (cancelItem.isChecked) {
                feb.add(cancelItem.name, "on");
            }
        }
        RequestBody formBody = feb
                .add(ParseConstants.EVENT_TARGET, "ctl00$ContentPlaceHolder1$Button2")
                .add(ParseConstants.EVENT_ARG, "")
                .build();
        RetrofitManager.api().postCancel(formBody)
                .retry(1)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        hideProgressBar();
                        onFailed(R.string.connection_error, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                postTheCancel();
                            }
                        });
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        changeItemsUI(document);
                    }
                });
    }


    public interface CheckedChangeListener {
        void onChange(CancelItem cancelItem, int pos, boolean isChecked);
    }

}
