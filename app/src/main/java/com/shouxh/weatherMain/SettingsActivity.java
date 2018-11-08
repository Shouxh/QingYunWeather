package com.shouxh.weatherMain;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ListView;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;


import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private List<Settings> settingsList = new ArrayList<>();
    private String[] settings = {"温度单位","湿度单位","开启摇一摇定位","关于"};
    public static String[][] choose = {
            {"\u2103-摄氏度","\u2109-华氏摄氏度"},
            {"%-相对湿度","g/\u33A5-绝对湿度"},
            {"开启后摇一摇即可快速切换到当前城市"},
            {"ok"}};
    private static String[] sharedSetting = new String[2];
    private SettingsListAdapter adapter;
    private Switch switcher;
    private SharedPreferences sharedPreferences;
    ProgressDialog progressDialog = null;
    private boolean isShackedChecked = false;
    private  int CHOICE=0;
    private int SECOND_CHOICE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.settingsBar);
        toolbar.setBackgroundColor(Color.BLACK);
        toolbar.setTitle("设置");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.arrow);
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
            setBar();
        }
        progressDialog = new ProgressDialog(this);
        sharedPreferences=getSharedPreferences("Setting",MODE_PRIVATE);
        String tF = sharedPreferences.getString("temperatureFormat",choose[0][0]);
        String hF = sharedPreferences.getString("humidityFormat",choose[1][0]);
        int choice1 = sharedPreferences.getInt("choice1",0);
        int choice2 = sharedPreferences.getInt("choice2",0);
        isShackedChecked =sharedPreferences.getBoolean("isShakeChecked",false);
            initSettingsList(0, CHOICE, false);
        {
            CHOICE=choice1;
            SECOND_CHOICE=choice2;
            if(tF.equals(choose[0][0])){
                initSettingsList(0,0,true);
            }else  {
                initSettingsList(0,1,true);
            }
            if(hF.equals(choose[1][0])){
                initSettingsList(1,0,true);
            }else {
                initSettingsList(1,1,true);
            }
        }
        final ListView settings = findViewById(R.id.settingList);
        adapter = new SettingsListAdapter(this, R.layout.setting_list, settingsList);
        settings.setAdapter(adapter);
        settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        setDialogStyle(position);
                        break;
                    case 1:
                        setDialogStyle(position);
                        break;
                    case 2:
                       switcher = adapter.getmSwitch(position);
                        if(!switcher.isChecked()){
                            switcher.setChecked(true);
                            isShackedChecked =true;
                        }else {
                            switcher.setChecked(false);
                            isShackedChecked =false;
                        }
                        break;
                    case 3:
                        Intent intent = new Intent(SettingsActivity.this,AboutActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setDialogStyle(final int order){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog;
        String[] args = {"温度单位","湿度单位"};
        View radioDialog;
        LayoutInflater inflater=LayoutInflater.from(this);
        radioDialog =inflater.inflate(R.layout.dialog_radio,null);
        alertDialog =builder.setView(radioDialog).setTitle(args[order])
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SettingsActivity.this,"您取消了操作",Toast.LENGTH_SHORT).show();
                    }
                }).create();
        RadioGroup radioGroup=radioDialog.findViewById(R.id.unitGroup);
        final RadioButton first=radioDialog.findViewById(R.id.firstChoose);
        final RadioButton second=radioDialog.findViewById(R.id.secondChoose);

        switch (order) {
            case 0:
            if (CHOICE == 0) {
                first.setChecked(true);
                second.setChecked(false);
            } else if (CHOICE == 1) {
                first.setChecked(false);
                second.setChecked(true);
            }
            break;
            case 1:
                if(SECOND_CHOICE==0){
                    first.setChecked(true);
                    second.setChecked(false);
                }else if (SECOND_CHOICE == 1) {
                    first.setChecked(false);
                    second.setChecked(true);
                }
                break;
            default:
                break;
        }

        first.setText(choose[order][0]);
        second.setText(choose[order][1]);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (order == 0) {
                    switch (checkedId) {
                        case R.id.firstChoose:
                            CHOICE = 0;
                            initSettingsList(order, CHOICE, true);
                            alertDialog.cancel();
                            break;
                        case R.id.secondChoose:
                            CHOICE = 1;
                            initSettingsList(order, CHOICE, true);
                            alertDialog.cancel();
                            break;
                        default:
                            break;
                    }
                }else {
                    switch (checkedId) {
                        case R.id.firstChoose:
                            SECOND_CHOICE = 0;
                            initSettingsList(order, SECOND_CHOICE, true);
                            alertDialog.cancel();
                            break;
                        case R.id.secondChoose:
                            SECOND_CHOICE = 1;
                            initSettingsList(order, SECOND_CHOICE, true);
                            alertDialog.cancel();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        alertDialog.show();
    }

    private void initSettingsList(int order,int choice,boolean isUpdate){
        if(!isUpdate) {
            for (int i = 0; i < settings.length; i++) {
                Settings setting = new Settings(settings[i], choose[i][order]);
                settingsList.add(setting);
            }
        }else {
                Settings newSetting = new Settings(settings[order],choose[order][choice]);
                settingsList.set(order,newSetting);
        }
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBar(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.BLACK);
    }

    @Override
    protected void onPause() {
        //双保险
        Intent intent = new Intent();
        intent.putExtra("tmpF",sharedSetting[0]);
        intent.putExtra("humF",sharedSetting[1]);
        setResult(RESULT_OK,intent);
        super.onPause();
    }
    @Override
    public void onBackPressed() {

        progressDialog.setTitle("请稍等一小会");
        progressDialog.setMessage("正在保存设置...");
        progressDialog.setCancelable(false);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Toast.makeText(SettingsActivity.this,"设置保存完成",Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog.show();
        sharedSetting[0]=settingsList.get(0).getValue();
        sharedSetting[1]=settingsList.get(1).getValue();
        Log.i(TAG, "未保存的温度单位："+sharedSetting[0]+"  未保存的湿度单位："+sharedSetting[1]);
        sharedPreferences.edit()
                .putString("temperatureFormat",sharedSetting[0])
                .putString("humidityFormat",sharedSetting[1])
                .putInt("choice1",CHOICE)
                .putInt("choice2",SECOND_CHOICE)
                .putBoolean("isShakeChecked", isShackedChecked)
                .apply();
//        Log.i(TAG, "文件中的温度单位："+sharedPreferences.getString("temperatureFormat","fake")
//                + "   文件中的湿度单位："+sharedPreferences.getString("humidityFormat","fake"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        progressDialog.dismiss();
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
