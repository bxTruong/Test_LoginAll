package com.example.test_loginall.retrofit.api_method;

import com.example.test_loginall.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface UserGoogleMethodApi {
    @GET
    Call<User> getUser(@Url String url);
}
