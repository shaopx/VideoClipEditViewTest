package com.spx.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 目前只支持本地mp4格式文件
 */
public class VideoFrameExtractor {
    private static final String TAG = "VideoFrameExtractor";
    private static final boolean VERBOSE = true;           // lots of logging
    private static final boolean USE_MEDIACODEC = true;
    private Uri videoFilePath = null;
    private Context context;
    private String filedir = "";

    public VideoFrameExtractor(Context context, Uri path) {
        this.context = context;
        videoFilePath = path;
    }

    /**
     * 生成bitmap后的回调接口
     */
    public interface Callback {
        void onFrameAvailable(Bitmap bitmap, int index);
    }

    private Callback callback;

    /**
     * 这个列表中存放的是每个缩略图对应的时间点(毫秒数)
     */
    private List<Long> thumbnailMillSecList = new ArrayList<>();

    /**
     * 这个列表中存放的是每个缩略图对应的frame索引
     */
    private Map<Integer, Integer> thumbnailIndexSet = new HashMap<>();

    // 记录下一个要处理的缩略图对应的索引
    private int processThumbnailIndex = 0;

    // 要生成的缩略图的宽高(px)
    private int saveWidth = 54;
    private int saveHeight = 96;

    /**
     * 获取截图接口
     *
     * @param millsecsPerFrame
     * @param callback
     */
    public void getThumbnail(int millsecsPerFrame, Callback callback) {
        this.callback = callback;
        long start = System.currentTimeMillis();
        //首先根据要提取的缩略图间隔时间, 和视频的总时长, 生成一份缩略图对应时间点的列表
        long duration = getVideoDuration();
        int thumbnailCount = (int) (duration / millsecsPerFrame);
        long millSec = 0l;
        MediaMetadataRetriever mMMR = null;
        if (!USE_MEDIACODEC) {
            mMMR = new MediaMetadataRetriever();
            mMMR.setDataSource(videoFilePath.getPath());
        }

        for (int i = 0; i < thumbnailCount; i++) {
            thumbnailMillSecList.add(millSec);
            Log.d(TAG, "getThumbnail()  [" + i + "] time:" + millSec);

            if (!USE_MEDIACODEC) {
                Bitmap thumbnailAtTime = createThumbnailAtTime(mMMR, (int) millSec);
                if (callback != null) {
                    callback.onFrameAvailable(thumbnailAtTime, i);
                }
            }

            millSec += millsecsPerFrame;
        }


        if (USE_MEDIACODEC) {

            filedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            Log.d(TAG, "getThumbnail: filedir:" + filedir);
            try {
                extractMpegFrames();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException", e);
            }
        }

        long end = System.currentTimeMillis();
        Log.e(TAG, "extract thumbnails use:" + (end - start) + "ms");
    }

