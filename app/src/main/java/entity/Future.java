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
public class Future  implements Serializable {

    private String temperature;
    private String weather;
    private Weather_id weather_id;
    private String wind;
    private String week;
    private String date;
    public void setTemperature(String temperature) {
         this.temperature = temperature;
     }
     public String getTemperature() {
         return temperature;
     }

    public void setWeather(String weather) {
         this.weather = weather;
     }
     public String getWeather() {
         return weather;
     }

    public void setWeather_id(Weather_id weather_id) {
         this.weather_id = weather_id;
     }
     public Weather_id getWeather_id() {
         return weather_id;
     }

    public void setWind(String wind) {
         this.wind = wind;
     }
     public String getWind() {
         return wind;
     }

    public void setWeek(String week) {
         this.week = week;
     }
     public String getWeek() {
         return week;
     }

    public void setDate(String date) {
         this.date = date;
     }
     public String getDate() {
         return date;
     }

}