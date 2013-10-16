package com.example.cameagoogleguide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
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
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.NetWork.NetMessage;
import com.example.NetWork.ProcessPara;
import com.example.NetWork.RequestSendPara;
import com.example.NetWork.RequestUavTranferPictue;
import com.example.Service.MessageService;
import com.example.Service.ReceiveDataThread;
import com.example.cameraGround.R;

public class MainActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {

	public static final String TAG = "MainActivity";

	public static final int MEDIA_TYPE_IMAGE = 11;
	public static final int MEDIA_TYPE_VIDEO = 12;
	// Flash
	public static final int FLASH_MODE_AUTO = 101;
	public static final int FLASH_MODE_OFF = 102;
	public static final int FLASH_MODE_ON = 100;
	public static final int FLASH_MODE_TORCH = 103;
	public static final String FLASH_AUTO = "flash_auto";
	public static final String FLASH_OFF = "flash_off";
	public static final String FLASH_ON = "flash_on";
	public static final String FLASH_TORCH = "flash_torch";

	// Scence Mode
	public static final int SCENE_MODE_ACTION = 1;
	public static final int SCENE_MODE_AUTO = 2;
	public static final int SCENE_MODE_LANDSCAPE = 3;
	public static final int SCENE_MODE_NIGHT = 4;
	public static final int SCENE_MODE_NIGHT_PORTRAIT = 5;
	public static final int SCENE_MODE_PORTRAIT = 6;
	public static final int SCENE_MODE_SPORTS = 7;
	public static final String SCENE_ACTION = "scene_action";
	public static final String SCENE_AUTO = "scene_auto";
	public static final String SCENE_LANDSCAPE = "scene_landscape";
	public static final String SCENE_NIGHT = "scene_night";
	public static final String SCENE_NIGHT_PORTRAIT = "scene_night_portrait";
	public static final String SCENE_PORTRAIT = "scene_portrait";
	public static final String SCENE_SPORTS = "scene_sports";

	// Relate Service
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/** Some text view we are using to show state information. */

	Button StartSend, btnConnect, getPara;
	EditText edtNumPicture, edtInterval, edtIP;

	SeekBar seekbarQuality;
	TextView valueSeekbar, numOfPicture;

	Spinner spnSize, spnFlash, spnScene;
	private int valueQuality = 100;
	private int sceneMode = 2;
	private int flashMode = 101;
	private int[] sizePicture = new int[4];

	public final static String action_disconnect = "hieu.action.Disconnect";
	public final static String action_numberPicture = "hieu.action.numberPicture";
	public final static String action_connect_success = "hieu.action.connectSucess";
	public final static String action_get_para = "hieu.action.getParameter";

	public final static String GET_PARA = "get parameter";
	AlarmBroadcast mReceiver;

	ArrayAdapter<String> adapterSize;
	ArrayAdapter<String> adapterScene;
	ArrayAdapter<String> adapterFlash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ----------------------------------------------------------------------------------------

		edtNumPicture = (EditText) findViewById(R.id.activity_main_edtNum);
		// Thiet lap gia tri min la 1, max la 255
		edtNumPicture.setFilters(new InputFilter[] { new InputFilterMinMax(1,
				255) });
		edtInterval = (EditText) findViewById(R.id.activity_main_edtDelay);
		// Thiet lap gia tri min la 2, max la 255
		edtInterval
				.setFilters(new InputFilter[] { new InputFilterMinMax(2, 255) });
		edtIP = (EditText) findViewById(R.id.activity_main_edtIP);
		edtIP.setText("192.168.1.117");
		// Display so anh da nhan
		numOfPicture = (TextView) findViewById(R.id.activity_main_tv_num);

		StartSend = (Button) findViewById(R.id.btn_Send);
		StartSend.setOnClickListener(this);
		StartSend.setEnabled(false);
		btnConnect = (Button) findViewById(R.id.btn_connect);
		btnConnect.setOnClickListener(this);
		getPara = (Button) findViewById(R.id.activity_main_getPara);
		getPara.setOnClickListener(this);
		getPara.setEnabled(false);

		seekbarQuality = (SeekBar) findViewById(R.id.activity_main_seekbarQuality);
		seekbarQuality.setOnSeekBarChangeListener(SeekBarchange);
		valueSeekbar = (TextView) findViewById(R.id.activity_main_valueSeekbar);

