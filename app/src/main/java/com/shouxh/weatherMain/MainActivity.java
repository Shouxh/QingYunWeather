package com.shouxh.weatherMain;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.shouxh.weatherMain.entities.HourWeather;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <h2>软件描述：</h2>
 * <p>
 *     <h3 style="color:red">晴云天气是一款专业的天气预报软件，能够准确的预报当日天气和未来7天的天气情况</h3>
 *     <p>目前主要实现的功能有：</p>
 *     <ul>
 *         <li>今日天气显示</li>
 *         <li>未来24小时天气显示</li>
 *         <li>未来7天天气预估显示</li>
 *         <li>空气质量查看</li>
 *         <li>其它天气参数查看</li>
 *         <li>今日的生活建议</li>
 *         <li>离线数据保存，没网也能看上一次查询的天气</li>
 *     </ul>
 * </p>
 *
 *
 *  <p style="color:yellow">初代版本编写时间：2018-5-1</p>
 *  <p style="color:orange">上一次版本编写时间：2018-11-3</p>
 *
 * @author shoux
 *

 *
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    public AMapLocationClient aMapLocationClient;
    public AMapLocationListener aMapLocationListener;
    public AMapLocationClientOption mapLocationClientOption=null;
    private String resultStr;
    private MyHandler myHandler;
    private Toolbar toolbar;
    private TextView temperature,weatherStatus;
    private ImageView weatherImage;
    private String tempF;
    private String HumdF;
    private boolean isShakeChecked;
    private String[] fore;
    private String today,airQ;
    private String[] life,hourlyWeatherArray;
    private boolean isStart =true;
    private String resultDataT,resultDataH;
    private List<Weather> weatherList = new ArrayList<>();
    private ServiceConnection serviceConnection;
    private UpdateService.UpdateBinder updateBinder;
    private Intent RService;
    private LinearLayout forecastLayout, weatherTips;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView hourWeatherRecyclerView;
    private refreshReceiver refreshReceiver;
    private DatabaseChangedReceiver databaseChangedReceiver;
    private long mExitTime;
    private LinearLayout aqiLayout;
    private SearchView searchView;
    private ListView searchList;
    private static long currentTime ;
    private List<String> cityList = new ArrayList<>();
    private FilterAdapter filterAdapter;
    private static String[] offlineForecast;
    private static String offlineToday;
    private static String[] offlineLifestyle;
    private static String offlineAir;
    private static String offlineCity;
    private static String[] offlineHourlyWeather;
    private static String[] forecastNames = {"today","tomo","aftertomo","four","five","six","seven"};
    private static String[] lifestyleNames = {"comf","sport","uvp","carw"};
    private static String[] hourlyWeatherTimeNames = {"first3","second3","third3","fourth3","fifth3","sixth3","seventh3","eighth3"};
    private static AppCompatActivity mActivity;
    private SensorManager sensorManager;
    private Vibrator vibrator;
    private  Sensor sensor;
    private SensorEventListener sensorEventListener;
    private static float lastPositionX;
    private static float lastPositionY;
    private static float LastPositionZ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            setBarTransparent();
        }
        setContentView(R.layout.activity_main);
        mActivity=MainActivity.this;
        myHandler = new MyHandler(MainActivity.this);
        swipeRefreshLayout=findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        initProvince();
        serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                updateBinder=(UpdateService.UpdateBinder)service;
                updateBinder.setMainActivity(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        //初始化recyclerView
        hourWeatherRecyclerView = findViewById(R.id.hour_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hourWeatherRecyclerView.setLayoutManager(linearLayoutManager);

        refreshReceiver = new refreshReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName()+".refreshCompleted");
        registerReceiver(refreshReceiver,filter);

        databaseChangedReceiver = new DatabaseChangedReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter.addAction(getPackageName()+".database.ok");
        registerReceiver(databaseChangedReceiver,filter1);

        forecastLayout=findViewById(R.id.forecastLayout);
        weatherTips =findViewById(R.id.weather_tips);
        ImageView locationImg = findViewById(R.id.locationImg);
        locationImg.setOnClickListener(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setSubtitle("正在更新");
        aqiLayout=findViewById(R.id.aqi_exp);
        searchView=findViewById(R.id.queryCity);
        drawerLayout=findViewById(R.id.drawer);
        temperature=findViewById(R.id.temperature);
        weatherStatus=findViewById(R.id.weatherStatus);
        weatherImage=findViewById(R.id.weatherImage);
        searchList=findViewById(R.id.cityList);
        initRandomWallpaper();
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService
                (CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null) {
            if (networkInfo.isConnected() || networkInfo.isAvailable()) {
                initLocationService();
            }
        }else {
            initOfflineData();
        }
        setSupportActionBar(toolbar);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
           requestNeedPermissions();
        }
        initData();
        setListener();
        SharedPreferences sharedPreferences = getSharedPreferences("Setting",MODE_PRIVATE);
        tempF = sharedPreferences.getString("temperatureFormat",SettingsActivity
                .choose[0][0]);
        HumdF = sharedPreferences.getString("humidityFormat",SettingsActivity.choose[1][0]);
        isShakeChecked =sharedPreferences.getBoolean("isShakeChecked",false);
        initService();
        if (isShakeChecked) {
            setShackedChangeCity();
        }else {
            if(sensorManager!=null) {
                sensorManager.unregisterListener(sensorEventListener, sensor);
                sensor=null;
                sensorManager=null;
                sensorEventListener=null;
                vibrator=null;
            }
        }
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                searchView.setIconifiedByDefault(true);
                searchView.setSubmitButtonEnabled(false);
                searchView.onActionViewExpanded();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initProvince();
                initRandomWallpaper();
                refreshCityInfo();
            }
        });
    }



    public void initLocationService(){
        swipeRefreshLayout.setRefreshing(true);
        aMapLocationClient=new AMapLocationClient(getApplicationContext());
        aMapLocationListener=new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if(aMapLocation!=null){
                    if(aMapLocation.getErrorCode()==0){
                        resultStr = aMapLocation.getCity();
                        getForecast getForecast = new getForecast(resultStr,myHandler,
                                MainActivity.this);
                        getForecast.start();
                        offlineCity=resultStr;
                        toolbar.setTitle(resultStr);
                        toolbar.setSubtitle(R.string.finished);
                    }else {
                        Toast.makeText(MainActivity.this,"定位出错："+aMapLocation.getErrorInfo(),Toast.LENGTH_LONG).show();
                        Log.e(TAG, "定位出错！错误代码： "+aMapLocation.getErrorCode()+"\n错误信息: " +
                                ""+aMapLocation.getErrorInfo());
                    }
                }
            }
        };
        aMapLocationClient.setLocationListener(aMapLocationListener);
        mapLocationClientOption=new AMapLocationClientOption();
        mapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode
                .Hight_Accuracy);
        mapLocationClientOption.setOnceLocation(true);
        mapLocationClientOption.setOnceLocationLatest(false);
        mapLocationClientOption.setHttpTimeOut(10000);
        if(null!=aMapLocationClient){
            aMapLocationClient.setLocationOption(mapLocationClientOption);
            aMapLocationClient.stopLocation();
            aMapLocationClient.startLocation();
        }

    }

    private void initProvince(){
        getProvince getProvince = new getProvince(this);
        getProvince.start();
    }

    private void initData(){
        ProvinceHelper helper = new ProvinceHelper(this);
        do {
            cityList = helper.getAllInformation();
        }while (cityList.size()<300);
        filterAdapter=new FilterAdapter(cityList, this, new FilterListener() {
            @Override
            public void getFilterData(List<String> list) {
                setItemClick(list);
            }
        });
        searchList.setAdapter(filterAdapter);
    }

    private void setListener(){
        // 没有进行搜索的时候，也要添加对listView的item单击监听
        setItemClick(cityList);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                        getForecast getForecast = new getForecast(query,myHandler,MainActivity.this);
//                        getForecast.start();
                if (searchView != null) {
                    // 得到输入管理对象
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // 这将让键盘在所有的情况下都被隐藏，但是一般我们在点击搜索按钮后，输入法都会乖乖的自动隐藏的。
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        // 输入法如果是显示状态，那么就隐藏输入法
                    }
                    //searchView.clearFocus();
                    Toast.makeText(MainActivity.this,"请在下方匹配的列表进行选择",Toast.LENGTH_SHORT).show();
//                    toolbar.setTitle(query);
//                    toolbar.setSubtitle(R.string.finished);
                   // drawerLayout.closeDrawer(Gravity.START);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(filterAdapter!=null){
                    filterAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    protected void setItemClick(final List<String> filter_lists) {
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String city = filter_lists.get(position);
                if(city.contains("省")||city.contains("自治区")) {
                    if(city.contains("地区")){
                        city=city.substring(0,city.indexOf("地区")+2);
                    }else if (city.contains("盟")){
                        city=city.substring(0,city.indexOf("盟")+1);
                    }else {
                        city = city.substring(0, city.indexOf("市") + 1);
                    }
                }else if (city.contains("北京")||city.contains("上海")||city.contains("重庆")||city
                        .contains("天津")){
                    if(city.contains("县")){
                        city=city.substring(0,city.indexOf("县")+1);
                    }else {
                        city = city.substring(0, city.indexOf("区") + 1);
                    }
                }else if (city.contains("特别行政区")){
                    city=city.substring(0,2);
                }
                drawerLayout.closeDrawer(Gravity.START);
                swipeRefreshLayout.setRefreshing(true);
               getForecast getForecast = new getForecast(city,myHandler,MainActivity.this);
                getForecast.start();
                offlineCity=city;
                toolbar.setTitle(city);
                if(!swipeRefreshLayout.isRefreshing()) {
                    toolbar.setSubtitle(R.string.finished);
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.settings,menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setting:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivityForResult(intent,110);
                break;
            case R.id.exit:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 110:
                if(resultCode==RESULT_OK){
                    resultDataT = data.getStringExtra("tmpF");
                    resultDataH = data.getStringExtra("humF");
                }
            break;
            default:
                break;
        }
    }



    /**
     *
     * 轮流切换壁纸
     * */
    private void initRandomWallpaper(){
        Random random = new Random();
        int pic = 0;
        for (int i=0;i<20;i++) {
            pic = random.nextInt(4);
        }
        switch (pic){
            case 1:
                drawerLayout.setBackgroundResource(R.mipmap.nice_pic);
                break;
            case 2:
                drawerLayout.setBackgroundResource(R.mipmap.ice);
                break;
            case 3:
                drawerLayout.setBackgroundResource(R.mipmap.night);
                break;
            case 0:
                drawerLayout.setBackgroundResource(R.mipmap.nice_pic);
             default:
                break;
        }
    }

    private void initUI(String[] forecast,String todayInfo,String[] lifestyle,String airQuality,
                        String tempFormat, String humFormat) {
        Toast weatherInfoEmptyToast = Toast.makeText(MainActivity.this, "天气数据异常，请重新打开网络后重试！", Toast.LENGTH_LONG);
            if (forecast[0].isEmpty() || todayInfo.isEmpty() || lifestyle[0].isEmpty() || airQuality.isEmpty()) {
                weatherInfoEmptyToast.show();
                return;
            }
        offlineForecast = forecast;
        offlineToday=todayInfo;
        offlineLifestyle=lifestyle;
        offlineAir=airQuality;
        String bodyTemp;

        TextView comfort = findViewById(R.id.comfort_text);
        TextView sport = findViewById(R.id.sport_text);
        TextView uvText = findViewById(R.id.uvi_text);
        TextView car_wash = findViewById(R.id.car_wash_text);
        TextView aqiText = findViewById(R.id.aqi_text);
        TextView pmText = findViewById(R.id.pm25_text);
        TextView min_and_max =findViewById(R.id.max_and_min);
        TextView cloudForce = findViewById(R.id.cloud_force);
        TextView bodyTmp = findViewById(R.id.body_temperature);
        TextView humidity = findViewById(R.id.humidity);
        TextView visible = findViewById(R.id.visibility_view);
        TextView uxT = findViewById(R.id.uv_power);
        TextView press = findViewById(R.id.pressures);



        if(tempFormat==null||humFormat==null) {
            SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
            tempF = sharedPreferences.getString("temperatureFormat", SettingsActivity
                    .choose[0][0]);
            HumdF = sharedPreferences.getString("humidityFormat", SettingsActivity.choose[1][0]);
        }
        else {
            tempF=tempFormat;
            HumdF=humFormat;
        }

        if(!weatherList.isEmpty()){
            weatherList.clear();
        }
            forecastLayout.removeAllViews();
            //让提示性文字加载进去
            if (weatherTips!=null) {
                forecastLayout.addView(weatherTips);
            }else {
                weatherTips= (LinearLayout) LayoutInflater.from(this).inflate(R.layout.weather_tips,forecastLayout,false);
                forecastLayout.addView(weatherTips);
            }
            //处理未来7天的数据的显示
            for (String day : forecast) {
                day = day.trim();
                if (!day.isEmpty()) {
                    String date = day.substring(day.indexOf("天是") + 3, day.indexOf("最高温度"));
                    String max = day.substring(day.indexOf("最高温度") + 5, day.indexOf("最低温度") - 1);
                    String min = day.substring(day.indexOf("最低温度") + 5, day.indexOf("风向") - 1);
                    String fx = day.substring(day.indexOf("风向") + 3, day.indexOf("风力"));
                    String fl = day.substring(day.indexOf("风力") + 3, day.indexOf("大气压"));
                    String pressure = day.substring(day.indexOf("大气压") + 4, day.indexOf("白天") - 1);
                    String dailyCondition = day.substring(day.indexOf("白天") + 7, day.indexOf("紫外线"));
                    String uv = day.substring(day.indexOf("紫外线") + 6, day.indexOf("能见度"));
                    String visibility = day.substring(day.indexOf("能见度") + 4);
                    Weather weather = new Weather(date, max, min, fx, fl, pressure, dailyCondition, uv, visibility);
                    weatherList.add(weather);
                }
            }
            for (Weather weather : weatherList) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                        forecastLayout, false);
                TextView dateText = view.findViewById(R.id.date_text);
                TextView infoText = view.findViewById(R.id.weather_information);
                TextView maxText = view.findViewById(R.id.max_text);
                TextView minText = view.findViewById(R.id.min_text);
                TextView cloudText = view.findViewById(R.id.cloud_direction);
                dateText.setText(weather.getDate());
                infoText.setText(weather.getDailyWeather());
                maxText.setText(setTemperatureFormat(weather.getMaxTemperature(),tempF));
                minText.setText(setTemperatureFormat(weather.getMinTemperature(),tempF));
                cloudText.setText(weather.getCloudDirection());
               forecastLayout.addView(view);
            }

            //处理空气指数
            airQuality=airQuality.trim();
            if(airQuality.equals("empty")){
                aqiLayout.setVisibility(View.GONE);
            }else {
                aqiLayout.setVisibility(View.VISIBLE);
                String aqiE = airQuality.substring(airQuality.indexOf("aqi") + 6, airQuality.indexOf("pm")
                        - 1);
                String pm25 = airQuality.substring(airQuality.indexOf("pm2.5") + 8);
                aqiText.setText(aqiE);
                pmText.setText(pm25);
            }
            //处理今日天气其他参数
            bodyTemp = todayInfo.substring(todayInfo.indexOf("体感温度：")+5);
            Weather weather = weatherList.get(0);
            cloudForce.setText(weather.getCloudForce());
            visible.setText(weather.getVisibility()+"千米");
            uxT.setText(weather.getUVPower());
            press.setText(weather.getPressure()+"帕");

            //处理生活建议
            String comf = "舒适度："+lifestyle[0];
            String soprt = "运动建议："+lifestyle[1];
            String uvx = "紫外线强度："+lifestyle[2];
            String carWash ="洗车指数："+lifestyle[3];

            {
                comfort.setText(comf);
                sport.setText(soprt);
                uvText.setText(uvx);
                car_wash.setText(carWash);
            }
        //处理今日温度显示
        String tday = forecast[0];
        if (!tday.isEmpty()) {
            String Tmin = tday.substring(tday.indexOf("最低温度") + 5, tday.indexOf("风向") - 1);
            String Tmax = tday.substring(tday.indexOf("最高温度") + 5, tday.indexOf("最低温度") - 1);
            String tmp = todayInfo.substring(todayInfo.indexOf("气温") + 3, todayInfo
                    .indexOf("湿")).trim();
            temperature.setText(setTemperatureFormat(tmp, tempF));
            min_and_max.setText(setTemperatureFormat(Tmin, tempF) + "-" + setTemperatureFormat(Tmax, tempF));
            bodyTmp.setText(setTemperatureFormat(bodyTemp, tempF));
            temperature.setTextSize(70);
        }

        //处理湿度显示方式
        if (!todayInfo.isEmpty()) {
            String hum = todayInfo.substring(todayInfo.indexOf("湿度") + 3, todayInfo
                    .lastIndexOf("天气")).trim();
            String units;
            if (HumdF.equals(getResources().getString(R.string.unitP))) {
                units = getResources().getString(R.string.singleP);
                humidity.setText(hum + units);
            } else if (HumdF.equals(getResources().getString(R.string.unitA))) {
                units = getResources().getString(R.string.singleA);
                int humd = Integer.valueOf(hum);
                humd -= 35;
                humidity.setText(humd + units);
            }
        }

        String status;
        if (!todayInfo.isEmpty()) {
           status  = todayInfo.substring(todayInfo.indexOf("状况") + 3, todayInfo.indexOf("体感") - 1)
                    .trim();
        }else {
            status="晴";
        }
       weatherStatus.setText(status);
        if(status.equals("晴")){
            Glide.with(this).load(R.mipmap.sun).into(weatherImage);
        }else if (status.equals("阴")){
            Glide.with(this).load(R.mipmap.cloud_nt).into(weatherImage);
        }else if (status.equals("多云")){
            Glide.with(this).load(R.mipmap.cloudy).into(weatherImage);
        }else if (status.equals("小雨")||status.equals("雨")||status.equals("中雨")
                ||status.equals("大雨")||status.equals("暴雨")||status.equals("大暴雨")||status.contains
                ("阵雨")){
            Glide.with(this).load(R.mipmap.rain).into(weatherImage);
        }else if (status.contains("雪")) {
            if (status.contains("雨")) {
                Glide.with(this).load(R.mipmap.rain_snow).into(weatherImage);
            } else {
                Glide.with(this).load(R.mipmap.snow).into(weatherImage);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }



    /**
     * @param hourWeatherData 今日或明日的24小时天气基本数据（三小时间隔时间）
     *                        <p>传入过来的数据格式应该为数组类型：</p>
     *                        <p>每个数组成员格式为：时间（08：00）-天气状况（晴/小雨）-->处理图片显示逻辑-此时温度（15 (℃) ）</p>
     *                        <p>分隔符：-</p>
     *
     * */

    private void initTodayHourWeather(String[] hourWeatherData){
        offlineHourlyWeather=hourWeatherData;
        List<HourWeather> hourWeatherList = new ArrayList<>();
        for (String hourWeather:hourWeatherData){
            String hourTime = hourWeather.split("-")[0];
            String hourWeatherText = hourWeather.split("-")[1];
            String hourTemperature = hourWeather.split("-")[2];
            int imageResource = 0;
            if(hourWeatherText.equals("晴")){
                imageResource = R.mipmap.sun;
            }else if (hourWeatherText.equals("阴")){
                imageResource = R.mipmap.cloud_nt;
            }else if (hourWeatherText.equals("多云")){
                imageResource = R.mipmap.cloudy;
            }else if (hourWeatherText.equals("小雨")||hourWeatherText.equals("雨")||hourWeatherText.equals("中雨")
                    ||hourWeatherText.equals("大雨")|hourWeatherText.equals("暴雨")||hourWeatherText.equals("大暴雨")||hourWeatherText.contains
                    ("阵雨")){
                imageResource = R.mipmap.rain;
            }else if (hourWeatherText.contains("雪")) {
                if (hourWeatherText.contains("雨")) {
                    imageResource = R.mipmap.rain_snow;
                } else {
                    imageResource = R.mipmap.snow;
                }
            }
            hourTemperature=setTemperatureFormat(hourTemperature,tempF);
            HourWeather weather = new HourWeather(hourTime,imageResource,hourTemperature);
            hourWeatherList.add(weather);
        }

        HourWeatherAdapter weatherAdapter = new HourWeatherAdapter(hourWeatherList);
        hourWeatherRecyclerView.setAdapter(weatherAdapter);
    }

    private void openDrawerLayout(){
        drawerLayout.openDrawer(Gravity.START);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        searchView.onActionViewExpanded();
    }

    private String setTemperatureFormat(String temperature,String Format){
        String result ="";
        int tem;
        String unit;
        if(Format.equals(getResources().getString(R.string.unitC))) {
            unit=getResources().getString(R.string.singleC);
            result = temperature+unit;
        }else if (Format.equals(getResources().getString(R
                .string.unitF))) {
            unit = getResources().getString(R
                    .string.singleF);
            tem = Integer.valueOf(temperature);
            tem = (9 * tem) / 5 + 32;
            temperature = String.valueOf(tem);
            result=temperature+unit;
        }
        return result;
    }

    private void initOfflineData(){
        SharedPreferences sharedPreferences = getSharedPreferences("lastWeather", MODE_PRIVATE);
        String city = sharedPreferences.getString("city", "");
        String forecast0 = sharedPreferences.getString(forecastNames[0], "");
        String forecast1 = sharedPreferences.getString(forecastNames[1], "");
        String forecast2 = sharedPreferences.getString(forecastNames[2], "");
        String forecast3 = sharedPreferences.getString(forecastNames[3], "");
        String forecast4 = sharedPreferences.getString(forecastNames[4], "");
        String forecast5 = sharedPreferences.getString(forecastNames[5], "");
        String forecast6 = sharedPreferences.getString(forecastNames[6], "");
        String[] forecast = {forecast0, forecast1, forecast2, forecast3, forecast4, forecast5, forecast6};
        String lifestyle0 = sharedPreferences.getString(lifestyleNames[0], "");
        String lifestyle1 = sharedPreferences.getString(lifestyleNames[1], "");
        String lifestyle2 = sharedPreferences.getString(lifestyleNames[2], "");
        String lifestyle3 = sharedPreferences.getString(lifestyleNames[3], "");
        String[] lifestyle = {lifestyle0, lifestyle1, lifestyle2, lifestyle3};
        String todayInfo = sharedPreferences.getString("todayInfor", "");
        String airQuality = sharedPreferences.getString("airQ", "");
        String first3 = sharedPreferences.getString(hourlyWeatherTimeNames[0], "");
        String second3 = sharedPreferences.getString(hourlyWeatherTimeNames[1], "");
        String third3 = sharedPreferences.getString(hourlyWeatherTimeNames[2], "");
        String fourth3 = sharedPreferences.getString(hourlyWeatherTimeNames[3], "");
        String fifth3 = sharedPreferences.getString(hourlyWeatherTimeNames[4], "");
        String sixth3 = sharedPreferences.getString(hourlyWeatherTimeNames[5], "");
        String seventh3 = sharedPreferences.getString(hourlyWeatherTimeNames[6], "");
        String eighth3 = sharedPreferences.getString(hourlyWeatherTimeNames[7], "");
        String[] hourlyWeather = {first3,second3,third3,fourth3,fifth3,sixth3,seventh3,eighth3};
        if (!city.isEmpty())
            toolbar.setTitle(city);
        toolbar.setSubtitle("离线使用");
        if (!forecast[0].isEmpty() && !lifestyle[0].isEmpty()) {
            initUI(forecast, todayInfo, lifestyle, airQuality, tempF, HumdF);
            initTodayHourWeather(hourlyWeather);
        }
    }

    private void initService(){
        RService = new Intent(this,UpdateService.class);
        startService(RService);
        bindService(RService,serviceConnection,BIND_AUTO_CREATE);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void requestNeedPermissions(){
       String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
               Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,Manifest.permission.WRITE_EXTERNAL_STORAGE};
       for(String permission:permissions){
           if(PackageManager.PERMISSION_GRANTED!=ContextCompat.checkSelfPermission(this,permission)){
               this.requestPermissions(permissions,100);
           }
       }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBarTransparent(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.locationImg:
                openDrawerLayout();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 100:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"定位失败，请开启GPS定位权限后重试",Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    initLocationService();
                }
                break;
            default:
                break;
        }
    }
    static class MyHandler extends Handler{

        WeakReference<MainActivity> mActivity;
        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            MainActivity mainActivity = mActivity.get();
            super.handleMessage(msg);
            switch (msg.what){
                case getForecast.UPDATE_WEATHER:
                    bundle = msg.getData();//获取传过来的天气数据;
                    String[] forecast = bundle.getStringArray("forecast");
                    String todayInfo = bundle.getString("today");
                    String[] lifestyle = bundle.getStringArray("lifestyle");
                    String air = bundle.getString("air");
                    String[] hourTimeWeather = bundle.getStringArray("hourTimeWeather");
                    if(air.isEmpty()){
                        mainActivity.airQ=air="empty";
                    }else {
                        mainActivity.airQ=air;
                    }
                    mainActivity.fore=forecast;
                    mainActivity.today=todayInfo;
                    mainActivity.life=lifestyle;
                    mainActivity.hourlyWeatherArray=hourTimeWeather;
                    mainActivity.isStart=false;
                    if (hourTimeWeather!=null) {
                        mainActivity.initTodayHourWeather(hourTimeWeather);
                    }
                    mainActivity.initUI(forecast,todayInfo,lifestyle,air,null,null);
                    break;
                default:
                    break;
            }
        }
    }


    public void refreshCityInfo(){
        swipeRefreshLayout.setRefreshing(true);
        getForecast getForecast = new getForecast(toolbar.getTitle().toString(),
                myHandler,MainActivity.this);
        toolbar.setSubtitle(R.string.finished);
        getForecast.start();
    }


    private void setShackedChangeCity(){
        int rate;
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        //获取重力感应器
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rate=SensorManager.SENSOR_DELAY_NORMAL;
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                if(lastPositionX==0){
                    lastPositionX=x;
                }
                if(lastPositionY==0){
                    lastPositionY=y;
                }
                if(LastPositionZ==0){
                    LastPositionZ=z;
                }
                if(lastPositionX!=x){
                    x-=lastPositionX;
                }
                if(lastPositionY!=y){
                    y-=lastPositionY;
                }
                if(LastPositionZ!=z){
                    z-=LastPositionZ;
                }
                float f = 15;
                if(Math.abs(x)>f||Math.abs(y)>f||Math.abs(z)>f){
                    if(currentTime==0) {
                        vibrator.vibrate(1000);
                        Toast.makeText(MainActivity.this, "正在定位到当前城市", Toast.LENGTH_SHORT).show();
                        initLocationService();
                        currentTime = System.currentTimeMillis();
                    }
                    long nowTime = System.currentTimeMillis();
                    if(nowTime-currentTime<4000){
                        currentTime=System.currentTimeMillis();
                    }else {
                        vibrator.vibrate(1000);
                        Toast.makeText(MainActivity.this, "正在定位到当前城市", Toast.LENGTH_SHORT).show();
                        initLocationService();
                        currentTime=System.currentTimeMillis();
                    }

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
            sensorManager.registerListener(sensorEventListener, sensor, rate);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)){
//            Log.i(TAG, " drawerLayout is open,trying close it.");
            drawerLayout.closeDrawer(Gravity.START);
        }else {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            }  else {
                //小于2000ms则认为是用户希望退出程序-调用exit方法进行退出
               System.exit(0);
            }
        }
    }

    @Override
    protected void onPostResume() {
        if(!isStart){
            initUI(fore,today,life,airQ,resultDataT,resultDataH);
            initTodayHourWeather(hourlyWeatherArray);
        }
        isShakeChecked =getSharedPreferences("Setting",MODE_PRIVATE).getBoolean
                ("isShakeChecked",false);
        if (isShakeChecked) {
            setShackedChangeCity();
        }else {
            if(sensorManager!=null) {
                sensorManager.unregisterListener(sensorEventListener, sensor);
                sensor=null;
                sensorManager=null;
                sensorEventListener=null;
                vibrator=null;
            }
        }
        super.onPostResume();
    }


    @Override
    protected void onPause() {
        SharedPreferences sharedPreferences = getSharedPreferences("lastWeather",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(offlineForecast!=null&&offlineLifestyle!=null){
            for(int i=0;i<offlineForecast.length;i++){
                editor.putString(forecastNames[i],offlineForecast[i]);
            }
            for(int j=0;j<offlineLifestyle.length;j++){
                editor.putString(lifestyleNames[j],offlineLifestyle[j]).apply();
            }
            for (int i=0;i<hourlyWeatherTimeNames.length;i++){
                editor.putString(hourlyWeatherTimeNames[i],offlineHourlyWeather[i]).apply();
            }
            editor.putString("city",offlineCity);
            editor.putString("todayInfor",offlineToday);
            editor.putString("airQ",offlineAir);
            editor.apply();
        }
        super.onPause();
    }



    @Override
    protected void onDestroy() {
        unregisterReceiver(refreshReceiver);
        unregisterReceiver(databaseChangedReceiver);
        stopService(RService);
        unbindService(serviceConnection);
        myHandler=null;
        mActivity=null;
        super.onDestroy();
    }


    /**
     * <p>在更新服务中如果通广播获取MainActivity失败，则通过此方法返回一个activity实例</p>
     * <p>可能导致内存泄漏</p>
     * */
    public static MainActivity getMain(){
        return (MainActivity) mActivity;
    }

    class refreshReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    class DatabaseChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.w(TAG, "onReceive:database changed. ");
            initData();
        }
    }
}
