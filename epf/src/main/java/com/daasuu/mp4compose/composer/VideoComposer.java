
package com.daasuu.mp4compose.composer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.daasuu.epf.filter.GlFilter;
import com.spx.egl.GlFilterList;
import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.FillModeCustomItem;
import com.spx.egl.Resolution;
import com.daasuu.mp4compose.Rotation;
import com.spx.egl.DecoderOutputSurface;
import com.spx.egl.EncoderSurface;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaExtractor.SEEK_TO_PREVIOUS_SYNC;

// Refer: https://android.googlesource.com/platform/cts/+/lollipop-release/tests/tests/media/src/android/media/cts/ExtractDecodeEditEncodeMuxTest.java
// Refer: https://github.com/ypresto/android-transcoder/blob/master/lib/src/main/java/net/ypresto/androidtranscoder/engine/VideoTrackTranscoder.java
class VideoComposer {
    private static final String TAG = "VComposer";
    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;

    private final MediaExtractor mediaExtractor;
    private final int trackIndex;
    private final MediaFormat outputFormat;
    private final MuxRender muxRender;
    private final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec decoder;
    private MediaCodec encoder;
    private ByteBuffer[] decoderInputBuffers;
    private ByteBuffer[] encoderOutputBuffers;
    private MediaFormat actualOutputFormat;
//    private DecoderSurface2 decoderSurface;
    private DecoderOutputSurface decoderSurface;
    private EncoderSurface encoderSurface;
    private boolean isExtractorEOS;
    private boolean isDecoderEOS;
    private boolean isEncoderEOS;
    private boolean decoderStarted;
    private boolean encoderStarted;
    private long writtenPresentationTimeUs;
    private final int timeScale;

    VideoComposer(MediaExtractor mediaExtractor, int trackIndex,
                  MediaFormat outputFormat, MuxRender muxRender, int timeScale) {
        this.mediaExtractor = mediaExtractor;
        this.trackIndex = trackIndex;
        this.outputFormat = outputFormat;
        this.muxRender = muxRender;
        this.timeScale = timeScale;
    }


