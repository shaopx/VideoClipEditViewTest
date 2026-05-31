# VideoClipEditViewTest

VideoClipEditViewTest 是一个 Android 原生视频编辑示例项目，目标是在不依赖 FFmpeg 的情况下，使用 Android 系统媒体能力和 OpenGL 完成本地视频播放、裁剪、滤镜、特效、帧预览和摄像头实时滤镜预览。

项目主要面向希望学习或实现 Android 视频编辑管线的开发者，尤其适合参考以下场景：

- 基于 `MediaExtractor` / `MediaCodec` / `MediaMuxer` 做本地视频解码、裁剪、转码和导出。
- 使用 OpenGL ES / EGL 对视频帧做滤镜、美颜和特效处理。
- 实现短视频编辑常见的时间轴、缩略图预览、裁剪区间选择和导出进度。
- 在 Camera2 预览阶段实时切换滤镜效果。

## Features

- 视频播放：支持从系统相册选择视频并播放，播放层可使用 ExoPlayer 或 MediaPlayer 封装。
- 视频裁剪：通过时间轴选择裁剪开始和结束时间，并生成新视频文件。
- 帧缩略图预览：从视频中抽取帧，生成横向时间轴缩略图。
- 动态裁剪区间：支持拖动左右裁剪手柄，限制最短和最长裁剪时长。
- 播放进度预览：支持拖动进度条在裁剪区间内预览不同时间点。
- 视频滤镜：支持对本地视频应用滤镜，并将滤镜结果渲染到导出视频中。
- 美颜处理：包含美颜、磨皮、高通、肤色调整等 OpenGL shader 示例。
- OpenGL 特效：支持闪白、抖动、灵魂出窍、分屏、缩放等特效。
- 分时段特效：通过 `GlFilterList` / `GlFilterPeriod` 在不同时间段应用不同特效。
- Camera2 实时预览滤镜：支持摄像头预览时选择并切换滤镜，要求 Android Lollipop 及以上。

## Screenshots

