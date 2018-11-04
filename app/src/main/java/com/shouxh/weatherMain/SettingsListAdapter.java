package com.shouxh.weatherMain;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SettingsListAdapter extends ArrayAdapter<Settings> {
    private List<Settings> settingsList;
    private Context context;
    private int resource;
    private List<Switch> switchList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private boolean isShaked =false;
    public SettingsListAdapter(@NonNull Context context, int resource, @NonNull List<Settings>
            objects) {
        super(context, resource, objects);
        this.context=context;
        settingsList=objects;
        this.resource=resource;
        sharedPreferences=context.getSharedPreferences("Setting",Context.MODE_PRIVATE);
        isShaked=sharedPreferences.getBoolean("isShakeChecked",false);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView= LayoutInflater.from(context).inflate(resource,parent,false);
        Settings settings = settingsList.get(position);
        TextView tips = convertView.findViewById(R.id.tips);
        TextView unit = convertView.findViewById(R.id.unit);
        Switch switcher = convertView.findViewById(R.id.switcher);
        if(isShaked){
            switcher.setChecked(true);
        }else {
            switcher.setChecked(false);
        }
        switchList.add(switcher);
        tips.setText(settings.getTip());
        unit.setText(settings.getValue());
        if(!(unit.getText().toString().contains("摇一摇"))){
            ((ViewGroup)convertView).removeView(switcher);
        }
        if(unit.getText().toString().equals("ok")){
            ((ViewGroup)convertView).removeView(unit);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)tips
                    .getLayoutParams();
            layoutParams.setMargins(15,40, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT);
            tips.setLayoutParams(layoutParams);
            tips.setGravity(Gravity.CENTER_VERTICAL);
        }
        return convertView;
    }

    @Nullable
    @Override
    public Settings getItem(int position) {
        return settingsList.get(position);
    }

    @Override
    public int getCount() {
        return settingsList.size();
    }


    public Switch getmSwitch(int position) {
        return switchList.get(position);

    }
}
