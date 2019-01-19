package com.readboy.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.VideoView;

/**
 *
 * @author oubin
 * @date 2019/1/15
 */
public class VideoSurfaceView extends SurfaceView {

    private MediaPlayer mMediaPalyer;

    public VideoSurfaceView(Context context) {
        this(context, null);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMediaPalyer.pause();

    }
}