		ArrayList<String> itemsSize = new ArrayList<String>();
		// Spinner Size
		spnSize = (Spinner) findViewById(R.id.activity_main_spSize);
		spnSize.setOnItemSelectedListener(this);
		adapterSize = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, itemsSize);
		adapterSize.setNotifyOnChange(true);
		adapterSize
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnSize.setAdapter(adapterSize);

		// Spinner Scene
		ArrayList<String> itemsScene = new ArrayList<String>();
		spnScene = (Spinner) findViewById(R.id.activity_main_spScene);
		spnScene.setOnItemSelectedListener(this);
		adapterScene = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, itemsScene);
		adapterScene.setNotifyOnChange(true);
		adapterScene
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnScene.setAdapter(adapterScene);
		// Spinner Flash
		ArrayList<String> itemsFlash = new ArrayList<String>();
		spnFlash = (Spinner) findViewById(R.id.activity_main_spFlash);
		spnFlash.setOnItemSelectedListener(this);

		adapterFlash = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, itemsFlash);
		adapterFlash.setNotifyOnChange(true);

		adapterFlash
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnFlash.setAdapter(adapterFlash);

		// bind service
		doBindService();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// set broadcast receiver
		mReceiver = new AlarmBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(action_disconnect);
		filter.addAction(action_numberPicture);
		filter.addAction(action_connect_success);
		filter.addAction(action_get_para);
		this.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(mReceiver);
	}

	public class AlarmBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			int num;
			if (intent.getAction().equals(action_disconnect)) {
				showDialog("Mat ket noi");

			} else if (intent.getAction().equals(action_numberPicture)) {
				num = intent.getIntExtra(ReceiveDataThread.ACTION_NUMBER, 0);
				Toast.makeText(getApplicationContext(), "" + num,
						Toast.LENGTH_LONG).show();
				numOfPicture.setText("" + num);
			} else if (intent.getAction().equals(action_connect_success)) {
				Toast.makeText(getApplicationContext(), "Ket noi thanh cong",
						Toast.LENGTH_LONG).show();
			} else if (intent.getAction().equals(action_get_para)) {
				byte para[] = intent.getByteArrayExtra(GET_PARA);
				ProcessPara processPara = new ProcessPara(
						NetMessage.CMD_TRANFER_PARA);
				processPara.parse(para);
				AddItemSpinner(processPara);
			}
		}

	}

	private OnSeekBarChangeListener SeekBarchange = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			valueSeekbar.setText("Quality is:" + progress);
			valueQuality = progress;
		}
	};

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {

		case R.id.activity_main_spSize:
			String size = (String) parent.getItemAtPosition(pos);
			sizePicture = getSizeFrSpiner(size);
			break;
		case R.id.activity_main_spScene:
//			sceneMode = getModeScene(pos);
			String sceneModeString = (String) parent.getItemAtPosition(pos);
			if(sceneModeString.equals(SCENE_ACTION))sceneMode = SCENE_MODE_ACTION;
			else if(sceneModeString.equals(SCENE_AUTO))sceneMode = SCENE_MODE_AUTO;
			else if(sceneModeString.equals(SCENE_LANDSCAPE))sceneMode = SCENE_MODE_LANDSCAPE;
			else if(sceneModeString.equals(SCENE_NIGHT))sceneMode = SCENE_MODE_NIGHT;
			else if(sceneModeString.equals(SCENE_NIGHT_PORTRAIT))sceneMode = SCENE_MODE_NIGHT_PORTRAIT;
			else if(sceneModeString.equals(SCENE_PORTRAIT))sceneMode = SCENE_MODE_PORTRAIT;
			else if(sceneModeString.equals(SCENE_SPORTS))sceneMode = SCENE_MODE_SPORTS;
			break;
		case R.id.activity_main_spFlash:
//			flashMode = getModeFlash(pos);
//			Toast.makeText(getApplicationContext(), "" + flashMode,
//					Toast.LENGTH_SHORT).show();
			String flashModeString = (String) parent.getItemAtPosition(pos);
			if(flashModeString.equals(FLASH_AUTO))flashMode = FLASH_MODE_AUTO;
			if(flashModeString.equals(FLASH_ON))flashMode = FLASH_MODE_ON;
			if(flashModeString.equals(FLASH_OFF))flashMode = FLASH_MODE_OFF;
			if(flashModeString.equals(FLASH_TORCH))flashMode = FLASH_MODE_TORCH;
			break;
		default:
			break;
		}
	}

	private void AddItemSpinner(ProcessPara a) {
		int[] widthArray;
		int[] heightArray;
		String width,height;
		String size;
		if (a.getFlashAuto() == 1)
			adapterFlash.add(FLASH_AUTO);
		if (a.getFlashOff() == 1)
			adapterFlash.add(FLASH_OFF);
		if (a.getFlashON() == 1)
			adapterFlash.add(FLASH_ON);
		if (a.getFlashTorch() == 1)
			adapterFlash.add(FLASH_TORCH);
		if (a.getSceneAction() == 1)
			adapterScene.add(SCENE_ACTION);
		if (a.getSceneAuto() == 1)
			adapterScene.add(SCENE_AUTO);
		if (a.getSceneLandscape() == 1)
			adapterScene.add(SCENE_LANDSCAPE);
		if (a.getSceneNight() == 1)
			adapterScene.add(SCENE_NIGHT);
		if (a.getSceneNightPortrait() == 1)
			adapterScene.add(SCENE_NIGHT_PORTRAIT);
		if (a.getScenePortrait() == 1)
			adapterScene.add(SCENE_PORTRAIT);
		if (a.getSceneSport() == 1)
			adapterScene.add(SCENE_SPORTS);
		widthArray = a.getWidth();
		heightArray = a.getHeight();
		for(int i=0;i<5;i++){
		size = String.valueOf(widthArray[i])+ "x" + String.valueOf(heightArray[i]);
		adapterSize.add(size);
		}
		

	}

	private int[] getSizeFrSpiner(String posSize) {
		int[] size = new int[4];
		int with, height;
		String[] sizeString = posSize.split("x");
		// with
		with = Integer.valueOf(sizeString[0]);
		size[0] = with / 100;
		size[1] = with % 100;
		// height
		height = Integer.valueOf(sizeString[1]);
		size[2] = height / 100;
		size[3] = height % 100;
		return size;
	}

