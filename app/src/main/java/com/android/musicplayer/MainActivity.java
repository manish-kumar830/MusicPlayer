package com.android.musicplayer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements RecyclerItemClickInterface {

    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_INTENT = 2;
    String[] permissions = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    AlertDialog.Builder alertDialog;
    ArrayList<MusicModel> musicList;
    TextView display_song_name, display_song_duration, display_song_current_duration, bottomDisplaySongName;
    ImageView play_pause_btn, play_next_song, play_previous_song, player_back_btn, play_pause_bottom_btn, play_previous_bottom_btn, play_next_bottom_btn;
    SeekBar seekBar;
    MusicAdapter musicAdapter;
    RecyclerView musicRecyclerView;
    View musicPlayerView, bottomPlayerView;
    SharedPreferences sharedPreferences;
    int backButtonIndex = 0;
    boolean play_saved_song = true;
    private static final String  CHANNEL_ID = "musicNotification";

    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    AudioManager audioManager;

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

            if (MyMediaPlayer.currentIndex == musicList.size() - 1) {

                MyMediaPlayer.currentIndex = 0;
                mediaStopReleaseResource();
                MusicModel songResource = musicList.get(MyMediaPlayer.currentIndex);

                displayResources(songResource.getTitle(), songResource.getDurationTime(songResource.getDuration()));
                try {
                    createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                MyMediaPlayer.currentIndex += 1;
                mediaStopReleaseResource();
                MusicModel songResource = musicList.get(MyMediaPlayer.currentIndex);

                displayResources(songResource.getTitle(), songResource.getDurationTime(songResource.getDuration()));
                try {
                    createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (mediaPlayer == null) {
                    try {
                        createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                startMedia();

            }

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (isPlaying()) {
                    mediaStopReleaseResource();
                }
            }

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                pauseMedia();
            }

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                if (isPlaying()) {
                    mediaPlayer.setVolume(0.2f, 0.2f);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        musicRecyclerView = findViewById(R.id.songRecyclerView);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicRecyclerView.setHasFixedSize(false);
        musicPlayerView = findViewById(R.id.music_player_layout);
        display_song_name = findViewById(R.id.display_song_name);
        display_song_duration = findViewById(R.id.display_song_duration);
        display_song_current_duration = findViewById(R.id.display_song_current_duration);
        play_pause_btn = findViewById(R.id.play_pause_song);
        play_next_song = findViewById(R.id.play_next_song);
        play_previous_song = findViewById(R.id.play_previous_song);
        seekBar = findViewById(R.id.seekbar);
        bottomPlayerView = findViewById(R.id.bottom_player_layout);
        bottomDisplaySongName = findViewById(R.id.display_song_name_bottom);
        player_back_btn = findViewById(R.id.player_back);
        play_pause_bottom_btn = findViewById(R.id.play_pause_song_bottom);
        play_next_bottom_btn = findViewById(R.id.play_next_song_bottom);
        play_previous_bottom_btn = findViewById(R.id.play_previous_song_bottom);


        if (checkPermission()) {
            bottomPlayerView.setVisibility(View.VISIBLE);
            showAllSongsInRecyclerView();
        } else {
            showPermissionDialog();
        }

        play_next_song.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });

        play_next_bottom_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });

        play_previous_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPreviousSong();
            }
        });

        play_previous_bottom_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPreviousSong();
            }
        });

        bottomPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomPlayerView.setVisibility(View.GONE);
                musicRecyclerView.setVisibility(View.GONE);
                musicPlayerView.setVisibility(View.VISIBLE);
            }
        });

        player_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomPlayerView.setVisibility(View.VISIBLE);
                musicRecyclerView.setVisibility(View.VISIBLE);
                musicPlayerView.setVisibility(View.GONE);
            }
        });

        play_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (play_saved_song) {
                    try {
                        createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                        play_saved_song = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (isPlaying()) {
                    pauseMedia();
                    play_pause_btn.setImageResource(R.drawable.play_icon);
                    play_pause_bottom_btn.setImageResource(R.drawable.play_icon);
                } else {
                    startMedia();
                    play_pause_btn.setImageResource(R.drawable.pause_icon);
                    play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
                }

            }
        });

        play_pause_bottom_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (play_saved_song) {
                    try {
                        createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                        play_saved_song = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (isPlaying()) {
                    pauseMedia();
                    play_pause_bottom_btn.setImageResource(R.drawable.play_icon);
                    play_pause_btn.setImageResource(R.drawable.play_icon);
                } else {
                    startMedia();
                    play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
                    play_pause_btn.setImageResource(R.drawable.pause_icon);
                }

            }
        });


        seekBar.setMax(mediaPlayer.getDuration());
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer != null) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        String current_duration = String.valueOf(mediaPlayer.getCurrentPosition());
                        display_song_current_duration.setText(getDurationTime(current_duration));
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int read = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            int write = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void showPermissionDialog() {
        if (SDK_INT >= Build.VERSION_CODES.R) {

            alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Storage Permission")
                    .setMessage("Please Allow Storage Permission")
                    .setCancelable(false)
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                backButtonIndex = 1;
                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                                startActivityForResult(intent, REQUEST_CODE_INTENT);
                            } catch (Exception e) {

                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                startActivityForResult(intent, REQUEST_CODE_INTENT);
                            }
                        }
                    });

            alertDialog.create().show();


        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    showAllSongsInRecyclerView();
                } else {
                    showPermissionDialog();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_INTENT) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    backButtonIndex = 0;
                    showAllSongsInRecyclerView();
                } else {
                    showPermissionDialog();
                }
            }
        }

    }


    private ArrayList<MusicModel> getAllMusics() {
        ArrayList<MusicModel> songList = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA
        };


        Cursor musicCursor = MainActivity.this.getContentResolver().query(musicUri, projection, null, null, null);

        while (musicCursor.moveToNext()) {

            String songName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String songDuration = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            String songSize = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            String songData = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));


            songList.add(new MusicModel(songName, songSize, songDuration, songData));
        }
        musicCursor.close();

        return songList;
    }

    private void showAllSongsInRecyclerView() {
        musicList = getAllMusics();

        if (musicList.size() > 0) {
            musicAdapter = new MusicAdapter(MainActivity.this, musicList, this);
            musicRecyclerView.setAdapter(musicAdapter);

            SharedPreferences preferences = getSharedPreferences("music", MODE_PRIVATE);
            MyMediaPlayer.currentIndex = preferences.getInt("index", 0);
            MusicModel musicModel = musicList.get(MyMediaPlayer.currentIndex);
            displayResources(musicModel.getTitle(), musicModel.getDurationTime(musicModel.getDuration()));
            play_pause_bottom_btn.setImageResource(R.drawable.play_icon);
            play_pause_btn.setImageResource(R.drawable.play_icon);
            bottomPlayerView.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(this, "No Song Found", Toast.LENGTH_SHORT).show();
            bottomPlayerView.setVisibility(View.GONE);
        }


    }

    @Override
    public void onBackPressed() {

        if (musicPlayerView.getVisibility() == View.VISIBLE) {
            bottomPlayerView.setVisibility(View.VISIBLE);
            musicRecyclerView.setVisibility(View.VISIBLE);
            musicPlayerView.setVisibility(View.GONE);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }

        if (backButtonIndex == 1) {
            super.onBackPressed();
        }

    }


    @Override
    public void onClickItem(int position) {


        if (position != MyMediaPlayer.currentIndex) {

            MyMediaPlayer.currentIndex = position;
            MusicModel songResource = musicList.get(position);
            mediaStopReleaseResource();
            try {
                createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                displayResources(songResource.getTitle(), songResource.getDurationTime(songResource.getDuration()));
                play_pause_btn.setImageResource(R.drawable.pause_icon);
                play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
                bottomPlayerView.setVisibility(View.GONE);
                musicRecyclerView.setVisibility(View.GONE);
                musicPlayerView.setVisibility(View.VISIBLE);
                createUpdateSharedPreference();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            bottomPlayerView.setVisibility(View.GONE);
            musicRecyclerView.setVisibility(View.GONE);
            musicPlayerView.setVisibility(View.VISIBLE);
        }


    }

    public void createUpdateSharedPreference(){

        sharedPreferences = getSharedPreferences("music", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("index", MyMediaPlayer.currentIndex);
        editor.apply();

    }


    private void createStartSong(String songPath) throws IOException {

        int request = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        }

    }

    public String getDurationTime(String duration) {
        int totalDuration = Integer.parseInt(duration);
        String totalDurationText;

        int hrs = totalDuration / (1000 * 60 * 60);
        int min = (totalDuration % (1000 * 60 * 60) / (1000 * 60));
        int sec = (totalDuration % (1000 * 60 * 60) % (1000 * 60 * 60) % (1000 * 60) / 1000);

        if (hrs < 1) {
            totalDurationText = String.format("%02d:%02d", min, sec);
        } else {
            totalDurationText = String.format("%02d:%02d:%02d", hrs, min, sec);
        }
        return totalDurationText;
    }

    private void pauseMedia() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void startMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void mediaStopReleaseResource() {
        mediaPlayer.stop();
        mediaPlayer.release();
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }


    private void displayResources(String song_name, String song_duration) {

        display_song_name.setText(song_name);
        display_song_name.setSelected(true);

        bottomDisplaySongName.setText(song_name);
        bottomDisplaySongName.setSelected(true);


        MusicModel song = musicList.get(MyMediaPlayer.currentIndex);
        display_song_duration.setText(song.getDurationTime(song.getDuration()));

        display_song_duration.setText(song_duration);


    }


    private void playNextSong() {

        if (MyMediaPlayer.currentIndex == musicList.size() - 1) {

            MyMediaPlayer.currentIndex = 0;
            mediaStopReleaseResource();
            MusicModel songResource = musicList.get(MyMediaPlayer.currentIndex);

            displayResources(songResource.getTitle(), songResource.getDurationTime(songResource.getDuration()));
            try {
                createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                play_pause_btn.setImageResource(R.drawable.pause_icon);
                play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
                createUpdateSharedPreference();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            MyMediaPlayer.currentIndex += 1;
            MusicModel songData = musicList.get(MyMediaPlayer.currentIndex);
            String duration = songData.getDuration();
            mediaStopReleaseResource();
            try {
                play_pause_btn.setImageResource(R.drawable.pause_icon);
                play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
                createStartSong(songData.getData());
                displayResources(songData.getTitle(), songData.getDurationTime(duration));
                createUpdateSharedPreference();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void playPreviousSong() {

        if (MyMediaPlayer.currentIndex == 0) {

            MyMediaPlayer.currentIndex = musicList.size() - 1;
            mediaStopReleaseResource();
            MusicModel songResource = musicList.get(MyMediaPlayer.currentIndex);

            displayResources(songResource.getTitle(), songResource.getDurationTime(songResource.getDuration()));
            play_pause_btn.setImageResource(R.drawable.pause_icon);
            play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
            try {
                createStartSong(musicList.get(MyMediaPlayer.currentIndex).getData());
                createUpdateSharedPreference();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            MyMediaPlayer.currentIndex -= 1;
            MusicModel songData = musicList.get(MyMediaPlayer.currentIndex);
            String duration = songData.getDuration();
            mediaStopReleaseResource();
            try {
                play_pause_btn.setImageResource(R.drawable.pause_icon);
                play_pause_bottom_btn.setImageResource(R.drawable.pause_icon);
                createStartSong(songData.getData());
                displayResources(songData.getTitle(), songData.getDurationTime(duration));
                createUpdateSharedPreference();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaStopReleaseResource();
    }

}