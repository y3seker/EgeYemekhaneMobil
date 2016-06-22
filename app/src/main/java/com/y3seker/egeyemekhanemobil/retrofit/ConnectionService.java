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

import com.squareup.okhttp.RequestBody;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;

import org.jsoup.nodes.Document;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Url;
import rx.Observable;

/**
 * Created by Yunus Emre Şeker on 17.10.2015.
 * -
 */
public interface ConnectionService {

    @GET
    Observable<Document> getRequest(@Url String url);

    @GET(UrlConstants.C_DEFAULT)
    Observable<Document> getLogin();

    @POST(UrlConstants.C_DEFAULT)
    Observable<Document> postLogin(@Body RequestBody body);

    @GET(UrlConstants.C_HOME)
    Observable<Document> getHome();

    @GET(UrlConstants.C_BAKIYE)
    Observable<Document> getBalance();

    @POST(UrlConstants.C_BAKIYE)
    Observable<Document> postBalance(@Body RequestBody body);

    @GET(UrlConstants.C_YEMEKLERIM)
    Observable<Document> getMyMenus();

    @POST(UrlConstants.C_YEMEKLERIM)
    Observable<Document> postMyMenus(@Body RequestBody body);

    @GET(UrlConstants.C_OGUN)
    Observable<Document> getOrder();

    @POST(UrlConstants.C_OGUN)
    Observable<Document> postOrder(@Body RequestBody body);

    @GET(UrlConstants.C_OGUNIPTAL)
    Observable<Document> getCancel();

    @POST(UrlConstants.C_OGUNIPTAL)
    Observable<Document> postCancel(@Body RequestBody body);

    @GET(UrlConstants.C_YEMEKHANE_H)
    Observable<Document> getMyHistory();

    @POST(UrlConstants.C_YEMEKHANE_H)
    Observable<Document> postMyHistory(@Body RequestBody body);

    @GET(UrlConstants.C_OGLE_MENU)
    Observable<Document> getLunchMenus();

    @GET(UrlConstants.C_AKSAM_MENU)
    Observable<Document> getDinnerMenus();


}
