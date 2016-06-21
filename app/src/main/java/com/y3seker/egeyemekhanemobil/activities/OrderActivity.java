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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;
import com.trello.rxlifecycle.ActivityEvent;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.models.OrderItem;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.OrderSessionException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.RequestBlockedException;
import com.y3seker.egeyemekhanemobil.ui.OrderGridAdapter;
import com.y3seker.egeyemekhanemobil.ui.WrappableGridLayoutManager;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.EVENT_ARG;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.EVENT_TARGET;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.HARCANAN_BAKIYE;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.KALAN_BAKIYE;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.NEXT;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.ON;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.VERIFIED;
import static com.y3seker.egeyemekhanemobil.constants.ParseConstants.YETERSIZ_BAKIYE;

/**
 * Created by Yunus Emre Şeker on 9.10.2015.
 * -
 */
public class OrderActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = OrderActivity.class.getSimpleName();

    private List<OrderItem> orderItems;
    private OrderGridAdapter rvAdapter;
    private boolean inProgress = false;
    private CoordinatorLayout.LayoutParams fabLP;

    @Bind(R.id.root_layout)
    CoordinatorLayout coLayout;
    @Bind(R.id.order_appbar)
    AppBarLayout appBar;
    @Bind(R.id.order_toolbar)
    Toolbar toolbar;
    @Bind(R.id.order_spinner)
    AppCompatSpinner spinner;
    @Bind(R.id.order_fab)
    FloatingActionButton fab;
    @Bind(R.id.order_kalan_text)
    TextView kalanText;
    @Bind(R.id.order_harcanan_text)
    TextView harcananText;
    @Bind(R.id.order_progress)
    ProgressBar progressBar;
    @Bind(R.id.order_divider)
    View cardDivider;
    @Bind(R.id.order_rv)
    RecyclerView recyclerView;

    CompositeSubscription postSubs;
    Subscription firstPageSub;
    List<Subscription> subs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ButterKnife.bind(this);
        setupToolbar(toolbar, null);
        postSubs = new CompositeSubscription();
        orderItems = new ArrayList<>();
        subs = new ArrayList<>();
        recyclerView.setLayoutManager(new WrappableGridLayoutManager(this, 5));
        rvAdapter = new OrderGridAdapter(this, R.layout.row_order_grid, orderItems);
        recyclerView.setAdapter(rvAdapter);
        spinner.setOnItemSelectedListener(this);
        rvAdapter.setCheckerListener(new OrderActivity.CheckerListener() {
            @Override
            public void onChange(OrderItem orderItem, int pos, boolean isChecked) {
                postItem(orderItem.name, pos);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inProgress) postTheOrder();
            }
        });
        fabLP = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        dismissProgressDialog();
    }

    private void getFirstPage(final int menuType) {
        showProgressBar();
        unsubscribeAll();
        firstPageSub = RetrofitManager.api().getOrder().cache()
                .flatMap(new Func1<Document, Observable<?>>() {
                    @Override
                    public Observable<?> call(Document document) {
                        user.setViewStates(ParseUtils.extractViewState((document)));
                        RequestBody requestBody = ConnectionUtils.febWithViewStates(user.getViewStates())
                                .add("ctl00$ContentPlaceHolder1$Button1", "İleri")
                                .add("ctl00$ContentPlaceHolder1$osec", "RadioButton" + menuType)
                                .build();
                        return RetrofitManager.api().postOrder(requestBody);
                    }
                })
                .retry(2)
                .compose(this.bindUntilEvent(ActivityEvent.STOP))
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        progressBar.setVisibility(View.GONE);
                        onFailed(R.string.connection_error, Snackbar.LENGTH_INDEFINITE,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getFirstPage(menuType);
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
        Log.i(TAG,"Parsing order objects");
        if (orderItems.size() != 0) {
            orderItems.clear();
            rvAdapter.notifyDataSetChanged();
        }

        Elements rows = doc.select("[class=tsec]").first().select("tbody").first().children();

        rows.remove(0);
        for (Element row : rows) {
            Elements items = row.children();
            for (Element item : items) {
                if (item.children().size() != 0) {
                    boolean isOrderedBefore = item.select("font").attr("color").contentEquals("Maroon");
                    boolean isDisabled = item.select("span").hasAttr("disabled");
                    String dateString;
                    try {
                        dateString = item.select("a").attr("href").substring(22, 31 + 1).replace("&", "").replace("o","");
                    } catch (StringIndexOutOfBoundsException e) {
                        continue;
                    }
                    if (dateString.length() == 8)
                        dateString = dateString.replaceFirst("\\.", ".0");
                    if (dateString.length() == 9)
                        dateString = "0" + dateString;
                    orderItems.add(new OrderItem(item.text(), item.select("span").select("input").attr("name"),
                            dateString, isDisabled, isOrderedBefore));
                }
            }
        }
        Collections.sort(orderItems);
        hideProgressBar();
        rvAdapter.notifyItemRangeChanged();
        harcananText.setText(doc.getElementById(HARCANAN_BAKIYE).text().replace("TL", " TL"));
        kalanText.setText(doc.getElementById(KALAN_BAKIYE).text().replace("TL", " TL"));
    }

    private void postItem(String name, final int pos) {
        hideFab();
        FormEncodingBuilder feb = ConnectionUtils.febWithViewStates(user.getViewStates());
        boolean hasCheckedItem = false;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.isChecked) {
                feb.add(orderItem.name, ON);
                hasCheckedItem = true;
            }
        }
        final boolean finalHasCheckItem = hasCheckedItem;
        RequestBody formBody = feb.add(EVENT_TARGET, name)
                .add(EVENT_ARG, "").build();
        Subscription sub = RetrofitManager.api().postOrder(formBody)
                .retry(1)
                .compose(this.bindUntilEvent(ActivityEvent.STOP))
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {
                        if (finalHasCheckItem) showFab();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (finalHasCheckItem) showFab();
                        rvAdapter.progressDone(false, pos);
                    }

                    @Override
                    public void onNext(Document document) {
                        rvAdapter.progressDone(true, pos);
                        harcananText.setText(document.getElementById(HARCANAN_BAKIYE).text().replace("TL", " TL"));
                        kalanText.setText(document.getElementById(KALAN_BAKIYE).text().replace("TL", " TL"));
                    }
                });
        subs.add(sub);
    }

    private void postTheOrder() {
        showProgressDialog("Sipariş işleniyor");
        FormEncodingBuilder feb = ConnectionUtils.febWithViewStates(user.getViewStates());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.isChecked) {
                feb.add(orderItem.name, "on");
            }
        }
        RequestBody postOrderBody = feb
                .add(EVENT_TARGET, "")
                .add(EVENT_ARG, "")
                .add("ctl00$ContentPlaceHolder1$Button3", NEXT)
                .build();
        RetrofitManager.api().postOrder(postOrderBody)
                .retry(1)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        dismissProgressDialog();
                        onFailed(R.string.connection_error, Snackbar.LENGTH_LONG, null);
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        dismissProgressDialog();
                        orderControlDialog(document);
                    }
                });
    }

    private void orderControlDialog(Document doc) throws NullPointerException {
        if (doc.getElementById(YETERSIZ_BAKIYE) != null) {
            Snackbar.make(coLayout, "Yetersiz Bakiye", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Elements rows = doc.getElementById("ctl00_ContentPlaceHolder1_Table1").select("tbody").first().children();
        rows.remove(0);
        String orders = "";
        for (Element row : rows) {
            orders += row.child(0).text().replace("(menü)", "") + "\n";
        }

        new AlertDialog.Builder(this, R.style.ControlDialog)
                .setTitle(spinner.getSelectedItem().toString())
                .setCancelable(false)
                .setMessage(orders)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelTheOrder();
                    }
                })
                .setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verifyTheOrder();
                    }
                })
                .create()
                .show();
    }

    private void cancelTheOrder() {
        showProgressDialog("Sipariş İptal Ediliyor");
        RetrofitManager.api().getOrder()
                .retry(3)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        restart();
                        rvAdapter.clearList();
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        dismissProgressDialog();
                        rvAdapter.clearList();
                        getFirstPage(getMenuType());
                    }
                });
    }

    private void verifyTheOrder() {
        showProgressDialog("Sipariş Onaylanıyor");
        RequestBody verifyOrderBody = ConnectionUtils.febWithViewStates(user.getViewStates())
                .add(EVENT_TARGET, "ctl00$ContentPlaceHolder1$Button6")
                .add(EVENT_ARG, "")
                .build();
        RetrofitManager.api().postOrder(verifyOrderBody)
                .retry(1)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HandlerSubscriber() {
                    @Override
                    public void onException(Throwable e) {
                        super.onException(e);
                        dismissProgressDialog();
                        if (e instanceof OrderSessionException) {
                            getFirstPage(getMenuType());
                            Snackbar.make(coLayout, "Beklenmedik bir hata oluştu, lütfen siparişi tekrarlayın.",
                                    Snackbar.LENGTH_LONG).show();
                        } else if (e instanceof RequestBlockedException) {
                            restart();
                            Snackbar.make(coLayout, "Beklenmedik bir hata oluştu, lütfen siparişi tekrarlayın.",
                                    Snackbar.LENGTH_LONG).show();
                        } else
                            onFailed(R.string.connection_error, Snackbar.LENGTH_LONG, null);
                    }

                    @Override
                    public void onDone(Document document) {
                        super.onDone(document);
                        dismissProgressDialog();
                        showDoneDialog(document);
                    }
                });
    }

    private void showDoneDialog(Document doc) {
        String doneMessage = doc.getElementById(VERIFIED).text();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DoneDialog);
        builder.setTitle(R.string.congrats)
                .setMessage(doneMessage)
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                })
                .setPositiveButton(R.string.new_order, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                    }
                })
                .create()
                .show();
    }

    public void onFailed(String why, int duration, View.OnClickListener actionListener) {
        Snackbar.make(coLayout, why, duration)
                .setAction(R.string.try_again, actionListener)
                .show();
    }

    public void onFailed(int message, int duration, View.OnClickListener actionListener) {
        onFailed(getString(message), duration, actionListener);
    }

    public int getMenuType() {
        return spinner.getSelectedItemPosition() + 2;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        hideFab();
        getFirstPage(position + 1);
    }

    private void unsubscribeAll() {
        for (Subscription sub : subs) {
            sub.unsubscribe();
        }
        subs.clear();
        if (firstPageSub != null) firstPageSub.unsubscribe();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        cardDivider.setVisibility(View.GONE);
        inProgress = true;
        fab.hide();
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        cardDivider.setVisibility(View.VISIBLE);
        inProgress = false;
    }

    private void hideFab() {
        fabLP.setAnchorId(View.NO_ID);
        fab.setVisibility(View.GONE);
        fab.setLayoutParams(fabLP);
    }

    private void showFab() {
        fabLP.setAnchorId(coLayout.getId());
        fab.setLayoutParams(fabLP);
        fab.show();
    }

    public interface CheckerListener {
        void onChange(OrderItem item, int pos, boolean isChecked);
    }
}
