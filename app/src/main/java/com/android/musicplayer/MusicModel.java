package com.android.musicplayer;

import java.io.Serializable;

public class MusicModel implements Serializable{

    private String title;
    private String size;
    private String duration;
    private String data;


    public MusicModel(String title, String size, String duration, String data) {
        this.title = title;
        this.size = size;
        this.duration = duration;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public String getSize() {
        return size;
    }

    public String getDuration() {
        return duration;
    }

    public String getData() {
        return data;
    }

    public String getDurationTime(String duration){
        int totalDuration = Integer.parseInt(duration);
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60)/(1000*60));
        int sec = (totalDuration%(1000*60*60)%(1000*60*60)%(1000*60)/1000);

        if (hrs < 1) {
            totalDurationText = String.format("%02d:%02d",min,sec);
        }else{
            totalDurationText = String.format("%02d:%02d:%02d",hrs,min,sec);
        }
        return totalDurationText;
    }
}
