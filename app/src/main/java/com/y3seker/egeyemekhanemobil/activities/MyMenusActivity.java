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

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.squareup.okhttp.RequestBody;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.Utils;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;
import com.y3seker.egeyemekhanemobil.ui.MenuRVAdapter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus Emre Şeker on 8.10.2015.
 * -
 */
public class MyMenusActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = MyMenusActivity.class.getSimpleName();
    private static final String NO_RESULT = "ctl00_ContentPlaceHolder1_Label2";

    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private Calendar fromDate, toDate;
    private boolean isFirstPageLoaded;
    private String URL;

    @Bind(R.id.root_layout)
    CoordinatorLayout coLayout;
    @Bind(R.id.mymenus_toolbar)
    Toolbar toolbar;
    @Bind(R.id.mymenus_appbar)
    AppBarLayout appBar;
    @Bind(R.id.mymenus_fab)
    FloatingActionButton fab;
    @Bind(R.id.mymenus_end_date)
    EditText toDateText;
    @Bind(R.id.mymenus_start_date)
    EditText fromDateText;
    @Bind(R.id.mymenus_rv)
    RecyclerView recyclerView;

    Map<String, MyMenusItem> myMenuz;
    MenuRVAdapter rvAdapter;
    Snackbar noResultSnackBar;
    CoordinatorLayout.LayoutParams fabLP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mymenus);
        ButterKnife.bind(this);
        setupToolbar(toolbar, "");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        myMenuz = new TreeMap<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        rvAdapter = new MenuRVAdapter(this, R.layout.row_mymenus, new ArrayList<>(myMenuz.values()));
        recyclerView.setAdapter(rvAdapter);
        isFirstPageLoaded = false;
        setupPickers();
        fromDateText.setOnClickListener(this);
        toDateText.setOnClickListener(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noResultSnackBar != null)
                    noResultSnackBar.dismiss();
                if (isFirstPageLoaded)
                    postDates();
                else
                    getFirstPage();
                hideFab();
            }
        });

        fabLP = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        hideFab();
        getFirstPage();
    }

    private void getFirstPage() {
        showProgressBar();
        RetrofitManager.api().getMyMenus().cache()
                .retry(2)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        isFirstPageLoaded = false;
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
                        isFirstPageLoaded = true;
                        postDates();
                    }
                });
    }

    private void postDates() {
        showProgressBar();
        rvAdapter.clearList();
        RequestBody formBody = getMyMenusRequestBody(user.getViewStates(), fromDate, toDate);
        RetrofitManager.api().postMyMenus(formBody)
                .retry(2)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        showProgressDialog();
                        postDates();
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        parseAndUpdateUI(document);
                    }
                });
    }

    private void parseAndUpdateUI(final Document doc) throws NullPointerException {
        myMenuz = parseMenus(doc);
        if (myMenuz == null) {
            hideProgressBar();
            rvAdapter.clearList();
            noResultSnackBar = Snackbar.make(coLayout, doc.getElementById(NO_RESULT).text().trim(), Snackbar.LENGTH_INDEFINITE);
            noResultSnackBar.show();
            return;
        }
        List<MyMenusItem> myMenusItemList = new ArrayList<>(myMenuz.values());
        rvAdapter.changeList(myMenusItemList);
        hideProgressBar();
        toolbar.setTitle(doc.select("[class=ogunust]").first().text());
    }

    public static Map<String, MyMenusItem> parseMenus(Document doc) throws NullPointerException {
        Map<String, MyMenusItem> map = new TreeMap<>();
        if (doc.getElementById(NO_RESULT) != null && doc.getElementById(NO_RESULT).hasText()) {
            return null;
        }
        Element table = doc.getElementById("ctl00_ContentPlaceHolder1_GridView1").child(0);
        final Elements list = table.children();
        list.remove(0);

        for (Element element : list) {
            String date = element.children().get(2).text();
            String rDate = Utils.getReverseDateString(date); //Utils.getReverseDateString(date);
            if (map.containsKey(rDate)) {
                map.get(rDate).updateMeals(element.children().get(3).text());
            } else {
                map.put(rDate, new MyMenusItem(element.children()));
            }
        }
        return map;
    }

    public static RequestBody getMyMenusRequestBody(HashMap<String, String> viewStates, Calendar from, Calendar to) {
        return ConnectionUtils.febWithViewStates(viewStates)
                .add("ctl00$ContentPlaceHolder1$DropDownList2", Utils.twoDigit.format(from.get(Calendar.DAY_OF_MONTH)))
                .add("ctl00$ContentPlaceHolder1$DropDownList3", Utils.twoDigit.format(from.get(Calendar.MONTH) + 1))
                .add("ctl00$ContentPlaceHolder1$DropDownList4", String.valueOf(from.get(Calendar.YEAR)))
                .add("ctl00$ContentPlaceHolder1$DropDownList5", Utils.twoDigit.format(to.get(Calendar.DAY_OF_MONTH)))
                .add("ctl00$ContentPlaceHolder1$DropDownList6", Utils.twoDigit.format(to.get(Calendar.MONTH) + 1))
                .add("ctl00$ContentPlaceHolder1$DropDownList7", String.valueOf(to.get(Calendar.YEAR)))
                .add("ctl00$ContentPlaceHolder1$DropDownList1", "K,O,A")
                .add("ctl00$ContentPlaceHolder1$Button2", "Sorgula")
                .build();
    }

    private void setupPickers() {
        fromDate = Calendar.getInstance();
        toDate = Calendar.getInstance();
        if (Utils.isTodayWeekend()) {
            fromDate.add(Calendar.DAY_OF_MONTH, 2);
            toDate.add(Calendar.DAY_OF_MONTH, 2);
        }
        fromDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        toDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

        String f = fromDate.get(Calendar.DAY_OF_MONTH) + "." + (fromDate.get(Calendar.MONTH) + 1) + "." + fromDate.get(Calendar.YEAR);
        fromDateText.setText(f);
        String t = toDate.get(Calendar.DAY_OF_MONTH) + "." + (toDate.get(Calendar.MONTH) + 1) + "." + toDate.get(Calendar.YEAR);
        toDateText.setText(t);

        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                fromDate.set(year, monthOfYear, dayOfMonth);
                String date = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
                if (!fromDateText.getText().toString().equals(date)) showFab();
                fromDateText.setText(date);
            }

        }, fromDate.get(Calendar.YEAR), fromDate.get(Calendar.MONTH), fromDate.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                toDate.set(year, monthOfYear, dayOfMonth);
                String date = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
                if (!toDateText.getText().toString().equals(date)) showFab();
                toDateText.setText(date);
            }

        }, toDate.get(Calendar.YEAR), toDate.get(Calendar.MONTH), toDate.get(Calendar.DAY_OF_MONTH));
    }

    private void hideFab() {
        fabLP.setAnchorId(View.NO_ID);
        fab.setVisibility(View.GONE);
        fab.setLayoutParams(fabLP);
    }

    private void showFab() {
        fabLP.setAnchorId(appBar.getId());
        fab.setLayoutParams(fabLP);
        fab.show();
    }

    @Override
    public void onClick(View v) {
        if (v == fromDateText) {
            fromDatePickerDialog.show();
        } else if (v == toDateText) {
            toDatePickerDialog.show();
        }
    }

}
