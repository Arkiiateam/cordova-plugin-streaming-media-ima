package cordova.plugin.streamingmediaima.StreamingMediaIMA;

import android.content.Context;
import com.ionicframework.elbotola609035.R;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jzvd.JZVideoPlayerStandard;

/**
 * This class echoes a string called from JavaScript.
 */
public class StreamingMediaIMA extends CordovaPlugin {

  private Context context;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("coolMethod")) {
      String message = args.getString(0);
      this.coolMethod(message, callbackContext);
      return true;
    }
    else if(action.equals("playVideo")) {

      this.context = this.cordova.getActivity().getApplicationContext();

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

    JZVideoPlayerStandard jzVideoPlayerStandard = (JZVideoPlayerStandard) cordova.getActivity().findViewById(R.id.videoplayer);
    jzVideoPlayerStandard.setUp("http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4",
      JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,
      "饺子闭眼睛");
//    jzVideoPlayerStandard.thumbImageView.setImageURI("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640");

      return true;
  }


  private void coolMethod(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
      callbackContext.success(message);
    } else {
      callbackContext.error("Expected one non-empty string argument.");
    }
  }
}
