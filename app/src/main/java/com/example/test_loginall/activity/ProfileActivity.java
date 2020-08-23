package com.example.test_loginall.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.test_loginall.R;
import com.example.test_loginall.model.User;
import com.example.test_loginall.retrofit.call_api.UserCallApi;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private ImageView imgAvatar;
    private TextView tvName;
    private TextView tvBirthDay;
    private TextView tvEmail;
    private TextView tvDate;
    private Button btnDisconnect;
    private String social;
    private Button btnLogOut;
    private String accessToken;
    private Date expirationTime;
    private GoogleApiClient googleApiClient;
    private GoogleSignInClient googleSignInClient;
    private UserCallApi userCallApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvBirthDay = findViewById(R.id.tvBirthDay);
        tvEmail = findViewById(R.id.tvEmail);
        tvDate = findViewById(R.id.tvDate);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnLogOut = findViewById(R.id.btnLogOut);

        social = getIntent().getStringExtra("social");
        btnDisconnect.setText("Ngắt kết nối với " + social);
        checkSocial(social);
    }

    public void checkSocial(String social) {
        switch (social) {
            case "Facebook":
                loadProfileFacebook();
                logOutFacebook();
                disconnectFaceBook();
                break;
            case "Google":
                loadProfileGoogle();
                logOutGoogle();
                disconnectGoogle();
                break;
            case "Zalo":
                loadProfileZalo();
                logOutZalo();
                break;
        }
    }

    private void logOutZalo() {
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZaloSDK.Instance.unauthenticate();
                finish();
            }
        });

    }

    private void loadProfileFacebook() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");
                    String birth_day = object.getString("birthday");
                    String id = object.getString("id");
                    expirationTime = AccessToken.getCurrentAccessToken().getDataAccessExpirationTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String date_exp = formatter.format(expirationTime);
                    String birth_day_format = birth_day.substring(3, 5) + "/" + birth_day.substring(0, 2) + "/" + birth_day.substring(6);
                    showDataFaceBook(date_exp, name, id, birth_day_format, email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, id, birthday ,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void showDataFaceBook(String date_exp, String name, String id, String birth_day, String email) {
        tvDate.setText(date_exp);
        tvName.setText(name);
        tvBirthDay.setText(birth_day);
        tvEmail.setText(email);
        String url_image = "https://graph.facebook.com/" + id + "/picture?return_ssl_resource=1";
        Glide.with(ProfileActivity.this)
                .load(url_image)
                .into(imgAvatar);
    }

    private void logOutFacebook() {
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                finish();
            }
        });
    }

    private void disconnectFaceBook() {
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                        .Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        LoginManager.getInstance().logOut();
                        finish();
                        Toast.makeText(ProfileActivity.this, "Hủy liên kết với facebook", Toast.LENGTH_SHORT).show();
                    }
                }).executeAsync();
            }
        });
    }

    private void loadProfileGoogle() {
        String serverClientId = getString(R.string.server_client_id);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void showDataGoogle(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            tvName.setText(account.getDisplayName());
            tvEmail.setText(account.getEmail());
            Glide.with(this)
                    .load(account.getPhotoUrl())
                    .into(imgAvatar);
            showExp(account);
        }
    }

    private void showExp(GoogleSignInAccount account) {
        Call<User> call = userCallApi.getUser(account.getIdToken());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()) {
                    tvDate.setText(response.code() + "");
                } else {
                    User user = response.body();
                    Log.e("EXP", user.getExp() + "");
                    long exp = Long.parseLong(user.getExp());
                    Date date = new Date(exp * 1000L);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String date_exp = formatter.format(date);

                    tvDate.setText(date_exp + "");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvDate.setText(t.getMessage());
            }
        });
    }

    private void logOutGoogle() {
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            finish();
                        }
                    }
                });
            }
        });

    }

    private void disconnectGoogle() {
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignInClient.revokeAccess()
                        .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                finish();
                                Toast.makeText(ProfileActivity.this, "Hủy liên lết với Google", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void loadProfileZalo() {
        ZaloOpenAPICallback callback = new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject jsonObject) {
                String name = jsonObject.optString("name");
                String birthday = jsonObject.optString("birthday");
                JSONObject jsonObject1 = jsonObject.optJSONObject("picture");
                JSONObject jsonObject2 = jsonObject1.optJSONObject("data");
                String url_image = jsonObject2.optString("url");
                showDataZalo(name, birthday, url_image);
            }
        };
        String fields[] = {"gender", "name", "picture", "birthday"};

        ZaloSDK.Instance.getProfile(ProfileActivity.this, callback, fields);
    }

    private void showDataZalo(String name, String birthday, String url_image) {
        tvName.setText(name);
        tvBirthDay.setText(birthday);
        if (url_image != null) {
            Glide.with(ProfileActivity.this)
                    .load(url_image)
                    .into(imgAvatar);
        } else {
            Glide.with(ProfileActivity.this)
                    .load(R.drawable.ic_launcher_background)
                    .into(imgAvatar);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (social.equalsIgnoreCase("Google")) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
            if (opr.isDone()) {
                GoogleSignInResult result = opr.get();
                showDataGoogle(result);
            } else {
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult result) {
                        showDataGoogle(result);
                    }
                });
            }
        }
    }


}