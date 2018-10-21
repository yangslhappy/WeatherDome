package entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 10734 on 2018/6/2 0002.
 * 城市集合
 */

public class Citys implements Serializable{

    public List<String> citys;

    public List<String> getCitys() {
        return citys;
    }

    public void setCitys(List<String> citys) {
        this.citys = citys;
    }
}
