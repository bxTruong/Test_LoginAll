package com.example.test_loginall.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.test_loginall.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.security.MessageDigest;
import java.util.Arrays;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener;
import com.zing.zalo.zalosdk.oauth.OauthResponse;
import com.zing.zalo.zalosdk.oauth.ValidateOAuthCodeCallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ImageView imgGoogle;
    private ImageView imgZalo;
    private LoginButton btnLoginWithFacebook;
    private GoogleApiClient googleApiClient;
    private final static int SIGN_IN_GOOGLE = 1;
    private final static int SIGN_IN_FACEBOOK = 2;
    private final static int SIGN_IN_ZALO = 3;


    private CallbackManager callbackManager;
    private OAuthCompleteListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        setUpFacebook();
        setUpGoogle();
        setUpZalo();

        try {
            getApplicationHashKey(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getApplicationHashKey(Context ctx) throws Exception {
        PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
        for (Signature signature : info.signatures) {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(signature.toByteArray());
            String sig = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
            if (sig.trim().length() > 0) {
                Log.e("ZALo", sig);
                return sig;
            }
        }
        return null;
    }


    private void findView() {
        imgGoogle = findViewById(R.id.imgGoogle);
        imgZalo = findViewById(R.id.imgZalo);
        btnLoginWithFacebook = findViewById(R.id.btnLoginFacebook);
    }

    private void setUpGoogle() {
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

        imgGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_GOOGLE);
            }
        });
    }

    private void setUpFacebook() {
        callbackManager = CallbackManager.Factory.create();
        btnLoginWithFacebook.setReadPermissions(Arrays.asList("email", "public_profile", "user_birthday"));

        btnLoginWithFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                openProfile("Facebook");
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, error + "", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setUpZalo() {
        imgZalo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZaloSDK.Instance.authenticate(MainActivity.this, LoginVia.APP_OR_WEB, listener);
            }
        });

        listener = new OAuthCompleteListener() {
            @Override
            public void onAuthenError(int errorCode, String message) {
                Log.e("ERROR", errorCode + "\n" + message);
            }

            @Override
            public void onGetOAuthComplete(OauthResponse response) {
                String code = response.getOauthCode();
                openProfile("Zalo");
            }
        };

        if (ZaloSDK.Instance.isAuthenticate(null)) {
            openProfile("Zalo");
        }

        ZaloSDK.Instance.isAuthenticate(new ValidateOAuthCodeCallback() {
            @Override
            public void onValidateComplete(boolean validated, int errorCode, long userId, String oauthCode) {
                if (validated) {
                    Log.e("ZALO", validated + "\n" + errorCode + "\n" + userId + "\n" + oauthCode);
                }
            }
        });
    }

    private void openProfile(String social) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("social", social);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.e("result", result.isSuccess() + "");
            if (result.isSuccess()) {
                openProfile("Google");
            } else {
                Toast.makeText(this, "Login Google Fail", Toast.LENGTH_SHORT).show();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
        ZaloSDK.Instance.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}