package com.example.rexchen.myim;

import android.app.Application;

import com.example.rexchen.myim.utils.SharedPreferencesContext;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imkit.RongIM;

/**
 * Created by Administrator on 2018/3/14.
 */

public class App extends Application {

    private static DisplayImageOptions options;

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化融云
        RongIM.init(this);
        SealAppContext.init(this);
        SharedPreferencesContext.init(this);
    }

    public static DisplayImageOptions getOptions() {
        return options;
    }
}
