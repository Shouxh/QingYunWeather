package com.shouxh.weatherMain.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Hourly implements Serializable {
    @SerializedName("cloud")
    private String cloud;

    @SerializedName("cond_code")
    private String condCode;

    @SerializedName("cond_txt")
    private String  condText;

    @SerializedName("hum")
    private String hum;

    @SerializedName("pop")
    private String Rainpop;

    @SerializedName("pres")
    private String pressure;

    @SerializedName("time")
    private String time;

    @SerializedName("tmp")
    private String temper;

    @SerializedName("wind_deg")
    private String windDegree;

    @SerializedName("wind_dir")
    private String windDirection;

    @SerializedName("wind_sc")
    private String windSc;

    @SerializedName("wind_spd")
    private String windSpd;


    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getCondCode() {
        return condCode;
    }

    public void setCondCode(String condCode) {
        this.condCode = condCode;
    }

    public String getCondText() {
        return condText;
    }

    public void setCondText(String condText) {
        this.condText = condText;
    }

    public String getHum() {
        return hum;
    }

    public void setHum(String hum) {
        this.hum = hum;
    }

    public String getRainpop() {
        return Rainpop;
    }

    public void setRainpop(String rainpop) {
        Rainpop = rainpop;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemper() {
        return temper;
    }

    public void setTemper(String temper) {
        this.temper = temper;
    }

    public String getWindDegree() {
        return windDegree;
    }

    public void setWindDegree(String windDegree) {
        this.windDegree = windDegree;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindSc() {
        return windSc;
    }

    public void setWindSc(String windSc) {
        this.windSc = windSc;
    }

    public String getWindSpd() {
        return windSpd;
    }

    public void setWindSpd(String windSpd) {
        this.windSpd = windSpd;
    }
}
