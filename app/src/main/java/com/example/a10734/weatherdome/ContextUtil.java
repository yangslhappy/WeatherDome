package com.example.a10734.weatherdome;

import android.app.Application;

/**
 * Created by 10734 on 2018/6/2 0002.
 * 获取全局Context工具类
 */

public class ContextUtil extends Application  {

    public static ContextUtil instance;

    public static ContextUtil getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
