package com.example.remotemediatest;

import java.util.List;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.media.RemoteController;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String NO_CLIENT = "Client state: NO CLIENT";
	protected static final String CLIENT_ACTIVE = "Client state: ACTIVE";
	private TextView mArtistTextView;
	private TextView mTitleTextView;
	private TextView mAlbumArtistTextView;
	private TextView mAlbumTextView;
	private TextView mDurationTextView;
	private TextView mFlagsTextView;
	private TextView mStateTextView;
	private ImageView mArtwork;
	private int maxWidth = 400;
	private int maxHeight = 400;
	
	
//	private RemoteMetadataProvider mProvider;
	private RemoteController mRemoteController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//finding all the necessary Views
		mArtistTextView=(TextView)findViewById(R.id.artist);
		mTitleTextView=(TextView)findViewById(R.id.title);
		mAlbumArtistTextView=(TextView)findViewById(R.id.album_artist);
		mAlbumTextView=(TextView)findViewById(R.id.album);
		mDurationTextView=(TextView)findViewById(R.id.duration);
		mArtwork=(ImageView)findViewById(R.id.bitmap);
		mFlagsTextView=(TextView)findViewById(R.id.flags);
		mStateTextView=(TextView)findViewById(R.id.state);
		
		//Acquiring instance of RemoteMetadataProvider
//		mProvider=RemoteMetadataProvider.getInstance(this);
		
		//setting up metadata listener
//		mProvider.setOnMetadataChangeListener(new OnMetadataChangeListener() {
//			@Override
//			public void onMetadataChanged(String artist, String title,
//					String album, String albumArtist, long duration) {
//				mArtistTextView.setText("ARTIST: "+artist);
//				mTitleTextView.setText("TITLE: "+title);
//				mAlbumTextView.setText("ALBUM: "+album);
//				mAlbumArtistTextView.setText("ALBUM ARTIST: "+albumArtist);
//				mDurationTextView.setText("DURATION: "+(duration/1000)+"s");
//			}
//		});
		
		//setting up KitKate metadata listener
		
		
		//setting up artwork listener
		mProvider.setOnArtworkChangeListener(new OnArtworkChangeListener() {
			@Override
			public void onArtworkChanged(Bitmap artwork) {
				mArtwork.setImageBitmap(artwork);
			}
		});
		
		//setting up remote control flags listener
		mProvider.setOnRemoteControlFeaturesChangeListener(new OnRemoteControlFeaturesChangeListener() {
			@Override
			public void onFeaturesChanged(
					List<RemoteControlFeature> usesFeatures) {
				StringBuilder builder=new StringBuilder();
				builder.append("USES FEATURES:\n");
				for(RemoteControlFeature flag:usesFeatures) {
					builder.append(flag.name());
					builder.append("\n");
				}
				mFlagsTextView.setText(builder.toString());
			}
		});
		
		//setting up playback state change listener
		mProvider.setOnPlaybackStateChangeListener(new OnPlaybackStateChangeListener() {
			@Override
			public void onPlaybackStateChanged(PlayState playbackState) {
				mStateTextView.setText("PLAYBACK STATE: "+playbackState.name());
			}
		});
		
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
				if(!mProvider.sendMediaCommand(MediaCommand.PLAY_PAUSE)) {
					Toast.makeText(getApplicationContext(), "Failed to send PLAY_PAUSE_EVENT", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.play_pause).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.PLAY_PAUSE);
				Toast.makeText(getApplicationContext(), "Broadcasted PLAY_PAUSE_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findViewById(R.id.play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.sendMediaCommand(MediaCommand.PLAY)) {
					Toast.makeText(getApplicationContext(), "Failed to send PLAY_EVENT", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.play).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.PLAY);
				Toast.makeText(getApplicationContext(), "Broadcasted PLAY_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findViewById(R.id.pause).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.sendMediaCommand(MediaCommand.PAUSE)) {
					Toast.makeText(getApplicationContext(), "Failed to send PAUSE_EVENT", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.pause).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.PAUSE);
				Toast.makeText(getApplicationContext(), "Broadcasted PAUSE_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findViewById(R.id.next).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.sendMediaCommand(MediaCommand.NEXT)) {
					Toast.makeText(getApplicationContext(), "Failed to send NEXT_EVENT", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.next).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.NEXT);
				Toast.makeText(getApplicationContext(), "Broadcasted NEXT_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findViewById(R.id.prev).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.sendMediaCommand(MediaCommand.PREVIOUS)) {
					Toast.makeText(getApplicationContext(), "Failed to send PREVIOUS_EVENT", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.prev).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.PREVIOUS);
				Toast.makeText(getApplicationContext(), "Broadcasted PREVIOUS_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findViewById(R.id.rewind).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.sendMediaCommand(MediaCommand.REWIND)) {
					Toast.makeText(getApplicationContext(), "Failed to send REWIND_EVENT", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.rewind).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.REWIND);
				Toast.makeText(getApplicationContext(), "Broadcasted REWIND_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		findViewById(R.id.ff).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.sendMediaCommand(MediaCommand.FAST_FORWARD)) {
					Toast.makeText(getApplicationContext(), "Failed to send FAST_FORWARD", Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		findViewById(R.id.ff).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mProvider.sendBroadcastMediaCommand(MediaCommand.FAST_FORWARD);
				Toast.makeText(getApplicationContext(), "Broadcasted FAST_FORWARD_EVENT", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		//button to check client status
		//this one demonstrates how to check
		//the status of client
		findViewById(R.id.check_status).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mProvider.isClientActive()) {
					Toast.makeText(getApplicationContext(), NO_CLIENT, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), CLIENT_ACTIVE, Toast.LENGTH_SHORT).show();
				}
			}	
		});
		
		//this one demonstrates how to open current player activity,
		//if there is any
		findViewById(R.id.open_player).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if(mProvider.getCurrentClientIntent()!=null) {
						startActivity(mProvider.getCurrentClientIntent());
					}
				} catch (NameNotFoundException e) {
					Toast.makeText(getApplicationContext(), "Failed to find client package!", Toast.LENGTH_SHORT).show();
				}
			}	
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		//acquiring remote media controls
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2){
			mProvider.acquireRemoteControls();
		} else {
			mProvider.acquireRemoteControls(maxWidth, maxHeight);
		}
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		//dropping remote media controls
		mProvider.dropRemoteControls(true);
	}
	
	
}
