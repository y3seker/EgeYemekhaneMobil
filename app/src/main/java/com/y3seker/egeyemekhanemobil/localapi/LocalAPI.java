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

package com.y3seker.egeyemekhanemobil.localapi;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.localapi.parsers.LoginParser;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;

import org.jsoup.nodes.Document;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by y3seker on 5.03.2017.
 * -
 */

public class LocalAPI {

    private static LocalAPI mInstance;

    public static LocalAPI get() {
        if (mInstance == null)
            mInstance = new LocalAPI();
        return mInstance;
    }

    private static RequestBody getLoginRequestBody(User user) {
        return new FormBody.Builder()
                .add("txtKullaniciAdi", user.getUsername())
                .add("txtParola", user.getPassword())
                .add("grs", user.getCafeteriaNumber() == 0 ? "rPersonel" : "rOgrenci")
                .add("Button1", "Giriş")
                .build();
    }

    public Subscription login(final User user, RxAppCompatActivity context, final Subscriber<User> caller) {
        if (user.isLoggedIn()) {
            caller.onNext(null);
            caller.onCompleted();
            return null;
        }
        return RetrofitManager.service().getLogin()
                .flatMap(new Func1<Document, Observable<?>>() {

                    @Override
                    public Observable<?> call(Document document) {
                        RequestBody requestBody = getLoginRequestBody(user);
                        return RetrofitManager.service().postLogin(requestBody);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(context.bindToLifecycle())
                .cast(Document.class)
                .map(LoginParser.parser(user))
                .subscribe(caller);
    }
}
