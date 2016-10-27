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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Yunus on 26.10.2016.
 * -
 */
public class ParseUtilsTest {

    private Document homePage, loginPage, orderWarningPage;

    @org.junit.Before
    public void setUp() throws Exception {
        homePage = Jsoup.parse(getClass().getClassLoader().getResourceAsStream("homepage.html"),
                null, "http://sks2noluyemek.ege.edu.tr");
        loginPage = Jsoup.parse(getClass().getClassLoader().getResourceAsStream("loginpage.html"),
                null, "http://sks2noluyemek.ege.edu.tr");
        orderWarningPage = Jsoup.parse(getClass().getClassLoader().getResourceAsStream("orderwarning.html"),
                null, "http://sks2noluyemek.ege.edu.tr");
    }

    @org.junit.Test
    public void getUserName() throws Exception {
        assertEquals(ParseUtils.getUserName(homePage), "YUNUS EMRE ŞEKER");
    }

    @org.junit.Test
    public void isLoginPage() throws Exception {
        assertTrue(ParseUtils.isLoginPage(loginPage));
        assertFalse(ParseUtils.isLoginPage(homePage));
    }

    @org.junit.Test
    public void isBlockedPaged() throws Exception {
        assertFalse(ParseUtils.isBlockedPaged(homePage));
    }

    @org.junit.Test
    public void isOrderWarningPage() throws Exception {
        assertFalse(ParseUtils.isOrderWarningPage(homePage));
        assertTrue(ParseUtils.isOrderWarningPage(orderWarningPage));
    }

    @org.junit.Test
    public void isLoginSucceed() throws Exception {
        assertFalse(ParseUtils.isLoginSucceed(loginPage));
        assertTrue(ParseUtils.isLoginSucceed(homePage));
    }
}