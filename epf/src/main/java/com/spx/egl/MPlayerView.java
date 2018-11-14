package com.spx.egl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.daasuu.epf.filter.GlFilter;
import com.spx.library.player.mp.TextureSurfaceRenderer2;

import java.io.IOException;

import static android.media.MediaPlayer.SEEK_CLOSEST;

/**
 * by shaopx 2018.11.5
 * textureview and egl entry
 */
public abstract class MPlayerView extends FrameLayout implements
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener {
    private static final String TAG = "MPlayerView";
    private Context mContext;
    private FrameLayout mContainer;
    private TextureView mTextureView;
    protected MediaPlayer mMediaPlayer;
    private String mUrl;

    //    private TextureSurfaceRenderer2 videoRenderer;
    private int surfaceWidth, surfaceHeight;
    private EncoderSurface encoderSurface;
    private DecoderOutputSurface decoderSurface;
    private GlFilterList filterList = null;

    protected long currentPostion = 0L;

//    private Handler uiHandler = new Handler();

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

        filterList = new GlFilterList();
//        filterList.setup();
    }

    public GlFilterList getFilterList() {
        return filterList;
    }

    public GlFilterPeriod setFiler(long startTimeMs, long endTimeMs, GlFilter glFilter) {

        GlFilterPeriod period = new GlFilterPeriod(startTimeMs, endTimeMs, glFilter);
        filterList.putGlFilter(period);
        return period;
    }

    public GlFilterPeriod addFiler(long startTimeMs, long endTimeMs, GlFilter glFilter) {
        GlFilterPeriod period = new GlFilterPeriod(startTimeMs, endTimeMs, glFilter);
        filterList.putGlFilter(period);
        return period;
    }

    public void setDataSource(String url) {
        mUrl = url;
    }

    public void start() {
        initTextureView();
        addTextureView();
        running = true;
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
            Log.d(TAG, "initMediaPlayer: ...");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setLooping(true);

            while (decoderSurface.getSurface() == null) {
                try {
                    Thread.sleep(30);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            Surface surface = decoderSurface.getSurface();
            try {
                mMediaPlayer.setDataSource(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setSurface(surface);

//            surface.release();
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
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ...");
        surfaceWidth = width;
        surfaceHeight = height;
//        videoRenderer = getVideoRender(surface, surfaceWidth, surfaceHeight);


        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {

                encoderSurface = new EncoderSurface(new Surface(surface));
                encoderSurface.makeCurrent();

                decoderSurface = new DecoderOutputSurface(new GlFilter(), filterList);
//        decoderSurface.setRotation(rotation);
                decoderSurface.setOutputResolution(new Resolution(surfaceWidth, surfaceHeight));
                decoderSurface.setInputResolution(new Resolution(540, 960));
//        decoderSurface.setFillMode(fillMode);
//        decoderSurface.setFillModeCustomItem(fillModeCustomItem);
//        decoderSurface.setFlipHorizontal(flipHorizontal);
//        decoderSurface.setFlipVertical(flipVertical);
                decoderSurface.setupAll();
                post(new Runnable() {
                    @Override
                    public void run() {
                        initMediaPlayer();
                    }
                });
                poll();
            }
        });
        th.start();


    }

    private volatile boolean running = false;
    protected volatile boolean notDestroyed = true;

    private void poll() {
        while (notDestroyed) {
            if (running) {
                try {
                    decoderSurface.awaitNewImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
//                    throw ex;
                    return;
                }
                decoderSurface.drawImage(currentPostion * 1000 * 1000);
                encoderSurface.setPresentationTime(System.currentTimeMillis());
                encoderSurface.swapBuffers();
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        notDestroyed = false;
        running = false;
        decoderSurface.stopRun();
    }

    public abstract TextureSurfaceRenderer2 getVideoRender(SurfaceTexture surface, int surfaceWidth, int surfaceHeight);


    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared: ...");
        mp.start();
    }

    public void resumePlay() {
        running = true;
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void pausePlay() {
        running = false;
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.pause();
            } catch (Exception ex) {
            }
        }
    }

    public void release() {
        notDestroyed = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        decoderSurface.stopRun();
    }

    public void seekTo(long timems) {
        if (mMediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer.seekTo(timems, SEEK_CLOSEST);
            } else {
                mMediaPlayer.seekTo((int) timems);
            }
        }
    }

}
