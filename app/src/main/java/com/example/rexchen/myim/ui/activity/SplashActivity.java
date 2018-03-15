package com.example.rexchen.myim.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

import com.example.rexchen.myim.MainActivity;
import com.example.rexchen.myim.R;
import com.example.rexchen.myim.SealAppContext;

import io.rong.imkit.RongIM;

/**
 * Created by Rex on 2018/3/15.
 * Email chenhm4444@gmail.com
 */

public class SplashActivity extends Activity {
    private Context context;
    private android.os.Handler handler = new android.os.Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        context = this;
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        String cacheToken = sp.getString("loginToken", "");
        if (!TextUtils.isEmpty(cacheToken)) { // 如果有 token，进入到 主界面
            RongIM.connect(cacheToken, SealAppContext.getInstance().getConnectCallback());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToMain();
                }
            }, 800);
        } else { // 没有 token，进入到 登录界面
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToLogin();
                }
            }, 800);
        }
    }


    // 跳转到 主界面
    private void goToMain() {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }

    // 跳转到 登录界面
    private void goToLogin() {
        startActivity(new Intent(context, LoginActivity.class));
        finish();
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
}
