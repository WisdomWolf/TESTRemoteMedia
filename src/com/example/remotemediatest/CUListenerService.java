package com.example.remotemediatest;





import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.RemoteController.OnClientUpdateListener;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.media.MediaMetadataRetriever;


@TargetApi(19)
public class CUListenerService extends NotificationListenerService implements OnClientUpdateListener {
	

	private RemoteController mRemoteController;
	private AudioManager mAudioManager;
	private CULServiceReceiver cuservicereceiver;
	private static final String TAG = "CUListenerService";
	
	@Override
    public void onCreate() {
        super.onCreate();
        cuservicereceiver = new CULServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
        registerReceiver(cuservicereceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cuservicereceiver);
    }
	
	@Override
	public void onNotificationPosted(StatusBarNotification sbn){
		//do stuff
	}
	
	@Override
	public void onNotificationRemoved(StatusBarNotification sbn){
		//do more stuff
	}
	
	/**
     * Called whenever all information, previously received through the other
     * methods of the listener, is no longer valid and is about to be refreshed.
     * This is typically called whenever a new {@link RemoteControlClient} has been selected
     * by the system to have its media information published.
     * @param clearing true if there is no selected RemoteControlClient and no information
     *     is available.
     */
	@Override
	public void onClientChange(boolean clearing){
		//Called whenever all information, previously received through the other methods of the listener, is no longer valid and is about to be refreshed.
	}
	
	/**
     * Called whenever new metadata is available.
     * See the {@link MediaMetadataEditor#putLong(int, long)},
     *  {@link MediaMetadataEditor#putString(int, String)},
     *  {@link MediaMetadataEditor#putBitmap(int, Bitmap)}, and
     *  {@link MediaMetadataEditor#putObject(int, Object)} methods for the various keys that
     *  can be queried.
     * @param metadataEditor the container of the new metadata.
     */
	@Override
	public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor){
		//Called whenever new metadata is available.
		String songArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "");
		String songTitle = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "");
		String albumArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "");
		String albumTitle = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "");
		String songDuration = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_DURATION, "");
		Intent i = new  Intent("com.example.remotemediatest.METADATA_YAY");
        i.putExtra("Song_Artist", songArtist);
        i.putExtra("Song_Title", songTitle);
        i.putExtra("Album_Artist", albumArtist);
        i.putExtra("Album_Title", albumTitle);
        i.putExtra("Song_Duration", songDuration);
        sendBroadcast(i);
	}
	
	/**
     * Called whenever the playback state has changed, and playback position
     * and speed are known.
     * @param state one of the playback states authorized
     *     in {@link RemoteControlClient#setPlaybackState(int)}.
     * @param stateChangeTimeMs the system time at which the state change was reported,
     *     expressed in ms. Based on {@link android.os.SystemClock#elapsedRealtime()}.
     * @param currentPosMs a positive value for the current media playback position expressed
     *     in ms, a negative value if the position is temporarily unknown.
     * @param speed  a value expressed as a ratio of 1x playback: 1.0f is normal playback,
     *    2.0f is 2x, 0.5f is half-speed, -2.0f is rewind at 2x speed. 0.0f means nothing is
     *    playing (e.g. when state is {@link RemoteControlClient#PLAYSTATE_ERROR}).
     */
	@Override
	public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed){
		//Called whenever the playback state has changed, and playback position and speed are known.
	}
	
	/**
     * Called whenever the playback state has changed.
     * It is called when no information is known about the playback progress in the media and
     * the playback speed.
     * @param state one of the playback states authorized
     *     in {@link RemoteControlClient#setPlaybackState(int)}.
     */
	@Override
	public void onClientPlaybackStateUpdate(int state){
		//Called whenever the playback state has changed.
	}
	
	 /**
     * Called whenever the transport control flags have changed.
     * @param transportControlFlags one of the flags authorized
     *     in {@link RemoteControlClient#setTransportControlFlags(int)}.
     */
	@Override
	public void onClientTransportControlUpdate(int transportControlFlags){
		//Called whenever the transport control flags have changed.
	}
	
	public void acquireRemoteControls(){
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mRemoteController = new RemoteController(this, this);
		mAudioManager.registerRemoteController(mRemoteController);
	}
	
	public void dropRemoteControls(boolean destroyRemoteControls) {
		if (mAudioManager != null) {
			mAudioManager.unregisterRemoteController(mRemoteController);
			if (destroyRemoteControls) mRemoteController = null;
		} else {
			Log.w(TAG, "Failed to get instance of AudioManager while adropping remote media controls");
		}
	}
	
	 class CULServiceReceiver extends BroadcastReceiver{

	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	if(intent.hasExtra("command")){
	        		 if(intent.getStringExtra("command").equals("registerRC")){
	        			acquireRemoteControls();
	        		 }
	        		 else if (intent.getStringExtra("command").equals("unregisterRC")) {
	        			 dropRemoteControls(true);
	        		 }
	        	}
	        }
	 }
}