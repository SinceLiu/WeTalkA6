package com.readboy.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.readboy.utils.ToastUtils;
import com.readboy.view.VideoView;
import com.readboy.wetalk.R;

import java.io.File;

/**
 * @author oubin
 * @date 2019/01/15
 */
public class VideoActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = "hwj_VideoActivity";

    public static final String EXTRA_DATA = "data";

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mFirstFrameIv;
    private Bitmap mFirstFrame;
    private Uri mUri;
    private View mPlayBtn;
    private String mVideoPath;
    private boolean isValidity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        parseData();
        assignView();
        playVideo();
    }

    private void parseData() {
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(EXTRA_DATA);
        if (TextUtils.isEmpty(mVideoPath) || !new File(mVideoPath).exists()) {
            Log.i(TAG, "parseData: data invalid, path = " + mVideoPath);
            mVideoPath = "";
            ToastUtils.show(this, "视频读取出错");
            finish();
        } else {
            isValidity = true;
        }
    }

    private void assignView() {
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setOnClickListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);

        mFirstFrameIv = findViewById(R.id.video_first_frame);

        mPlayBtn = findViewById(R.id.video_play_btn);
        mPlayBtn.setOnClickListener(this);
    }

    private void playVideo() {
        if (!isValidity) {
            Log.w(TAG, "playVideo: invalid");
            return;
        }
        Log.i(TAG, "playVideo: m");
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }

    private void resumeVideo() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        showFirstFrame();
    }

    private void showFirstFrame() {
        Log.i(TAG, "showFirstFrame:  1 >>");
        if (mFirstFrame == null && isValidity) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mVideoPath);
            mFirstFrame = retriever.getFrameAtTime();
            retriever.release();
        }
        if (mFirstFrame != null) {
            mFirstFrameIv.setImageBitmap(mFirstFrame);
            mFirstFrameIv.setVisibility(View.VISIBLE);
        }
        Log.i(TAG, "showFirstFrame:  2 >>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mFirstFrame != null) {
            mFirstFrame.recycle();
            mFirstFrame = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "onCompletion: ");
        mPlayBtn.setVisibility(View.VISIBLE);
//        showFirstFrame();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_view:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    mPlayBtn.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "onClick: can pause  " + mVideoView.isPlaying());
                }
                break;
            case R.id.video_play_btn:
                playVideo();
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPrepared: ");
        mPlayBtn.setVisibility(View.GONE);
        mFirstFrameIv.setVisibility(View.GONE);
    }
}
