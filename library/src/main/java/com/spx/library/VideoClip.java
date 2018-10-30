package com.spx.library;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by 逆流的鱼yuiop on 16/12/18.
 * blog : http://blog.csdn.net/hejjunlin
 */
public class VideoClip {
    private final static String TAG = "VideoClip";
    private MediaExtractor mMediaExtractor;
    private MediaFormat mMediaFormat;
    private MediaMuxer mMediaMuxer;
    private String mime = null;

    public boolean clipVideo(String url, String outputPath, long clipPoint, long clipDuration) {
        int videoTrackIndex = -1;
        int audioTrackIndex = -1;
        int videoMaxInputSize = 0;
        int audioMaxInputSize = 0;
        int sourceVTrack = 0;
        int sourceATrack = 0;
        long videoDuration, audioDuration;
        Log.d(TAG, ">>　url : " + url);
        //创建分离器
        mMediaExtractor = new MediaExtractor();
        try {
            //设置文件路径
            mMediaExtractor.setDataSource(url);
            // 创建合成器
            mMediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (Exception e) {
            Log.e(TAG, "error path" + e.getMessage());
        } //获取每个轨道的信息
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            try {
                mMediaFormat = mMediaExtractor.getTrackFormat(i);
                mime = mMediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    sourceVTrack = i;
                    int width = mMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                    int height = mMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                    videoMaxInputSize = mMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    videoDuration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);
                    //检测剪辑点和剪辑时长是否正确
                    if (clipPoint >= videoDuration) {
                        Log.e(TAG, "clip point is error!");
                        return false;
                    }
                    if ((clipDuration != 0) && ((clipDuration + clipPoint) >= videoDuration)) {
                        Log.e(TAG, "clip duration is error!");
                        return false;
                    }
                    Log.d(TAG, "width and height is " + width + " " + height
                            + ";maxInputSize is " + videoMaxInputSize + ";duration is " + videoDuration);
                    //向合成器添加视频轨
                    videoTrackIndex = mMediaMuxer.addTrack(mMediaFormat);
                } else if (mime.startsWith("audio/")) {
                    sourceATrack = i;
                    int sampleRate = mMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    int channelCount = mMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    audioMaxInputSize = mMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    audioDuration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);
                    Log.d(TAG, "sampleRate is " + sampleRate + ";channelCount is "
                            + channelCount + ";audioMaxInputSize is " + audioMaxInputSize
                            + ";audioDuration is " + audioDuration);
                    //添加音轨
                    audioTrackIndex = mMediaMuxer.addTrack(mMediaFormat);
                }
                Log.d(TAG, "file mime is " + mime);
            } catch (Exception e) {
                Log.e(TAG, " read error " + e.getMessage());
            }
        } //分配缓冲
        ByteBuffer inputBuffer = ByteBuffer.allocate(videoMaxInputSize);
        //根据官方文档的解释MediaMuxer的start一定要在addTrack之后
        mMediaMuxer.start(); //视频处理部分
        mMediaExtractor.selectTrack(sourceVTrack);
        MediaCodec.BufferInfo videoInfo = new MediaCodec.BufferInfo();
        videoInfo.presentationTimeUs = 0;
        long videoSampleTime;
        //获取源视频相邻帧之间的时间间隔。(1)
        {
            mMediaExtractor.readSampleData(inputBuffer, 0);
            //skip first I frame
            if (mMediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC)
                mMediaExtractor.advance();
            mMediaExtractor.readSampleData(inputBuffer, 0);
            long firstVideoPTS = mMediaExtractor.getSampleTime();
            mMediaExtractor.advance();
            mMediaExtractor.readSampleData(inputBuffer, 0);
            long SecondVideoPTS = mMediaExtractor.getSampleTime();
            videoSampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
            Log.d(TAG, "videoSampleTime is " + videoSampleTime);
        } //选择起点
        mMediaExtractor.seekTo(clipPoint, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        while (true) {
            int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                //这里一定要释放选择的轨道，不然另一个轨道就无法选中了
                mMediaExtractor.unselectTrack(sourceVTrack);
                break;
            }
            int trackIndex = mMediaExtractor.getSampleTrackIndex();
            //获取时间戳
            long presentationTimeUs = mMediaExtractor.getSampleTime();
            //获取帧类型，只能识别是否为I帧
            int sampleFlag = mMediaExtractor.getSampleFlags();
            Log.d(TAG, "trackIndex is " + trackIndex + ";presentationTimeUs is " + presentationTimeUs
                    + ";sampleFlag is " + sampleFlag + ";sampleSize is " + sampleSize);
            //剪辑时间到了就跳出
            if ((clipDuration != 0) && (presentationTimeUs > (clipPoint + clipDuration))) {
                mMediaExtractor.unselectTrack(sourceVTrack);
                break;
            }
            mMediaExtractor.advance();
            videoInfo.offset = 0;
            videoInfo.size = sampleSize;
            videoInfo.flags = sampleFlag;
            mMediaMuxer.writeSampleData(videoTrackIndex, inputBuffer, videoInfo);
            videoInfo.presentationTimeUs += videoSampleTime;//presentationTimeUs
        }
        // 音频部分
        mMediaExtractor.selectTrack(sourceATrack);
        MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
        audioInfo.presentationTimeUs = 0;
        long audioSampleTime;
        //获取音频帧时长
        {
            mMediaExtractor.readSampleData(inputBuffer, 0);
            //skip first sample
            if (mMediaExtractor.getSampleTime() == 0) mMediaExtractor.advance();
            mMediaExtractor.readSampleData(inputBuffer, 0);
            long firstAudioPTS = mMediaExtractor.getSampleTime();
            mMediaExtractor.advance();
            mMediaExtractor.readSampleData(inputBuffer, 0);
            long SecondAudioPTS = mMediaExtractor.getSampleTime();
            audioSampleTime = Math.abs(SecondAudioPTS - firstAudioPTS);
            Log.d(TAG, "AudioSampleTime is " + audioSampleTime);
        }
        mMediaExtractor.seekTo(clipPoint, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        while (true) {
            int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                mMediaExtractor.unselectTrack(sourceATrack);
                break;
            }
            int trackIndex = mMediaExtractor.getSampleTrackIndex();
            long presentationTimeUs = mMediaExtractor.getSampleTime();
            Log.d(TAG, "trackIndex is " + trackIndex + ";presentationTimeUs is " + presentationTimeUs);
            if ((clipDuration != 0) && (presentationTimeUs > (clipPoint + clipDuration))) {
                mMediaExtractor.unselectTrack(sourceATrack);
                break;
            }
            mMediaExtractor.advance();
            audioInfo.offset = 0;
            audioInfo.size = sampleSize;
            mMediaMuxer.writeSampleData(audioTrackIndex, inputBuffer, audioInfo);
            audioInfo.presentationTimeUs += audioSampleTime;//presentationTimeUs;
        }
        // 全部写完后释放MediaMuxer和MediaExtractor
        mMediaMuxer.stop();
        mMediaMuxer.release();
        mMediaExtractor.release();
        mMediaExtractor = null;
        return true;
    }


}
