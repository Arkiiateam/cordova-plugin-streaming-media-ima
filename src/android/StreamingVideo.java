package cordova.plugins.streamingmediaima;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

public class StreamingVideo extends Activity implements
  MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
  MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, AdEventListener,
  AdErrorListener{
  private String TAG = getClass().getSimpleName();
  private VideoView mVideoView = null;
  private MediaPlayer mMediaPlayer = null;
  private MediaController mMediaController = null;
  private ProgressBar mProgressBar = null;
  private String mVideoUrl;
  private Boolean mShouldAutoClose = true;
  private boolean mControls;

  private ImaSdkFactory mSdkFactory;
  private AdsLoader mAdsLoader;
  private AdsManager mAdsManager;
  // The container for the ad's UI.
  private ViewGroup mAdUiContainer;
  // Whether an ad is displayed.
  private boolean mIsAdDisplayed;
  public String adTagUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // Create an AdsLoader.
    mSdkFactory = ImaSdkFactory.getInstance();
    mAdsLoader = mSdkFactory.createAdsLoader(this.getApplicationContext());
    // Add listeners for when ads are loaded and for errors.
    mAdsLoader.addAdErrorListener(this);
    mAdsLoader.addAdsLoadedListener(new AdsLoadedListener() {
      @Override
      public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
        // events for ad playback and errors.
        mAdsManager = adsManagerLoadedEvent.getAdsManager();

        // Attach event and error event listeners.
        mAdsManager.addAdErrorListener(StreamingVideo.this);
        mAdsManager.addAdEventListener(StreamingVideo.this);
        mAdsManager.init();
      }
    });

    Bundle b = getIntent().getExtras();
    mVideoUrl = b.getString("mediaUrl");
    adTagUrl = b.getString("adTagUrl");
    mShouldAutoClose = b.getBoolean("shouldAutoClose", true);
    mControls = b.getBoolean("controls", true);

    RelativeLayout relLayout = new RelativeLayout(this);
    relLayout.setBackgroundColor(Color.BLACK);
    RelativeLayout.LayoutParams relLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    relLayoutParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    mVideoView = new VideoView(this);
    mVideoView.setLayoutParams(relLayoutParam);
    relLayout.addView(mVideoView);

    mAdUiContainer = relLayout;

    requestAds(adTagUrl);

    // Create progress throbber
    mProgressBar = new ProgressBar(this);
    mProgressBar.setIndeterminate(true);
    // Center the progress bar
    RelativeLayout.LayoutParams pblp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    pblp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    mProgressBar.setLayoutParams(pblp);
    // Add progress throbber to view
    relLayout.addView(mProgressBar);
    mProgressBar.bringToFront();

    setOrientation(b.getString("orientation"));

    setContentView(relLayout, relLayoutParam);

    play();

  }

  private void requestAds(String adTagUrl) {
    AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
    adDisplayContainer.setAdContainer(mAdUiContainer);

    // Create the ads request.
    AdsRequest request = mSdkFactory.createAdsRequest();
    request.setAdTagUrl(adTagUrl);
    request.setAdDisplayContainer(adDisplayContainer);
    request.setContentProgressProvider(new ContentProgressProvider() {
      @Override
      public VideoProgressUpdate getContentProgress() {
        if (mIsAdDisplayed || mVideoView == null || mVideoView.getDuration() <= 0) {
          return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
        }
        return new VideoProgressUpdate(mVideoView.getCurrentPosition(),
          mVideoView.getDuration());
      }
    });

    // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
    mAdsLoader.requestAds(request);
  }

  private void play() {
    mProgressBar.setVisibility(View.VISIBLE);
    Uri videoUri = Uri.parse(mVideoUrl);
    try {
      mVideoView.setOnCompletionListener(this);
      mVideoView.setOnPreparedListener(this);
      mVideoView.setOnErrorListener(this);
      mVideoView.setVideoURI(videoUri);
      mMediaController = new MediaController(this);
      mMediaController.setAnchorView(mVideoView);
      mMediaController.setMediaPlayer(mVideoView);

      if (!mControls) {
        mMediaController.setVisibility(View.GONE);
      }
      mVideoView.setMediaController(mMediaController);
    } catch (Throwable t) {
      Log.d(TAG, t.toString());
    }
  }

  private void setOrientation(String orientation) {
    if ("landscape".equals(orientation)) {
      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }else if("portrait".equals(orientation)) {
      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
  }

  private Runnable checkIfPlaying = new Runnable() {
    @Override
    public void run() {
      if (mVideoView.getCurrentPosition() > 0) {
        // Video is not at the very beginning anymore.
        // Hide the progress bar.
        mProgressBar.setVisibility(View.GONE);
      } else {
        // Video is still at the very beginning.
        // Check again after a small amount of time.
        mVideoView.postDelayed(checkIfPlaying, 100);
      }
    }
  };

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.d(TAG, "Stream is prepared");
    mMediaPlayer = mp;
    mMediaPlayer.setOnBufferingUpdateListener(this);
    mVideoView.requestFocus();
    mVideoView.start();
    mVideoView.postDelayed(checkIfPlaying, 0);
  }

  private void pause() {
    Log.d(TAG, "Pausing video.");
    mVideoView.pause();
  }

  private void stop() {
    Log.d(TAG, "Stopping video.");
    mVideoView.stopPlayback();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy triggered.");
    stop();
  }

  private void wrapItUp(int resultCode, String message) {
    Log.d(TAG, "wrapItUp was triggered.");
    Intent intent = new Intent();
    intent.putExtra("message", message);
    setResult(resultCode, intent);
    finish();
  }

  public void onCompletion(MediaPlayer mp) {
    Log.d(TAG, "onCompletion triggered.");
    // Handle completed event for playing post-rolls.
    if (mAdsLoader != null) {
      mAdsLoader.contentComplete();
    }
    stop();
    if (mShouldAutoClose) {
      wrapItUp(RESULT_OK, null);
    }
  }

  public boolean onError(MediaPlayer mp, int what, int extra) {
    StringBuilder sb = new StringBuilder();
    sb.append("MediaPlayer Error: ");
    switch (what) {
      case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
        sb.append("Not Valid for Progressive Playback");
        break;
      case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
        sb.append("Server Died");
        break;
      case MediaPlayer.MEDIA_ERROR_UNKNOWN:
        sb.append("Unknown");
        break;
      default:
        sb.append(" Non standard (");
        sb.append(what);
        sb.append(")");
    }
    sb.append(" (" + what + ") ");
    sb.append(extra);
    Log.e(TAG, sb.toString());

    wrapItUp(RESULT_CANCELED, sb.toString());
    return true;
  }

  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    Log.d(TAG, "onBufferingUpdate : " + percent + "%");
  }

  @Override
  public void onBackPressed() {
    // If we're leaving, let's finish the activity
    wrapItUp(RESULT_OK, null);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // The screen size changed or the orientation changed... don't restart the activity
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mMediaController != null)
      mMediaController.show();
    return false;
  }

  @Override
  public void onAdError(AdErrorEvent adErrorEvent) {
    Log.e("Adlog", "Ad Error: " + adErrorEvent.getError().getMessage());
    mVideoView.resume();
  }

  @Override
  public void onResume() {
    if (mAdsManager != null && mIsAdDisplayed) {
      mAdsManager.resume();
    } else {
      mVideoView.resume();
    }
    super.onResume();
  }

  @Override
  public void onPause() {
    if (mAdsManager != null && mIsAdDisplayed) {
      mAdsManager.pause();
    } else {
      mVideoView.pause();
    }
    super.onPause();
  }

  @Override
  public void onAdEvent(AdEvent adEvent) {
    Log.i("Adlog", "Event: " + adEvent.getType());

    // These are the suggested event types to handle. For full list of all ad event
    // types, see the documentation for AdEvent.AdEventType.
    switch (adEvent.getType()) {
      case LOADED:
        // AdEventType.LOADED will be fired when ads are ready to be played.
        // AdsManager.start() begins ad playback. This method is ignored for VMAP or
        // ad rules playlists, as the SDK will automatically start executing the
        // playlist.
        mAdsManager.start();
        break;
      case CONTENT_PAUSE_REQUESTED:
        // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video
        // ad is played.
        mIsAdDisplayed = true;
        mVideoView.suspend();
        break;
      case CONTENT_RESUME_REQUESTED:
        // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed
        // and you should start playing your content.
        mIsAdDisplayed = false;
        mProgressBar.setVisibility(View.VISIBLE);
        mVideoView.resume();
        break;
      case ALL_ADS_COMPLETED:
        if (mAdsManager != null) {
          mAdsManager.destroy();
          mAdsManager = null;
        }
        break;
      default:
        break;
    }
  }
}
