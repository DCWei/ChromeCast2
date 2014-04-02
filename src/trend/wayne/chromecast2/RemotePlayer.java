package trend.wayne.chromecast2;

import com.google.android.gms.cast.MediaInfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.ControlRequestCallback;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.media.RemotePlaybackClient.SessionActionCallback;
import android.support.v7.media.RemotePlaybackClient.StatusCallback;
import android.support.v7.media.MediaItemStatus;
import android.support.v7.media.MediaSessionStatus;
import android.support.v7.media.RemotePlaybackClient;
import android.util.Log;
import android.support.v7.media.RemotePlaybackClient.ItemActionCallback;

public class RemotePlayer {
	private String TAG="RemotePlayer";
	private RouteInfo mRoute;
	private RemotePlaybackClient mClient;
	private Context mContext;
	private StatusCallback mStatusCallback;
	private Callback mCallback;
	private String mTrackInfo;
	private Bitmap mSnapshot;
	private boolean mPause=false;
	
	public RemotePlayer(Context ctx)
	{
		mContext=ctx;
	}
	
	public void connect(RouteInfo route)
	{
		mRoute=route;
		mStatusCallback = new MyStatusCallback();
		mClient = new RemotePlaybackClient(mContext, route);
		mClient.setStatusCallback(mStatusCallback);
	}
	
	public void release() {
		mClient.release();
	}
	
	public void play(final PlayItem item)
	{
		Log.d(TAG,"route info: "+mRoute.toString());
		mClient.play(item.getUri(), item.getContentType(), null, 0, null, new ItemActionCallback() {
			 @Override
	            public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus,
	                    String itemId, MediaItemStatus itemStatus) {
				 	mPause=false;
	                item.setRemoteItemId(itemId);
	                if(item.getPosition()>0)
	                {
	                	seek(item,item.getPosition());
	                }
	                if (item.getPlayState() == MediaItemStatus.PLAYBACK_STATE_PAUSED) {
						
					}
	            }

	            @Override
	            public void onError(String error, int code, Bundle data) {

	            }
		});
	}
	
	public void pause()
	{
		if(!mClient.hasSession())
			return;
		mClient.pause(null, new SessionActionCallback() {
            @Override
            public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus) {
            	mPause=true;

            }

            @Override
            public void onError(String error, int code, Bundle data) {
            }
        });
	}
	
	public void resume()
	{
		if(!mClient.hasSession())
			return;
		mClient.resume(null, new SessionActionCallback() {
            @Override
            public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus) {
            	mPause=false;
            }

            @Override
            public void onError(String error, int code, Bundle data) {

            }
        });
	}
	
	public void stop()
	{
		if(!mClient.hasSession())
			return;
		mClient.stop(null, new SessionActionCallback() {
            @Override
            public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus) {
            	mPause=false;
                if (mClient.isSessionManagementSupported()) {
                    endSession();
                }
            }

            @Override
            public void onError(String error, int code, Bundle data) {
            }
        });
	}
	public boolean isPause()
	{
		return mClient.hasSession() && mPause;
	}
	public void seek(final PlayItem item, long pos)
	{
		if(!mClient.hasSession())
			return;
		mClient.seek(item.getRemoteItemId(), pos, null, new ItemActionCallback() {
	           @Override
	           public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus,
	                   String itemId, MediaItemStatus itemStatus) {
	           }

	           @Override
	           public void onError(String error, int code, Bundle data) {
	        	   
	           }
	        });
	}
	
	public void endSession()
	{
		mClient.endSession(null, new SessionActionCallback() {
            @Override
            public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus) {
            }

            @Override
            public void onError(String error, int code, Bundle data) {
            }
        });
	}
	
	public void getStatus(final PlayItem item, final boolean update)
	{
		if(!mClient.hasSession() || item.getRemoteItemId() == null)
			return;
		mClient.getStatus(item.getRemoteItemId(), null, new ItemActionCallback() {
			@Override
            public void onResult(Bundle data, String sessionId, MediaSessionStatus sessionStatus,
                    String itemId, MediaItemStatus itemStatus) {
                int state = itemStatus.getPlaybackState();
                if (state == MediaItemStatus.PLAYBACK_STATE_PLAYING
                        || state == MediaItemStatus.PLAYBACK_STATE_PAUSED
                        || state == MediaItemStatus.PLAYBACK_STATE_PENDING) {
                    item.setState(state);
                    item.setPosition(itemStatus.getContentPosition());
                }
            }

            @Override
            public void onError(String error, int code, Bundle data) {
            }
		});
	}
	
	private class MyStatusCallback extends StatusCallback
	{
		@Override
		public void onItemStatusChanged(Bundle data, String sessionId, MediaSessionStatus sessionStatus,
                String itemId, MediaItemStatus itemStatus) {
			if(mCallback != null)
			{
				if(itemStatus.getPlaybackState() == MediaItemStatus.PLAYBACK_STATE_ERROR)
					mCallback.onError();
				else if(itemStatus.getPlaybackState() == MediaItemStatus.PLAYBACK_STATE_FINISHED)
					mCallback.onCompletion();
			}
		}
		@Override
        public void onSessionStatusChanged(Bundle data,
                String sessionId, MediaSessionStatus sessionStatus) {
			
		}
		@Override
        public void onSessionChanged(String sessionId) {
			
		}

	}
	
	public interface Callback {
		void onError();
		void onCompletion();
	}
}
