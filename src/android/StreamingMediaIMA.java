package cordova.plugin.streamingmediaima;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.lombrinus.projects.mods.AJCPlayer;

/**
 * This class echoes a string called from JavaScript.
 */
public class StreamingMediaIMA extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        else if(action.equals("playVideo")) {

            this.callbackContext = callbackContext;
		    JSONObject options = null;
            try {
                options = args.getJSONObject(1);
            } catch (JSONException e) {
                // Developer provided no options. Leave options null.
            }

            this.playVideo(args.getString(0), options);

            return true;

        }

        return false;
    }

    private boolean playVideo(String url, JSONObject options) {

		// return play(SimpleVideoStream.class, url, options);
        AJCPlayer videoPlayer = new VideoPlayer(this, new MediaPlayer());

        // load SurfaceHolder
        SurfaceHolder holder = mSurfaceView.getHolder();
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        holder.setFixedSize(metrics.widthPixels, (int) ((float) metrics.widthPixels / (float) 16 / (float) 9));
        holder.addCallback(this);

        // views you want to hide automatically when content is playing
        View viewBot = findViewById(R.id.audioPlayerLayoutBottom);
        View viewTop = findViewById(R.id.audioPlayerLayoutTop);
        CList viewsToHide = new CList(viewBot, viewTop);

        // we send FrameLayout as param because AJCPlayer uses this view to detect click and double click
        FrameLayout mFrameLayout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
                
        final VideoPlayerView videoPlayerView = new VideoPlayerView(mFrameLayout, surfaceHolder, currentPositionTextView, durationTextView, seekBar, viewsToHide); // you could send currentPosition, duration or seekbar views as null

        final VideoPlayerOptions options = new VideoPlayerOptions(ActivityInfo.SCREEN_ORIENTATION_SENSOR, true, true, true);
        final Controls controls = new Controls(plays, pauses, stops);
        controlBarManager = new VideoControlBarManager(context, controls, new LoadingView(){...}, new OnDoubleClick{...}, videoPlayerView, options); //LoadingView you can hide/show your progress bar. LoadingView and OnDoubleClick could be null
        videoPlayer.addEventListener(controlBarManager);

        Asset asset = new Asset(idString, "http://cdnamd-hls-globecast.akamaized.net/live/ramdisk/arriadia/hls_snrt/index.m3u8", ContentType.VIDEO);
        videoPlayer.play(asset, true); // autoplay=true


	}


    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
