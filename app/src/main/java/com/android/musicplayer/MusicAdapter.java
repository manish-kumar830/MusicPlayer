package com.android.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final RecyclerItemClickInterface recyclerItemClickInterface;
    ArrayList<MusicModel> musicListData;
    Context context;

    public MusicAdapter(Context context, ArrayList<MusicModel> musicListData,RecyclerItemClickInterface recyclerItemClickInterface) {
        this.context = context;
        this.musicListData = musicListData;
        this.recyclerItemClickInterface = recyclerItemClickInterface;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item_list,parent,false);

        return new MusicViewHolder(view,recyclerItemClickInterface);
    }


    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {

        MusicModel musicData = musicListData.get(position);

        holder.songName.setText(musicData.getTitle());
        holder.size.setText(sizeInBytes(musicData.getSize()));
        holder.duration.setText(musicData.getDurationTime(musicData.getDuration()));

    }

    @Override
    public int getItemCount() {
        return musicListData.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {

        TextView songName;
        TextView size;
        TextView duration;



        public MusicViewHolder(@NonNull View itemView, RecyclerItemClickInterface recyclerItemClickInterface) {
            super(itemView);

            songName = itemView.findViewById(R.id.song_name);
            size = itemView.findViewById(R.id.sizeInMB);
            duration = itemView.findViewById(R.id.duration);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (recyclerItemClickInterface != null) {

                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {

                            recyclerItemClickInterface.onClickItem(position);
                        }

                    }
                }
            });

        }
    }

    public String sizeInBytes(String size){

        String sizeToDisplay;
        long bytes = Long.parseLong(size);

        double k = bytes/1024.0;
        double m = ((bytes/1024.0)/1024.0);
        double g = (m)/1024.0;

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (g > 1) {
            sizeToDisplay = decimalFormat.format(g).concat(" GB");
        }
        else if (m > 1) {
            sizeToDisplay = decimalFormat.format(m).concat(" MB");
        }
        else if (k > 1) {
            sizeToDisplay = decimalFormat.format(k).concat(" KB");
        }
        else {
            sizeToDisplay = decimalFormat.format(g).concat(" Bytes");
        }
        return sizeToDisplay;
    }




}
