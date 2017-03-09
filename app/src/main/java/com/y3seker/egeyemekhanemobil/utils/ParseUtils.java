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

package com.y3seker.egeyemekhanemobil.utils;

import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.localapi.parsers.LoginParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;

/**
 * Created by Yunus Emre Şeker on 2.11.2015.
 * -
 */
public final class ParseUtils {

    public static HashMap<String, String> extractViewState(Document document) throws NullPointerException {
        HashMap<String, String> result = new HashMap<>();
        extractViewState(result, document);
        return result;
    }

    public static void extractViewState(HashMap<String, String> viewStates, Document document) throws NullPointerException {
        viewStates.put(ParseConstants.VIEW_STATE, document.getElementById(ParseConstants.VIEW_STATE).val());
        viewStates.put(ParseConstants.VIEW_STATE_GEN, document.getElementById(ParseConstants.VIEW_STATE_GEN).val());
        Element eventVal = document.getElementById(ParseConstants.EVENT_VAL);
        if (eventVal == null) return;
        viewStates.put(ParseConstants.EVENT_VAL, document.getElementById(ParseConstants.EVENT_VAL).val());
    }

    public static boolean isBlockedPaged(Document doc) {
        return doc.getElementById("box") != null;
    }

    public static boolean isOrderWarningPage(Document doc) {
        return doc.select("form").first().attr("action").equals("./hata.aspx?no=1");
    }

    public static HashMap<String, String> extractViewState(String rawHTML) {
        HashMap<String, String> result = new HashMap<>();
        Document doc = Jsoup.parse(rawHTML);
        try {
            result.put(ParseConstants.VIEW_STATE, doc.getElementById(ParseConstants.VIEW_STATE).val());
            result.put(ParseConstants.VIEW_STATE_GEN, doc.getElementById(ParseConstants.VIEW_STATE_GEN).val());
            result.put(ParseConstants.EVENT_VAL, doc.getElementById(ParseConstants.EVENT_VAL).val());
        } catch (NullPointerException e) {
            return null;
        }
        return result;
    }

    public static boolean isLoginSuccess(String rawHTML) {
        return !isLoginPage(rawHTML);
    }

    public static boolean isLoginPage(String rawHTML) {
        return LoginParser.isLoginPage(Jsoup.parse(rawHTML));
    }

    public static String getUserName(String rawHTML) {
        return Jsoup.parse(rawHTML).getElementById(ParseConstants.USERS_NAME).text();
    }

    public static boolean isHomePage(Document document) {
        try {
            return document.getElementById("aspnetForm").attr("action").equals("./anasayfa.aspx");
        } catch (Exception e) {
            return false;
        }
    }
}
