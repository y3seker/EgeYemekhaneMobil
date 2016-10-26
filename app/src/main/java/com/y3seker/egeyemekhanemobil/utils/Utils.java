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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.y3seker.egeyemekhanemobil.BuildConfig;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.ReminderReceiver;
import com.y3seker.egeyemekhanemobil.activities.LoginActivity;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Yunus Emre Şeker on 7.10.2015.
 * -
 */
public final class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static final SimpleDateFormat myMenusDateFormat;
    public static final SimpleDateFormat myMenusDateStringFormat;
    private static final SimpleDateFormat myMenusReverseDateFormat;
    public static final SimpleDateFormat orderDateFormat;
    public static final SimpleDateFormat balanceDateFormat;
    public static final DecimalFormat twoDigit;
    public static final Calendar today;
    private static final TimeZone trTimeZone;
    private static final Locale trLocale;

    static {
        trTimeZone = TimeZone.getTimeZone("Europe/Istanbul");
        trLocale = new Locale("tr-TR");
        twoDigit = new DecimalFormat("00");
        today = Calendar.getInstance(trTimeZone);
        myMenusDateFormat = new SimpleDateFormat("MM.dd.yyyy", trLocale);
        myMenusDateStringFormat = new SimpleDateFormat("EE, dd.MM.yyyy", trLocale);
        myMenusReverseDateFormat = new SimpleDateFormat("yyyy.MM.dd", trLocale);
        balanceDateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", trLocale);
        orderDateFormat = new SimpleDateFormat("MM.dd.yyyy", trLocale);
    }

    public static boolean isTodayWeekend() {
        return today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
    }

    public static Snackbar makeSnackBar(Activity activity, String message) {
        return Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
    }

    public static long getCurrentTime() {
        return Calendar.getInstance(trTimeZone).getTimeInMillis();
    }

    public static String getReverseDateString2(String dateString) {
        try {
            return myMenusReverseDateFormat.format(myMenusDateFormat.parse(dateString));
        } catch (ParseException e) {
            return dateString;
        }
    }

    public static String getReverseDateString(String dateString) {
        String[] splitted = dateString.split("\\.");
        return splitted[2] + "." + splitted[1] + "." + splitted[0];
    }

    public static MyMenusItem findMyMenusItem(ArrayList<MyMenusItem> list, Calendar c) {
        for (MyMenusItem myMenusItem : list) {
            if (myMenusItem.date.equals(c))
                return myMenusItem;
        }
        return null;
    }


    private static void setupReminder(Context context) throws NumberFormatException {
        AlarmManager manager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PrefConstants.REMINDER_HOUR, 12);
        int minute = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PrefConstants.REMINDER_MINUTE, 0);
        String dayString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefConstants.REMINDER_DAY, "6");
        int currentDay = cal.get(Calendar.DAY_OF_WEEK);
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = cal.get(Calendar.MINUTE);
        int day = Integer.parseInt(dayString);
        int addDay = day > currentDay ? day - currentDay : (7 - currentDay) + day;
        addDay = day == currentDay && (hour >= currentHour && minute > currentMinute) ? 0 : addDay;
        cal.add(Calendar.DAY_OF_WEEK, addDay);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        long interval = AlarmManager.INTERVAL_DAY * 7;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pendingIntent);
        Log.d(TAG, "Reminder set to " + cal.getTime().toString());
    }

    public static void setupReminder(Context context, boolean cancelFirst) {
        if (cancelFirst) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            manager.cancel(pendingIntent);
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PrefConstants.REMINDER, true)) {
            try {
                setupReminder(context);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Reminder Set Failed");
                e.printStackTrace();
            }
        }
    }

    public static void reminderNotification(Context context, boolean isTest) {
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
                        .setSmallIcon(R.drawable.ic_action_help)
                        .setContentTitle(reminderText)
                        .setAutoCancel(true)
                        .setTicker(reminderText);

        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        if (!isTest)
            mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(87878787, mBuilder.build());
    }

    public static String getDeviceInfo(Context context) {
        return String.format(context.getString(R.string.device_info),
                android.os.Build.BRAND,
                android.os.Build.MODEL,
                android.os.Build.DEVICE,
                android.os.Build.VERSION.RELEASE,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE);
    }
}
