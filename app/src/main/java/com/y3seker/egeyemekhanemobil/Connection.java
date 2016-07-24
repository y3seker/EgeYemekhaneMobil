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

package com.y3seker.egeyemekhanemobil;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yunus Emre Şeker on 6.10.2015.
 * -
 */
public class Connection {

    private static final String TAG = Connection.class.getSimpleName();

    private static final int NO_VIEWSTATE = 3;
    private static final int RESPONSE_NOT_SUCCEED = 2;
    private static final int CONNECTION_ERROR = 1;

    private static OkHttpClient okHttpClient;
    private static CookieManager cookieManager;

    static {
        okHttpClient = new OkHttpClient();
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        okHttpClient.setCookieHandler(cookieManager);
        okHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
        okHttpClient.setRetryOnConnectionFailure(true);
        //okHttpClient.setCache(new Cache(ThisApplication.cacheDir, 1024));
    }

    public static CookieManager getCookieManager() {
        return cookieManager;
    }

    public static OkHttpClient getClient() {
        return okHttpClient;
    }

    public static void setCookie(@NotNull HttpCookie cookie, int caf) throws URISyntaxException {
        cookieManager.getCookieStore().removeAll();
        cookieManager.getCookieStore()
                .add(new URI(ConnectionUtils.findBaseUrl(caf) + "/"), cookie);
    }

    public static void setCookie(@NotNull HttpCookie cookie) {
        cookieManager.getCookieStore().removeAll();
        cookieManager.getCookieStore()
                .add(null, cookie);
    }

