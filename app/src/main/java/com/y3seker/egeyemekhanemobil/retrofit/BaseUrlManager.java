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


import com.squareup.okhttp.HttpUrl;

import retrofit.BaseUrl;

/**
 * Created by Yunus Emre Şeker on 17.10.2015.
 * -
 */
class BaseUrlManager implements BaseUrl {

    private final String _url;
    private HttpUrl url;

    public BaseUrlManager(String url) {
        this._url = url;
        this.url = HttpUrl.parse(url);
    }

    public void setBaseUrl(String url) {
        this.url = HttpUrl.parse(url);
    }

    @Override
    public HttpUrl url() {
        return url;
    }
}
