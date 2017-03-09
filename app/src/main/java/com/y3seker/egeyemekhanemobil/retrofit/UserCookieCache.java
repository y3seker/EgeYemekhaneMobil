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

import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.y3seker.egeyemekhanemobil.UserManager;
import com.y3seker.egeyemekhanemobil.models.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Cookie;

/**
 * Created by y3seker on 9.03.2017.
 * <p>
 * Handle cookies based on current users, hopefully
 */

public class UserCookieCache implements CookieCache {

    private Map<User, Set<UserCookie>> cookies;

    public UserCookieCache() {
        cookies = new HashMap<>();
    }

    public Set<UserCookie> getCookies() {
        Set<UserCookie> userCookies = cookies.get(UserManager.getInstance().getCurrentUser());
        if (userCookies == null) {
            userCookies = new HashSet<>();
            cookies.put(UserManager.getInstance().getCurrentUser(), userCookies);
        }
        return userCookies;
    }

    @Override
    public void addAll(Collection<Cookie> newCookies) {
        for (UserCookie cookie : UserCookie.decorateAll(newCookies)) {
            this.getCookies().remove(cookie);
            this.getCookies().add(cookie);
        }
    }

    @Override
    public void clear() {
        cookies.clear();
    }

    @Override
    public Iterator<Cookie> iterator() {
        return new UserCookieCache.UserCookieIterator();
    }

    /**
     * Straight copypasta from IdentifiableCookie at PersistentCookieJar. That was package private.
     */
    private static class UserCookie {
        private Cookie cookie;

        UserCookie(Cookie cookie) {
            this.cookie = cookie;
        }

        static List<UserCookie> decorateAll(Collection<Cookie> cookies) {
            List<UserCookie> identifiableCookies = new ArrayList<>(cookies.size());
            for (Cookie cookie : cookies) {
                identifiableCookies.add(new UserCookie(cookie));
            }
            return identifiableCookies;
        }

        Cookie getCookie() {
            return cookie;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof UserCookie)) return false;
            UserCookie that = (UserCookie) other;
            return that.cookie.name().equals(this.cookie.name())
                    && that.cookie.domain().equals(this.cookie.domain())
                    && that.cookie.path().equals(this.cookie.path())
                    && that.cookie.secure() == this.cookie.secure()
                    && that.cookie.hostOnly() == this.cookie.hostOnly();
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = 31 * hash + cookie.name().hashCode();
            hash = 31 * hash + cookie.domain().hashCode();
            hash = 31 * hash + cookie.path().hashCode();
            hash = 31 * hash + (cookie.secure() ? 0 : 1);
            hash = 31 * hash + (cookie.hostOnly() ? 0 : 1);
            return hash;
        }
    }

    private class UserCookieIterator implements Iterator<Cookie> {

        private Iterator<UserCookie> iterator;

        UserCookieIterator() {
            iterator = getCookies().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next().getCookie();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
