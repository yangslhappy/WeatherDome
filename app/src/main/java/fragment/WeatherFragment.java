package fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.a10734.weatherdome.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import adpter.WeatherAdapter;
import config.AppConfig;
import entity.Future;
import entity.JsonRootBean;
import entity.Sk;
import entity.Today;
import entity.Weather_id;
import util.DPutil;
import util.DialogUtil;
import util.SharedPreferenceUtil;
import util.ToastUtil;
import util.WebUtil;
import view.MyScrollView;
import weatheranim.BaseType;
import weatheranim.DynamicWeatherView;
import weatheranim.RainTypeImpl;
import weatheranim.TaiYangTypeImpl;
import weatheranim.XueTypeImpl;

/**
 * Created by 10734 on 2018/6/1 0001.
 * 天气界面Fragment
 */

public class WeatherFragment extends BaseFragment {

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    String city_name;

    Dialog mdialog;

    MyScrollView scrollView;
    TextView top_city_tv;
    TextView city_tv, weather_state_tv, temp_tv, week_tv, s_temp, l_temp, proposal_tv;
    TextView date_tv, humi_tv, wind_tv, wind_strength_tv, uv_tv, comfort_index_tv, wash_index_tv, travel_index_tv;
    ListView listView;
    SwipeRefreshLayout swipeRefresh;

    LineChart weartherChart;

    JsonRootBean bean;

