package seriver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import config.AppConfig;
import entity.Citys;
import entity.Future;
import entity.JsonRootBean;
import entity.Sk;
import entity.Today;
import entity.Weather_id;
import receiver.WeatherReceiver;
import util.IsInternet;
import util.SharedPreferenceUtil;
import util.WebUtil;

public class AutoUpdataSeriver extends Service {

    boolean flag = true;

    public AutoUpdataSeriver() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AutoUpdataSeriver", "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AutoUpdataSeriver", "onStartCommand: ");
        //更新数据
        updataData();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //可以修改时间
//        int Minutes = 90*60*1000;
        int Minutes =  6 * 60 * 60 * 1000;
//        int Minutes =  5 * 1000;
        //SystemClock.elapsedRealtime()表示1970年1月1日0点至今所经历的时间
        long triggerAtTime = SystemClock.elapsedRealtime() + Minutes;
        //此处设置开启AlarmReceiver这个Service
        Intent i = new Intent(this, AutoUpdataSeriver.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        Intent bi = new Intent(this, WeatherReceiver.class);
        flag = true;
        if(!IsInternet.isNetworkAvalible(AutoUpdataSeriver.this)){
            flag = false;
        }
        if(flag){
            bi.putExtra("msg","天气数据更新成功");
        }else {
            bi.putExtra("msg","天气数据更新失败");
        }
        PendingIntent bpi = PendingIntent.getBroadcast(this, 0, bi, 0);
        //ELAPSED_REALTIME_WAKEUP表示让定时任务的出发时间从系统开机算起，并且会唤醒CPU。
        manager.cancel(pi);
        manager.cancel(bpi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, bpi);
        return super.onStartCommand(intent, flags, startId);
    }

    public void updataData() {
        Log.i("AutoUpdataWeather", "更新天气数据");
        Citys citys = (Citys) SharedPreferenceUtil.get("citys", "citys");
        if (citys == null) {
            return;
        }
        final List<String> city_names = citys.getCitys();
        if (city_names.size() == 0) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String city : city_names) {
                    String url = null;
                    try {
                        url = AppConfig.QUERY + URLEncoder.encode(city, "utf-8") + "&format=2&key=" + AppConfig.KEY;
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
                            JsonRootBean bean = new JsonRootBean();
                            bean.setSk(sk);
                            bean.setToday(today);
                            bean.setFuture(futures);
                            SharedPreferenceUtil.save("city_info_sp", city, bean);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
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
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("AutoUpdataSeriver", "onDestroy: ");
        //在Service结束后关闭AlarmManager
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AutoUpdataSeriver.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        Intent bi = new Intent(this, WeatherReceiver.class);
        PendingIntent bpi = PendingIntent.getBroadcast(this, 0, bi, 0);
        manager.cancel(pi);
        manager.cancel(bpi);
    }
}
