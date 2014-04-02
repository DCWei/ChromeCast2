package trend.wayne.chromecast2;


import java.math.BigDecimal;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.CastApi;
import com.google.android.gms.cast.Cast.Listener;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaItemStatus;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.R.integer;
import android.R.string;
import android.app.PendingIntent;
import android.content.ClipData.Item;
import android.content.ComponentName;
import android.content.Intent;

import com.google.android.gms.cast.Cast;

import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private String TAG = "MainActivity";
	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;
	private CastDevice mDevice;
	private RemotePlayer mRemotePlayer;
	private VideoView mVideoView;
	private ImageView mPlayPause;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private SeekBar mSeekBar;
	private PlayItem mCurrentItem;
	private View mControllers;
	private PendingIntent mMediaPendingIntent;
	private RemoteControlClient mRemoteControlClient;
	private String appId="CC1AD845";
	private Handler mHandler;
	private ComponentName mEventReceiver;
	private GoogleApiClient mApiClient;
	private ConnectionCallbacks mConnectionCallbacks;
	private MessageChannel mMsgChannel;
	private Listener mCastListener;
	private boolean mWaitingForReconnect;
	private RemoteMediaPlayer mRemoteMediaPlayer;
	private MediaInfo mMediaInfo;
	private Runnable mUpdateSeekRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			updateProgress();
			mHandler.postDelayed(this, 1000);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_player);
		
		loadViews();
		
		setControllersListener();
		//mCurrentItem = new PlayItem(media);
		
		mMediaRouter = MediaRouter.getInstance(this);
		mMediaRouteSelector=new MediaRouteSelector.Builder().addControlCategory(
                CastMediaControlIntent.categoryForCast(appId)).build();
								
		mMediaRouterCallback = new MyMediaRouterCallback();
		/*mEventReceiver = new ComponentName(getPackageName(),
                MediaButtonReceiver.class.getName());
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(mEventReceiver);
		mMediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);*/
		//mHandler.postDelayed(mUpdateSeekRunnable, 1000);
		
	}
	
	private void loadMediaInfo(String strURL, String videoType)
	{
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Big Bunny");
		mMediaInfo = new MediaInfo.Builder(strURL)
									.setContentType(videoType)
									.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
									.setMetadata(mediaMetadata)
									.build();
		try {
			mRemoteMediaPlayer.load(mApiClient, mMediaInfo,true)
				.setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
					@Override
					public void onResult(MediaChannelResult result) {
						// TODO Auto-generated method stub
						if(result.getStatus().isSuccess())
							Log.d(TAG, "Media loaded successfully");
					}
				});
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "Problem opening media during loading", e);
		}
									
	}
	
	private void launchReceiver()
	{
		Log.d(TAG,"Enter launchReceiver");
		mConnectionCallbacks = new ConnectionCallbacks();
		mCastListener = new Cast.Listener() {
			@Override
			public void onApplicationDisconnected(int errorCode) {
				Log.d(TAG, "application has stopped");
			}
		};
		Cast.CastOptions.Builder apiOptionBuilder = Cast.CastOptions.builder(mDevice, mCastListener);
		mApiClient = new GoogleApiClient.Builder(this)
										.addApi(Cast.API, apiOptionBuilder.build())
										.addConnectionCallbacks(mConnectionCallbacks)
										.build();
		mApiClient.connect();
		
		loadMediaInfo("http://archive.org/download/BigBuckBunny_328/BigBuckBunny_512kb.mp4","video/mp4");
	}
	
	/*private void registerRCC()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Create the RCC and register with AudioManager and MediaRouter
            mRemoteControlClient = new RemoteControlClient(mMediaPendingIntent);
            mMediaRouter.addRemoteControlClient(mRemoteControlClient);
            MediaButtonReceiver.setActivity(MainActivity.this);
            mRemoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
            mRemoteControlClient.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_PLAYING);
            
        }
	}*/
	
	private void loadViews()
	{
		mPlayPause = (ImageView) findViewById(R.id.PlayPause);
		mVideoView = (VideoView) findViewById(R.id.videoView1);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		mCurrentTime = (TextView) findViewById(R.id.currentTime);
		mTotalTime = (TextView) findViewById(R.id.totalTime);
		mControllers = findViewById(R.id.controllers);
	}
	
	private void setControllersListener()
	{
		mPlayPause.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				mRemoteMediaPlayer.play(mApiClient);
			}
		});
	}
	
	/*public boolean handleMediaKey(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                {
                    Log.d(TAG, "Received Play/Pause event from RemoteControlClient");
                    if (mRemotePlayer.isPause()) {
                    	mRemotePlayer.resume();
                    } else {
                    	mRemotePlayer.pause();
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                {
                    Log.d(TAG, "Received Play event from RemoteControlClient");
                    if (mRemotePlayer.isPause()) {
                    	mRemotePlayer.resume();
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                {
                    Log.d(TAG, "Received Pause event from RemoteControlClient");
                    if (!mRemotePlayer.isPause()) {
                    	mRemotePlayer.pause();
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_STOP:
                {
                    Log.d(TAG, "Received Stop event from RemoteControlClient");
                    mRemotePlayer.stop();
                    return true;
                }
                default:
                    break;
            }
        }
        return false;
    }*/
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}
	@Override
	public void onStop()
	{
		mMediaRouter.removeCallback(mMediaRouterCallback);
		super.onStop();
	}
	@Override
	public void onResume()
	{
		super.onResume();
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}
	@Override
	public void onPause()
	{
		mMediaRouter.removeCallback(mMediaRouterCallback);
		super.onPause();
	}
	private void updateProgress()
	{
		int progress = 0;
		if(mCurrentItem != null)
		{
			long duration = mCurrentItem.getDuration();
			if(duration <=0)
			{
				if(mCurrentItem.getPlayState() == MediaItemStatus.PLAYBACK_STATE_PLAYING ||
				   mCurrentItem.getPlayState() == MediaItemStatus.PLAYBACK_STATE_PAUSED)
					mRemotePlayer.getStatus(mCurrentItem, true);
			}
			else {
				long pos = mCurrentItem.getPosition();
				long timeDelta = mRemotePlayer.isPause() ? 0 : (SystemClock.elapsedRealtime() - mCurrentItem.getTimestamp());
				progress = (int)(100.0*(pos+timeDelta)/duration);
			}
			mSeekBar.setProgress(progress);
		}
	}

	private class MyMediaRouterCallback extends MediaRouter.Callback
	{
		@Override
		public void onRouteSelected(MediaRouter router,RouteInfo info) {
			
			mDevice = CastDevice.getFromBundle(info.getExtras());
			Log.d(TAG,"Device selected. DeviceID: "+ mDevice.getDeviceId());
			//registerRCC();
			launchReceiver();
		}
		
		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo info) {
			
			mDevice = null;
		}
	}
	
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

		@Override
		public void onConnected(Bundle connectionHint) {
			// TODO Auto-generated method stub
			if(mWaitingForReconnect)
			{
				mWaitingForReconnect=false;
			}
			else {
				Cast.CastApi.launchApplication(mApiClient, appId,false)
				.setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
					
					@Override
					public void onResult(ApplicationConnectionResult result) {
						// TODO Auto-generated method stub
						Status status = result.getStatus();
						if(status.isSuccess())
						{
							ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
							String sessionId=result.getSessionId();
							String applocationStatus = result.getApplicationStatus();
							boolean wasLaunched = result.getWasLaunched();
							
							mRemoteMediaPlayer = new RemoteMediaPlayer();
							mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener(){

								@Override
								public void onStatusUpdated() {
									// TODO Auto-generated method stub
									MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
									
									//boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
								}
								
							});
							mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener(){

								@Override
								public void onMetadataUpdated() {
									// TODO Auto-generated method stub
									if(mRemoteMediaPlayer.getMediaInfo()!=null)
									{
										MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
										MediaMetadata mediaMetadata = mediaInfo.getMetadata();
									}
								}
								
							});
							
							try {
								Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
							} catch (Exception e) {
								// TODO: handle exception
								Log.e(TAG, "Exception: "+ e.getMessage());
							}
							
							mRemoteMediaPlayer.requestStatus(mApiClient)
								.setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>(){

									@Override
									public void onResult(MediaChannelResult result) {
										// TODO Auto-generated method stub
										if(!result.getStatus().isSuccess())
											Log.d(TAG,"Failed to request status");
									}
									
								});
							
						}
					}
				});
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			// TODO Auto-generated method stub
			mWaitingForReconnect = true;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem mediaRouteItem = menu.findItem(R.id.action_cast);
		MediaRouteActionProvider mediaRouteActionProvider =
				(MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteItem);
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MessageChannel implements Cast.MessageReceivedCallback {

		public String getNamespace() {
		    return "urn:x-cast:trend.wayne.message";
		}
		
		@Override
		public void onMessageReceived(CastDevice device, String arg1, String msg) {
			// TODO Auto-generated method stub
			Log.d(TAG,"Message from device[" + device.getDeviceId() + "]: " + msg);
		}
		
	}

}
