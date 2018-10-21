package com.example.a10734.weatherdome;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.jaeger.library.StatusBarUtil;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocateState;
import com.zaaach.citypicker.model.LocatedCity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import config.AppConfig;
import entity.Citys;
import fragment.WeatherFragment;
import util.IPUtil;
import util.IsInternet;
import util.SharedPreferenceUtil;
import util.ToastUtil;
import util.WebUtil;
import weatheranim.DynamicWeatherView;

/**
 * **/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //侧滑菜单
    DrawerLayout drawerLayout;
    LinearLayout menu_layout;

    TextView usehelp_tv, about_tv, setting_tv, quit_tv;

    //标题栏城市Textview
    TextView top_city_tv;
    //分别为菜单，添加城市和删除城市按钮
    ImageView home, add_city_img, delete;
    //多城市天气信息页面切换ViewPager
    ViewPager weather_viewpager;

    String city_name = "北京";

    List<Fragment> fragments = null;

    //存放城市的List集合
    List<String> citys = new ArrayList<>();

    //Viewpager 适配器
    FragmentStatePagerAdapter fragmentStatePagerAdapter;

    //存放底部导航的小圆点
    ImageView[] imageViews;
    ViewGroup viewGroup;

    //动态背景
    DynamicWeatherView dynamicWeatherView;

    LocationClient locationClient;


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.usehelp_tv:
                    startActivity(new Intent(MainActivity.this, UseHelpActivity.class));
                    break;
                case R.id.about_tv:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
                case R.id.setting_tv:
                    startActivity(new Intent(MainActivity.this, SeetingActivity.class));
                    break;
                case R.id.quit_tv:
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        //弹出提示，可以有多种方式
                        Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                    } else {
                        finish();
                    }
                    ToastUtil.toastShow(MainActivity.this, "退出");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (!IsInternet.isNetworkAvalible(MainActivity.this)) {
            ToastUtil.toastShow(MainActivity.this, "当前无网络！");
        }
        StatusBarUtil.setTranslucentForDrawerLayout(this, drawerLayout, 0);
        initData();
        initIndexPoint();
    }

    /**
     * 初始化界面
     * 控件初始化
     **/
    public void initView() {
        usehelp_tv = findViewById(R.id.usehelp_tv);
        about_tv = findViewById(R.id.about_tv);
        setting_tv = findViewById(R.id.setting_tv);
        quit_tv = findViewById(R.id.quit_tv);
        usehelp_tv.setOnClickListener(onClickListener);
        about_tv.setOnClickListener(onClickListener);
        setting_tv.setOnClickListener(onClickListener);
        quit_tv.setOnClickListener(onClickListener);
        drawerLayout = findViewById(R.id.drawerlayout);
        menu_layout = findViewById(R.id.menu_layout);
        ViewGroup.LayoutParams params = menu_layout.getLayoutParams();
        params.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.618);
        menu_layout.setLayoutParams(params);
        top_city_tv = findViewById(R.id.top_city_tv);
        dynamicWeatherView = findViewById(R.id.dynamicWertherview);
        viewGroup = findViewById(R.id.index_point);
        weather_viewpager = findViewById(R.id.weather_viewpager);
        home = findViewById(R.id.home_img);
        //点击打开侧滑菜单
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        delete = findViewById(R.id.delete);
        //点击删除城市
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (citys.size() == 1) {
                    ToastUtil.toastShow(MainActivity.this, "你调皮了，这可是最后一个城市");
                    return;
                }
                Citys now = new Citys();
                citys.remove(weather_viewpager.getCurrentItem());
                now.setCitys(citys);
                SharedPreferenceUtil.save("citys", "citys", now);
                initData();
                initIndexPoint();
            }
        });
        add_city_img = findViewById(R.id.add_city_img);
        //点击添加城市
        add_city_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsInternet.isNetworkAvalible(MainActivity.this)) {
                    showCity();
                } else {
                    IsInternet.checkNetwork(MainActivity.this, "嗯哼?调皮了！没网络也想添加城市！");
                }
            }
        });
    }

    /**
     * 初始化数据
     **/
    public void initData() {
        //从SharedPreference拿到序列化后的城市List对象，如果存在，拿出对象，否则跳到选择测城市界面
        Object o = SharedPreferenceUtil.get("citys", "citys");
        if (o != null) {
            Citys basecity = (Citys) o;
            //如果basecity城市数量大于0，从basecity拿出城市，否则跳到选择测城市界面
            if (basecity.getCitys().size() > 0) {
                citys = basecity.getCitys();
                Log.i("initData", "citys = " + citys.toString());
            } else {
                showCity();
            }
        } else {
            showCity();
        }

        //初始化ViewPager页面
        fragments = new ArrayList<>();
        for (int i = 0; i < citys.size(); i++) {
            Fragment fragment = new WeatherFragment();
            //给每一个Fragment添加一个Bundle , 把城市带过去
            Bundle bundle = new Bundle();
            bundle.putString("city_name", citys.get(i));
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }
        fragmentStatePagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Log.i("TAG", "getItem: " + position);
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object obj) {
                return view == ((Fragment) obj).getView();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                Fragment fragment = (Fragment) object;
            }
        };
        weather_viewpager.setAdapter(fragmentStatePagerAdapter);
        //给viewpager设置滑动监听事件
        weather_viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int alpha = (int) (positionOffset * 255);
                if (alpha > 254 / 2) {
                    dynamicWeatherView.getBackground().setAlpha(254 - alpha);
                } else {
                    dynamicWeatherView.getBackground().setAlpha(alpha);
                }
                Log.i("onPageScrolled", alpha + "" + weather_viewpager.getCurrentItem());
            }

            @Override
            public void onPageSelected(int position) {
                //滑倒指定页，背景更换为相对应的背景
                ((WeatherFragment) fragments.get(weather_viewpager.getCurrentItem())).updataBackground();
                //滑倒指定页，标题栏城市更换为相对应的城市
                top_city_tv.setText(citys.get(position));
                //滑倒指定页，底部导航小圆点根据页面的滑动也跟着滑动
                for (int i = 0; i < fragments.size(); i++) {
                    if (position == i) {
                        if (i == 0) {
                            imageViews[i].setImageResource(R.drawable.now_true);
                        } else {
                            imageViews[i].setImageResource(R.drawable.point_true);
                        }
                    } else {
                        if (i == 0) {
                            imageViews[i].setImageResource(R.drawable.now_false);
                        } else {
                            imageViews[i].setImageResource(R.drawable.point_false);
                        }
                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //延时500ms 设置动态背景
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fragments.size() > 0) {
                    ((WeatherFragment) fragments.get(weather_viewpager.getCurrentItem())).updataBackground();
                }
            }
        }, 500);
        //设置标题栏的城市
        if (citys.size() > 0) {
            top_city_tv.setText(citys.get(weather_viewpager.getCurrentItem()));
        }
    }

    /**
     * 初始化底部导航小圆点
     **/
    public void initIndexPoint() {
        imageViews = new ImageView[fragments.size()];
        viewGroup.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
        params.setMargins(3, 0, 3, 0);
        for (int i = 0; i < imageViews.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            if (i == 0) {
                imageView.setImageResource(R.drawable.now_true);
            } else {
                imageView.setImageResource(R.drawable.point_false);
            }
            imageViews[i] = imageView;
            imageView.setOnClickListener(this);
            imageView.setTag(i);
            viewGroup.addView(imageViews[i]);
        }
    }

    /**
     * 显示城市
     **/
    public void showCity() {
        List<HotCity> hotCities = new ArrayList<>();
        hotCities.add(new HotCity("北京", "北京", "101010100"));
        hotCities.add(new HotCity("上海", "上海", "101020100"));
        hotCities.add(new HotCity("广州", "广东", "101280101"));
        hotCities.add(new HotCity("深圳", "广东", "101280601"));
        hotCities.add(new HotCity("杭州", "浙江", "101210101"));
        CityPicker.getInstance()
                .setFragmentManager(getSupportFragmentManager())    //此方法必须调用
                .enableAnimation(true)    //启用动画效果//自定义动画
                .setLocatedCity(null) //APP自身已定位的城市，默认为null（定位失败）
                .setHotCities(hotCities)//指定热门城市
                .setOnPickListener(new OnPickListener() {
                    @Override
                    public void onPick(int position, City data) {
                        if (data != null) {
                            Toast.makeText(getApplicationContext(), data.getName(), Toast.LENGTH_SHORT).show();
                            city_name = data.getName();
                            Object o = SharedPreferenceUtil.get("citys", "citys");
                            if (o != null) {
                                citys = ((Citys) o).getCitys();
                                int flag = 0;
                                int i;
                                for (i = 0; i < citys.size(); i++) {
                                    if (citys.get(i).equals(city_name)) {
                                        flag = 1;
                                        break;
                                    }
                                }
                                if (flag != 1) {
                                    citys.add(city_name);
//                                    initData();
                                    Fragment fragment = new WeatherFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("city_name", city_name);
                                    fragment.setArguments(bundle);
                                    fragments.add(fragment);
                                    fragmentStatePagerAdapter.notifyDataSetChanged();
                                    initIndexPoint();
//                                    ((WeatherFragment) fragments.get(weather_viewpager.getCurrentItem())).updataData(now.get(now.size()));
                                    weather_viewpager.setCurrentItem(i);
                                } else {
                                    weather_viewpager.setCurrentItem(i);
                                }
                                Citys nowcity = new Citys();
                                nowcity.setCitys(citys);
                                SharedPreferenceUtil.save("citys", "citys", nowcity);
                            } else {
                                List<String> now = new ArrayList<>();
                                now.add(city_name);
                                Citys nowcity = new Citys();
                                nowcity.setCitys(now);
                                SharedPreferenceUtil.save("citys", "citys", nowcity);
                                initData();
                                initIndexPoint();
                            }
                        }

                    }

                    @Override
                    public void onLocate() {
                        //开始定位
                        if(!IsInternet.isNetworkAvalible(MainActivity.this)){
                            ToastUtil.toastShow(MainActivity.this,"当前无网络");
                            return;
                        }
                        locationClient = new LocationClient(MainActivity.this);//创建一个LocationClient实例
                        LocationClientOption option = new LocationClientOption();//定位配置
                        option.setIsNeedAddress(true);//是否获得地址信息，true是
                        locationClient.setLocOption(option);
                        List<String> permissionList = new ArrayList<>();//权限集合
                        //回调监听，请求之后后回到这个方法
                        locationClient.registerLocationListener(new BDLocationListener() {
                            @Override
                            public void onReceiveLocation(BDLocation bdLocation) {
                                //BDLocation位置信息，可以获得经纬度，位置，城市等信息
                                Log.i("GPS", bdLocation.getLatitude() + " -- " + bdLocation.getLongitude());
                                if (bdLocation.getCity() != null) {
                                    //bdLocation.getCity()获得城市，bdLocation.getProvince()获得省份，bdLocation.getAdCode()获得才收到吗
                                    Log.i("GPS", "onReceiveLocation: "+bdLocation.getCity()+bdLocation.getProvince()+bdLocation.getAdCode());
                                            LocatedCity locatedCity = new LocatedCity(bdLocation.getCity(),bdLocation.getProvince(),bdLocation.getAdCode());
                                            CityPicker.getInstance()
                                                    .locateComplete(locatedCity, LocateState.SUCCESS);
                                        } else {
                                            CityPicker.getInstance()
                                                    .locateComplete(new LocatedCity("北京", "北京", "101010100"), LocateState.FAILURE);

                                        }

                            }
                        });
                        locationClient.start();//开始获取位置信息

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            permissionList.add(Manifest.permission.READ_PHONE_STATE);
                        }
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                        if (!permissionList.isEmpty()) {
                            String[] permisson = permissionList.toArray(new String[permissionList.size()]);
                            ActivityCompat.requestPermissions(MainActivity.this, permisson, 1);
                        } else {
                            locationClient.start();
                        }
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "你必须允许权限！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    locationClient.start();
                } else {
                    locationClient.start();
                }
        }
    }

    //底部导航监听事件，点击左边的小圆点页面向左滑动，点击右边的小圆点页面向右滑动，
    @Override
    public void onClick(View v) {
        int index = (int) v.getTag();
        if (index < (fragments.size()) / 2) {
            if (weather_viewpager.getCurrentItem() == 0) {
                weather_viewpager.setCurrentItem((weather_viewpager.getCurrentItem()));
            } else {
                weather_viewpager.setCurrentItem((weather_viewpager.getCurrentItem() - 1));
            }
        } else {
            if (weather_viewpager.getCurrentItem() == fragments.size()) {
                weather_viewpager.setCurrentItem((weather_viewpager.getCurrentItem()));
            } else {
                weather_viewpager.setCurrentItem((weather_viewpager.getCurrentItem() + 1));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // 用来计算返回键的点击间隔时间
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                //弹出提示，可以有多种方式
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


}
