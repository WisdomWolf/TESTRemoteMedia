package com.example.remotemediatest;





import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.RemoteController.OnClientUpdateListener;
import android.os.Build;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.KeyEvent;
import android.media.RemoteControllerHelper;


@SuppressLint("NewApi")
public class CUListenerService extends NotificationListenerService {
	
	public RemoteController mRemoteController;
	public RemoteController.OnClientUpdateListener mClientUpdateListener;
	private AudioManager mAudioManager;
	private CULServiceReceiver cuservicereceiver;
	private static final String TAG = "CUListenerService";
	private Context mContext;
	private RemoteControllerHelper mRCHelper;
	
	@Override
	public void onCreate() {
		super.onCreate();
		cuservicereceiver = new CULServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
		registerReceiver(cuservicereceiver,filter);
		mContext = getApplicationContext();
		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mClientUpdateListener = new OnClientUpdateListener() {

						/**
						 * Called whenever all information, previously received through the other
						 * methods of the listener, is no longer valid and is about to be refreshed.
						 * This is typically called whenever a new {@link RemoteControlClient} has been selected
						 * by the system to have its media information published.
						 * @param clearing true if there is no selected RemoteControlClient and no information
						 *     is available.
						 */
						@Override
						public void onClientChange(boolean clearing) {
							//Called whenever all information, previously received through the other methods of the listener, is no longer valid and is about to be refreshed.
							Log.d(TAG, "Client Change triggered. Clearing = "
									+ clearing);
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
						public void onClientMetadataUpdate(
								RemoteController.MetadataEditor metadataEditor) {
							//Called whenever new metadata is available.
							Log.d(TAG, "MetadataUpdate received.");
							String songArtist = metadataEditor.getString(
									MediaMetadataRetriever.METADATA_KEY_ARTIST,
									"");
							String songTitle = metadataEditor.getString(
									MediaMetadataRetriever.METADATA_KEY_TITLE,
									"");
							String albumArtist = metadataEditor
									.getString(
											MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
											"");
							String albumTitle = metadataEditor.getString(
									MediaMetadataRetriever.METADATA_KEY_ALBUM,
									"");
							long songDuration = metadataEditor
									.getLong(
											MediaMetadataRetriever.METADATA_KEY_DURATION,
											10);
							Bitmap coverArt = metadataEditor.getBitmap(
									MediaMetadataEditor.BITMAP_KEY_ARTWORK,
									null);
							//Must remain commented out for AIDE compatibility & stuff
							//String remoteControlAppName = mRemoteController.getRemoteControlClientPackageName(); //requires hidden API
							Intent i = new Intent(
									"com.example.remotemediatest.METADATA_YAY");
							i.putExtra("metadata", true);
							i.putExtra("Song_Artist", songArtist);
							i.putExtra("Song_Title", songTitle);
							i.putExtra("Album_Artist", albumArtist);
							i.putExtra("Album_Title", albumTitle);
							i.putExtra("Song_Duration", songDuration);
							i.putExtra("Cover_Art", coverArt);
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
						public void onClientPlaybackStateUpdate(int state,
								long stateChangeTimeMs, long currentPosMs,
								float speed) {
							//Called whenever the playback state has changed, and playback position and speed are known.
							Log.d(TAG, "PlaybackState Updated with all info "
									+ state + " " + stateChangeTimeMs + " "
									+ currentPosMs + " " + speed);
							Intent i = new Intent(
									"com.example.remotemediatest.METADATA_YAY");
							i.putExtra("playback_update", state);
							sendBroadcast(i);
						}

						/**
						 * Called whenever the playback state has changed.
						 * It is called when no information is known about the playback progress in the media and
						 * the playback speed.
						 * @param state one of the playback states authorized
						 *     in {@link RemoteControlClient#setPlaybackState(int)}.
						 */
						@Override
						public void onClientPlaybackStateUpdate(int state) {
							//Called whenever the playback state has changed.
							Log.d(TAG, "PlaybackState Updated " + state + ".");
							Intent i = new Intent(
									"com.example.remotemediatest.METADATA_YAY");
							i.putExtra("playback_update", state);
							sendBroadcast(i);
						}

						/**
						 * Called whenever the transport control flags have changed.
						 * @param transportControlFlags one of the flags authorized
						 *     in {@link RemoteControlClient#setTransportControlFlags(int)}.
						 */
						@Override
						public void onClientTransportControlUpdate(
								int transportControlFlags) {
							//Called whenever the transport control flags have changed.
							Log.d(TAG, "TransportControlUpdate: "
									+ transportControlFlags);
						}
					};
		}
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
	
	public void acquireRemoteControls(){
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		Log.d(TAG, "AudioManager Set");
		mRemoteController = new RemoteController(mContext, mClientUpdateListener);
		mRCHelper = new RemoteControllerHelper(mRemoteController, mContext);
		Log.d(TAG, "RemoteController instantiated");
		if (!mRemoteController.setArtworkConfiguration(300, 300)){
			Log.d(TAG, "Set ArtworkConfiguration failed.");
		}
		if (!mAudioManager.registerRemoteController(mRemoteController)){
			Log.d(TAG, "RemoteController registration via AudioManager failed.");
			if (mRCHelper.registerRemoteController(mRemoteController)){
				Log.d(TAG, "RemoteController successfully registered via helper.");
			} else {
				Log.d(TAG, "RemoteController registration failed permanently.");
			}
		}
	}

	public void dropRemoteControls(boolean destroyRemoteControls) {
		if (mAudioManager != null) {
			mAudioManager.unregisterRemoteController(mRemoteController);
			if (destroyRemoteControls) mRemoteController = null;
		} else {
			Log.w(TAG, "Failed to get instance of AudioManager while dropping remote media controls");
		}
	}
	class CULServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("command")){
				if(intent.getStringExtra("command").equals("registerRC")){
					acquireRemoteControls();
				}
				else if (intent.getStringExtra("command").equals("unregisterRC")) {
					dropRemoteControls(true);
				}
			}
			if (intent.hasExtra("mediacommand")){
				int keycode = intent.getIntExtra("mediacommand", 0);
				mRemoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
				mRemoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keycode));
			}
		}
	}
}

	
	
	
	
