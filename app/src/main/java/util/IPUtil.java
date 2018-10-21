package util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by 10734 on 2018/6/2 0002.
 * 获取当前IP工具类
 */

public class IPUtil {

    public static String getIP() {
        InputStream is = null;
        try {
            is = WebUtil.getByWeb("http://ip.chinaz.com/getip.aspx");
            String result = WebUtil.getStringByInputStream(is);
            if (!result.isEmpty()) {
                JSONObject jsonObject = new JSONObject(result);
                return jsonObject.getString("ip");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
