package com.example.cameagoogleguide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.IntToString;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Service.MessageService;
import com.example.network.NetMessage;
import com.example.network.ConfigReveivePicture;
import com.example.network.TranferPicture;
import com.example.network.Tranparameter;
import com.example.save.SavePictureThread;

public class MainActivity extends Activity {

	public static final String TAG = "ERROR";
	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	public static final int MEDIA_TYPE_IMAGE = 11;
	public static final int MEDIA_TYPE_VIDEO = 12;
	// Flash
	public static final int FLASH_MODE_AUTO = 101;
	public static final int FLASH_MODE_OFF = 102;
	public static final int FLASH_MODE_ON = 100;
	public static final int FLASH_MODE_TORCH = 103;

	// Scence Mode
	public static final int SCENE_MODE_ACTION = 1;
	public static final int SCENE_MODE_AUTO = 2;
	public static final int SCENE_MODE_LANDSCAPE = 3;
	public static final int SCENE_MODE_NIGHT = 4;
	public static final int SCENE_MODE_NIGHT_PORTRAIT = 5;
	public static final int SCENE_MODE_PORTRAIT = 6;
	public static final int SCENE_MODE_SPORTS = 7;
	public static final String RESTORE = "restore";
	public static final String PARAMETER = "para";

	private int flash_auto = 0;
	private int flash_off = 0;
	private int flash_on = 0;
	private int flash_torch = 0;
	private int scene_action = 0;
	private int scene_auto = 0;
	private int scene_landscape = 0;
	private int scene_night = 0;
	private int scene_night_portrait = 0;
	private int scene_portrait = 0;
	private int scene_sport = 0;
	private int[] width = new int[10];
	private int[] height = new int[10];
	private byte[] para = new byte[31];

	private boolean isRecording = false;
	Button captureButton, setPara;
	Button captureVideo_btn;
	FrameLayout preview;

	int numberPicture = 0;

