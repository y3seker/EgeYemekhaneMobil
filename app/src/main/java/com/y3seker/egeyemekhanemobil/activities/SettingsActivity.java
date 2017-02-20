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

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TimePicker;

import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.UserManager;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.utils.Utils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Yunus Emre Şeker on 2.11.2015.
 * -
 */
public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.settings_toolbar)
    Toolbar toolbar;
    private SharedPreferences appPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
        SettingsFragment settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment_root, settingsFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        Utils.setupReminder(this, true);
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        int hour;
        int minute;
        List<User> users;
        SharedPreferences appPrefs;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.settings);
            appPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            users = UserManager.getInstance().getUsers();
        }

        @Override
        public void onStart() {
            super.onStart();
            final ListPreference listPreference = (ListPreference) findPreference("default_user_s");
            if (users.size() == 0) {
                listPreference.setEnabled(false);
                return;
            }
            CharSequence[] entries = new CharSequence[users.size()];
            CharSequence[] entryValues = new CharSequence[users.size()];
            long defaultVal = appPrefs.getLong(PrefConstants.DEFAULT_USER, 0);
            int x = 0;
            for (int i = 0; i < users.size(); i++) {
                entries[i] = users.get(i).getName() + " #" + users.get(i).getCafeteriaNumber();
                entryValues[i] = users.get(i).getUniqeID() + "";
                if (defaultVal == users.get(i).getUniqeID())
                    x = i;
            }
            listPreference.setEntries(entries);
            listPreference.setEntryValues(entryValues);
            listPreference.setValueIndex(x);
            listPreference.setSummary(listPreference.getEntries()[x]);
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    appPrefs.edit().putLong(PrefConstants.DEFAULT_USER, Long.parseLong(o.toString())).apply();
                    String sum = "";
                    for (int i = 0; i < listPreference.getEntryValues().length; i++) {
                        if (o.equals(listPreference.getEntryValues()[i]))
                            sum = (String) listPreference.getEntries()[i];
                    }
                    listPreference.setSummary(sum);
                    return true;
                }
            });
            final ListPreference reminderDay = (ListPreference) findPreference(PrefConstants.REMINDER_DAY);
            reminderDay.setSummary(reminderDay.getEntry());
            reminderDay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    int day = Integer.parseInt(((String) o));
                    reminderDay.setSummary(reminderDay.getEntries()[day - 2]);
                    return true;
                }
            });
            final EditTextPreference reminderText = (EditTextPreference) findPreference(PrefConstants.REMINDER_TEXT);
            reminderText.setSummary(reminderText.getText());
            reminderText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    reminderText.setSummary(((String) o));
                    return true;
                }
            });

            minute = appPrefs.getInt(PrefConstants.REMINDER_MINUTE, 0);
            hour = appPrefs.getInt(PrefConstants.REMINDER_HOUR, 12);
            Preference reminderHour = findPreference(PrefConstants.REMINDER_HOUR);
            reminderHour.setSummary(generateTimeString(hour, minute));
            reminderHour.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTimePicker();
                    return true;
                }
            });
            findPreference(PrefConstants.REMINDER_TEST).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.reminderNotification(getActivity().getBaseContext(), true);
                    return true;
                }
            });
        }

        private void showTimePicker() {
            new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    appPrefs.edit()
                            .putInt(PrefConstants.REMINDER_HOUR, hourOfDay)
                            .putInt(PrefConstants.REMINDER_MINUTE, minute)
                            .apply();
                    findPreference(PrefConstants.REMINDER_HOUR)
                            .setSummary(generateTimeString(hourOfDay, minute));
                }
            }, hour, minute, true).show();
        }

        private String generateTimeString(int h, int m) {
            return (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m);
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

    }

}
