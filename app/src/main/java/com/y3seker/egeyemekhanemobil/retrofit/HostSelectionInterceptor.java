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

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by y3seker on 9.03.2017.
 * -
 */

public class HostSelectionInterceptor implements Interceptor {
    private String currentHost = UrlConstants.SKS1_BASE;

    public String getHost() {
        return currentHost;
    }

    public void setHost(String host) {
        HttpUrl httpUrl = HttpUrl.parse(host);
        currentHost = httpUrl != null ? httpUrl.host() : host;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (currentHost != null && !currentHost.equals(request.url().host())) {
            HttpUrl newUrl = request.url().newBuilder()
                    .host(currentHost)
                    .build();
            request = request.newBuilder()
                    .url(newUrl)
                    .build();
        }
        return chain.proceed(request);
    }
}
