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

import com.y3seker.egeyemekhanemobil.InvalidCredentialException;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.localapi.parsers.LoginParser;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.NonLoginException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.OrderSessionException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.RequestBlockedException;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by Yunus Emre Şeker on 1.11.2015.
 * -
 */
class HandlerResponseInterceptor implements Interceptor {
    private Map<String, String> url2ViewStates = new HashMap<>();

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "";
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl url = chain.request().url();
        Request request = chain.request();
        MediaType urlencodedType = MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8");
        if (request.method().equals("POST") && !url2ViewStates.isEmpty()) {
            String viewStates = url2ViewStates.get(url.toString());
            String bodyToString = bodyToString(request.body());
            RequestBody requestBody = RequestBody.create(urlencodedType,
                    bodyToString + (bodyToString.length() > 0 ? "&" : "") + viewStates);
            request = request.newBuilder()
                    .post(requestBody)
                    .build();
        }

        Response response = chain.proceed(request);
        MediaType contentType = response.body().contentType();
        String body = response.body().string();
        Document responseDoc = Jsoup.parse(body);
        response.body().close();

        try {
            storeViewStates(url, responseDoc);
        } catch (NullPointerException ignored) {
        }

        if (!response.isSuccessful())
            throw new IOException();
        if (ParseUtils.isBlockedPaged(responseDoc))
            throw new RequestBlockedException();
        if (!url.encodedPath().equals(UrlConstants.C_DEFAULT) && LoginParser.isLoginPage(responseDoc))
            throw new NonLoginException();
        if (LoginParser.isCredentialsInvalid(responseDoc))
            throw new InvalidCredentialException();
        if (ParseUtils.isOrderWarningPage(responseDoc))
            throw new OrderSessionException();

        return response.newBuilder().body(ResponseBody.create(contentType, body)).build();
    }

    private void storeViewStates(HttpUrl url, Document responseDoc) {
        HashMap<String, String> viewStates = ParseUtils.extractViewState(responseDoc);
        FormBody.Builder builder1 = new FormBody.Builder();
        if (viewStates != null && !viewStates.isEmpty())
            for (Map.Entry<String, String> entry : viewStates.entrySet()) {
                builder1.add(entry.getKey(), entry.getValue());
            }
        String viewStatesString = bodyToString(builder1.build());
        url2ViewStates.put(url.toString(), viewStatesString);
    }
}
