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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.squareup.okhttp.Call;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.constants.OtherConstants;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;
import com.y3seker.egeyemekhanemobil.utils.AnimUtils;
import com.y3seker.egeyemekhanemobil.utils.Utils;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.NonLoginException;

import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus Emre Şeker on 12.10.2015.
 * -
 */
public class BaseActivity extends RxAppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public int[] revealPos;
    public User user;
    private View rootView;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    Menu menu;
    Map<String, Call> calls;
    private boolean isClosing = false;
    private boolean isSavedState, isRestart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calls = new HashMap<>();
        user = getIntent().getParcelableExtra(ParseConstants.USER);
        revealPos = getIntent().getIntArrayExtra(OtherConstants.REVEAL_POSITION);
        if (user == null)
            throw new NullPointerException("User is null");
        RetrofitManager.setBaseUrl(user.getBaseUrl());
        if (user.getCookie() != null) {
            RetrofitManager.addCookie(user.getCookie());
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        isSavedState = savedInstanceState != null;
        isRestart = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isRestart = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        rootView = findViewById(R.id.root_layout);
        if (!isSavedState && !isRestart)
            reveal(rootView, 850);
    }

    public void reveal(final View view, final int duration) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                AnimUtils.revealFrom(revealPos[0], revealPos[1], view, duration, null);
            }
        });
    }

    public void collapse(final View view, final int duration, final AnimUtils.AnimationListener listener) {
        AnimUtils.collapseTo(revealPos[0], revealPos[1], view, duration, listener);
    }

    public void setupToolbar(Toolbar toolbar, String title) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        if (title == null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        else
            getSupportActionBar().setTitle("");
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void showProgressDialog(final String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void showProgressDialog(int message) {
        showProgressDialog(getString(message));
    }

    public void showProgressDialog() {
        showProgressDialog(null);
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        if (user != null) {
            int icon = 0;
            if (user.getCafeteriaNumber() == 0)
                icon = R.drawable.ic_parking_white_24dp;
            else if (user.getCafeteriaNumber() == 1)
                icon = R.drawable.ic_numeric_1_box_outline_white_24dp;
            else if (user.getCafeteriaNumber() == 2)
                icon = R.drawable.ic_numeric_2_box_outline_white_24dp;
            menu.findItem(R.id.action_user).setIcon(icon);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user:
                new AlertDialog.Builder(this).setTitle(R.string.user_info)
                        .setMessage(user.toString())
                        .create()
                        .show();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (isClosing)
            return;
        isClosing = true;
        Intent i = new Intent();
        i.putExtra(ParseConstants.USER, user);
        setResult(RESULT_OK, i);

        // bottom center of window
        //revealPos[0] = rootView.getWidth() / 2;
        //revealPos[1] = rootView.getHeight();
        collapse(rootView, 600, new AnimUtils.AnimationListener() {
            @Override
            public void onAnimEnd() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    public void onFailed(int why, View.OnClickListener actionListener) {
        dismissProgressDialog();
        Utils.makeSnackBar(this, getString(why))
                .setAction(R.string.try_again, actionListener)
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    public void onException(Exception e) {
        String errorMessage = e.getMessage() != null ? "\nHata Mesajı: " + e.getMessage() : "";
        new AlertDialog.Builder(this)
                .setTitle("Hata!")
                .setMessage("Bir hata ile karşılaşıldı. Bu durum uygulamadan kaynaklı olmayabilir. " +
                        "Geçerli bir bağlantınız olduğundan eminseniz, daha sonra tekrar deneyin."
                        + errorMessage)
                .setPositiveButton("Tekrar Dene", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recreate();
                    }
                })
                .setNegativeButton("Kapat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    public void restart() {
        recreate();
        overridePendingTransition(0, 0);
    }

    <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private void login(final User currentUser) {
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.show();
        progressDialog.setCancelable(false);
        ConnectionUtils.loginObservable(currentUser)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {
                        //progressDialog.setCancelable(true);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        //progressDialog.setCancelable(true);
                        progressDialog.dismiss();
                        onException(((Exception) e));
                    }

                    @Override
                    public void onNext(Document document) {
                        currentUser.setCookie(RetrofitManager.getCookie());
                        restart();
                    }
                });
    }

    public void showOrderFailedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(R.string.order_failed)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                    }
                })
                .create()
                .show();
    }

    public class HandlerSubscriber extends Subscriber<Document> implements HandlerCallback {

        @Override
        public void onCompleted() {
            hideProgressBar();
            dismissProgressDialog();
        }

        @Override
        public void onError(Throwable e) {
            hideProgressBar();
            dismissProgressDialog();
            e.printStackTrace();
            if (e instanceof NonLoginException) {
                Log.e(TAG, "Login Required. Try to login");
                login(user);
            } else {
                onException(e);
            }
        }

        @Override
        public void onNext(Document document) {
            ParseUtils.extractViewState(user.getViewStates(), document);
            onDone(document);
        }

        @Override
        public void onException(Throwable e) {

        }

        @Override
        public void onDone(Document doc) {

        }
    }

    public interface HandlerCallback {
        void onException(Throwable e);

        void onDone(Document doc);
    }
}
