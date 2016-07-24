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

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by Yunus Emre Şeker on 6.11.2015.
 * -
 */
public final class AnimUtils {
    public static String TAG = "REVEAL_EFFECT";

    public interface AnimationListener {
        void onAnimEnd();
    }

    public static SupportAnimator revealFrom(int x, int y, View target, int duration, final AnimationListener callback) {
        int radius = Math.max(target.getWidth(), target.getHeight());

        SupportAnimator animator2 = ViewAnimationUtils.createCircularReveal(target, x, y, 0, radius);

        if (duration > 0)
            animator2.setDuration(duration);

        animator2.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                if (callback != null)
                    callback.onAnimEnd();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        target.setVisibility(View.VISIBLE);
        animator2.start();
        return animator2;
    }

    public static void collapseTo(int x, int y, final View target, int duration, final AnimationListener callback) {
        int radius = Math.max(target.getWidth(), target.getHeight());

        SupportAnimator animator2 = ViewAnimationUtils.createCircularReveal(target, x, y, radius, 0);

        if (duration > 0)
            animator2.setDuration(duration);
        target.setVisibility(View.VISIBLE);
        animator2.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                target.setVisibility(View.GONE);
                if (callback != null) callback.onAnimEnd();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator2.start();
    }

}
