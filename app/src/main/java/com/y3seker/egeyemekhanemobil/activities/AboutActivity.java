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

package com.y3seker.egeyemekhanemobil.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.y3seker.egeyemekhanemobil.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Yunus Emre Şeker on 3.11.2015.
 *
 */
public class AboutActivity extends AppCompatActivity {

    private int i = 0;
    private long lastClick = 0;

    @OnClick(R.id.about_license)
    void licensesClick() {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .add(android.R.id.content, new AboutFragment())
                .addToBackStack("app_licenses")
                .commit();
    }

    @OnClick(R.id.about_logo)
    void logoClick() {
        if ((SystemClock.elapsedRealtime() - lastClick) < 750) {
            if ((++i) == 7) {
                i = 0;
                Toast.makeText(this, "Premium Users Only", Toast.LENGTH_SHORT).show();
            }
        } else {
            i = 1;
        }
        lastClick = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public static class AboutFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            //webView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey_900));
            webView.loadUrl("file:///android_res/raw/licenses.html");
            return webView;
        }
    }

}
