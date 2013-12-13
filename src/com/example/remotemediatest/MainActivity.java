package com.example.remotemediatest;

import java.util.List;

import org.electricwisdom.unifiedremotemetadataprovider.media.RemoteMetadataProvider;
import org.electricwisdom.unifiedremotemetadataprovider.media.enums.MediaCommand;
import org.electricwisdom.unifiedremotemetadataprovider.media.enums.PlayState;
import org.electricwisdom.unifiedremotemetadataprovider.media.enums.RemoteControlFeature;
import org.electricwisdom.unifiedremotemetadataprovider.media.listeners.OnArtworkChangeListener;
import org.electricwisdom.unifiedremotemetadataprovider.media.listeners.OnMetadataChangeListener;
import org.electricwisdom.unifiedremotemetadataprovider.media.listeners.OnPlaybackStateChangeListener;
import org.electricwisdom.unifiedremotemetadataprovider.media.listeners.OnRemoteControlFeaturesChangeListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private static final String NO_CLIENT = "Client state: NO CLIENT";
	protected static final String CLIENT_ACTIVE = "Client state: ACTIVE";
	private static final String TAG = "RemoteMediaTest";
	private TextView mArtistTextView;
	private TextView mTitleTextView;
	private TextView mAlbumArtistTextView;
	private TextView mAlbumTextView;
	private TextView mDurationTextView;
	private TextView mFlagsTextView;
	private TextView mStateTextView;
	private ImageButton mPlaybackButton;
	private boolean isPlaying = false;
	private ImageView mArtwork;
	private NotificationReceiver nReceiver;
	private int maxWidth = 300;
	private int maxHeight = 300;
	
	
	private RemoteMetadataProvider mProvider;
	private RemoteController mRemoteController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.remotemediatest.METADATA_YAY");
        registerReceiver(nReceiver,filter);
		
		//finding all the necessary Views
		mArtistTextView=(TextView)findViewById(R.id.artist);
		mTitleTextView=(TextView)findViewById(R.id.title);
		mAlbumArtistTextView=(TextView)findViewById(R.id.album_artist);
		mAlbumTextView=(TextView)findViewById(R.id.album);
		mDurationTextView=(TextView)findViewById(R.id.duration);
		mArtwork=(ImageView)findViewById(R.id.bitmap);
		mFlagsTextView=(TextView)findViewById(R.id.flags);
		mStateTextView=(TextView)findViewById(R.id.state);
		mPlaybackButton=(ImageButton)findViewById(R.id.play_pause);
		
		
		
		
		//setting up KitKate metadata listener
		
		
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				//Acquiring instance of RemoteMetadataProvider
				mProvider = RemoteMetadataProvider.getInstance(this);
				//setting up metadata listener
				mProvider
						.setOnMetadataChangeListener(new OnMetadataChangeListener() {
							@Override
							public void onMetadataChanged(String artist,
									String title, String album,
									String albumArtist, long duration) {
								mArtistTextView.setText("ARTIST: " + artist);
								mTitleTextView.setText("TITLE: " + title);
								mAlbumTextView.setText("ALBUM: " + album);
								mAlbumArtistTextView.setText("ALBUM ARTIST: "
										+ albumArtist);
								mDurationTextView.setText("DURATION: "
										+ (duration / 1000) + "s");
							}
						});
	
				//setting up artwork listener
				mProvider
						.setOnArtworkChangeListener(new OnArtworkChangeListener() {
							@Override
							public void onArtworkChanged(Bitmap artwork) {
								mArtwork.setImageBitmap(artwork);
							}
						});
				

				//setting up remote control flags listener
				mProvider
						.setOnRemoteControlFeaturesChangeListener(new OnRemoteControlFeaturesChangeListener() {
							@Override
							public void onFeaturesChanged(
									List<RemoteControlFeature> usesFeatures) {
								StringBuilder builder = new StringBuilder();
								builder.append("USES FEATURES:\n");
								for (RemoteControlFeature flag : usesFeatures) {
									builder.append(flag.name());
									builder.append("\n");
								}
								mFlagsTextView.setText(builder.toString());
							}
						});
			
			
				//setting up playback state change listener
				mProvider
						.setOnPlaybackStateChangeListener(new OnPlaybackStateChangeListener() {
							@Override
							public void onPlaybackStateChanged(
									PlayState playbackState) {
								mStateTextView.setText("PLAYBACK STATE: "
										+ playbackState.name());
							}
						});
			
		}
		
		
		
		//now setting up listeners for remote media control buttons
		//we check the return of the RemoteMetadataProvider#sendMediaCommand because
		//it will return true in case media input event was delivered to destination,
		//and it will return false in case the delivery failed.
		
		//Also it assigns long-click listeners. To send broadcast-type media event, long click the button.
		
		//Also, about multiple listeners. I know it's a bad practice, but here they are just 
		//to isolate actions, so you can get a better grasp on how my library works.
		findViewById(R.id.play_pause).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
					if(!mProvider.sendMediaCommand(MediaCommand.PLAY_PAUSE)) {
						Toast.makeText(getApplicationContext(), "Failed to send PLAY_PAUSE_EVENT", Toast.LENGTH_SHORT).show();
					}
				} else {
					Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
					i.putExtra("mediacommand", KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
					sendBroadcast(i);
				}	
				if (isPlaying){
					v.setBackgroundResource(R.drawable.pause);
				} else {
					v.setBackgroundResource(R.drawable.play);
				}
			}
		});
		
		