//	private int getModeScene(int pos) {
//		switch (pos) {
//		case 0:
//			return SCENE_MODE_AUTO;
//		case 1:
//			return SCENE_MODE_ACTION;
//		case 2:
//			return SCENE_MODE_LANDSCAPE;
//		case 3:
//			return SCENE_MODE_NIGHT;
//		case 4:
//			return SCENE_MODE_NIGHT_PORTRAIT;
//		case 5:
//			return SCENE_MODE_PORTRAIT;
//		case 6:
//			return SCENE_MODE_SPORTS;
//
//		default:
//			return SCENE_MODE_AUTO;
//		}
//	}

//	private int getModeFlash(int pos) {
//		switch (pos) {
//		case 0:
//			return FLASH_MODE_AUTO;
//		case 1:
//			return FLASH_MODE_OFF;
//		case 2:
//			return FLASH_MODE_ON;
//		case 3:
//			return FLASH_MODE_TORCH;
//
//		default:
//			return FLASH_MODE_AUTO;
//		}
//	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_connect:
			if (!edtIP.getText().toString().equals("")) {
				// Ket noi socket toi UAV
				Message msgConnect = Message.obtain();
				msgConnect.what = MessageService.MSG_CONNECT;
				Bundle bConnect = new Bundle();
				bConnect.putString(MessageService.MESSAGE2Service, edtIP
						.getText().toString());
				msgConnect.setData(bConnect);
				try {
					mService.send(msgConnect);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Enable button Startsend and getPara
				StartSend.setEnabled(true);
				getPara.setEnabled(true);
			} else {
				// Thong bao la phai nhap IP
				showDialog("Please enter IP address");
			}
			break;

		case R.id.btn_Send:
			if ((!edtNumPicture.getText().toString().equals(""))
					&& (!edtInterval.getText().toString().equals(""))) {
				int numberPicture = Integer.valueOf(edtNumPicture.getText()
						.toString());
				int interval = Integer
						.valueOf(edtInterval.getText().toString());
				RequestUavTranferPictue requestUAVTranPicture = new RequestUavTranferPictue(
						sizePicture, valueQuality, flashMode, sceneMode,
						numberPicture, interval);
				Message msg = Message.obtain();
				msg.what = MessageService.MSG_SEND_REQUEST;
				Bundle b = new Bundle();
				b.putByteArray(MessageService.MESSAGE2Service,
						requestUAVTranPicture.generateToByte());
				msg.setData(b);
				try {
					mService.send(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// thong bao la nhap day du thong tin
				showDialog("Please enter number of picture or interval");
			}
			break;
		case R.id.activity_main_getPara:
			RequestSendPara requestSendData = new RequestSendPara(
					NetMessage.CMD_REQUEST_PARA);
			Message msg_getPara = Message.obtain();
			msg_getPara.what = MessageService.MSG_SEND_REQUEST;
			Bundle b = new Bundle();
			b.putByteArray(MessageService.MESSAGE2Service,
					requestSendData.generateToByte());
			msg_getPara.setData(b);
			try {
				mService.send(msg_getPara);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		doUnbindService();
	}

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageService.MSG_DATA_FR_SERVICE_TO_ACTIVITY:
				// mCallbackText.setText("Received from service: " + msg.arg1);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void showDialog(String msg) {
		AlertDialog.Builder ErrorDialog = new AlertDialog.Builder(
				MainActivity.this);
		ErrorDialog.setTitle("Error");
		ErrorDialog.setMessage(msg);
		ErrorDialog.setNeutralButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		ErrorDialog.show();
	}

	// ============================ Methods relate Service
	// ==========================================================================================//
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
			// mCallbackText.setText("Disconnected.");

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
			// mCallbackText.setText("Attached.");

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						MessageService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

				// Give it some value as an example.
				/*
				 * msg = Message.obtain(null, MessageService.MSG_SEND_DATA,
				 * this.hashCode(), 0); mService.send(msg);
				 */
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
		// mCallbackText.setText("Binding.");
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
			// mCallbackText.setText("Unbinding.");
		}
	}
	// ==============================================================================================
	
}
