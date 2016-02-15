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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.y3seker.egeyemekhanemobil.activities.LoginActivity;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.utils.Utils;

/**
 * Created by Yunus on 14.11.2015.
 * -
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.reminderNotification(context, false);
    }

    public void notify(Context context) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i,
                PendingIntent.FLAG_ONE_SHOT);

        String reminderText = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefConstants.REMINDER_TEXT, context.getString(R.string.next_week_reminder));
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_done)
                        .setContentTitle(reminderText)
                        .setAutoCancel(true)
                        .setTicker(reminderText);

        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(87878787, mBuilder.build());
        Log.d("SERVICE", "Notify created!");
    }
}
