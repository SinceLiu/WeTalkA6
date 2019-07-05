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
import com.readboy.view.ConversationView;
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

    private VideoView mVideoView;
    private ImageView mFirstFrameIv;
    private Bitmap mFirstFrame;
    private Uri mUri;
    private View mPlayBtn;
    private String mVideoPath;
    private boolean isValidity = false;
    private boolean bResumeVideo = false;

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
            ToastUtils.show(this, getString(R.string.parse_video_fail));
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
        if (!bResumeVideo) {
            mVideoView.setVideoPath(mVideoPath);
        }
        ConversationView.muteAudioFocus(VideoActivity.this, true);
        mVideoView.start();
        mPlayBtn.setVisibility(View.GONE);
    }

    private void resumeVideo() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        bResumeVideo = false;
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        showFirstFrame();
        ConversationView.muteAudioFocus(VideoActivity.this, false);
        mPlayBtn.setVisibility(View.VISIBLE);
    }

    private void showFirstFrame() {
        Log.i(TAG, "showFirstFrame:  1 >>");
        if (mFirstFrame == null && isValidity) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(mVideoPath);
            } catch (Exception e) {
                Log.e(TAG, "video path is invalid");
            }
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
        ConversationView.muteAudioFocus(VideoActivity.this, false);
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
                    ConversationView.muteAudioFocus(VideoActivity.this, false);
                    mVideoView.pause();
                    bResumeVideo = true;
                    mPlayBtn.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "onClick: can pause  " + mVideoView.isPlaying());
                }
                break;
            case R.id.video_play_btn:
                playVideo();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPrepared: ");
        mFirstFrameIv.setVisibility(View.GONE);
    }
}
