package trend.wayne.chromecast2;

import java.security.PublicKey;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
	private static final String TAG = "MediaButtonReceiver";
	private static MainActivity mActivity;

	public static void setActivity(MainActivity activity)
	{
		mActivity = activity;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		/*if (mActivity != null && Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
	            mActivity.handleMediaKey(
	                    (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));
	        }*/
	}

}