//		findViewById(R.id.play_pause).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.PLAY_PAUSE);
//				Toast.makeText(getApplicationContext(), "Broadcasted PLAY_PAUSE_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
//		findViewById(R.id.play).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
//					if(!mProvider.sendMediaCommand(MediaCommand.PLAY)) {
//						Toast.makeText(getApplicationContext(), "Failed to send PLAY_EVENT", Toast.LENGTH_SHORT).show();
//					}
//				} else {
//					Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
//					i.putExtra("mediacommand", KeyEvent.KEYCODE_MEDIA_PLAY);
//					sendBroadcast(i);
//				}
//				
//			}	
//		});
		
//		findViewById(R.id.play).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.PLAY);
//				Toast.makeText(getApplicationContext(), "Broadcasted PLAY_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
//		findViewById(R.id.pause).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
//					if(!mProvider.sendMediaCommand(MediaCommand.PAUSE)) {
//						Toast.makeText(getApplicationContext(), "Failed to send PAUSE_EVENT", Toast.LENGTH_SHORT).show();
//					}
//				} else {
//					Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
//					i.putExtra("mediacommand", KeyEvent.KEYCODE_MEDIA_PAUSE);
//					sendBroadcast(i);
//				}
//			}	
//		});
		
//		findViewById(R.id.pause).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.PAUSE);
//				Toast.makeText(getApplicationContext(), "Broadcasted PAUSE_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
		findViewById(R.id.next).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
					if(!mProvider.sendMediaCommand(MediaCommand.NEXT)) {
						Toast.makeText(getApplicationContext(), "Failed to send NEXT_EVENT", Toast.LENGTH_SHORT).show();
					}
				} else {
					Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
					i.putExtra("mediacommand", KeyEvent.KEYCODE_MEDIA_NEXT);
					sendBroadcast(i);
				}
			}	
		});
		
//		findViewById(R.id.next).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.NEXT);
//				Toast.makeText(getApplicationContext(), "Broadcasted NEXT_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
		findViewById(R.id.prev).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
					if(!mProvider.sendMediaCommand(MediaCommand.PREVIOUS)) {
						Toast.makeText(getApplicationContext(), "Failed to send PREVIOUS_EVENT", Toast.LENGTH_SHORT).show();
					}
				} else {
					Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
					i.putExtra("mediacommand", KeyEvent.KEYCODE_MEDIA_PREVIOUS);
					sendBroadcast(i);
				}
			}	
		});
		
