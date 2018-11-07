package com.shouxh.weatherMain;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shouxh.weatherMain.entities.Hourly;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * 获取天气数据的子线程
 *
 * @author shoux
 * */
public class getForecast extends Thread {
    static final int UPDATE_WEATHER = 0x101;
    private String CITY;
    private Handler handler;
    private  Context context;
    public static Message message;
    private final String KEY = "&key=620425af90534ee9afdea057b8fa62dd";
    private final String backupKEY = "&key=620425af90534ee9afdea057b8fa62dd";
    public getForecast(String requestCity, Handler handler, Context context) {
        this.context=context;
        this.handler=handler;
        CITY = requestCity;
        //debug使用昆山代替模拟器的国外城市，防止崩溃，正式发布请务必使用正确的构造赋值式
//        CITY="昆山";
    }

    @Override
    public void run() {
        initForecast(CITY);
        Intent intent = new Intent(context.getPackageName()+".refreshCompleted");
        context.sendBroadcast(intent);
    }

    private void initForecast(String city) {
        final String requestURL = "https://free-api.heweather" +
                ".com/s6/weather?location=";
        try {
            URL url = new URL(requestURL + city + KEY);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String tempStr;
            while ((tempStr = bufferedReader.readLine()) != null) {
                stringBuilder.append(tempStr);
            }
            String data = stringBuilder.toString();
            String status;
            String forecast ="";
            String today="";
            String lifestyle="";
            if(data.contains("status")&&!data.contains("invalid")){
                try {
                    status = data.substring(data.indexOf("status") + 9, data.indexOf("now")-3);
                    forecast = data.substring(data.indexOf("daily_forecast")+16,data.indexOf
                            ("lifestyle")-2);
                    today = data.substring(data.indexOf("now")+5,data.indexOf("daily_forecast")-2);
                } catch (StringIndexOutOfBoundsException e) {
                    status="404";
                }
                StringBuilder todayBuilder = new StringBuilder();
                todayBuilder.append(today);
                todayBuilder.insert(0,"[");
                todayBuilder.append("]");
                today=todayBuilder.toString();
                try {
                    lifestyle = data.substring(data.lastIndexOf("["), data.lastIndexOf("]") -1);
                } catch (StringIndexOutOfBoundsException e) {
                    status= "404";
                }
            }else {
                status = "404";
            }
            Bundle bundle;
            String[] forecastArray;
            String todayInfo;
            forecastArray = resolveJson(forecast,status);
            todayInfo = resolveTodayJson(today,status);
            String[] lifeStyleArray = resolveLifestyle(lifestyle,status);
            String[] finaLifeStyle = new String[4];
            if(lifeStyleArray!=null) {
                finaLifeStyle[0] = lifeStyleArray[0];
                finaLifeStyle[1] = lifeStyleArray[3];
                finaLifeStyle[2] = lifeStyleArray[5];
                finaLifeStyle[3] = lifeStyleArray[6];
            }
           String airQuality = getAirQuality(CITY);
            if(airQuality==null){
                airQuality="";
            }
            String[] weatherArray = initAndResolveTodayHourTimeJson(CITY);

            message = new Message();
            bundle = new Bundle();
            bundle.putStringArray("forecast", forecastArray);
            bundle.putString("today", todayInfo);
            bundle.putStringArray("lifestyle", finaLifeStyle);
            bundle.putString("air",airQuality);
            bundle.putStringArray("hourTimeWeather",weatherArray);
            message.setData(bundle);
            message.what = UPDATE_WEATHER;
            handler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getAirQuality(String city){
        String data;
        String status;
        String airQ ;
        String result = "";
        URL url;
        OkHttpClient client;
        Request request;
        Response response;
        final String requestURL = "https://free-api.heweather.com/s6/air/now?&location=";
        final String BackupRequestURL = "https://www.sojson.com/open/api/weather/json.shtml?city=";
        try {
            url = new URL(requestURL+city+backupKEY);
            client = new OkHttpClient();
            request = new Request.Builder().url(url).build();
            response = client.newCall(request).execute();
            data = response.body().string();
//            Log.i("TAG--find error", data);
            if(data.contains("status")&&!data.contains("denied")){
                try {
                    status = data.substring(data.indexOf("status") + 9, data.indexOf("air_now_city")
                            - 3);
                    airQ = data.substring(data.indexOf("air_now_city") + 14, data.indexOf("air_now_station") - 2);
                } catch (StringIndexOutOfBoundsException e) {
                    status = "404";
                    airQ="";
                }
                result = resolveAir(airQ, status);
            }else {
                String pm25;
                URL url1=new URL(BackupRequestURL+city);
                OkHttpClient client1=new OkHttpClient();
                Request request1=new Request.Builder().url(url1).build();
                response=client1.newCall(request1).execute();
                String data1=response.body().string();
                if(data1.contains("status")&&!data1.contains("频繁调用")&&!data1.contains("上限")){
                    Log.e("getForecast", "错误数据："+data1);
                    status=data1.substring(data1.indexOf("status")+8,data1.indexOf("city")-2);
                    airQ=data1.substring(data1.indexOf("forecast")+10,data1.lastIndexOf("]")+1);
                    if(data1.contains("pm")) {
                        pm25 = data1.substring(data1.indexOf("pm25") + 6, data1.indexOf("pm10") - 4);
                    }else {
                        pm25=resolveAir(airQ,status);
                    }
                    result = "aqi指数："+resolveAir(airQ,status)+"\npm2.5指数："+pm25;
                }else {
                    result="";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String[] resolveJson(String data,String status) {
        String[] threeDaysWeather = {"","","","","","",""};
        String[] dayOrder = {"今天", "明天", "后天","大后天","5天","6天","7天"};
        try {
            if (status.equals("ok")) {
                JSONArray forecastArray = new JSONArray(data);
                for (int i = 0, j = 0; i < forecastArray.length(); i++, j++) {
                    JSONObject jsonObject = forecastArray.getJSONObject(i);
                    String date = jsonObject.getString("date");//查询日期
                    String tmp_max = jsonObject.getString("tmp_max");//最高温度
                    String tmp_min = jsonObject.getString("tmp_min");//最低温度
                    String wind_dir = jsonObject.getString("wind_dir");//风向
                    String wind_sc = jsonObject.getString("wind_sc");//风力
                    String pressure = jsonObject.getString("pres");//大气压强
                    String weather = jsonObject.getString("cond_txt_d");//白天天气状况
                    String uv_index = jsonObject.getString("uv_index");//紫外线
                    String visible = jsonObject.getString("vis");//能见度
                    String temp = dayOrder[i] + "是：" + date + "\n最高温度：" + tmp_max + "\n最低温度：" + tmp_min
                            + "\n风向：" + wind_dir + "\n风力：" + wind_sc + "\n大气压：" + pressure
                            + "\n白天天气状况：" + weather + "\n紫外线强度：" + uv_index + "\n能见度：" + visible;
                    threeDaysWeather[j] = temp;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return threeDaysWeather;
    }

    private   String[] initAndResolveTodayHourTimeJson(String city)  {
        final String url = "https://free-api.heweather.com/s6/weather/hourly?location=" +city+
                "&key=620425af90534ee9afdea057b8fa62dd";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String data = null;
        try {
            Response response = client.newCall(request).execute();
            data = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String content;
        String hourly = null;
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            content = jsonArray.getJSONObject(0).toString();
            hourly = new JSONObject(content).getJSONArray("hourly").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        List<Hourly> hourlies =  gson.fromJson(hourly,new TypeToken<List<Hourly>>(){}.getType());
        String[] hourWeather = new String[8];
        if (hourlies!=null) {
            for (int i = 0; i < hourWeather.length; i++) {
                String time = hourlies.get(i).getTime().substring(11);
                hourWeather[i] = time + "-" + hourlies.get(i).getCondText() + "-" + hourlies.get(i).getTemper();
            }
        }
        return hourWeather;
    }

    private String resolveTodayJson(String data,String status){
        String result = "";
        try {
            if (status.equals("ok")) {
                JSONArray todayArray = new JSONArray(data);
                for (int j = 0; j < todayArray.length(); j++) {
                    JSONObject object = todayArray.getJSONObject(j);
                    String tmp = object.getString("tmp");//气温
                    String humidity = object.getString("hum");//湿度
                    String condition = object.getString("cond_txt");//天气状况
                    String bodyTmp = object.getString("fl");
                    result = "\n今天气温：" + tmp + "\n湿度：" + humidity + "\n天气状况：" +
                            condition+"\n体感温度："+bodyTmp;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String[] resolveLifestyle(String data,String status){
        String[] lifestyles = {"","","","","","","",""};
        try {
            if (status.equals("ok") && !data.isEmpty()) {
                JSONArray lifestyleArray = new JSONArray(data);
                for (int k = 0; k < lifestyleArray.length(); k++) {
                    JSONObject object = lifestyleArray.getJSONObject(k);
                    String info = object.getString("txt");
                    lifestyles[k] = info;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lifestyles;
    }

    private String resolveAir(String data,String status){
        String air = "0";
        if (status.equals("ok")) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String aqi = jsonObject.getString("aqi");
                String pm25 = jsonObject.getString("pm25");
                air="aqi指数："+aqi+"\npm2.5指数："+pm25;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (status.equals("200")){
            try {
                JSONArray jsonArray = new JSONArray(data);
                JSONObject object = jsonArray.getJSONObject(0);
                String aqi = object.getString("aqi");
                air=aqi;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return air;
    }

}
