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

import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.NonLoginException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.OrderSessionException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.RequestBlockedException;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Yunus Emre Şeker on 1.11.2015.
 * -
 */
class HandlerResponseInterceptor implements Interceptor {
    private Map<String, String> lastViewStates = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl url = chain.request().url();
        Response response = chain.proceed(chain.request());
        MediaType contentType = response.body().contentType();
        String body = response.body().string();
        Document document = Jsoup.parse(body);
        response.body().close();
        if (!response.isSuccessful())
            throw new IOException();

        if (ParseUtils.isBlockedPaged(document))
            throw new RequestBlockedException();
        else if (!url.encodedPath().equals(UrlConstants.C_DEFAULT) && ParseUtils.isLoginPage(document))
            throw new NonLoginException();
        else if (ParseUtils.isOrderWarningPage(document))
            throw new OrderSessionException();

        return response.newBuilder().body(ResponseBody.create(contentType, body)).build();
    }

    private void handleViewStates(Chain chain) {
        if (chain.request().method().equals("POST") && !lastViewStates.isEmpty()) {
            RequestBody body = chain.request().body();
        }
    }
}