    void setUp(GlFilter filter,GlFilterList filterList,
               Rotation rotation,
               Resolution outputResolution,
               Resolution inputResolution,
               FillMode fillMode,
               FillModeCustomItem fillModeCustomItem,
               final boolean flipVertical,
               final boolean flipHorizontal) {
        mediaExtractor.selectTrack(trackIndex);
        try {
            encoder = MediaCodec.createEncoderByType(outputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoderSurface = new EncoderSurface(encoder.createInputSurface());
        encoderSurface.makeCurrent();
        encoder.start();
        encoderStarted = true;
        encoderOutputBuffers = encoder.getOutputBuffers();

        MediaFormat inputFormat = mediaExtractor.getTrackFormat(trackIndex);
        if (inputFormat.containsKey("rotation-degrees")) {
            // Decoded video is rotated automatically in Android 5.0 lollipop.
            // Turn off here because we don't want to encode rotated one.
            // refer: https://android.googlesource.com/platform/frameworks/av/+blame/lollipop-release/media/libstagefright/Utils.cpp
            inputFormat.setInteger("rotation-degrees", 0);
        }

        decoderSurface = new DecoderOutputSurface(filter, filterList);
//        decoderSurface = new DecoderSurface2(new GlComposeFilter());
        decoderSurface.setRotation(rotation);
        decoderSurface.setOutputResolution(outputResolution);
        decoderSurface.setInputResolution(inputResolution);
        decoderSurface.setFillMode(fillMode);
        decoderSurface.setFillModeCustomItem(fillModeCustomItem);
        decoderSurface.setFlipHorizontal(flipHorizontal);
        decoderSurface.setFlipVertical(flipVertical);
        decoderSurface.setupAll();
        try {
            decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        decoder.configure(inputFormat, decoderSurface.getSurface(), null, 0);
        decoder.start();
        decoderStarted = true;
        decoderInputBuffers = decoder.getInputBuffers();
    }


    boolean stepPipeline() {
        boolean busy = false;

        int status;
        while (drainEncoder() != DRAIN_STATE_NONE) {
            busy = true;
        }
        do {
            status = drainDecoder();
            if (status != DRAIN_STATE_NONE) {
                busy = true;
            }
            // NOTE: not repeating to keep from deadlock when encoder is full.
        } while (status == DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY);
        while (drainExtractor() != DRAIN_STATE_NONE) {
            busy = true;
        }

        return busy;
    }


    long getWrittenPresentationTimeUs() {
        return writtenPresentationTimeUs;
    }


    boolean isFinished() {
        return isEncoderEOS;
    }


    void release() {
        if (decoderSurface != null) {
            decoderSurface.release();
            decoderSurface = null;
        }
        if (encoderSurface != null) {
            encoderSurface.release();
            encoderSurface = null;
        }
        if (decoder != null) {
            if (decoderStarted) decoder.stop();
            decoder.release();
            decoder = null;
        }
        if (encoder != null) {
            if (encoderStarted) encoder.stop();
            encoder.release();
            encoder = null;
        }
    }

    private int drainExtractor() {
        Log.d(TAG, "drainExtractor(): isExtractorEOS:"+isExtractorEOS);
        if (isExtractorEOS) return DRAIN_STATE_NONE;
        int trackIndex = mediaExtractor.getSampleTrackIndex();
        Log.d(TAG, "drainExtractor(): trackIndex:"+trackIndex+", this.trackIndex:"+this.trackIndex);
        if (trackIndex >= 0 && trackIndex != this.trackIndex) {
            return DRAIN_STATE_NONE;
        }
        int result = decoder.dequeueInputBuffer(0);
        Log.d(TAG, "drainExtractor(): decoder.dequeueInputBuffer result:" + result);
        if (result < 0) return DRAIN_STATE_NONE;
        if (trackIndex < 0) {
            isExtractorEOS = true;
            decoder.queueInputBuffer(result, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return DRAIN_STATE_NONE;
        }
        int sampleSize = mediaExtractor.readSampleData(decoderInputBuffers[result], 0);
        boolean isKeyFrame = (mediaExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;

        long sampleTime = mediaExtractor.getSampleTime();
        Log.d(TAG, "drainExtractor(): sampleTime:"+sampleTime +", endTimeMs:"+endTimeMs);
        if (sampleTime > endTimeMs * 1000 && enableClip()) {
            Log.e(TAG, "drainExtractor(): sampleTime:"+sampleTime+", reach the end time");
            isExtractorEOS = true;
            mediaExtractor.unselectTrack(this.trackIndex);
            decoder.queueInputBuffer(result, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return DRAIN_STATE_NONE;
        }

        decoder.queueInputBuffer(result, 0, sampleSize, sampleTime / timeScale, isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
        mediaExtractor.advance();
        return DRAIN_STATE_CONSUMED;
    }

    private int drainDecoder() {
        if (isDecoderEOS) return DRAIN_STATE_NONE;
        int result = decoder.dequeueOutputBuffer(bufferInfo, 0);
        Log.d(TAG+".drainDecoder", "drainDecoder: dequeueOutputBuffer, return:"+result);
        switch (result) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG+".drainDecoder", "drainDecoder: end of stream! bufferInfo.offset:"+bufferInfo.offset+", size:"+bufferInfo.size+",presentationTimeUs:"+bufferInfo.presentationTimeUs);
            encoder.signalEndOfInputStream();
            isDecoderEOS = true;
            bufferInfo.size = 0;
        }

        // added by shaopx begin
        Log.d(TAG+".drainDecoder", "drainDecoder: bufferInfo.presentationTimeUs:"+bufferInfo.presentationTimeUs +", endTimeMs:"+endTimeMs);
        if (enableClip() && bufferInfo.presentationTimeUs > endTimeMs*1000) {
            Log.w(TAG+".drainDecoder", "drainDecoder: reach the clip end ms! bufferInfo.offset:"+bufferInfo.offset+", size:"+bufferInfo.size+",presentationTimeUs:"+bufferInfo.presentationTimeUs);
            encoder.signalEndOfInputStream();
            isDecoderEOS = true;
            bufferInfo.flags = bufferInfo.flags | MediaCodec.BUFFER_FLAG_END_OF_STREAM;
        }
        // added by shaopx end

        boolean doRender = (bufferInfo.size > 0);



        // NOTE: doRender will block if buffer (of encoder) is full.
        // Refer: http://bigflake.com/mediacodec/CameraToMpegTest.java.txt
        decoder.releaseOutputBuffer(result, doRender);
        if (doRender) {
            decoderSurface.awaitNewImage();
            decoderSurface.drawImage(bufferInfo.presentationTimeUs* 1000);
            encoderSurface.setPresentationTime(bufferInfo.presentationTimeUs * 1000);
            encoderSurface.swapBuffers();
        }
        return DRAIN_STATE_CONSUMED;
    }

    private int drainEncoder() {
        if (isEncoderEOS) return DRAIN_STATE_NONE;
        int result = encoder.dequeueOutputBuffer(bufferInfo, 0);
        Log.d(TAG+".drainEncoder", "drainEncoder: dequeueOutputBuffer() return:"+result);
        switch (result) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                if (actualOutputFormat != null) {
                    throw new RuntimeException("Video output format changed twice.");
                }
                actualOutputFormat = encoder.getOutputFormat();
                muxRender.setOutputFormat(MuxRender.SampleType.VIDEO, actualOutputFormat);
                muxRender.onSetOutputFormat();
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                encoderOutputBuffers = encoder.getOutputBuffers();
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if (actualOutputFormat == null) {
            throw new RuntimeException("Could not determine actual output format.");
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG+".drainEncoder", "drainEncoder: reach the end@!");
            isEncoderEOS = true;
            bufferInfo.set(0, 0, 0, bufferInfo.flags);
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // SPS or PPS, which should be passed by MediaFormat.
            encoder.releaseOutputBuffer(result, false);
            return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        Log.d(TAG+".drainEncoder", "drainEncoder: writeSampleData time:"+bufferInfo.presentationTimeUs);
        muxRender.writeSampleData(MuxRender.SampleType.VIDEO, encoderOutputBuffers[result], bufferInfo);
        writtenPresentationTimeUs = bufferInfo.presentationTimeUs;
        encoder.releaseOutputBuffer(result, false);
        return DRAIN_STATE_CONSUMED;
    }

    private long startTimeMs, endTimeMs;

    private boolean enableClip() {
        return endTimeMs > startTimeMs && startTimeMs >= 0;
    }

    public void setClipRange(long startTimeMs, long endTimeMs) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        mediaExtractor.seekTo(startTimeMs, SEEK_TO_PREVIOUS_SYNC);
    }
}
