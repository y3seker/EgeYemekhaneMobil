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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.InvalidCredentialException;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.UserManager;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;

import org.jsoup.nodes.Document;

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
    ProgressDialog progressDialog;
    User newUser;
    Subscription loginSubscription;

    @OnClick(R.id.addnewuser_cancel)
    public void addnewuser_cancel() {
        onBackPressed();
    }

    @OnClick(R.id.addnewuser_button)
    public void login_button() {
        login();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewuser);
        ButterKnife.bind(this);
        newUser = new User();
        cafSpinner.setSelection(1, true);
        RetrofitManager.removeCookies();
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
                if (loginSubscription != null)
                    loginSubscription.unsubscribe();
            }
        });
    }

    void login() {
        int caf = cafSpinner.getSelectedItemPosition();
        String u = username.getText().toString();
        String p = password.getText().toString();

        if (!validateCredentials(u, p)) {
            onLoginFailed(getString(R.string.wrong_info));
            return;
        }

        newUser.setUsername(u);
        newUser.setPassword(p);
        newUser.setCafeteriaNumber(caf);

        if (UserManager.getInstance().isUserExist(newUser)) {
            onLoginFailed(getString(R.string.user_already_exist));
        } else {
            progressDialog.show();
            UserManager.getInstance().addUser(newUser).setCurrentUser(newUser);
            loginSubscription = UserManager.getInstance().login(this, new Subscriber<Document>() {
                @Override
                public void onCompleted() {
                    progressDialog.dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    progressDialog.dismiss();
                    if (e instanceof InvalidCredentialException) {
                        onLoginFailed(getString(R.string.wrong_info));
                    } else {
                        onLoginFailed(getString(R.string.connection_error));
                    }
                    e.printStackTrace();
                }

                @Override
                public void onNext(Document document) {
                    onLoginSucceed();
                }
            });
        }
    }

    void onLoginSucceed() {
        setResult(RESULT_OK);
        finish();
    }

    void onLoginFailed(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private boolean validateCredentials(String u, String p) {
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
