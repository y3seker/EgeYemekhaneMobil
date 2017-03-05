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

package com.y3seker.egeyemekhanemobil.utils;

import android.util.Log;

import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;

import org.jsoup.nodes.Document;

import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus Emre Şeker on 2.11.2015.
 * -
 */
public final class ConnectionUtils {

    private static Observable<Object> forceLoginObservable(final User user) {
        //RetrofitManager.setBaseUrl(user.getBaseUrl());
        return RetrofitManager.service().getLogin()
                .flatMap(new Func1<Document, Observable<?>>() {

                    @Override
                    public Observable<?> call(Document document) {
                        RequestBody requestBody = getLoginRequestBody(user);
                        Log.e("loginObservable", "Login posting for " + user.getUsername());
                        return RetrofitManager.service().postLogin(requestBody);
                    }
                })
                .retry(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Document> loginObservable(final User user) {
        //RetrofitManager.setBaseUrl(user.getBaseUrl());
        //RetrofitManager.addCookie(user.getCookie());
        return forceLoginObservable(user).cast(Document.class);
//        return RetrofitManager.service().getHome()
//                .retry(1)
//                .onErrorResumeNext(ConnectionUtils.forceLoginObservable(user).cast(Document.class))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());
    }


    private static RequestBody getLoginRequestBody(User user) {
        return new FormBody.Builder()
                .add("txtKullaniciAdi", user.getUsername())
                .add("txtParola", user.getPassword())
                .add("grs", user.getCafeteriaNumber() == 0 ? "rPersonel" : "rOgrenci")
                .add("Button1", "Giriş")
                .build();
    }

    public static String findBaseUrl(int cafeteriaNumber) {
        switch (cafeteriaNumber) {
            case 0:
                return UrlConstants.PERSONEL_BASE;
            case 1:
                return UrlConstants.SKS1_BASE;
            case 2:
                return UrlConstants.SKS2_BASE;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static FormBody.Builder febWithViewStates(HashMap<String, String> viewStates) {
        return new FormBody.Builder()
                .add(ParseConstants.VIEW_STATE, viewStates.get(ParseConstants.VIEW_STATE))
                .add(ParseConstants.VIEW_STATE_GEN, viewStates.get(ParseConstants.VIEW_STATE_GEN))
                .add(ParseConstants.EVENT_VAL, viewStates.get(ParseConstants.EVENT_VAL));
    }
}