    DynamicWeatherView dynamicWeatherView;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                Object o = SharedPreferenceUtil.get("city_info_sp", city_name);
                if (o != null) {
                    bean = (JsonRootBean) o;
                    updataView();
                }
                ToastUtil.toastShow(getContext(), "网络错误");
            } else {
                updataView();
                if(swipeRefresh.isRefreshing()){
                    ToastUtil.toastShow(getContext(), "更新成功");
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!swipeRefresh.isRefreshing()){
                        DialogUtil.closeDialog(mdialog);
                    }
                    swipeRefresh.setRefreshing(false);
                }
            },0);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //从Acitivity拿到传来的Bundle
        Bundle bundle = getArguments();
        Log.d("onCreateView", bundle.getString("city_name"));
        if (bundle != null) {
            city_name = bundle.getString("city_name");
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.weather_fragment;
    }

    /**
     * 初始化界面，控件的初始化
     * */
    @Override
    public void initView() {
        top_city_tv = getActivity().findViewById(R.id.top_city_tv);
        swipeRefresh = view.findViewById(R.id.swiperefresh);
        scrollView = view.findViewById(R.id.scrollView);
        city_tv = view.findViewById(R.id.city_tv);
        weather_state_tv = view.findViewById(R.id.weather_state_tv);
        temp_tv = view.findViewById(R.id.temp_tv);
        week_tv = view.findViewById(R.id.week);
        s_temp = view.findViewById(R.id.s_temp);
        l_temp = view.findViewById(R.id.l_temp);
        proposal_tv = view.findViewById(R.id.proposal_tv);
        date_tv = view.findViewById(R.id.date_tv);
        humi_tv = view.findViewById(R.id.humi_tv);
        wind_tv = view.findViewById(R.id.wind_tv);
        wind_strength_tv = view.findViewById(R.id.wind_strength_tv);
        uv_tv = view.findViewById(R.id.uv_tv);
        comfort_index_tv = view.findViewById(R.id.comfort_index_tv);
        wash_index_tv = view.findViewById(R.id.wash_index_tv);
        travel_index_tv = view.findViewById(R.id.travel_index_tv);
        listView = view.findViewById(R.id.weather_lv);
        weartherChart = view.findViewById(R.id.weather_chart);
        //scrollview设置监听事件，根据scrollview改变标题栏城市TextView的透明度
        scrollView.setOnScrollListener(new MyScrollView.OnScrollListener() {
            @Override
            public void onScroll(int scrollY) {
                int dp = DPutil.dip2px(getContext(), scrollY);
                Log.i("ScrollView", dp + "");
                if (dp < 200) {
                    top_city_tv.setAlpha(0);
                }else if (dp > 200 && dp < 255*6) {
                    float alpha = (float)dp/255/6;
                    Log.i("ScrollView", alpha + "");
                    top_city_tv.setAlpha(alpha);
                }if(dp > 255*6){
                    top_city_tv.setAlpha(1);
                }
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updataData(city_name);
                Log.i("TAG", "onRefresh: ");
            }
        });
    }

    /**
     * 初始化数据
     * */
    @Override
    public void initData() {
        city_tv.setText(city_name);
        updataData(city_name);
    }

    /**
     * 更新数据，根据城市名请求天气信息
     * */
    public void updataData(final String city) {
        if(!swipeRefresh.isRefreshing()){
            mdialog = DialogUtil.createLoadingDialog(getContext(),"加载中...");
        }
        city_name = city;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = null;
                try {
                    url = AppConfig.QUERY + URLEncoder.encode(city_name, "utf-8") + "&format=2&key=" + AppConfig.KEY;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.i("updataData", url);
                InputStream is = null;
                try {
                    is = WebUtil.getByWeb(url);
                    String json = WebUtil.getStringByInputStream(is);
                    JSONObject jsonObject = new JSONObject(json);
                    Log.i("updataData", jsonObject.toString());
                    if (!jsonObject.getString("resultcode").equals("200")) {
                        Message message = new Message();
                        message.what = ERROR;
                        handler.sendMessage(message);
                    } else {
                        JSONObject result = jsonObject.getJSONObject("result");

                        //获得实时天气数据
                        JSONObject sk_json = result.getJSONObject("sk");
                        Sk sk = new Sk();
                        //温度
                        sk.setTemp(sk_json.getString("temp"));
                        //风向
                        sk.setWind_direction(sk_json.getString("wind_direction"));
                        //等级
                        sk.setWind_strength(sk_json.getString("wind_strength"));
                        //湿度
                        sk.setHumidity(sk_json.getString("humidity"));
                        //时间
                        sk.setTime(sk_json.getString("time"));

                        //获得当天天气数据
                        JSONObject today_json = result.getJSONObject("today");
                        Today today = new Today();
                        today.setTemperature(today_json.getString("temperature"));
                        today.setWeather(today_json.getString("weather"));
                        JSONObject today_weather_id_json = today_json.getJSONObject("weather_id");
                        Weather_id today_weather_id = new Weather_id();
                        today_weather_id.setFa(today_weather_id_json.getString("fa"));
                        today_weather_id.setFb(today_weather_id_json.getString("fb"));
                        today.setWeather_id(today_weather_id);
                        today.setWind(today_json.getString("wind"));
                        today.setWeek(today_json.getString("week"));
                        today.setCity(today_json.getString("city"));
                        today.setDate_y(today_json.getString("date_y"));
                        today.setDressing_index(today_json.getString("dressing_index"));
                        today.setDressing_advice(today_json.getString("dressing_advice"));
                        today.setUv_index(today_json.getString("uv_index"));
                        today.setComfort_index(today_json.getString("comfort_index"));
                        today.setWash_index(today_json.getString("wash_index"));
                        today.setTravel_index(today_json.getString("travel_index"));
                        today.setExercise_index(today_json.getString("exercise_index"));
                        today.setDrying_index(today_json.getString("drying_index"));

                        JSONArray future_json = result.getJSONArray("future");
                        List<Future> futures = new ArrayList<>();
                        for (int i = 0; i < future_json.length(); i++) {
                            JSONObject object = future_json.getJSONObject(i);
                            Future future = new Future();
                            future.setTemperature(object.getString("temperature"));
                            future.setWeather(object.getString("weather"));
                            JSONObject weather_id_json = object.getJSONObject("weather_id");
                            Weather_id weather_id = new Weather_id();
                            weather_id.setFa(weather_id_json.getString("fa"));
                            weather_id.setFb(weather_id_json.getString("fb"));
                            future.setWeather_id(weather_id);
                            future.setWind(object.getString("wind"));
                            future.setWeek(object.getString("week"));
                            future.setDate(object.getString("date"));
                            futures.add(future);
                        }
                        bean = new JsonRootBean();
                        bean.setSk(sk);
                        bean.setToday(today);
                        bean.setFuture(futures);
                        // SharedPreferenceUtil.save("SharedPreference文件名", key, value);
                        SharedPreferenceUtil.save("city_info_sp", city_name, bean);

                        Message message = new Message();
                        message.what = SUCCESS;
                        handler.sendMessage(message);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = ERROR;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = ERROR;
                    handler.sendMessage(message);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 更新视图，把获取到的天气信息更新到界面上
     * */
    public void updataView() {
        Log.i("TAG", city_name + "更新数据");
        String str = bean.getToday().getTemperature();
        String str1 = str.substring(str.indexOf("~"));
        city_tv.setText(bean.getToday().getCity());
        weather_state_tv.setText(bean.getToday().getWeather());
        temp_tv.setText(bean.getSk().getTemp() + "℃");
//        proposal_tv.setText(bean.getToday().getDressing_advice());
        proposal_tv.setText("今天：" + bean.getToday().getDate_y() + ",当前天气" + bean.getToday().getWeather() + ",气温" + bean.getSk().getTemp() +
                "℃，预计最高气温" + str1.substring(1, str1.length() - 1) + "。\n\n" + bean.getToday().getDressing_advice());
        week_tv.setText(bean.getToday().getWeek());
        s_temp.setText(str.substring(0, str.indexOf("℃")));
        l_temp.setText(str1.substring(1, str1.length() - 1));
        listView.setAdapter(new WeatherAdapter(getActivity(), bean.getFuture()));
        date_tv.setText(bean.getSk().getTime());
        humi_tv.setText(bean.getSk().getHumidity());
        wind_tv.setText(bean.getSk().getWind_direction());
        wind_strength_tv.setText(bean.getSk().getWind_strength());
        uv_tv.setText(bean.getToday().getUv_index());
        comfort_index_tv.setText(bean.getToday().getDressing_index());
        wash_index_tv.setText(bean.getToday().getWash_index());
        travel_index_tv.setText(bean.getToday().getTravel_index());
        updataChart();
    }

    /**
     * 更新图表，把未来天气信息，用图表绘制出来
     * */
    public void updataChart() {
        int MaxValue = 0;//记录未来天气中最高气温
        int MinValue = 0;//记录未来天气中最低气温
        weartherChart.getLegend().setEnabled(false);//设置图例不显示
        weartherChart.setDrawGridBackground(false);//设置网格背景不显示
        weartherChart.setDrawBorders(false);//设置边框不显示
        weartherChart.setTouchEnabled(false);//设置图表禁止滑动
        XAxis xAxis = weartherChart.getXAxis();//拿到图表X轴
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴在底部
        xAxis.setDrawGridLines(false);//设置X轴不绘制背景网格
        xAxis.setDrawAxisLine(false);//设置X轴不绘制线条
        xAxis.setAxisLineColor(Color.WHITE);//设置X轴的颜色为白色
        xAxis.setTextColor(Color.WHITE);//设置X轴上的文字颜色为白色
        xAxis.setTextSize(14);//设置X轴上的文字大小
        weartherChart.getAxisRight().setEnabled(false);//设置右边的Y轴不显示
        YAxis yAxis = weartherChart.getAxisLeft();//设置左边的Y轴
        yAxis.setDrawLabels(false);//设置Y轴的标签不显示
        yAxis.setDrawAxisLine(false);//设置Y轴不绘制背景网格
        yAxis.setDrawGridLines(false);//设置Y轴不绘制线条
        weartherChart.setDescription(" ");
//        weartherChart.setDescriptionPosition(1210, 25);
        weartherChart.setData(new LineData());//给图表添加数据集 LineData
        weartherChart.animateY(1000);//设置图表绘制动画，沿Y轴从下往上绘制，持续时间1000ms
        LineData lineData = weartherChart.getLineData();//拿到图表数据集 lineData
        for (Future future : bean.getFuture()) {
            lineData.addXValue(future.getWeek());//拿到未来天气信息，给X轴添加值为 星期几
        }
        //最低气温数据集合
        List<Entry> minEntries = new ArrayList<>();
        //最高气温数据集合
        List<Entry> maxEntries = new ArrayList<>();
        for (int i = 0; i < bean.getFuture().size(); i++) {
            Future future = bean.getFuture().get(i);
            String str = future.getTemperature();
            String str1 = str.substring(str.indexOf("~"));
            int min = Integer.parseInt(str.substring(0, str.indexOf("℃")));
            int max = Integer.parseInt(str1.substring(1, str1.length() - 1));
            if (i == 0) {
                MaxValue = max;
                MinValue = min;
            } else {
                if (max > MinValue) {
                    MaxValue = max;
                }
                if (min < MinValue) {
                    MinValue = min;
                }
            }
            Entry minEntry = new Entry(min, i);
            Entry maxEntry = new Entry(max, i);
            minEntries.add(minEntry);
            maxEntries.add(maxEntry);
        }

        Log.i("Chart", MaxValue + "-------" + MinValue);
        yAxis.setStartAtZero(false);//设置Y轴不从0开始
        yAxis.setAxisMaxValue(MaxValue + 6);
        yAxis.resetAxisMaxValue();
        yAxis.setAxisMinValue(MinValue - 2);

        //自定义数据格式
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) v);
            }
        };

        LineDataSet minLineDataSet = new LineDataSet(minEntries, "min");//new 一条折线
        minLineDataSet.setColor(Color.GRAY);
        minLineDataSet.setValueTextColor(Color.GRAY);
        minLineDataSet.setValueFormatter(formatter);
        minLineDataSet.setDrawCircleHole(true);//绘制圆点
        minLineDataSet.setCircleColorHole(Color.GRAY);
        minLineDataSet.setCircleColor(Color.GRAY);
        minLineDataSet.setDrawCubic(true);//设置曲线
        lineData.addDataSet(minLineDataSet);//把折线minLineDataSet添加到LineData中

        LineDataSet maxLineDataSet = new LineDataSet(maxEntries, "max");
        maxLineDataSet.setColor(Color.WHITE);
        maxLineDataSet.setValueTextColor(Color.WHITE);
        maxLineDataSet.setValueFormatter(formatter);
        maxLineDataSet.setDrawCircleHole(true);
        maxLineDataSet.setCircleColorHole(Color.WHITE);
        maxLineDataSet.setCircleColor(Color.WHITE);
        maxLineDataSet.setDrawCubic(true);
        lineData.addDataSet(maxLineDataSet);

        weartherChart.notifyDataSetChanged();
        weartherChart.invalidate();
    }

    /**
     *  更新背景，根据天气状况，动态变更天气背景
     * */
    public void updataBackground() {
        BaseType rainType;
        dynamicWeatherView = getActivity().findViewById(R.id.dynamicWertherview);
        if (bean != null) {
            String info = bean.getToday().getWeather();
            Log.i("----------------------", city_name + "设置背景" + info);
            if (info.contains("阳") || info.contains("晴")) {
                rainType = new TaiYangTypeImpl(getContext(), dynamicWeatherView);
            } else if (info.contains("雨")) {
                rainType = new RainTypeImpl(getContext(), dynamicWeatherView);
            } else if (info.contains("雪")) {
                rainType = new XueTypeImpl(getContext(), dynamicWeatherView);
            } else {
                rainType = new TaiYangTypeImpl(getContext(), dynamicWeatherView);
            }
            dynamicWeatherView.setType(rainType);
        } else {
            rainType = new TaiYangTypeImpl(getContext(), dynamicWeatherView);
            dynamicWeatherView.setType(rainType);
        }
        weartherChart.animateY(1000);
    }

}
