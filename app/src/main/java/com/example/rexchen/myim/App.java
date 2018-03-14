package com.example.rexchen.myim;

import android.app.Application;

import io.rong.imkit.RongIM;

/**
 * Created by Administrator on 2018/3/14.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化融云
        RongIM.init(this);
    }
}
