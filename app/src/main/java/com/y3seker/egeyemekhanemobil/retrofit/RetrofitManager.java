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

package com.y3seker.egeyemekhanemobil.retrofit;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.y3seker.egeyemekhanemobil.ThisApplication;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;


/**
 * Created by Yunus Emre Şeker on 17.10.2015.
 * -
 */
public class RetrofitManager {

    private static RetrofitManager mInstance = new RetrofitManager();
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private ConnectionService service;
    private ClearableCookieJar cookieJar;

    public static RetrofitManager instance(){
        return mInstance;
    }

    public static ConnectionService api() {
        return instance().getApi();
    }

    public static void setBaseUrl(String url) {
        //setup(url);
    }

    public static void addCookie(HttpCookie cookie) {
    }

    public static HttpCookie getCookie() {
        return null;
    }

    public void init(Context context) {
        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        okHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(new HandlerResponseInterceptor())
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(UrlConstants.SKS1_BASE)
                .client(okHttpClient)
                .addConverterFactory(new DocumentConverterFactory())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(ConnectionService.class);
    }

    public ConnectionService getApi() {
        return service;
    }
}
