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

import com.squareup.okhttp.OkHttpClient;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;

import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Created by Yunus Emre Şeker on 17.10.2015.
 * -
 */
public class RetrofitManager {

    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;
    private static CookieManager cookieManager;
    private static ConnectionService service;
    private static BaseUrlManager baseUrlManager;

    static {
        setup();
    }

    public static ConnectionService api() {
        if (retrofit == null) {
            setup();
        }
        return service;
    }

    public static void setBaseUrl(String url) {
        baseUrlManager.setBaseUrl(url);
    }

    public static void addCookie(HttpCookie cookie) {
        if (cookie != null)
            cookieManager.getCookieStore().add(null, cookie);
    }

    public static HttpCookie getCookie() {
        return cookieManager.getCookieStore().getCookies().size() != 0 ?
                cookieManager.getCookieStore().getCookies().get(0) : null;
    }

    private static void setup() {
        baseUrlManager = new BaseUrlManager(UrlConstants.SKS1_BASE);
        okHttpClient = new OkHttpClient();
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        okHttpClient.setCookieHandler(cookieManager);
        okHttpClient.interceptors().add(new HandlerResponseInterceptor());
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrlManager)
                .client(okHttpClient)
                .addConverterFactory(new DocumentConverterFactory())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(ConnectionService.class);
    }

    public static void removeCookies() {
        cookieManager.getCookieStore().removeAll();
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
