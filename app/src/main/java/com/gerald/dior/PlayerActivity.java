package com.gerald.dior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnPlay, btnPrevious, btnNext, btnFastForward, btnFastBackward;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;

    //BarVisualizer barVisualizer;
    ImageView imageView;
    String songName;

    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

  /*  @Override
    protected void onDestroy() {
        if (barVisualizer != null)
        {
            barVisualizer.release();
        }
        super.onDestroy();
    }*/

    Thread updateSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Jaguh player");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnFastForward = findViewById(R.id.btnFastForward);
        btnFastBackward = findViewById(R.id.btnFastBackward);
        txtSongName = findViewById(R.id.txtSong);
        txtSongStart = findViewById(R.id.txtSongStart);
        txtSongEnd = findViewById(R.id.txtSongEnd);
        seekMusicBar = findViewById(R.id.seekBar);
        //barVisualizer = findViewById(R.id.wave);
        imageView = findViewById(R.id.imgView);

        if (mediaPlayer != null)
        {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos",0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekBar = new Thread()
        {
            @Override
            public void run()
            {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition<totalDuration)
                {
                    try
                    {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentPosition);

                    }catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.teal_200), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.teal_200), PorterDuff.Mode.SRC_IN);

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else
                {
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();

                    //make circle dance on play or pause ðð
                    TranslateAnimation moveAnim = new TranslateAnimation(-25,25,-25,25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(2);
                    imageView.startAnimation(moveAnim);
                }
            }
        });

        //auto clicking on song end...
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
       /* if (audioSessionId != -1)
        {
            barVisualizer.setAudioSessionId(audioSessionId);
        }*/

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implementing the next button to skip to next song
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(imageView, 360f);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implementing the previous button to skip back to previous song
                mediaPlayer.stop();
                mediaPlayer.release();
                //crazy right?
                position = ((position-1)<0)?(mySongs.size()-1):position-1;
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(imageView,-360f);
            }
        });
        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        btnFastBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }
    public void startAnimation(View view, Float degree)
    {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation",0f, degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }
    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time = time+min+":";
        if (sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }
}