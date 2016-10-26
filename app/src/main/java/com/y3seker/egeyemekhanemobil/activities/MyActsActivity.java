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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.okhttp.RequestBody;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.models.MyActsItem;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.ui.MyActsRVAdapter;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus Emre Şeker on 8.10.2015.
 * -
 */
public class MyActsActivity extends BaseActivity {

    private static final String TAG = MyActsActivity.class.getSimpleName();

    private boolean hasPages = false;

    @Bind(R.id.myacts_toolbar)
    Toolbar toolbar;
    @Bind(R.id.myacts_rv)
    RecyclerView recyclerView;

    private List<MyActsItem> actsHistory;
    private Elements pages = null;
    private int nextPage = 1;
    private MyActsRVAdapter rvAdapter;

    private boolean isLoadingPageFailed = false;

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!rvAdapter.isLoading()) {
                if (isLoadingPageFailed)
                    loadPage(nextPage);
                else if (nextPage != pages.size())
                    loadPage(++nextPage);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myacts);
        ButterKnife.bind(this);
        setupToolbar(toolbar, "");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        toolbar.setTitle("");
        actsHistory = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvAdapter = new MyActsRVAdapter(getBaseContext(), R.layout.row_myacts, actsHistory);
        recyclerView.setAdapter(rvAdapter);
        getActsPage();
    }

    private void getActsPage() {
        showProgressBar();
        RetrofitManager.api().getMyHistory().cache()
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
                                getActsPage();
                            }
                        });
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        parseAndUpdateUI(document);
                    }
                });
    }

    private void parseAndUpdateUI(Document doc) throws NullPointerException {

        Element table = doc.getElementById("ctl00_ContentPlaceHolder1_GridView1").child(0);
        final Elements list = table.children();
        list.remove(0);

        if (list.last().select("[align=center]").first() != null) {
            hasPages = true;
            Elements _pages = list.last().select("tr").get(1).children();
            if (_pages.first().text().equals("...")) {
                if (nextPage == pages.size()) {
                    _pages.remove(0);
                    _pages.remove(0);
                    pages.addAll(_pages);
                }
            } else {
                pages = _pages;
            }
            list.remove(list.size() - 1);
        }

        if (hasPages) {
            rvAdapter.setupForPages(R.layout.row_footer, clickListener);
            if (nextPage == pages.size()) {
                rvAdapter.removeFooter();
            }
        }

        hideProgressBar();
        for (Element element : list) {
            actsHistory.add(new MyActsItem(element.children()));
            rvAdapter.notifyItemInserted(actsHistory.size() - 1);
        }

        toolbar.setTitle(doc.getElementById("bk1").text());
    }

    private void loadPage(final int nextPage) {
        rvAdapter.showLoadingFooter(true);
        RequestBody requestBody = ConnectionUtils.febWithViewStates(user.getViewStates())
                .add(ParseConstants.EVENT_ARG, "Page$" + nextPage)
                .add(ParseConstants.EVENT_TARGET, "ctl00$ContentPlaceHolder1$GridView1")
                .build();
        RetrofitManager.api().postMyHistory(requestBody)
                .retry(1)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        isLoadingPageFailed = true;
                        rvAdapter.showErrorFooter();
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        isLoadingPageFailed = false;
                        rvAdapter.showLoadingFooter(false);
                        parseAndUpdateUI(document);
                    }
                });

    }

}
