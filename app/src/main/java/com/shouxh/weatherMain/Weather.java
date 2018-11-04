package com.shouxh.weatherMain;

public class Weather {
    private String date;
    private String maxTemperature;
    private String minTemperature;
    private String cloudDirection;
    private String cloudForce;
    private String pressure;
    private String dailyWeather;
    private String UVPower;
    private String visibility;

    public Weather(String date, String maxTemperature, String minTemperature, String cloudDirection, String cloudForce, String pressure, String dailyWeather, String UVPower, String visibility) {
        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.cloudDirection = cloudDirection;
        this.cloudForce = cloudForce;
        this.pressure = pressure;
        this.dailyWeather = dailyWeather;
        this.UVPower = UVPower;
        this.visibility = visibility;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(String maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public String getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(String minTemperature) {
        this.minTemperature = minTemperature;
    }

    public String getCloudDirection() {
        return cloudDirection;
    }

    public void setCloudDirection(String cloudDirection) {
        this.cloudDirection = cloudDirection;
    }

    public String getCloudForce() {
        return cloudForce;
    }

    public void setCloudForce(String cloudForce) {
        this.cloudForce = cloudForce;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getDailyWeather() {
        return dailyWeather;
    }

    public void setDailyWeather(String dailyWeather) {
        this.dailyWeather = dailyWeather;
    }

    public String getUVPower() {
        return UVPower;
    }

    public void setUVPower(String UVPower) {
        this.UVPower = UVPower;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
