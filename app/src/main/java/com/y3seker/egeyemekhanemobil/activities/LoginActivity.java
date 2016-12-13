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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.UserManager;
import com.y3seker.egeyemekhanemobil.constants.RequestCodes;

import org.jsoup.nodes.Document;

import rx.Subscriber;

/**
 * Created by Yunus Emre Şeker on 7.10.2015.
 * -
 */
public class LoginActivity extends RxAppCompatActivity {

    public static final String ADD_USER_ACTION = "add_user";
    public static final String LOGIN_FAILED_ACTION = "login_failed";
    private static final String LOGIN_SUCCEED_ACTION = "login_succeed";
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserManager.getInstance().login(this, new Subscriber<Document>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof IllegalStateException) {
                    startAddUserActivity();
                } else {
                    startActivity(LOGIN_FAILED_ACTION);
                }
            }

            @Override
            public void onNext(Document document) {
                startActivity(LOGIN_SUCCEED_ACTION);
            }
        });
    }

    void startAddUserActivity() {
        Intent l = new Intent(getApplicationContext(), AddUserActivity.class);
        l.setAction(LoginActivity.ADD_USER_ACTION);
        startActivityForResult(l, RequestCodes.LOGIN_REQ_CODE);
    }

    void startActivity(String action) {
        Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        mainIntent.setAction(action);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.LOGIN_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                startActivity(LOGIN_SUCCEED_ACTION);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.add_account_to_use, Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }
}
