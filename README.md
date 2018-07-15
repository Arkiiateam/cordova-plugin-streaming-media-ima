# Cordova Streaming Media IMA plugin -Arkiia project-

For iOS and Android,

## Description

This plugin allows you to stream hls audio and video in a fullscreen, native player on iOS and Android, with Google IMA sdk support to traffic vast ad tags.

* 1.0.0 Works with Cordova 3.x
* 1.0.1+ Works with Cordova >= 4.0

## Vast ad limitations

Please refer to https://developers.google.com/interactive-media-ads/docs/sdks/html5/compatibility#vmap-footnote

## Installation

```
cordova plugin add https://github.com/Arkiiateam/cordova-plugin-streaming-media-ima
```

### iOS specifics

### Android specifics

## Usage

```javascript
  var VIDEO_URL = STREAMING_VIDEO_URL;

  // Just play a video
  window.plugins.streamingMedia.playVideo(VIDEO_URL, {adTagUrl: AD_TAG_URL});

  // Play a video with callbacks
  var options = {
    successCallback: function() {
      console.log("Video was closed without error.");
    },
    errorCallback: function(errMsg) {
      console.log("Error! " + errMsg);
    },
    orientation: 'landscape',
    shouldAutoClose: true,  // true(default)/false
    controls: true // true(default)/false. Used to hide controls on fullscreen,
    adTagUrl: AD_TAG_URL
  };
  window.plugins.streamingMedia.playVideo(videoUrl, options);

```

## Special Thanks

[ALOUANE Nour-Eddine (@alouane)](https://github.com/alouane)

[Salah-Eddine Bellemalem (@bellemallem)](https://github.com/bellemallem)
