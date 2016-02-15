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

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.NonLoginException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.OrderSessionException;
import com.y3seker.egeyemekhanemobil.retrofit.exceptions.RequestBlockedException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Yunus Emre Şeker on 1.11.2015.
 * -
 */
public class HandlerResponseInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        URL url = chain.request().url();
        Response response = chain.proceed(chain.request());
        MediaType contentType = response.body().contentType();
        String body = response.body().string();
        Document document = Jsoup.parse(body);
        response.body().close();
        if (!response.isSuccessful())
            throw new IOException();

        if (ParseUtils.isBlockedPaged(document))
            throw new RequestBlockedException();
        else if (!url.getPath().equals(UrlConstants.C_DEFAULT) && ParseUtils.isLoginPage(document))
            throw new NonLoginException();
        else if (ParseUtils.isOrderWarningPage(document))
            throw new OrderSessionException();
        //Log.d("Handler", response.headers().toString());
        return response.newBuilder().body(ResponseBody.create(contentType, body)).build();
    }
}
