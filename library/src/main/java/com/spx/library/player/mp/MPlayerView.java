package com.spx.library.player.mp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * by shaopx 2018.11.5
 * textureview and egl entry
 */
public abstract class MPlayerView extends FrameLayout implements
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener {
    private Context mContext;
    private FrameLayout mContainer;
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private String mUrl;

    private TextureSurfaceRenderer2 videoRenderer;
    private int surfaceWidth, surfaceHeight;

    public MPlayerView(Context context) {
        this(context, null);
    }

    public MPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    public void setDataSource(String url) {
        mUrl = url;
    }

    public void start() {
        initTextureView();
        addTextureView();
    }


    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    private void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mTextureView, 0, params);
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setLooping(true);

            while (videoRenderer.getVideoTexture() == null) {
                try {
                    Thread.sleep(30);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            Surface surface = new Surface(videoRenderer.getVideoTexture());
            try {
                mMediaPlayer.setDataSource(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setSurface(surface);

            surface.release();
            mMediaPlayer.prepareAsync();
        }
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        videoRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
        videoRenderer = getVideoRender(surface, surfaceWidth, surfaceHeight);
        initMediaPlayer();
    }

    public abstract TextureSurfaceRenderer2 getVideoRender(SurfaceTexture surface, int surfaceWidth, int surfaceHeight);


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void resumePlay() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void pausePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

}