![video clip screen](https://github.com/shaopx/VideoClipEditViewTest/blob/master/screensnap1.png)

![video effect screen](https://github.com/shaopx/VideoClipEditViewTest/blob/master/screensnap2.png)

## Project Structure

```text
.
├── app/             Demo app, activities, UI, video clip/edit flows
├── library/         Shared playback, thumbnail, utility, and video helper code
├── epf/             OpenGL video preview, filters, effects, EGL, and MP4 compose code
├── filterlibrary/   Additional image/video filter implementations and shader assets
└── mp4compose/      Legacy MP4 compose module kept for reference
```

### `app`

The demo application contains the main user flows:

- `MainActivity`: entry point, video picker, and feature navigation.
- `VideoClipActivity`: video trimming UI, thumbnail timeline, preview, and clip export.
- `VideoEditActivity`: local video filter/effect editing.
- `VideoProgressActivity`: video generation progress screen.
- `CameraEffectActivity` / `Camera2BasicFragment`: Camera2 preview with OpenGL filters.
- `ClipContainer`: custom timeline view for trim handles, shadows, and playback progress.

### `library`

Common media and UI helper layer:

- ExoPlayer / MediaPlayer playback wrappers.
- Thumbnail extraction and thumbnail preview view.
- Video metadata and utility helpers.
- Basic video item model and shared extensions.

### `epf`

The main OpenGL and video processing module:

- OpenGL filter base classes and custom filters.
- EGL rendering helpers.
- `Mp4Composer` pipeline for decode, render, encode, and mux.
- Time-aware filter configuration through `GlFilterList`, `GlFilterPeriod`, and `GlFilterConfig`.
- Video process configuration classes used by the demo app.

### `filterlibrary`

Additional shader and native filter experiments:

- GLSL shaders for color filters, mosaic, blur, beauty, stickers, and effects.
- C++ / JNI image filter implementations.
- Filter thumbnails and lookup texture assets.

## Technology Stack

### Android media APIs

- `MediaExtractor`: reads video/audio tracks and samples from local media files.
- `MediaCodec`: decodes and encodes video frames through Android native codecs.
- `MediaMuxer`: writes encoded audio/video samples into an output MP4 file.
- `MediaMetadataRetriever`: reads duration, resolution, rotation, and frame metadata.
- `Surface` / `SurfaceTexture`: passes decoded frames into the rendering pipeline.

### Rendering and image processing

- OpenGL ES 2.0 for video frame rendering and shader effects.
- EGL for creating and managing rendering contexts and surfaces.
- GLSL fragment and vertex shaders for filters, blur, mosaic, beauty, lookup-table color effects, and motion-style effects.
- FBO-based processing for offscreen rendering during preview and export.

### Camera and playback

- Camera2 API for live camera preview and camera switching.
- ExoPlayer 2.17.1 for playback and thumbnail preview support.
- Android `MediaPlayer` wrapper for alternate playback paths.

### App and build

- Kotlin and Java mixed Android project.
- Android Gradle Plugin 7.0.4.
- Kotlin 1.5.20.
- `compileSdkVersion 31`, `targetSdkVersion 31`, `minSdkVersion 21`.
- CMake / JNI are used by native filter modules.

## Main Workflows

### Video trimming

1. Select a local video from the system gallery.
2. Load video duration and prepare the playback view.
3. Generate timeline thumbnails.
4. Drag the left and right trim handles to select a valid time range.
5. Use `Mp4Composer.clip(startMs, endMs)` to render the selected segment.
6. Write the generated video to the configured output path.

### Video filters and effects

1. Select a local video and enter the edit screen.
2. Pick a global filter, or press and hold an effect to apply it to a time range.
3. Store selected filters as `GlFilterConfig`.
4. Recreate the filter timeline in `VideoProgressActivity`.
5. Use `Mp4Composer.filterList(...)` to render the result into a new MP4.

### Camera preview filters

1. Open the Camera2 preview screen on Android Lollipop or newer.
2. Create a camera preview session.
3. Send camera frames through an OpenGL renderer.
4. Select a filter and update the renderer in real time.

## Getting Started

### Requirements

- Android Studio compatible with Android Gradle Plugin 7.0.x.
- Android SDK 31.
- Android device or emulator with API 21 or newer.
- Camera preview filter mode requires Android 5.0+ and Camera2 support.

### Run the demo

1. Clone the repository.

   ```bash
   git clone https://github.com/shaopx/VideoClipEditViewTest.git
   cd VideoClipEditViewTest
   ```

2. Open the project in Android Studio.

3. Sync Gradle and install the `app` module on an Android device.

4. Grant storage and camera permissions when prompted.

5. Use the main screen to choose one of the demo flows:

   - Video clip
   - Local video edit
   - Camera preview filter

## Output File

The demo writes processed video to:

```text
/storage/emulated/0/movies/process.mp4
```

This path is defined in `Config.DEFAULT_TEMP_VIDEO_LOCATION`. If the output fails, confirm that the app has storage permissions and that the target directory exists on the device.

## Notes and Limitations

- The project is a research/demo implementation for Android native video processing, not a polished production SDK.
- The current Gradle configuration requires API 21+, although older notes in the project history mentioned lower Android versions.
- Some modules and classes are experimental and kept for learning/reference purposes.
- The repository currently has no explicit open-source license file. If you plan to reuse the code, add or confirm a license first.
- Newer Android versions have stricter scoped storage behavior, so storage permission and output-path handling may need modernization.
- The original author noted that video recording features may not continue to be maintained.

## References and Special Thanks

- [Mp4Composer-android](https://github.com/MasayukiSuda/Mp4Composer-android)
- [android-transcoder](https://github.com/ypresto/android-transcoder)
- [android-transcoder Japanese blog](http://qiita.com/yuya_presto/items/d48e29c89109b746d000)
- [android-gpuimage](https://github.com/CyberAgent/android-gpuimage)
- [Android MediaCodec stuff](http://bigflake.com/mediacodec/)
- [grafika](https://github.com/google/grafika)
- [libstagefright](https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright)

## Related Article

The author also wrote a related summary article about this video processing work:

https://www.jianshu.com/p/cbebba28b12c