	// Relate Service
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/** Some text view we are using to show state information. */
	TextView mCallbackText;
	boolean isPause = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ----------------------------------------------------------------------------------------
		Log.e("Main", "ads");
		// Create an instance of Camera
		mCamera = getCameraInstance(this);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		setPara = (Button) findViewById(R.id.button_setPara);
		setPara.setOnClickListener(SetPara);
		// Add a listener to the Capture button
		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCamera.takePicture(null, null, mPicture);
			}
		});
		captureVideo_btn = (Button) findViewById(R.id.button_captureVideo);
		captureVideo_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isRecording) {
					// stop recording and release camera
					mMediaRecorder.stop(); // stop the recording
					releaseMediaRecorder(); // release the MediaRecorder object
					mCamera.lock(); // take camera access back from
									// MediaRecorder

					// inform the user that recording has stopped
					setCaptureButtonText("Capture", MEDIA_TYPE_VIDEO);
					isRecording = false;
				} else {
					// initialize video camera
					if (prepareVideoRecorder()) {
						// Camera is available and unlocked, MediaRecorder is
						// prepared,
						// now you can start recording
						mMediaRecorder.start();

						// inform the user that recording has started
						setCaptureButtonText("Stop", MEDIA_TYPE_VIDEO);
						isRecording = true;
					} else {
						// prepare didn't work, release the camera
						releaseMediaRecorder();
						// inform user
					}
				}
			}
		});
		// ----------------------------------------------------------------------------------------
		mCallbackText = (TextView) findViewById(R.id.tv_status_service);
		doBindService();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		Boolean save = false;
		Log.e("Main", "onRestoreInstanceState");
		if (savedInstanceState != null) {
			save = savedInstanceState.getBoolean(RESTORE, false);
		}
		if (save) {
			// Create an instance of Camera
			mCamera = getCameraInstance(this);
			// Create our Preview view and set it as the content of our
			// activity.
			mPreview = new CameraPreview(this, mCamera);
			preview.addView(mPreview);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putBoolean(RESTORE, true);
		super.onSaveInstanceState(outState);
		Log.e("Main", "onSaveInstanceState");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("Main", "onResume");
		if (isPause) {
			preview.removeAllViews();
			Log.e("Main", "isPause =true");
			mCamera = getCameraInstance(this);
			// Create our Preview view and set it as the content of our
			// activity.
			mPreview = new CameraPreview(this, mCamera);
			preview.addView(mPreview);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
		// first
		releaseCamera(); // release the camera immediately on pause event
		Log.e("Main", "pause");
		isPause = true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		doUnbindService();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
		// first
		releaseCamera(); // release the camera immediately on pause event
		Log.e("Main", "pause");
	}

	private OnClickListener SetPara = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "Click", Toast.LENGTH_SHORT)
					.show();

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageService.MSG_SET_VALUE:
				mCallbackText.setText("Received from service: " + msg.arg1);

				break;
			case MessageService.NEW_MSG_SER2ACTIVITY:
				Bundle b = msg.getData();
				int[] input = b.getIntArray(MessageService.MESSAGEToActivity);
				numberPicture++;

				mPreview.setParaTakePicture(input[MessageService.indexWith],
						input[MessageService.indexheight],
						input[MessageService.indexquality],
						input[MessageService.indexFlashMode],
						input[MessageService.indexSceneMode]);

				mCamera.takePicture(null, null, mPicture);
				Log.d("NEW_MSG_SER2ACTIVITY", "" + numberPicture);
				// if(numberPicture == input[MessageService.indexNumberPicture]
				// +1) numberPicture =0;

				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			mCallbackText.setText("Disconnected.");

			// As part of the sample, tell the user what happened.
			Toast.makeText(getApplicationContext(),
					R.string.remote_service_disconnected, Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			mCallbackText.setText("Attached.");

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						MessageService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

				// Send Message to start receiveDataThread
				msg = Message.obtain(null, MessageService.MSG_SET_VALUE,
						this.hashCode(), 0);
				Bundle b= new Bundle();
				Tranparameter tran = new Tranparameter(NetMessage.CMD_TRANFER_PARA, para);
				b.putByteArray(PARAMETER, tran.generateByteArray());
				msg.setData(b);
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			Toast.makeText(getApplicationContext(),
					R.string.remote_service_connected, Toast.LENGTH_SHORT)
					.show();
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(MainActivity.this, MessageService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		mCallbackText.setText("Binding.");
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							MessageService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			mCallbackText.setText("Unbinding.");
		}
	}

	// ==================== Các lớp, hàm phục vụ cho
	// camera========================================

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device is has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	private void setCaptureButtonText(String text, int type) {
		if (type == MEDIA_TYPE_IMAGE) {
			captureButton.setText(text);
		} else if (type == MEDIA_TYPE_VIDEO) {
			captureVideo_btn.setText(text);
		}
	}

	public static Camera getCameraInstance(Context context) {
		Camera c = null;
		Context ctx = context;
		try {
			c = Camera.open();
			if (ctx.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				// parameter.set("orientation", "portrait");
				c.setDisplayOrientation(90);
			} else {
				// parameter.set("orientation", "landscape");
				c.setDisplayOrientation(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			// Log.d("Picture send msg", "ok");
			TranferPicture tranfer = new TranferPicture(
					getApplicationContext(), NetMessage.CMD_TRANFER_PICTURE,
					data);
			byte[] dataSend = tranfer.generateByteArray();

			// Gui sang service
			Message msg = Message.obtain();
			msg.what = MessageService.MSG_SEND_DATA_SERVICE;
			Bundle bundle = new Bundle();
			bundle.putByteArray(MessageService.MESSAGE_send_data_2ser, dataSend);
			msg.setData(bundle);
			try {
				mService.send(msg);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.d("Picture send msg", e1.getMessage());
			}

			// Save picture
			SavePictureThread saveThread = new SavePictureThread(data);
			saveThread.start();
			try {
				camera.startPreview();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, " StartPreviewPIctureCallback");
			}

		}
	};

	private boolean prepareVideoRecorder() {

		mMediaRecorder = new MediaRecorder();

		// Step 1: Unclock and set camera to the MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		// Step2 :Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// step 3 : Set a CamcorderProfile (requires API Level 8 or higher)
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		// Step 4 : Set output file
		mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO)
				.toString());

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			// TODO Auto-generated constructor stub
			this.mCamera = camera;
			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				this.mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here

			// start preview with new settings
			try {
				this.mCamera.setPreviewDisplay(mHolder);
				this.mCamera.startPreview();

			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			try {
			
				
				// Thiet lap thong so
				Camera.Parameters parameter = this.mCamera.getParameters();
				parameter.setJpegQuality(100);
				parameter.setPictureFormat(ImageFormat.JPEG);				
				List<String> Flashs = parameter.getSupportedFlashModes();
				for (int i = 0; i < Flashs.size(); i++) {
					Log.e("ModeFlashSupport", Flashs.get(i));
					if (Flashs.get(i).equals("auto"))
					{
						flash_auto =1;
						para[0]= (byte)flash_auto;
					}
						
					else if (Flashs.get(i).equals("off")){
						flash_off =1;
						para[1]= (byte)flash_off;
					}
					else if (Flashs.get(i).equals("on")){
						
							flash_on =1;
							para[2]= (byte)flash_on;
					}  
					else if(Flashs.get(i).equals("torch")){						
						flash_torch =1;
						para[3]= (byte)flash_torch;
					}
									
				}
				List<String> scene = parameter.getSupportedSceneModes();
				for (int i = 0; i < scene.size(); i++) {
					Log.e("ModeSceneSupport", scene.get(i));
					if(scene.get(i).equals("action")){
						scene_action =1;
						para[4]= (byte)scene_action;
					}
					else if(scene.get(i).equals("auto")){
						scene_auto =1;
						para[5]= (byte)scene_auto;
					}
					else if(scene.get(i).equals("landscape")){
						scene_landscape =1;
						para[6]= (byte)scene_landscape;
					}
					else if(scene.get(i).equals("night")){
						scene_night=1;
						para[7]= (byte)scene_night;
					}
					else if(scene.get(i).equals("portrait")){
						scene_portrait =1;
						para[8]= (byte)scene_portrait;
					}
					else if(scene.get(i).equals("sports")){
						scene_sport =1;
						para[9]= (byte)scene_sport;
					}
					else if(scene.get(i).equals("night-portrait")){
						scene_night_portrait =1;
						para[10]= (byte)scene_night_portrait;
					}
				}			
				List<Size> sizes = parameter.getSupportedPictureSizes();
				int index = 11;
				for (int i = 0; i < sizes.size(); i++) {
					
					 Log.e("sizeSupport","size: " + sizes.get(i).height +
					 "  width: " + sizes.get(i).width);
					 width[i] = intToByteArray(sizes.get(i).width)[2];
					 para[index] = (byte)width[i];
					 index ++;
					 width[i+1] = intToByteArray(sizes.get(i).width)[3];
					 para[index] = (byte)width[i+1];
					 index ++;
					 
					 height[i] = intToByteArray(sizes.get(i).height)[2] ;
					 para[index] = (byte)height[i];
					 index ++;
					 height[i+1] = intToByteArray(sizes.get(i).height)[3] ;
					 para[index] = (byte)height[i+1];
					 index ++;
					 if(i >= 4)
						 break;
				}
				 
				
				
				this.mCamera.setParameters(parameter);

				this.mCamera.setPreviewDisplay(holder);
				this.mCamera.startPreview();

			} catch (IOException e) {
				// Log.d(TAG, "Error setting camera preview: " +
				// e.getMessage());
			}

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				this.mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here

			// start preview with new settings
			try {
				this.mCamera.setPreviewDisplay(mHolder);
				this.mCamera.startPreview();

			} catch (Exception e) {
				Log.d("Error",
						"Error starting camera preview: " + e.getMessage());
			}
		}

		/**
		 * @param width
		 * @param height
		 * @param quality
		 * @param FlashMode
		 *            : 100:on, 101: auto, 102: off, 103: torch
		 * @param ScenceMode
		 *            : 1 : 16
		 * @param numberOfPicture
		 * @param Delay
		 *            : 1 tuong ung voi 1/10(s)
		 */
		public void setParaTakePicture(int width, int height, int quality,
				int FlashMode, int ScenceMode) {
			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				Log.d(TAG, "Holder:null");
				return;
			}
			// stop preview before making changes
			try {
				this.mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}
			String flash = null;
			String Scence = null;
			switch (FlashMode) {
			case FLASH_MODE_AUTO:
				flash = Camera.Parameters.FLASH_MODE_AUTO;
				break;
			case FLASH_MODE_OFF:
				flash = Camera.Parameters.FLASH_MODE_OFF;
				break;
			case FLASH_MODE_ON:
				flash = Camera.Parameters.FLASH_MODE_ON;
				break;
			case FLASH_MODE_TORCH:
				flash = Camera.Parameters.FLASH_MODE_TORCH;
				break;
			default:
				break;
			}
			switch (ScenceMode) {
			case SCENE_MODE_ACTION:
				Scence = Camera.Parameters.SCENE_MODE_ACTION;
				break;
			case SCENE_MODE_AUTO:
				Scence = Camera.Parameters.SCENE_MODE_AUTO;
				break;
			case SCENE_MODE_LANDSCAPE:
				Scence = Camera.Parameters.SCENE_MODE_LANDSCAPE;
				break;
			case SCENE_MODE_NIGHT:
				Scence = Camera.Parameters.SCENE_MODE_NIGHT;
				break;
			case SCENE_MODE_NIGHT_PORTRAIT:
				Scence = Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT;
				break;
			case SCENE_MODE_PORTRAIT:
				Scence = Camera.Parameters.SCENE_MODE_PORTRAIT;
				break;
			case SCENE_MODE_SPORTS:
				Scence = Camera.Parameters.SCENE_MODE_SPORTS;
				break;
			default:
				break;
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here
			Camera.Parameters parameter = this.mCamera.getParameters();
			parameter.setPictureFormat(ImageFormat.JPEG);
			parameter.setJpegQuality(quality);
			parameter.setFlashMode(flash);

			parameter.setSceneMode(Scence);
			parameter.setPictureSize(width, height);

			// start preview with new settings
			try {
				this.mCamera.setParameters(parameter);

				this.mCamera.setPreviewDisplay(mHolder);
				this.mCamera.startPreview();
				Log.d("a", "success");

			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview setParaTakePicture: "
						+ e.getMessage());
			}

		}

	}

	/** Create a file Uri for saving an image or video */
	/*
	 * private static Uri getOutputMediaFileUri(int type) { return
	 * Uri.fromFile(getOutputMediaFile(type)); }
	 */

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	// ==================================================================================================
	public byte[] intToByteArray(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}
}
