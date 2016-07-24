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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.SerializableHttpCookie;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;

import org.jsoup.nodes.Document;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Yunus Emre Şeker on 7.10.2015.
 * -
 */
public class AddUserActivity extends RxAppCompatActivity {

    private static final String TAG = AddUserActivity.class.getSimpleName();

    @Bind(R.id.addnewuser_button)
    FloatingActionButton loginButton;
    @Bind(R.id.addnewuser_username_wrapper)
    TextInputLayout usernameWrapper;
    @Bind(R.id.addnewuser_password_wrapper)
    TextInputLayout passwordWrapper;
    @Bind(R.id.addnewuser_username)
    EditText username;
    @Bind(R.id.addnewuser_password)
    EditText password;
    //TODO correct spinner theme
    @Bind(R.id.addnewuser_caf_spinner)
    AppCompatSpinner cafSpinner;
    @Bind(R.id.addnewuser_cancel)
    TextView cancelText;

    @OnClick(R.id.addnewuser_cancel)
    public void addnewuser_cancel() {
        onBackPressed();
    }

    @OnClick(R.id.addnewuser_button)
    public void login_button() {
        login();
    }

    int caf = 0;
    List<User> users;
    User newUser;
    SharedPreferences cookiesPrefs;
    ProgressDialog progressDialog;
    Subscription loginSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewuser);
        ButterKnife.bind(this);
        newUser = new User();
        cookiesPrefs = this.getSharedPreferences(PrefConstants.COOKIE_STORE_PREF, MODE_PRIVATE);
        cafSpinner.setSelection(1, true);
        RetrofitManager.removeCookies();
        users = getIntent().getParcelableArrayListExtra(ParseConstants.USERS);
        password.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    login();
                    return true;
                }
                return false;
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loginSubscription.unsubscribe();
            }
        });
    }

    private void login() {
        caf = cafSpinner.getSelectedItemPosition();
        String u = username.getText().toString();
        String p = password.getText().toString();

        if (!isValidateCredentials(u, p)) {
            onLoginFailed(getString(R.string.wrong_info));
            return;
        }

        newUser.setUsername(u);
        newUser.setPassword(p);
        newUser.setCafeteriaNumber(caf);

        if (isUserExist(newUser)) {
            onLoginFailed(getString(R.string.user_already_exist));
            return;
        }

        progressDialog.show();
        loginSubscription = ConnectionUtils.loginObservable(newUser)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        onLoginFailed(getString(R.string.connection_error));
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Document document) {
                        if (!ParseUtils.isLoginPage(document)) {
                            newUser.setViewStates(ParseUtils.extractViewState(document));
                            newUser.setIsLoggedIn(true);
                            newUser.setName(ParseUtils.getUserName(document));
                            newUser.setCookie(RetrofitManager.getCookie());
                            cookiesPrefs.edit()
                                    .putString(newUser.getCookieKey(),new SerializableHttpCookie().encode(newUser.getCookie()))
                                    .apply();
                            if (users.size() == 0)
                                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                                        .putLong(PrefConstants.DEFAULT_USER, newUser.getUniqeID()).apply();
                            onLoginSucceed(newUser);
                        } else {
                            onLoginFailed(getString(R.string.wrong_info));
                        }
                    }
                });

    }

    private void onLoginSucceed(User user) {
        Intent data = new Intent();
        data.putExtra(ParseConstants.USER, user);
        setResult(RESULT_OK, data);
        finish();
    }

    private void onLoginFailed(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    public boolean isValidateCredentials(String u, String p) {
        boolean valid = true;

        if (u.isEmpty()) {
            valid = false;
            usernameWrapper.setError(getString(R.string.cant_be_empty));
        } else {
            usernameWrapper.setErrorEnabled(false);
        }
        if (p.isEmpty()) {
            passwordWrapper.setError(getString(R.string.cant_be_empty));
            valid = false;
        } else {
            passwordWrapper.setErrorEnabled(false);
        }

        return valid;
    }

    public boolean isUserExist(User u) {
        if (users != null && users.size() != 0) {
            for (User user : users) {
                if (user.getUniqeID() == u.getUniqeID())
                    return true;
            }
            return false;
        } else
            return false;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