    /**
     * 这是一种更快速的截图的办法, 但是却可能无法截取到正确的截图, 因为视频的关键帧不够完整
     */
    private Bitmap createThumbnailAtTime(MediaMetadataRetriever mMMR, int timeInSeconds) {
        //api time unit is microseconds
        return mMMR.getFrameAtTime(timeInSeconds * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
//        return mMMR.getScaledFrameAtTime(timeInSeconds * 1000, MediaMetadataRetriever.OPTION_CLOSEST, saveWidth, saveHeight);
    }

    /**
     * 读取媒体文件的时长
     *
     * @return
     */
    public long getVideoDuration() {
        long start = System.currentTimeMillis();
        String duration = null;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

        try {
            mmr.setDataSource(context, videoFilePath);
            duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            long end = System.currentTimeMillis();
            Log.e(TAG, "duration " + duration + ", use:" + (end - start) + "ms");
            return Long.parseLong(duration);
        } catch (Exception ex) {
        } finally {
            mmr.release();
        }

        return 0;
    }

    /**
     * Tests extraction from an MP4 to a series of PNG files.
     * <p>
     * We scale the video to 640x480 for the PNG just to demonstrate that we can scale the
     * video with the GPU.  If the input video has a different aspect ratio, we could preserve
     * it by adjusting the GL viewport to get letterboxing or pillarboxing, but generally if
     * you're extracting frames you don't want black bars.
     */
    private void extractMpegFrames() throws IOException {


        MediaCodec decoder = null;
        CodecOutputSurface outputSurface = null;
        MediaExtractor extractor = null;


        try {
//            File inputFile = new File(videoFilePath);   // must be an absolute path
//            Log.d(TAG, "extractMpegFrames: inputFile:" + inputFile.getAbsolutePath());
//            // The MediaExtractor error messages aren't very useful.  Check to see if the input
//            // file exists so we can throw a better one if it's not there.
//            if (!inputFile.canRead()) {
//                throw new FileNotFoundException("Unable to read " + inputFile);
//            }

            extractor = new MediaExtractor();
            extractor.setDataSource(context, videoFilePath, null);
            int trackIndex = selectTrack(extractor);
            Log.d(TAG, "extractMpegFrames: trackIndex:" + trackIndex);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + videoFilePath);
            }
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + format.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                        format.getInteger(MediaFormat.KEY_HEIGHT));
            }

            // Could use width/height from the MediaFormat to get full-size frames.
            outputSurface = new CodecOutputSurface(saveWidth, saveHeight);

            // Create a MediaCodec decoder, and configure it with the MediaFormat from the
            // extractor.  It's very important to use the format from the extractor because
            // it contains a copy of the CSD-0/CSD-1 codec-specific data chunks.
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, outputSurface.getSurface(), null, 0);
            decoder.start();

            doExtract(extractor, trackIndex, decoder, outputSurface);
        } finally {
            // release everything we grabbed
            if (outputSurface != null) {
                outputSurface.release();
                outputSurface = null;
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
    }

    /**
     * Selects the video track, if any.
     *
     * @return the track index, or -1 if no video track is found.
     */
    private int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        Log.d(TAG, "selectTrack: numTracks:" + numTracks);
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "selectTrack: track " + i + " (" + mime + "): " + format);
        }

        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Work loop.
     */
    void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                   CodecOutputSurface outputSurface) throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;

        boolean outputDone = false;
        boolean inputDone = false;

        while (!outputDone) {
//            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.

                    int chunkSize = extractor.readSampleData(inputBuf, 0);
//                    Log.d(TAG, "doExtract: chunkSize:" + chunkSize);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
//                            Log.w(TAG, "WEIRD: got sample from track " +
//                                    extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                        }
                        long presentationTimeUs = extractor.getSampleTime();

                        if (processThumbnailIndex < thumbnailMillSecList.size()) {
                            long nextFrameTimeLine = thumbnailMillSecList.get(processThumbnailIndex);
                            if (presentationTimeUs > nextFrameTimeLine * 1000) {
                                Log.e(TAG, "FOUND frame:" + inputChunk + ", time:" + presentationTimeUs + ", index:" + processThumbnailIndex);
                                thumbnailIndexSet.put(inputChunk, processThumbnailIndex);
                                Log.e(TAG, "thumbnailIndexSet:" + thumbnailIndexSet);
                                processThumbnailIndex++;
                            }
                        }


                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);

                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, presentationTimeUs:" + presentationTimeUs + ", size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }

                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        if (VERBOSE)
                            Log.d(TAG, "awaiting decode of frame " + decodeCount);
                        outputSurface.awaitNewImage();
                        outputSurface.drawImage(true);

                        if (thumbnailIndexSet.containsKey(decodeCount)) {
                            Integer index = thumbnailIndexSet.get(decodeCount);
                            long startWhen = System.nanoTime();
                            Bitmap bitmap = outputSurface.getBitmap();
//                        String filename = filedir + "/save_" + format(decodeCount) + ".png";
//                        outputSurface.saveFrame(filename);


                            if (callback != null && bitmap != null) {
                                callback.onFrameAvailable(bitmap, index == null ? 0 : index);
                            }
                        }
                        decodeCount++;
                    }
                }
            }
        }

    }

    private String format(int decodeCount) {
        String s = "0000000000" + decodeCount;
        return s.substring(s.length() - 6);
    }

    private static void fail(String s) {
        Log.d(TAG, "fail: " + s);
    }


    /**
     * Holds state associated with a Surface used for MediaCodec decoder output.
     * <p>
     * The constructor for this class will prepare GL, create a SurfaceTexture,
     * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
     * MediaCodec.configure() to receive decoder output.  When a frame arrives, we latch the
     * texture with updateTexImage(), then render the texture with GL to a pbuffer.
     * <p>
     * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
     * can potentially drop frames.
     */
    private static class CodecOutputSurface
            implements SurfaceTexture.OnFrameAvailableListener {
        private VideoFrameExtractor.STextureRender mTextureRender;
        private SurfaceTexture mSurfaceTexture;
        private Surface mSurface;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
        int mWidth;
        int mHeight;

        private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
        private boolean mFrameAvailable;

        private ByteBuffer mPixelBuf;                       // used by saveFrame()

        /**
         * Creates a CodecOutputSurface backed by a pbuffer with the specified dimensions.  The
         * new EGL context and surface will be made current.  Creates a Surface that can be passed
         * to MediaCodec.configure().
         */
        public CodecOutputSurface(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException();
            }
            mWidth = width;
            mHeight = height;

            eglSetup();
            makeCurrent();
            setup();
        }

        /**
         * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
         */
        private void setup() {
            mTextureRender = new VideoFrameExtractor.STextureRender();
            mTextureRender.surfaceCreated();

            if (VERBOSE) Log.d(TAG, "textureID=" + mTextureRender.getTextureId());
            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());

            // This doesn't work if this object is created on the thread that CTS started for
            // these test cases.
            //
            // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
            // create a Handler that uses it.  The "frame available" message is delivered
            // there, but since we're not a Looper-based thread we'll never see it.  For
            // this to do anything useful, CodecOutputSurface must be created on a thread without
            // a Looper, so that SurfaceTexture uses the main application Looper instead.
            //
            // Java language note: passing "this" out of a constructor is generally unwise,
            // but we should be able to get away with it here.
            mSurfaceTexture.setOnFrameAvailableListener(this);

            mSurface = new Surface(mSurfaceTexture);

            mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
            mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
         */
        private void eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0)) {
                throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
            }

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);
            checkEglError("eglCreateContext");
            if (mEGLContext == null) {
                throw new RuntimeException("null context");
            }

            // Create a pbuffer surface.
            int[] surfaceAttribs = {
                    EGL14.EGL_WIDTH, mWidth,
                    EGL14.EGL_HEIGHT, mHeight,
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
            checkEglError("eglCreatePbufferSurface");
            if (mEGLSurface == null) {
                throw new RuntimeException("surface was null");
            }
        }

        /**
         * Discard all resources held by this class, notably the EGL context.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface.release();

            // this causes a bunch of warnings that appear harmless but might confuse someone:
            //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
            //mSurfaceTexture.release();

            mTextureRender = null;
            mSurface = null;
            mSurfaceTexture = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        /**
         * Returns the Surface.
         */
        public Surface getSurface() {
            return mSurface;
        }

        /**
         * Latches the next buffer into the texture.  Must be called from the thread that created
         * the CodecOutputSurface object.  (More specifically, it must be called on the thread
         * with the EGLContext that contains the GL texture object used by SurfaceTexture.)
         */
        public void awaitNewImage() {
            final int TIMEOUT_MS = 15000;

            synchronized (mFrameSyncObject) {
                while (!mFrameAvailable) {
                    try {
                        // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                        // stalling the test if it doesn't arrive.
                        mFrameSyncObject.wait(TIMEOUT_MS);
                        if (!mFrameAvailable) {
                            // TODO: if "spurious wakeup", continue while loop
                            throw new RuntimeException("frame wait timed out");
                        }
                    } catch (InterruptedException ie) {
                        // shouldn't happen
                        throw new RuntimeException(ie);
                    }
                }
                mFrameAvailable = false;
            }

            // Latch the data.
            mTextureRender.checkGlError("before updateTexImage");
            mSurfaceTexture.updateTexImage();
        }

        /**
         * Draws the data from SurfaceTexture onto the current EGL surface.
         *
         * @param invert if set, render the image with Y inverted (0,0 in top left)
         */
        public void drawImage(boolean invert) {
            mTextureRender.drawFrame(mSurfaceTexture, invert);
        }

        // SurfaceTexture callback
        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            if (VERBOSE) Log.d(TAG, "new frame available");
            synchronized (mFrameSyncObject) {
                if (mFrameAvailable) {
                    throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                }
                mFrameAvailable = true;
                mFrameSyncObject.notifyAll();
            }
        }

        public Bitmap getBitmap() {
            mPixelBuf.rewind();
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    mPixelBuf);

            try {
                Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                mPixelBuf.rewind();
                bmp.copyPixelsFromBuffer(mPixelBuf);
                return bmp;
            } finally {

            }
        }

        /**
         * 这是原来的保存bitmap的逻辑
         * Saves the current frame to disk as a PNG image.
         */
        public void saveFrame(String filename) throws IOException {
            // glReadPixels gives us a ByteBuffer filled with what is essentially big-endian RGBA
            // data (i.e. a byte of red, followed by a byte of green...).  To use the Bitmap
            // constructor that takes an int[] array with pixel data, we need an int[] filled
            // with little-endian ARGB data.
            //
            // If we implement this as a series of buf.get() calls, we can spend 2.5 seconds just
            // copying data around for a 720p frame.  It's better to do a bulk get() and then
            // rearrange the data in memory.  (For comparison, the PNG compress takes about 500ms
            // for a trivial frame.)
            //
            // So... we set the ByteBuffer to little-endian, which should turn the bulk IntBuffer
            // get() into a straight memcpy on most Android devices.  Our ints will hold ABGR data.
            // Swapping B and R gives us ARGB.  We need about 30ms for the bulk get(), and another
            // 270ms for the color swap.
            //
            // We can avoid the costly B/R swap here if we do it in the fragment shader (see
            // http://stackoverflow.com/questions/21634450/ ).
            //
            // Having said all that... it turns out that the Bitmap#copyPixelsFromBuffer()
            // method wants RGBA pixels, not ARGB, so if we create an empty bitmap and then
            // copy pixel data in we can avoid the swap issue entirely, and just copy straight
            // into the Bitmap from the ByteBuffer.
            //
            // Making this even more interesting is the upside-down nature of GL, which means
            // our output will look upside-down relative to what appears on screen if the
            // typical GL conventions are used.  (For ExtractMpegFrameTest, we avoid the issue
            // by inverting the frame when we render it.)
            //
            // Allocating large buffers is expensive, so we really want mPixelBuf to be
            // allocated ahead of time if possible.  We still get some allocations from the
            // Bitmap / PNG creation.

            mPixelBuf.rewind();
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    mPixelBuf);

            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(filename));
                Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                mPixelBuf.rewind();
                bmp.copyPixelsFromBuffer(mPixelBuf);
                bmp.compress(Bitmap.CompressFormat.PNG, 60, bos);
                bmp.recycle();
            } finally {
                if (bos != null) bos.close();
            }
            if (VERBOSE) {
                Log.d(TAG, "Saved " + mWidth + "x" + mHeight + " frame as '" + filename + "'");
            }
        }

        /**
         * Checks for EGL errors.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }


    /**
     * Code for rendering a texture onto a surface using OpenGL ES 2.0.
     */
    private static class STextureRender {
        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f, 1.0f, 0, 0.f, 1.f,
                1.0f, 1.0f, 0, 1.f, 1.f,
        };

        private FloatBuffer mTriangleVertices;

        private static final String VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "    gl_Position = uMVPMatrix * aPosition;\n" +
                        "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                        "}\n";

        private static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +      // highp here doesn't seem to matter
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";

        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID = -12345;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;

        public STextureRender() {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public int getTextureId() {
            return mTextureID;
        }

        /**
         * Draws the external texture in SurfaceTexture onto the current EGL surface.
         */
        public void drawFrame(SurfaceTexture st, boolean invert) {
            checkGlError("onDrawFrame start");
            st.getTransformMatrix(mSTMatrix);
            if (invert) {
                mSTMatrix[5] = -mSTMatrix[5];
                mSTMatrix[13] = 1.0f - mSTMatrix[13];
            }

            // (optional) clear to green so we can see if we're failing to set pixels
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            checkGlError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkGlError("glEnableVertexAttribArray maTextureHandle");

            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            checkGlError("glDrawArrays");

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }

        /**
         * Initializes GL state.  Call this after the EGL surface has been created and made current.
         */
        public void surfaceCreated() {
            mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }

            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            checkLocation(maPositionHandle, "aPosition");
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
            checkLocation(maTextureHandle, "aTextureCoord");

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            checkLocation(muMVPMatrixHandle, "uMVPMatrix");
            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
            checkLocation(muSTMatrixHandle, "uSTMatrix");

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
        }

        /**
         * Replaces the fragment shader.  Pass in null to reset to default.
         */
        public void changeFragmentShader(String fragmentShader) {
            if (fragmentShader == null) {
                fragmentShader = FRAGMENT_SHADER;
            }
            GLES20.glDeleteProgram(mProgram);
            mProgram = createProgram(VERTEX_SHADER, fragmentShader);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            checkGlError("glCreateShader type=" + shaderType);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program == 0) {
                Log.e(TAG, "Could not create program");
            }
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            return program;
        }

        public void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        public static void checkLocation(int location, String label) {
            if (location < 0) {
                throw new RuntimeException("Unable to locate '" + label + "' in program");
            }
        }
    }
}