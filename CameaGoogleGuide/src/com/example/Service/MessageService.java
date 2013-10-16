package com.example.Service;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.example.cameagoogleguide.MainActivity;
import com.example.cameagoogleguide.R;
import com.example.network.NetMessage;
import com.example.network.ConfigReveivePicture;

public class MessageService extends Service {

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_VALUE = 3;
	public static final int RECEI2SER_NEW_MSG = 4;
	public static final int SER2TRAN_NEW_MSG = 5;
	public static final int ACTIVITI2SER_NEW_MSG = 6;
	public static final int NEW_MSG_SER2ACTIVITY = 7;
	public static final int MSG_SEND_DATA_SERVICE = 8;
	public static final int SER2TRAN_MSG_PARA = 10;

	public static final String MESSAGE = "message";
	public static final String MESSAGE_send_data_2ser = "message to service";
	public static final String MESSAGEToThread = "messagetoThread";
	public static final String MESSAGEToActivity = "messagetoActivity";

	public static final int indexWith = 0;
	public static final int indexheight = 1;
	public static final int indexquality = 2;
	public static final int indexFlashMode = 3;
	public static final int indexSceneMode = 4;
	public static final int indexNumberPicture = 5;

	byte para[]; // mang de luu tru tham so cau hinh
	Timer mTimer = null;

	/** Keeps track of all current registered clients. */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	/** Holds last value set by a client. */

	public Handler mHandler;
	ReceiveDataThread receiveDataThread = null;
	TransmitThread transmitThread = null;

	// int mValue = 0;
	Boolean isCDTimerLive = false;

	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_VALUE:
				receiveDataThread = new ReceiveDataThread(mHandler);
				receiveDataThread.start();
				Bundle bPara = new Bundle();
				bPara = msg.getData();
				para = bPara.getByteArray(MainActivity.PARAMETER);				
				break;
			case RECEI2SER_NEW_MSG:
				System.out.print("RECEI2SER_NEW_MSG /n");
				Bundle b = new Bundle();
				b = msg.getData();
				byte msgReceive[] = b.getByteArray(MESSAGE);
				ConfigReveivePicture ReceivePicture = new ConfigReveivePicture(
						NetMessage.CMD_RECEIVE_PICTURE);
				System.out.print("\n cmd_id: " + NetMessage.getCmd_id(msgReceive) );
				switch (NetMessage.getCmd_id(msgReceive)) {
				case NetMessage.CMD_RECEIVE_PICTURE:
					System.out.print("\n switch");
					tranfer2Timer(msgReceive, ReceivePicture);
					break;
				case NetMessage.CMD_REQUEST_PARA:
					Message msgSendPara = Message.obtain();
					msgSendPara.what = MessageService.SER2TRAN_MSG_PARA;
					Bundle bSendPara = new Bundle();
					bSendPara.putByteArray(MainActivity.PARAMETER, para);
					msgSendPara.setData(bSendPara);
					sendMessageToThreadTranfer(msgSendPara);
				default:
					break;
				}

				break;
			case MSG_SEND_DATA_SERVICE:
				Bundle dataBundle = new Bundle();
				dataBundle = msg.getData();
				Message msgToThreadTranfer = Message.obtain();
				msgToThreadTranfer.what = MessageService.SER2TRAN_NEW_MSG;
				msgToThreadTranfer.setData(dataBundle);
				sendMessageToThreadTranfer(msgToThreadTranfer);
				Log.d("MSG_SEND_DATA_SERVICE", "chuan");
				break;
			case ACTIVITI2SER_NEW_MSG:
			
				break;
			default:
				super.handleMessage(msg);
			}
		}

	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(mHandler =new IncomingHandler());

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
//		mHandler = new IncomingHandler();
		isCDTimerLive = false;
		transmitThread = new TransmitThread();
		transmitThread.start();
		Toast.makeText(this, R.string.remote_service_started,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// Dung luong lam viec
		ReceiveDataThread.End();
		TransmitThread.End();
		
	
		Toast.makeText(this, R.string.remote_service_stopped,
				Toast.LENGTH_SHORT).show();
	}

	private void sendMessageToThreadTranfer(Message msg) {
		transmitThread.getHandler().sendMessage(msg);
	}

	public void startTimer(final int numberPicture, int delay,
			final ConfigReveivePicture ReceivePicture) {
		System.out.print("\n startTimer");
		long miliInterval = delay * 100; // Doi ra milis
		
		isCDTimerLive = true;
		mTimer = new Timer();

		mTimer.scheduleAtFixedRate(new TimerTask() {
			int num = 1;

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (num <= numberPicture) {
					sendMsg2Activity(ReceivePicture);
					num++;
					Log.d("mTimer", "mtimer");
				}
			}
		}, 0, miliInterval);
		
	}

	public void sendMsg2Activity(ConfigReveivePicture ReceivePicture) {
		Message msg = Message.obtain();
		msg.what = NEW_MSG_SER2ACTIVITY;
		Bundle b = new Bundle();
		int para[] = new int[6];
		para[indexWith] = ReceivePicture.getWidth();
		para[indexheight] = ReceivePicture.getHeight();
		para[indexquality] = ReceivePicture.getQuality();
		para[indexFlashMode] = ReceivePicture.getFlashMode();
		para[indexSceneMode] = ReceivePicture.getSceneMode();
		para[indexNumberPicture] = ReceivePicture.getNumberPicture();
		b.putIntArray(MESSAGEToActivity, para);
		msg.setData(b);
		try {
			if(mClients.size()!=0)
			mClients.get(0).send(msg);
			else {
				// Thong bao la chua co con nao ket noi
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopTimer(Timer mTimer) {
		if (isCDTimerLive) {
			mTimer.cancel();
			isCDTimerLive = false;
		}

	}

	public void tranfer2Timer(byte[] a, ConfigReveivePicture ReceivePicture) {
		System.out.print("\ntranfer2Timer\n");
		ReceivePicture.parseMsg(a);
		if (isCDTimerLive)
			stopTimer(mTimer);
		startTimer(ReceivePicture.getNumberPicture(),
				ReceivePicture.getDelay(), ReceivePicture);
		System.out.print("\ntranfer2TimerEnd");
	}

}
