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
import android.util.Xml;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Observable;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yunus on 25.03.2016.
 * -
 */
public class MonthlyMenus {

    private static final String TAG = MonthlyMenus.class.getSimpleName();
    public static final MediaType JSON
            = MediaType.parse("text/xml; charset=utf-8");

    public void fetch(final FetchListener listener) {
        final Request request = new Request.Builder()
                .url("http://155.223.64.30/Services/Mobil/Yemekhane/YemekhaneSrvc.asmx/GetYemekhaneMenu")
                .post(RequestBody.create(JSON, ""))
                .build();

        rx.Observable.create(new rx.Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                try {
                    Response response = new OkHttpClient().newCall(request).execute();
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                    if (!response.isSuccessful())
                        subscriber.onError(new Exception("HTTP Request Failed"));
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFetchFailed(e);
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Response>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onFetchFailed((Exception) e);
                    }

                    @Override
                    public void onNext(Response response) {
                        try {
                            Log.d(TAG, "before parse: " + response.body().string());
                            parse(response);
                            listener.onFetchDone(null);
                        } catch (XmlPullParserException | IOException e) {
                            e.printStackTrace();
                            listener.onFetchFailed(e);
                        }
                    }
                });

    }

    private void parse(Response res) throws XmlPullParserException, IOException {
        //TODO return list of menus
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(res.body().byteStream(), null);
        String s = xmlParser.getText();
        Log.i(TAG, "parse: " + s);
    }

    public interface FetchListener {
        void onFetchDone(List asd);

        void onFetchFailed(Exception e);
    }
}
