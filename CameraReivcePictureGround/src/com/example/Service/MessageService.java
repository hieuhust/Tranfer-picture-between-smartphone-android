package com.example.Service;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.example.cameagoogleguide.CameraApp;
import com.example.cameraGround.R;

public class MessageService extends Service {

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SEND_REQUEST = 3;
	public static final int RECEI2SER_NEW_MSG = 4;
	public static final int SER2TRAN_NEW_MSG = 5;
	public static final int MSG_DATA_FR_SERVICE_TO_ACTIVITY = 6;
	public static final int MSG_CONNECT = 7;
	public static final String MESSAGE = "message";
	public static final String MESSAGE2Service= "messageToService";
	public static final String MESSAGEToThread = "messagetoThread";
	/** Keeps track of all current registered clients. */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	/** Holds last value set by a client. */
	int mValue = 0;
	public Handler mHandler;
	
	ReceiveDataThread receiveDataThread = null;
	TransmitThread transmitThread = null;

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
			case MSG_SEND_REQUEST:
				Bundle sendData = msg.getData();
				Message msg2TransmitThr = Message.obtain();
				msg2TransmitThr.what = SER2TRAN_NEW_MSG;
				msg2TransmitThr.setData(sendData);
				sendMessageToTransmit(msg2TransmitThr);
				
				break;
			case RECEI2SER_NEW_MSG:
				Bundle b = new Bundle();
				b = msg.getData();
				int a = b.getInt(MessageService.MESSAGE);
				try {
					mClients.get(0).send(Message.obtain(null, MessageService.MSG_DATA_FR_SERVICE_TO_ACTIVITY,a ,0));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case MSG_CONNECT:
				Bundle bConnect = new Bundle();
				bConnect = msg.getData();
				String ip = bConnect.getString(MESSAGE2Service);
				transmitThread = new TransmitThread(getApplicationContext(),ip);
				transmitThread.start();
				break;
			default:
				super.handleMessage(msg);
			}
		}

	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mHandler = new IncomingHandler();
	/*	String IP = "192.168.1.118";
		transmitThread = new TransmitThread(getApplicationContext(),IP);
		transmitThread.start();*/
		Toast.makeText(this, R.string.remote_service_started,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Toast.makeText(this, R.string.remote_service_stopped,
				Toast.LENGTH_SHORT).show();
		ReceiveDataThread.End();
		TransmitThread.End();
		CameraApp.bis = null;
		CameraApp.bos = null;
	}
	private void sendMessageToTransmit(Message msg){
		transmitThread.getHandler().sendMessage(msg);
	}

}
