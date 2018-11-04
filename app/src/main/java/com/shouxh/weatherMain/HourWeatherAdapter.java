package com.shouxh.weatherMain;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shouxh.weatherMain.entities.HourWeather;

import java.util.List;

public class HourWeatherAdapter extends RecyclerView.Adapter<HourWeatherAdapter.ViewHolder> {

    private List<HourWeather> mhourWeatherList ;

    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView hourTime ;
        private ImageView hourWeatherImage;
        private TextView hourTemper;

        public ViewHolder(View itemView) {
            super(itemView);
            hourTime = itemView.findViewById(R.id.hour_time);
            hourWeatherImage = itemView.findViewById(R.id.hour_weather);
            hourTemper = itemView.findViewById(R.id.hour_temper);
        }


    }

    public HourWeatherAdapter(List<HourWeather> mhourWeatherList) {
        this.mhourWeatherList = mhourWeatherList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hour_temper_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourWeather weather = mhourWeatherList.get(position);
        holder.hourTime.setText(weather.getHourTime());
        holder.hourWeatherImage.setImageResource(weather.getHourWeatherImage());
        holder.hourTemper.setText(weather.getHourTemper());
    }

    @Override
    public int getItemCount() {
        return mhourWeatherList.size();
    }
}
