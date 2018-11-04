package com.shouxh.weatherMain.entities;

import java.io.Serializable;

public class HourWeather implements Serializable {
    private String hourTime;
    private int hourWeatherImage;
    private String hourTemper;

    public HourWeather(String hourTime, int hourWeatherImage, String hourTemper) {
        this.hourTime = hourTime;
        this.hourWeatherImage = hourWeatherImage;
        this.hourTemper = hourTemper;
    }

    public String getHourTime() {
        return hourTime;
    }

    public void setHourTime(String hourTime) {
        this.hourTime = hourTime;
    }

    public int getHourWeatherImage() {
        return hourWeatherImage;
    }

    public void setHourWeatherImage(int hourWeatherImage) {
        this.hourWeatherImage = hourWeatherImage;
    }

    public String getHourTemper() {
        return hourTemper;
    }

    public void setHourTemper(String hourTemper) {
        this.hourTemper = hourTemper;
    }
}
