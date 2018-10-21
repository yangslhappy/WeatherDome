package receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.example.a10734.weatherdome.MainActivity;
import com.example.a10734.weatherdome.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import seriver.AutoUpdataSeriver;

/**
 * Created by 10734 on 2018/6/6 0006.
 */

public class WeatherReceiver extends BroadcastReceiver {
    String msg = null;
    SimpleDateFormat format = new SimpleDateFormat("MM月dd日 hh:mm:ss");
    @Override
    public void onReceive(Context context, Intent intent) {
        //设置通知内容并在onReceive()这个函数执行时开启
        msg = intent.getStringExtra("msg");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                /**设置通知左边的大图标**/
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.weather))
                /**设置通知右边的小图标**/
                .setSmallIcon(R.mipmap.weather)
                /**通知首次出现在通知栏，带上升动画效果的**/
                .setTicker("通知来了")
                /**设置通知的标题**/
                .setContentTitle(msg)
                /**设置通知的内容**/
                .setContentText("更新时间:"+ format.format(new Date(System.currentTimeMillis())))
                /**通知产生的时间，会在通知信息里显示**/
                .setWhen(System.currentTimeMillis())
                /**设置该通知优先级**/
                .setPriority(Notification.PRIORITY_DEFAULT)
                /**设置这个标志当用户单击面板就可以让通知将自动取消**/
                .setAutoCancel(true)
                /**设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)**/
                .setOngoing(false)
                /**向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：**/
//                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentIntent(PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
                .build();
        manager.notify(1, notification);
        //再次开启LongRunningService这个服务，从而可以
//        Intent i = new Intent(context, AutoUpdataSeriver.class);
//        context.startService(i);
    }

}
