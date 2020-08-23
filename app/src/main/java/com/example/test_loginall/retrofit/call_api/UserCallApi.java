package com.example.test_loginall.retrofit.call_api;

import com.example.test_loginall.model.User;
import com.example.test_loginall.retrofit.api_method.UserGoogleMethodApi;
import com.example.test_loginall.retrofit.connect_api.ConnectRetrofit;

import retrofit2.Call;

public class UserCallApi {
    private static String url_user = "https://oauth2.googleapis.com/";
    private static ConnectRetrofit connectRetrofit;
    private static UserGoogleMethodApi userGoogleMethodApi = connectRetrofit
            .createRetrofit(url_user)
            .create(UserGoogleMethodApi.class);

    public static Call<User> getUser(String idToken){
        Call<User> call=userGoogleMethodApi.getUser("https://oauth2.googleapis.com/tokeninfo?id_token="+idToken);
        return call;
    }
}
