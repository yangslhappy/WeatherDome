package adpter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a10734.weatherdome.R;

import java.util.List;

import entity.Future;

/**
 * Created by 10734 on 2018/6/2 0002.
 * 未来天气信息Listview适配器
 */

public class WeatherAdapter extends BaseAdapter{

    LayoutInflater layoutInflater;
    List<Future> futures;
    Context context;

    public WeatherAdapter(Context context, List<Future> futures) {
        this.layoutInflater = LayoutInflater.from(context);
        this.futures = futures;
        this.context = context;
    }

    @Override
    public int getCount() {
        return futures.size();
    }

    @Override
    public Object getItem(int position) {
        return futures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.weather_item,null);
            viewHolder.week = convertView.findViewById(R.id.item_week);
            viewHolder.s_temp = convertView.findViewById(R.id.item_s_temp);
            viewHolder.l_temp = convertView.findViewById(R.id.item_l_temp);
            viewHolder.image = convertView.findViewById(R.id.item_image);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Future future = futures.get(position);
        viewHolder.week.setText(future.getWeek());
        String str = future.getTemperature();
        String str1 = str.substring(str.indexOf("~"));
        viewHolder.s_temp.setText(str.substring(0,str.indexOf("℃")));
        viewHolder.l_temp.setText(str1.substring(1,str1.length()-1));
        int id = context.getResources().getIdentifier("w"+future.getWeather_id().getFa(),"drawable",context.getPackageName());
        Log.i("id", id+"");
        viewHolder.image.setImageResource(id);
        return convertView;
    }

    class ViewHolder{
        TextView week ;
        TextView s_temp ;
        TextView l_temp ;
        ImageView image;
    }
}
