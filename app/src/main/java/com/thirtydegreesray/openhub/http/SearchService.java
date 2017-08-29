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

package com.thirtydegreesray.openhub.http;

import android.support.annotation.NonNull;

import com.thirtydegreesray.openhub.mvp.model.Repository;
import com.thirtydegreesray.openhub.mvp.model.SearchResult;
import com.thirtydegreesray.openhub.mvp.model.User;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by ThirtyDegreesRay on 2017/8/25 13:52:09
 */

public interface SearchService {

//    https://api.github.com/search/users?q=Ray&sort=followers&order=desc
    @NonNull
    @GET("search/users")
    Observable<Response<SearchResult<User>>> searchUsers(
            @Query("q") String query,
            @Query("sort") String sort,
            @Query("order") String order,
            @Query("page") int page
    );

    @NonNull
    @GET("search/repositories")
    Observable<Response<SearchResult<Repository>>> searchRepos(
            @Query("q") String query,
            @Query("sort") String sort,
            @Query("order") String order,
            @Query("page") int page
    );

}
