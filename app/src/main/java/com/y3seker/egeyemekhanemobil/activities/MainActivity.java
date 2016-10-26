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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.y3seker.egeyemekhanemobil.Database;
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.constants.OtherConstants;
import com.y3seker.egeyemekhanemobil.constants.ParseConstants;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.constants.RequestCodes;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.retrofit.SerializableHttpCookie;
import com.y3seker.egeyemekhanemobil.ui.MainRVAdapter;
import com.y3seker.egeyemekhanemobil.utils.ConnectionUtils;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;
import com.y3seker.egeyemekhanemobil.utils.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOGGED_IN = 1;
    private static final int LOGGED_OUT = 2;
    private static final int LOGIN_FAILED = 3;
    private static final int NO_USER = 4;
    private static final int USER_DELETED = 5;
    @Bind(R.id.main_appbar)
    AppBarLayout appBarLayout;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_image)
    ImageView mainImage;
    @Bind(R.id.main_rv)
    RecyclerView mainRV;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Menu menu;
    private TextView navHeaderName;
    private TextView navHeaderUsername;
    private MainRVAdapter mainRVAdapter;
    private List<Object> cardList;
    private List<MenuItem> menuItems;
    private List<User> users;
    private Map<User, MyMenusItem> userMenus;
    private Subscription userInfoSub;
    private User currentUser;
    private Database database;
    private ProgressDialog progressDialog;
    private SharedPreferences cookiesPrefs;
    private SharedPreferences appPrefs;
    @SignState
    private int LOGIN_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        menuItems = new ArrayList<>();
        userMenus = new HashMap<>();
        database = new Database(this);
        cookiesPrefs = this.getSharedPreferences(PrefConstants.COOKIE_STORE_PREF, MODE_PRIVATE);
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        users = getIntent().getParcelableArrayListExtra("users");
        if (users == null)
            users = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.logging_in));

        setupDrawer();
        setupUsers();
        setupCards();
        Utils.setupReminder(this, true);
    }

    private void setupUsers() {
        if (users.size() == 0) {
            updateNavigationView(NO_USER);
            return;
        }
        User defaultUser = null;
        long defUser = appPrefs.getLong(PrefConstants.DEFAULT_USER, 0);
        for (User user : users) {
            menu.add(menu.findItem(R.id.nav_add_acc).getGroupId(), user.hashCode(), Menu.NONE, user.getMenuLabel())
                    .setIcon(R.drawable.ic_action_label_outline);
            if (user.isLoggedIn())
                setCurrentUser(user);
            if (user.getUniqeID() == defUser)
                defaultUser = user;
        }

        if (getIntent().getAction().equals(LoginActivity.LOGIN_FAILED_ACTION)) {
            setCurrentUser(defaultUser != null ? defaultUser : users.get(0));
        }
    }

    private void setupCards() {
        cardList = new ArrayList<>();
        mainRV.setLayoutManager(new LinearLayoutManager(this));
        mainRVAdapter = new MainRVAdapter(this, cardList);
        mainRV.setAdapter(mainRVAdapter);
        if (currentUser != null) {
            addMenuCard("O");
            addMenuCard("A");
        }
    }

    private void getUserInfo() {
        if (userInfoSub != null && !userInfoSub.isUnsubscribed())
            userInfoSub.unsubscribe();
        userInfoSub = RetrofitManager.api().getMyMenus()
                .flatMap(new Func1<Document, Observable<?>>() {
                    @Override
                    public Observable<?> call(Document document) {
                        ParseUtils.extractViewState(currentUser.getViewStates(), document);
                        return RetrofitManager.api()
                                .postMyMenus(MyMenusActivity.getMyMenusRequestBody(currentUser.getViewStates(),
                                        Utils.today, Utils.today));
                    }
                })
                .retry(2)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "getUserInfo, onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Document document) {

                        ArrayList<MyMenusItem> menusItems = MyMenusActivity.parseMenus(document);
                        if (menusItems != null) {
                            for (MyMenusItem menusItem : menusItems) {
                                menusItem.dateString = getString(R.string.mymenus_today);
                                userMenus.put(currentUser, menusItem);
                                cardList.add(0, menusItem);
                                mainRVAdapter.notifyItemInserted(0);
                            }
                        } else {
                            MyMenusItem noMenu = new MyMenusItem(getString(R.string.mymenus_today));
                            userMenus.put(currentUser, noMenu);
                            cardList.add(0, noMenu);
                            mainRVAdapter.notifyItemInserted(0);
                        }
                    }
                });
    }

    private void addMenuCard(final String menuType) {
        final String date = Utils.orderDateFormat.format(Utils.today.getTime());
        final String url = currentUser.getBaseUrl() +
                String.format(UrlConstants.C_MENU, date, menuType);
        RetrofitManager.api().getRequest(url)
                .compose(this.bindUntilEvent(ActivityEvent.STOP))
                .cast(Document.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "addMenuCard failed for " + url);
                    }

                    @Override
                    public void onNext(Document document) {
                        String menu;
                        Element table = document.select("[id=lblTable]").first();
                        if (table.children().size() == 0)
                            menu = getString(R.string.no_menu_found);
                        else {
                            Elements menuRows = table.select("tbody").first().children();
                            menuRows.remove(0);
                            menu = (menuType.equals("O") ? "Öğle " : "Akşam ") + "Yemek Listesi\n\n";
                            for (Element menuRow : menuRows) {
                                menu += menuRow.text() + "\n";
                            }
                        }
                        cardList.add(menu.trim());
                        mainRVAdapter.notifyItemInserted(cardList.size() - 1);
                    }
                });
    }

    private void setupDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        if (!appPrefs.getBoolean(PrefConstants.DRAWER_LEARNED, false)) {
            drawer.openDrawer(GravityCompat.START);
            appPrefs.edit().putBoolean(PrefConstants.DRAWER_LEARNED, true).apply();
        }
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        navHeaderName = (TextView) navHeader.findViewById(R.id.nav_header_name);
        navHeaderUsername = (TextView) navHeader.findViewById(R.id.nav_header_username);
        menu = navigationView.getMenu();

        navHeaderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LOGIN_STATE == LOGGED_IN)
                    toggleMenuAccounts();
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        for (int i = 0; i < menu.size(); i++) {
            menuItems.add(menu.getItem(i));
        }
    }

    private void toggleMenuAccounts() {
        boolean isExpanded = menu.findItem(R.id.nav_add_acc).isVisible();
        if (Build.VERSION.SDK_INT == 17)
            navHeaderName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                    isExpanded ? R.drawable.ic_action_expand_more : R.drawable.ic_action_expand_less, 0);
        else
            navHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    isExpanded ? R.drawable.ic_action_expand_more : R.drawable.ic_action_expand_less, 0);
        menu.setGroupVisible(R.id.nav_group_nologin, !isExpanded);
    }

    private void addNewUser(User newUser) {
        users.add(newUser);
        database.insertUser(newUser);
        menu.add(menu.findItem(R.id.nav_add_acc).getGroupId(), newUser.hashCode(), Menu.NONE, newUser.getMenuLabel())
                .setIcon(R.drawable.ic_action_label_outline);
        setCurrentUser(newUser);
    }

    private void setCurrentUser(User user) {
        User prevUser = currentUser;
        if (prevUser != null && menu.findItem(prevUser.hashCode()) != null) {
            Log.d(TAG, "user changed, prevUser: " + prevUser.getName());
            menu.findItem(prevUser.hashCode()).setIcon(R.drawable.ic_action_label_outline);
            if (cardList.contains(userMenus.get(prevUser)))
                cardList.remove(0);
        }
        currentUser = user;

        if (currentUser != null) {
            mainImage.setImageResource(currentUser.getCafeteriaNumber() != 2 ? R.drawable.ege1_t : R.drawable.ege2_t);
            if (!currentUser.isLoggedIn())
                login(currentUser);
            else {
                updateNavigationView(LOGGED_IN);
                if (userMenus.containsKey(currentUser)) {
                    cardList.add(0,userMenus.get(currentUser));
                    mainRVAdapter.notifyItemChanged(0);
                } else {
                    getUserInfo();
                }
            }
        }

    }

    private void updateNavigationView(final int state) {
        LOGIN_STATE = state;
        switch (state) {
            case LOGGED_IN:
                navHeaderName.setText(currentUser.getName());
                navHeaderUsername.setText(currentUser.getUsername());
                menu.findItem(currentUser.hashCode()).setIcon(R.drawable.ic_action_label);
                navigationView.setCheckedItem(currentUser.hashCode());
                menu.setGroupVisible(R.id.nav_group_login, true);
                menu.setGroupVisible(R.id.nav_group_nologin, false);
                toggleMenuAccounts();
                break;
            case LOGGED_OUT:
            case LOGIN_FAILED:
                navHeaderName.setText(R.string.pls_login);
                navHeaderUsername.setText("");
                menu.setGroupVisible(R.id.nav_group_login, false);
                menu.setGroupVisible(R.id.nav_group_nologin, true);
                navHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                break;
            case NO_USER:
                navHeaderName.setText(R.string.add_account_to_use);
                navHeaderUsername.setText("");
                navHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                menu.setGroupVisible(R.id.nav_group_login, false);
                menu.setGroupVisible(R.id.nav_group_nologin, true);
                break;
            case USER_DELETED:
                break;
            default:
                break;
        }

    }

    private void afterLoginUpdateUI(final boolean isLoginSucceed) {
        progressDialog.dismiss();
        if (isLoginSucceed) {
            currentUser.setIsLoggedIn(true);
            updateNavigationView(LOGGED_IN);
            setCurrentUser(currentUser);
            makeSnackBar(String.format(getString(R.string.logged_in_as), currentUser.getName())).show();
        } else {
            currentUser.setIsLoggedIn(false);
            updateNavigationView(LOGGED_OUT);
        }
    }

    private void login(final User currentUser) {
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.show();
        ConnectionUtils.loginObservable(currentUser)
                .compose(this.bindToLifecycle())
                .cast(Document.class)
                .subscribe(new Subscriber<Document>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        afterLoginUpdateUI(false);
                        makeSnackBar(getString(R.string.connection_error)).setDuration(Snackbar.LENGTH_LONG)
                                .setAction(R.string.try_again, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        login(currentUser);
                                    }
                                }).show();
                    }

                    @Override
                    public void onNext(Document document) {
                        currentUser.setCookie(RetrofitManager.getCookie());
                        cookiesPrefs.edit()
                                .putString(currentUser.getCookieKey(), new SerializableHttpCookie().encode(currentUser.getCookie()))
                                .apply();
                        afterLoginUpdateUI(true);
                    }
                });
    }

    private Snackbar makeSnackBar(String message) {
        return Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        database.close();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.LOGIN_REQ_CODE:
                if (resultCode == RESULT_OK) {
                    User user = data.getParcelableExtra(ParseConstants.USER);
                    addNewUser(user);
                    navigationView.getMenu().setGroupVisible(R.id.nav_group_login, true);
                    navigationView.getMenu().setGroupVisible(R.id.nav_group_nologin, false);
                }
                break;
            case RequestCodes.BASE_REQ_CODE:
                if (resultCode == RESULT_OK) {
                    User user = data.getParcelableExtra(ParseConstants.USER);
                    if (!currentUser.getCookie().getValue().equals(user.getCookie().getValue())) {
                        currentUser.setCookie(user.getCookie());
                        cookiesPrefs.edit()
                                .putString(currentUser.getCookieKey(),
                                        new SerializableHttpCookie().encode(currentUser.getCookie()))
                                .apply();
                    }
                }
                break;
            case RequestCodes.SETTINGS_REQ_CODE:

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        int[] clickPos = new int[2];
        findNavItemPosition(item, clickPos);
        switch (id) {
            case R.id.nav_add_acc:
                startActivityFR(users, AddUserActivity.class, RequestCodes.LOGIN_REQ_CODE);
                break;

            case R.id.nav_balance:
                startActivityFR(currentUser, BalanceActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_my_menus:
                startActivityFR(currentUser, MyMenusActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_menu_order:
                startActivityFR(currentUser, OrderActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_menu_cancel:
                startActivityFR(currentUser, CancelActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_caf_history:
                startActivityFR(currentUser, MyActsActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;
            case R.id.nav_settings:
                startActivityFR(users, SettingsActivity.class, RequestCodes.SETTINGS_REQ_CODE);
                break;
            case R.id.nav_about:
                startActivityFR(currentUser, AboutActivity.class, RequestCodes.SETTINGS_REQ_CODE, clickPos);
                break;
            case R.id.nav_feedback:
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("y3seker@gmail.com") +
                        "?subject=" + Uri.encode(getString(R.string.feedback_title)) +
                        "&body=" + Uri.encode(Utils.getDeviceInfo(this));
                Uri uri = Uri.parse(uriText);
                send.setData(uri);
                try {
                    startActivity(Intent.createChooser(send, getString(R.string.send_via)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, getString(R.string.feedback_cant_send),
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                User selected = null;
                for (User user : users) {
                    if (user.hashCode() == id)
                        selected = user;
                }
                if (selected != null) {
                    // Clicked on other user
                    if (!selected.equals(currentUser))
                        setCurrentUser(selected);
                    else {
                        // Clicked on current user
                        if (currentUser.isLoggedIn())
                            showLoggedUserDialog().show();
                        else
                            login(currentUser);
                    }
                }
                break;
        }
        return true;
    }

    private void findNavItemPosition(MenuItem item, int[] pos) {
        int posIndex = 1;
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).equals(item))
                posIndex = i;
        }
        View v = navigationMenuView.getChildAt(posIndex);
        if (v != null)
            v.getLocationOnScreen(pos);
    }

    private void startActivityFR(User user, Class cls, int code, int[] clickPos) {
        Intent i = new Intent(getApplicationContext(), cls);
        i.putExtra(ParseConstants.USER, user);
        i.putExtra(OtherConstants.REVEAL_POSITION, clickPos);
        startActivityForResult(i, code);
    }

    private void startActivityFR(List<User> users, Class cls, int code) {
        Intent l = new Intent(getApplicationContext(), cls);
        l.setAction(LoginActivity.ADD_USER_ACTION);
        l.putParcelableArrayListExtra(ParseConstants.USERS, (ArrayList<? extends Parcelable>) users);
        startActivityForResult(l, code);
    }

    private AlertDialog.Builder showLoggedUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.user_info)
                .setMessage(currentUser.toString())
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (database.deleteUser(currentUser.getUniqeID())) {
                            makeSnackBar(getString(R.string.user_deleted) + currentUser.getMenuLabel());
                            users.remove(currentUser);
                            menu.removeItem(currentUser.hashCode());
                            if (users.size() == 0) {
                                updateNavigationView(NO_USER);
                                setCurrentUser(null);
                            } else {
                                setCurrentUser(users.get(0));
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.close, null)
                .create();
        return builder;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOGGED_IN, LOGGED_OUT, LOGIN_FAILED, NO_USER, USER_DELETED})
    public @interface SignState {
    }
}
