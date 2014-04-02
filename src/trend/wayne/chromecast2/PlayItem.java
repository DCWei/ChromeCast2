package trend.wayne.chromecast2;

import android.R.integer;
import android.net.Uri;
import android.support.v7.media.MediaItemStatus;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;

public class PlayItem {
	private String mItemId;
	private MediaInfo mMedia;
	private int mPlayState = MediaItemStatus.PLAYBACK_STATE_PENDING;
	private String mRemoteItemId;
	private long mPosition;
	private long mTimestamp;
	
	public PlayItem(MediaInfo media)
	{
		mMedia = media;
		mPosition = 0;
	}
	public void setPosition(long position)
	{
		mPosition = position;
	}
	public void setRemoteItemId(String remoteId)
	{
		mRemoteItemId = remoteId;
	}
	public void setTimestamp(long time)
	{
		mTimestamp = time;
	}
	public void setState(int state)
	{
		mPlayState = state;
	}
	public int getPlayState()
	{
		return mPlayState;
	}
	public long getDuration()
	{
		return mMedia.getStreamDuration();
	}
	public long getPosition()
	{
		return mPosition;
	}
	public String getRemoteItemId()
	{
		return mRemoteItemId;
	}
	public Uri getUri()
	{
		return Uri.parse(mMedia.getContentId());
	}
	public String getContentType()
	{
		return mMedia.getContentType();
	}
	public long getTimestamp()
	{
		return mTimestamp;
	}
}