//		findViewById(R.id.prev).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.PREVIOUS);
//				Toast.makeText(getApplicationContext(), "Broadcasted PREVIOUS_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
//		findViewById(R.id.rewind).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(!mProvider.sendMediaCommand(MediaCommand.REWIND)) {
//					Toast.makeText(getApplicationContext(), "Failed to send REWIND_EVENT", Toast.LENGTH_SHORT).show();
//				}
//			}	
//		});
		
//		findViewById(R.id.rewind).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.REWIND);
//				Toast.makeText(getApplicationContext(), "Broadcasted REWIND_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
//		findViewById(R.id.ff).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(!mProvider.sendMediaCommand(MediaCommand.FAST_FORWARD)) {
//					Toast.makeText(getApplicationContext(), "Failed to send FAST_FORWARD", Toast.LENGTH_SHORT).show();
//				}
//			}	
//		});
		
//		findViewById(R.id.ff).setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				mProvider.sendBroadcastMediaCommand(MediaCommand.FAST_FORWARD);
//				Toast.makeText(getApplicationContext(), "Broadcasted FAST_FORWARD_EVENT", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//		});
		
		//button to check client status
		//this one demonstrates how to check
		//the status of client
//		findViewById(R.id.check_status).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(!mProvider.isClientActive()) {
//					Toast.makeText(getApplicationContext(), NO_CLIENT, Toast.LENGTH_SHORT).show();
//				} else {
//					Toast.makeText(getApplicationContext(), CLIENT_ACTIVE, Toast.LENGTH_SHORT).show();
//				}
//			}	
//		});
		
		//this one demonstrates how to open current player activity,
		//if there is any
//		findViewById(R.id.open_player).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				try {
//					if(mProvider.getCurrentClientIntent()!=null) {
//						startActivity(mProvider.getCurrentClientIntent());
//					}
//				} catch (NameNotFoundException e) {
//					Toast.makeText(getApplicationContext(), "Failed to find client package!", Toast.LENGTH_SHORT).show();
//				}
//			}	
//		});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(nReceiver);
		Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
		i.putExtra("command", "unregisterRC");
		sendBroadcast(i);
	}
	@Override
	public void onResume() {
		super.onResume();
		
		//acquiring remote media controls
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2){
			mProvider.acquireRemoteControls();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			mProvider.acquireRemoteControls(maxWidth, maxHeight);
		} else {
			//KitKat
			Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
			i.putExtra("command", "registerRC");
			sendBroadcast(i);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			//dropping remote media controls
			mProvider.dropRemoteControls(true);
		}
		//Don't think this is necessary on kitkat, moved to onDestroy() instead
		//KitKat
//		Intent i = new Intent("com.example.remotemediatest.REMOTE_CONTROLLER_COMMANDS");
//		i.putExtra("command", "unregisterRC");
//		sendBroadcast(i);
	}
	
	class NotificationReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("toast")) {
				Toast.makeText(getApplicationContext(), intent.getStringExtra("toast"), Toast.LENGTH_SHORT).show();
			}
			if (intent.hasExtra("playback_update")) {
				if ((intent.getIntExtra("playback_update", 1)) != RemoteControlClient.PLAYSTATE_PLAYING) {
					//mPlaybackButton.setBackgroundResource(R.drawable.play);
					isPlaying = false;
				} else {
					//mPlaybackButton.setBackgroundResource(R.drawable.pause);
					isPlaying = true;
				}
			}
			mArtistTextView.setText("ARTIST: " + intent.getStringExtra("Song_Artist"));
			mTitleTextView.setText("TITLE: " + intent.getStringExtra("Song_Title"));
			mAlbumTextView.setText("ALBUM: " + intent.getStringExtra("Album_Title"));
			mAlbumArtistTextView.setText("ALBUM ARTIST: " + intent.getStringExtra("Album_Artist"));
			mDurationTextView.setText("DURATION: " + intent.getLongExtra("Song_Duration", 11));
			Bitmap coverArt = intent.getParcelableExtra("Cover_Art");
			mArtwork.setImageBitmap(coverArt);
		}
	}
	
	
}
