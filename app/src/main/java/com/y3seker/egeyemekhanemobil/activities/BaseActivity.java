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

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.UserManager;
import com.y3seker.egeyemekhanemobil.constants.OtherConstants;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.NonLoginException;
import com.y3seker.egeyemekhanemobil.utils.AnimUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;
import com.y3seker.egeyemekhanemobil.utils.Utils;

import org.jsoup.nodes.Document;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus Emre Şeker on 12.10.2015.
 * -
 */
public class BaseActivity extends RxAppCompatActivity {

    static final String TAG = BaseActivity.class.getSimpleName();
    User user;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    private int[] revealPos;
    private View rootView;
    private boolean isClosing = false;
    private boolean isSavedState, isRestart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = UserManager.getInstance().getCurrentUser();
        revealPos = getIntent().getIntArrayExtra(OtherConstants.REVEAL_POSITION);
        if (user == null)
            throw new NullPointerException("User is null");
        RetrofitManager.setBaseUrl(user.getBaseUrl());
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

    private void reveal(final View view, final int duration) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
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

    private void collapse(final View view, final int duration, final AnimUtils.AnimationListener listener) {
        AnimUtils.collapseTo(revealPos[0], revealPos[1], view, duration, listener);
    }

    void setupToolbar(Toolbar toolbar, String title) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        if (title == null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        else
            getSupportActionBar().setTitle("");
    }

    void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    void showProgressDialog(final String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void showProgressDialog(int message) {
        showProgressDialog(getString(message));
    }

    void showProgressDialog() {
        showProgressDialog(null);
    }

    void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
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
    public void onBackPressed() {
        if (isClosing)
            return;
        isClosing = true;
        setResult(RESULT_OK);

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

    void onFailed(int why, View.OnClickListener actionListener) {
        dismissProgressDialog();
        Utils.makeSnackBar(this, getString(why))
                .setAction(R.string.try_again, actionListener)
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    void onException(Exception e) {
        String errorMessage = e.getMessage() != null ? getString(R.string.error_message) + e.getMessage() : "";
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.unknown_error)
                        + errorMessage)
                .setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recreate();
                    }
                })
                .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    void restart() {
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

    void login(final User currentUser) {
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.show();
        progressDialog.setCancelable(false);
        UserManager.getInstance().login(this, new Subscriber<User>() {
            @Override
            public void onCompleted() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                progressDialog.dismiss();
                onException((Exception) e);
                e.printStackTrace();
            }

            @Override
            public void onNext(User user) {
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

    public interface HandlerCallback {
        void onException(Throwable e);

        void onDone(Document doc);
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
            onDone(document);
        }

        @Override
        public void onException(Throwable e) {

        }

        @Override
        public void onDone(Document doc) {

        }
    }
}