    public static String _get(String url) {
        String rawHTML = "";
        Request getReq = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response getRes = okHttpClient.newCall(getReq).execute();
            if (!getRes.isSuccessful())
                return null;
            rawHTML = getRes.body().string();
            getRes.body().close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "_get exception " + e.getMessage());
            return null;
        }
        return rawHTML;
    }

    public static String _post(String url, RequestBody body) {
        String rawHTML = "";
        Request postReq = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response postRes = okHttpClient.newCall(postReq).execute();
            if (!postRes.isSuccessful())
                return null;
            rawHTML = postRes.body().string();
            postRes.body().close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "_post exception " + e.getMessage());
            return null;
        }
        return rawHTML;
    }

    public static Call _get(String url, Callback callback) {
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call c = okHttpClient.newCall(req);
        c.enqueue(callback);
        return c;
    }

    public static Call _post(String url, RequestBody formBody, Callback callback) {
        Request req = new Request.Builder()
                .url(url)
                .post(formBody)
                //.header("User-Agent", ParseConstants.USER_AGENT)
                .build();
        Call c = okHttpClient.newCall(req);
        c.enqueue(callback);
        return c;
    }

    public static Call get(final String url, final RequestListener listener) {
        return _get(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onRequestFail(url, "Bağlantı Hatası", CONNECTION_ERROR);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful())
                    listener.onRequestFail(url, "Bağlantı Hatası", RESPONSE_NOT_SUCCEED);
                else {
                    String rawHTML = response.body().string();
                    if (ParseUtils.isLoginPage(rawHTML)) {
                        Log.e(TAG, "ITS LOGIN PAGE " + url);
                    }
                    HashMap<String, String> vs = ParseUtils.extractViewState(rawHTML);
                    if (vs == null) {
                        listener.onRequestFail(url, "Bağlantı Hatası", NO_VIEWSTATE);
                        return;
                    }
                    listener.onRequestSuccess(url, rawHTML, vs);
                    response.body().close();
                }
            }
        });

    }

    public static Call post(final String url, RequestBody formBody, final RequestListener listener) {
        return _post(url, formBody, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onRequestFail(url, "Bağlantı Hatası", CONNECTION_ERROR);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful())
                    listener.onRequestFail(url, "Bağlantı Hatası", RESPONSE_NOT_SUCCEED);
                else {
                    String rawHTML = response.body().string();
                    HashMap<String, String> vs = ParseUtils.extractViewState(rawHTML);
                    if (vs == null) {
                        listener.onRequestFail(url, "Bağlantı Hatası", NO_VIEWSTATE);
                        return;
                    }
                    listener.onRequestSuccess(url, rawHTML, vs);
                    response.body().close();
                }
            }
        });
    }

    public static User login(User user) {
        Request getReq = new Request.Builder()
                .url(user.getBaseUrl() + UrlConstants.C_DEFAULT)
                .get()
                .build();
        try {
            Response getRes = okHttpClient.newCall(getReq).execute();
            if (!getRes.isSuccessful())
                return null;
            String rawHTML_G = getRes.body().string();
            user.setViewStates(ParseUtils.extractViewState(rawHTML_G));
            Request postReq = new Request.Builder()
                    .url(ConnectionUtils.findBaseUrl(user.getCafeteriaNumber()) + UrlConstants.C_DEFAULT)
                    .post(getLoginRequestBody(user))
                    .build();
            Response postRes = okHttpClient.newCall(postReq).execute();
            if (!postRes.isSuccessful()) {
                return null;
            }
            String rawHTML_P = postRes.body().string();
            user.setViewStates(ParseUtils.extractViewState(rawHTML_P));
            user.setCookie(cookieManager.getCookieStore().getCookies().get(0));
            getRes.body().close();
            getRes.body().close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    public static void login(User user, LoginListener listener) {
        login(user.getUsername(), user.getPassword(), user.getCafeteriaNumber(), listener);
    }

    public static void login(final String u, final String p, final int caf, final LoginListener listener) {

        final String url = ConnectionUtils.findBaseUrl(caf) + UrlConstants.C_DEFAULT;
        get(url, new RequestListener() {
            @Override
            public void onRequestFail(String url, String why, int causeCode) {
                super.onRequestFail(url, why, causeCode);
                Log.e(TAG, "G onRequestFail" + causeCode);
                listener.onLoginFail("Bağlantı Hatası");
            }

            @Override
            public void onRequestSuccess(String url, String rawHTML, HashMap<String, String> viewStates) {
                super.onRequestSuccess(url, rawHTML, viewStates);
                RequestBody formBody = new FormEncodingBuilder()
                        .add(ParseConstants.VIEW_STATE, viewStates.get(ParseConstants.VIEW_STATE))
                        .add(ParseConstants.VIEW_STATE_GEN, viewStates.get(ParseConstants.VIEW_STATE_GEN))
                        .add(ParseConstants.EVENT_VAL, viewStates.get(ParseConstants.EVENT_VAL))
                        .add("txtKullaniciAdi", u)
                        .add("txtParola", p)
                        .add("grs", caf == 0 ? "rPersonel" : "rOgrenci")
                        .add("Button1", "Giriş")
                        .build();
                post(url, formBody, new RequestListener() {
                    @Override
                    public void onRequestFail(String url, String why, int causeCode) {
                        super.onRequestFail(url, why, causeCode);
                        Log.e(TAG, "P onRequestFail" + causeCode);
                        listener.onLoginFail("Bağlantı Hatası");
                    }

                    @Override
                    public void onRequestSuccess(String url, String rawHTML, HashMap<String, String> viewStates) {
                        super.onRequestSuccess(url, rawHTML, viewStates);
                        if (ParseUtils.isLoginSuccess(rawHTML)) {
                            viewStates.put(ParseConstants.LAST_URL, ConnectionUtils.findBaseUrl(caf) + UrlConstants.C_HOME);
                            User newUser;
                            newUser = new User(ParseUtils.getUserName(rawHTML), u, p, caf);
                            if (cookieManager.getCookieStore().getCookies().size() != 0) {
                                //Log.e(TAG, "LOGGED IN CS " + cookieManager.getCookieStore().getCookies().toString());
                                newUser.setCookie(cookieManager.getCookieStore().getCookies().get(0));
                            }
                            newUser.setIsLoggedIn(true);
                            newUser.setViewStates(viewStates);
                            listener.onLoginSuccess(newUser);
                        } else {
                            listener.onLoginFail("Bilgiler Hatalı");
                        }
                    }
                });
            }
        });
    }

    public static void login(final User user, final int tryAgain, final LoginListener listener) {
        final String homeURL = user.getBaseUrl() + UrlConstants.C_HOME;
        Log.d(TAG, "login try:" + tryAgain);
        if (user.getCookie() != null) {
            Log.d(TAG, "login, cookie login try:" + tryAgain);
            cookieManager.getCookieStore().add(null, user.getCookie());
            _get(homeURL, new RequestCallback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    super.onFailure(request, e);
                    if (tryAgain != 0)
                        login(user, tryAgain - 1, listener);
                    else
                        listener.onLoginFail("Bağlantı Hatası");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    super.onResponse(response);
                    String rawHTML = response.body().string();
                    if (ParseUtils.isLoginPage(rawHTML)) {
                        user.setCookie(null);
                        user.setIsLoggedIn(false);
                        login(user, tryAgain, listener);
                    } else {
                        user.setIsLoggedIn(true);
                        listener.onLoginSuccess(user);
                    }
                }
            });
        } else {
            Log.d(TAG, "login, nocookie login try:" + tryAgain);
            login(user, new LoginListener() {
                @Override
                public void onLoginFail(String why) {
                    if (tryAgain != 0)
                        login(user, tryAgain - 1, listener);
                    else
                        listener.onLoginFail(why);
                }

                @Override
                public void onLoginSuccess(User user) {
                    listener.onLoginSuccess(user);
                }
            });
        }
    }

    public static void logout(User user, final DoListener listener) {
        Log.d(TAG, user.getViewStates().toString());
        RequestBody formBody = new FormEncodingBuilder()
                .add(ParseConstants.VIEW_STATE, user.getViewStates().get(ParseConstants.VIEW_STATE))
                .add(ParseConstants.VIEW_STATE_GEN, user.getViewStates().get(ParseConstants.VIEW_STATE_GEN))
                .add(ParseConstants.EVENT_VAL, user.getViewStates().get(ParseConstants.EVENT_VAL))
                .add("ctl00$Button1", "Güvenli Çıkış")
                .build();
        post(user.getViewStates().get(ParseConstants.LAST_URL), formBody, new RequestListener() {
            @Override
            public void onRequestFail(String url, String why, int causeCode) {
                super.onRequestFail(url, why, causeCode);
                listener.onFail(why);
            }

            @Override
            public void onRequestSuccess(String url, String rawHTML, HashMap<String, String> viewStates) {
                super.onRequestSuccess(url, rawHTML, viewStates);
                listener.onSuccess(rawHTML, viewStates);
            }
        });
    }

    public static RequestBody getLoginRequestBody(User user) {
        HashMap<String, String> viewStates = user.getViewStates();
        return new FormEncodingBuilder()
                .add(ParseConstants.VIEW_STATE, viewStates.get(ParseConstants.VIEW_STATE))
                .add(ParseConstants.VIEW_STATE_GEN, viewStates.get(ParseConstants.VIEW_STATE_GEN))
                .add(ParseConstants.EVENT_VAL, viewStates.get(ParseConstants.EVENT_VAL))
                .add("txtKullaniciAdi", user.getUsername())
                .add("txtParola", user.getPassword())
                .add("grs", user.getCafeteriaNumber() == 0 ? "rPersonel" : "rOgrenci")
                .add("Button1", "Giriş")
                .build();
    }

    public static HttpCookie getCookie() {
        return cookieManager.getCookieStore().getCookies().get(0);
    }

    public static void removeCookies() {
        cookieManager.getCookieStore().removeAll();
    }

    public static void closeOngoingCalls() {
        if (okHttpClient.getConnectionPool() != null && okHttpClient.getConnectionPool().getConnectionCount() != 0) {
            okHttpClient.getConnectionPool().evictAll();
        }
    }

    public interface LoginListener {
        void onLoginFail(String why);

        void onLoginSuccess(User user);
    }

    public interface DoListener {
        void onFail(String why);

        void onSuccess(String rawHTML, HashMap<String, String> viewStates);
    }

    public interface ReqListener {
        void onRequestFail(String url, String why, int causeCode);

        void onRequestSuccess(String url, String rawHTML, HashMap<String, String> viewStates);
    }

    public static class RequestListener implements ReqListener {

        @Override
        public void onRequestFail(String url, String why, int causeCode) {

        }

        @Override
        public void onRequestSuccess(String url, String rawHTML, HashMap<String, String> viewStates) {
            viewStates.put(ParseConstants.LAST_URL, url);

        }
    }

    public static class RequestCallback implements Callback {

        @Override
        public void onFailure(Request request, IOException e) {

        }

        @Override
        public void onResponse(Response response) throws IOException {

        }
    }
}
