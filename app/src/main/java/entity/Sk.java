/**
  * Copyright 2018 bejson.com 
  */
package entity;

import java.io.Serializable;

/**
 * Auto-generated: 2018-05-18 15:59:14
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Sk implements Serializable{

    private String temp;
    private String wind_direction;
    private String wind_strength;
    private String humidity;
    private String time;
    public void setTemp(String temp) {
         this.temp = temp;
     }
     public String getTemp() {
         return temp;
     }

    public void setWind_direction(String wind_direction) {
         this.wind_direction = wind_direction;
     }
     public String getWind_direction() {
         return wind_direction;
     }

    public void setWind_strength(String wind_strength) {
         this.wind_strength = wind_strength;
     }
     public String getWind_strength() {
         return wind_strength;
     }

    public void setHumidity(String humidity) {
         this.humidity = humidity;
     }
     public String getHumidity() {
         return humidity;
     }

    public void setTime(String time) {
         this.time = time;
     }
     public String getTime() {
         return time;
     }

}