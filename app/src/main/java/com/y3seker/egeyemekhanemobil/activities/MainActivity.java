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
import android.preference.PreferenceManager;
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
import com.y3seker.egeyemekhanemobil.R;
import com.y3seker.egeyemekhanemobil.UserManager;
import com.y3seker.egeyemekhanemobil.constants.OtherConstants;
import com.y3seker.egeyemekhanemobil.constants.PrefConstants;
import com.y3seker.egeyemekhanemobil.constants.RequestCodes;
import com.y3seker.egeyemekhanemobil.constants.UrlConstants;
import com.y3seker.egeyemekhanemobil.models.MyMenusItem;
import com.y3seker.egeyemekhanemobil.models.User;
import com.y3seker.egeyemekhanemobil.retrofit.RetrofitManager;
import com.y3seker.egeyemekhanemobil.ui.MainRVAdapter;
import com.y3seker.egeyemekhanemobil.utils.ParseUtils;
import com.y3seker.egeyemekhanemobil.utils.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    public static final String TAG = MainActivity.class.getSimpleName();
    @Bind(R.id.main_appbar)
    AppBarLayout appBarLayout;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_image)
    ImageView mainImage;
    @Bind(R.id.main_rv)
    RecyclerView mainRV;
    Menu menu;
    MainRVAdapter mainRVAdapter;
    List<Object> cardList;
    Map<User, MyMenusItem> userMenus = new HashMap<>();
    ProgressDialog progressDialog;
    List<MenuItem> userMenuItems = new ArrayList<>();
    private NavigationView navigationView;
    private TextView navHeaderName;
    private TextView navHeaderUsername;
    private Subscription userInfoSub;
    private SharedPreferences appPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        if (UserManager.getInstance().hasUser()) {
            for (User user : UserManager.getInstance().getUsers()) {
                MenuItem menuItem = menu.add(menu.findItem(R.id.nav_add_acc).getGroupId(), user.hashCode(), Menu.NONE, user.getMenuLabel())
                        .setIcon(user.isLoggedIn() ? R.drawable.ic_action_label : R.drawable.ic_action_label_outline);
                userMenuItems.add(menuItem);
            }
        }
        updateNavigationView();
    }

    private void setupCards() {
        cardList = new ArrayList<>();
        mainRV.setLayoutManager(new LinearLayoutManager(this));
        mainRVAdapter = new MainRVAdapter(this, cardList);
        mainRV.setAdapter(mainRVAdapter);
        addMenuCard("O");
        addMenuCard("A");
    }

    // FIXME
    private void getUserInfo() {
        if (userInfoSub != null && !userInfoSub.isUnsubscribed())
            userInfoSub.unsubscribe();
        final User currentUser = UserManager.getInstance().getCurrentUser();
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
        User currentUser = UserManager.getInstance().getCurrentUser();
        final int menuIndex = menuType.equals("O") ? 1 : 2;
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
                        e.printStackTrace();
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
                        int insertIndex = menuIndex < cardList.size() ? menuIndex : cardList.size();
                        cardList.add(insertIndex, menu.trim());
                        mainRVAdapter.notifyItemInserted(insertIndex);
                    }
                });
    }

    private void setupDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                toggleMenuAccounts();
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
    }

    void toggleMenuAccounts() {
        boolean isExpanded = menu.findItem(R.id.nav_add_acc).isVisible();
        if (Build.VERSION.SDK_INT == 17)
            navHeaderName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                    isExpanded ? R.drawable.ic_action_expand_more : R.drawable.ic_action_expand_less, 0);
        else
            navHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    isExpanded ? R.drawable.ic_action_expand_more : R.drawable.ic_action_expand_less, 0);
        menu.setGroupVisible(R.id.nav_group_nologin, !isExpanded);
    }

    private void onNewUser() {
        User currentUser = UserManager.getInstance().getCurrentUser();
        MenuItem menuItem = menu.add(menu.findItem(R.id.nav_add_acc).getGroupId(), currentUser.hashCode(), Menu.NONE, currentUser.getMenuLabel())
                .setIcon(R.drawable.ic_action_label_outline);
        userMenuItems.add(menuItem);
        updateNavigationView();
    }

    void updateNavigationView() {
        // Reset login badges
        for (MenuItem userMenuItem : userMenuItems) {
            userMenuItem.setIcon(R.drawable.ic_action_label_outline);
        }

        // If we have no user
        if (!UserManager.getInstance().hasUser()) {
            navHeaderName.setText(R.string.add_account_to_use);
            navHeaderUsername.setText("");
            navHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            menu.setGroupVisible(R.id.nav_group_login, false);
            menu.setGroupVisible(R.id.nav_group_nologin, true);
            return;
        }

        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            mainImage.setImageResource(currentUser.getCafeteriaNumber() == 1 ? R.drawable.ege1_t : R.drawable.ege2_t);
            // If we have current user
            if (currentUser.isLoggedIn()) {
                navHeaderName.setText(currentUser.getName());
                navHeaderUsername.setText(currentUser.getUsername());
                menu.findItem(currentUser.hashCode()).setIcon(R.drawable.ic_action_label);
                navigationView.setCheckedItem(currentUser.hashCode());
                menu.setGroupVisible(R.id.nav_group_login, true);
                menu.setGroupVisible(R.id.nav_group_nologin, false);
                toggleMenuAccounts();
            }
        } else {
            navHeaderName.setText(R.string.pls_login); // :(
            navHeaderUsername.setText("");
            navHeaderName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            menu.setGroupVisible(R.id.nav_group_login, false);
            menu.setGroupVisible(R.id.nav_group_nologin, true);
        }
    }

    void login(final User user) {
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.show();
        UserManager.getInstance().login(user, this, new Subscriber<Document>() {
            @Override
            public void onCompleted() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                progressDialog.dismiss();
                updateNavigationView();
                makeSnackBar(getString(R.string.connection_error)).setDuration(Snackbar.LENGTH_LONG)
                        .setAction(R.string.try_again, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                login(user);
                            }
                        }).show();
                e.printStackTrace();
            }

            @Override
            public void onNext(Document document) {
                updateNavigationView();
                makeSnackBar(String.format(getString(R.string.logged_in_as),
                        UserManager.getInstance().getCurrentUser().getName())).show();
            }
        });
    }

    Snackbar makeSnackBar(String message) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.LOGIN_REQ_CODE:
                if (resultCode == RESULT_OK) {
                    onNewUser();
                }
                break;
            case RequestCodes.BASE_REQ_CODE:
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
                startActivityFR(AddUserActivity.class, RequestCodes.LOGIN_REQ_CODE, clickPos);
                break;

            case R.id.nav_balance:
                startActivityFR(BalanceActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_my_menus:
                startActivityFR(MyMenusActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_menu_order:
                startActivityFR(OrderActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_menu_cancel:
                startActivityFR(CancelActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_caf_history:
                startActivityFR(MyActsActivity.class, RequestCodes.BASE_REQ_CODE, clickPos);
                break;

            case R.id.nav_settings:
                startActivityFR(SettingsActivity.class, RequestCodes.SETTINGS_REQ_CODE, clickPos);
                break;

            case R.id.nav_about:
                startActivityFR(AboutActivity.class, RequestCodes.SETTINGS_REQ_CODE, clickPos);
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
                if (UserManager.getInstance().getCurrentUser().hashCode() == id)
                    showLoggedUserDialog().show();
                else {
                    login(UserManager.getInstance().getUserByHashcode(id));
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

    private void startActivityFR(Class cls, int code, int[] clickPos) {
        Intent i = new Intent(getApplicationContext(), cls);
        i.putExtra(OtherConstants.REVEAL_POSITION, clickPos);
        startActivityForResult(i, code);
    }

    private AlertDialog.Builder showLoggedUserDialog() {
        final User currentUser = UserManager.getInstance().getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.user_info)
                .setMessage(currentUser.toString())
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (UserManager.getInstance().deleteCurrentUser()) {
                            makeSnackBar(getString(R.string.user_deleted) + currentUser.getMenuLabel());
                            MenuItem menuItem = menu.findItem(currentUser.hashCode());
                            userMenuItems.remove(menuItem);
                            menu.removeItem(currentUser.hashCode());
                            updateNavigationView();
                        }
                    }
                })
                .setPositiveButton(R.string.close, null)
                .create();
        return builder;
    }
}
