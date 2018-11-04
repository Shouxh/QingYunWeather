package com.shouxh.weatherMain;

import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class getProvince extends Thread {

    private String[] provinces =new String[34];
    private int[] provincesID =new int[34];
    private ArrayList<String> citiesList = new ArrayList<>();
    private boolean isOnlyCheckOnce =true;
    private ProvinceHelper provinceHelper;
    public getProvince(Context context) {
        provinceHelper = new ProvinceHelper(context);
    }

    @Override
    public void run() {
        if(!provinceHelper.isEmpty()){
            return;
        }
        getProvinceData();
    }

    private void getProvinceData() {
        final String requestURL = "https://api.it120.cc/common/region/province";
            try {
                URL url = new URL(requestURL);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                resolveJsonData(responseData);
            }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCitiesData(){
        final String requestURL = "https://api.it120.cc/common/region/child?pid=";
        for(int i=0;i<provincesID.length-1;i++){
            try {
                URL url = new URL(requestURL+provincesID[i]);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                if(i!=31) {
                    resolveCitiesJsonData(responseData, i);
                } else {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void resolveJsonData(String data){
        String code = data.substring(data.indexOf("code")+6,data.indexOf("\"data")-1);
        if(Integer.valueOf(code)==0){
            String content = data.substring(data.indexOf("["),data.lastIndexOf("]")+1);
            try {
                JSONArray jsonArray = new JSONArray(content);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String provinceName = jsonObject.getString("name");
                    int provinceID = jsonObject.getInt("id");
                    provinces[i]=provinceName;
                    provincesID[i]=provinceID;
                }
               getCitiesData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void resolveCitiesJsonData(String data,int order){
        String code;
        String content= data.substring(data.indexOf("["),data.lastIndexOf("]")+1);
        if(isOnlyCheckOnce) {
            citiesList.clear();
            code = data.substring(data.indexOf("code") + 6, data.indexOf("\"data") - 1);
            if(Integer.valueOf(code)==0) {
                isOnlyCheckOnce = false;
            }
        }else {
            code="0";
        }
        if(Integer.valueOf(code)==0){
            try {
                JSONArray jsonArray = new JSONArray(content);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String city = jsonObject.getString("name");
                    if(!city.equals("自治区直辖县级行政区划")) {
                            if (!city.contains("自治州")&&!city.contains("直辖县")) {
                                citiesList.add(city + "      " + provinces[order]);
                            }
                    }else {
                        citiesList.add("香港      "+provinces[order+2]);
                        citiesList.add("澳门      "+provinces[order+3]);
                        if(provinceHelper.isEmpty()) {
                           provinceHelper.initData(citiesList);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
