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

package com.y3seker.egeyemekhanemobil.localapi.parsers;

import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.models.User;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import rx.functions.Func1;

/**
 * Created by y3seker on 5.03.2017.
 * -
 */

public class LoginParser {
    public static Func1<Document, User> parser(final User user) {
        return new Func1<Document, User>() {
            @Override
            public User call(Document document) {
                String name = getUserName((Document) document);
                user.setIsLoggedIn(true);
                user.setName(name);
                return user;
            }
        };
    }

    public static boolean isCredentialsInvalid(Document doc) {
        Element element = doc.getElementById(ParseConstants.INVALID_CREDENTIAL);
        return isLoginPage(doc) && element != null &&
                element.hasText();
    }

    public static boolean isLoginPage(Document doc) {
        return doc.getElementById(ParseConstants.LOGIN) != null;
    }

    public static boolean isLoginSucceed(Document doc) {
        return doc.getElementById(ParseConstants.LOGIN) == null;
    }

    public static String getUserName(Document document) {
        return document.getElementById(ParseConstants.USERS_NAME).text();
    }
}
