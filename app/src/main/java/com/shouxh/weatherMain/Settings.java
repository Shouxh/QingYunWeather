package com.shouxh.weatherMain;

public class Settings {
    private String tip;
    private String value;

    public Settings(String tip, String value) {
        this.tip = tip;
        this.value = value;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
