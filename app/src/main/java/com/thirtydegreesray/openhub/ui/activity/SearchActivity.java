/*
 *    Copyright 2017 ThirtyDegreesRay
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.thirtydegreesray.openhub.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.common.AppEventBus;
import com.thirtydegreesray.openhub.common.Event;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.inject.module.ActivityModule;
import com.thirtydegreesray.openhub.mvp.contract.ISearchContract;
import com.thirtydegreesray.openhub.mvp.model.SearchModel;
import com.thirtydegreesray.openhub.mvp.presenter.SearchPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.PagerActivity;
import com.thirtydegreesray.openhub.ui.adapter.base.FragmentPagerModel;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ThirtyDegreesRay on 2017/8/25 10:54:04
 */

public class SearchActivity extends PagerActivity<SearchPresenter>
        implements ISearchContract.View,
        MenuItemCompat.OnActionExpandListener,
        SearchView.OnQueryTextListener {

    public static void show(@NonNull Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        context.startActivity(intent);
    }

    private final Map<Integer, List<Integer>> MENU_ID_MAP = new HashMap<>();

    private boolean isInputMode = true;
    private String[] sortInfos;

    @Override
    protected void initActivity() {
        super.initActivity();
        MENU_ID_MAP.put(0, SearchModel.REPO_SORT_ID_LIST);
        MENU_ID_MAP.put(1, SearchModel.USER_SORT_ID_LIST);
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(new ActivityModule(getActivity()))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    protected int getContentView() {
        return R.layout.activity_view_pager;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setToolbarBackEnable();
        sortInfos = new String[]{
                getString(R.string.best_match), getString(R.string.best_match)
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQuery(mPresenter.getSearchModels().get(0).getQuery(), false);
        if (isInputMode) {
            MenuItemCompat.expandActionView(searchItem);
        } else {
            MenuItemCompat.collapseActionView(searchItem);
        }
        MenuItemCompat.setOnActionExpandListener(searchItem, this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            filterSortMenu(item);
        } else if (SearchModel.SORT_ID_LIST.contains(item.getItemId())) {
            int page = viewPager.getCurrentItem();
            postSearchEvent(mPresenter.getSortModel(page, item.getItemId()));
            sortInfos[page] = item.getTitle().toString();
            setSubTitle(page);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isInputMode) {
            menu.findItem(R.id.action_info).setVisible(false);
            menu.findItem(R.id.action_sort).setVisible(false);
            SearchView searchView = (SearchView) MenuItemCompat.
                    getActionView(menu.findItem(R.id.action_search));
            searchView.setQuery(mPresenter.getSearchModels().get(0).getQuery(), false);
        } else {
            menu.findItem(R.id.action_info).setVisible(false);
            menu.findItem(R.id.action_sort).setVisible(pagerAdapter.getCount() != 0);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        isInputMode = true;
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        isInputMode = false;
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (StringUtils.isBlank(query)) {
            showWarningToast(getString(R.string.invalid_query));
            return true;
        }
        isInputMode = false;
        invalidateOptionsMenu();
        search(query);
        setSubTitle(viewPager.getCurrentItem());
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void filterSortMenu(MenuItem item) {
        int index = viewPager.getCurrentItem();
        List<Integer> idList = MENU_ID_MAP.get(index);
        for (Integer id : SearchModel.SORT_ID_LIST) {
            item.getSubMenu().findItem(id).setVisible(idList.contains(id));
        }
    }

    private void search(String query) {
        if (pagerAdapter.getCount() == 0) {
            pagerAdapter.setPagerList(FragmentPagerModel.
                    createSearchPagerList(getActivity(), mPresenter.getQueryModels(query)));
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setupWithViewPager(viewPager);
            viewPager.setAdapter(pagerAdapter);
        } else {
            for (SearchModel searchModel : mPresenter.getQueryModels(query)) {
                postSearchEvent(searchModel);
            }
        }
    }

    private void postSearchEvent(SearchModel searchModel) {
        AppEventBus.INSTANCE.getEventBus().post(new Event.SearchEvent(searchModel));
    }

    private void setSubTitle(int page) {
        setToolbarSubTitle(mPresenter.getSearchModels().get(0).getQuery() + "/" + sortInfos[page]);
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        setSubTitle(position);
    }
}
