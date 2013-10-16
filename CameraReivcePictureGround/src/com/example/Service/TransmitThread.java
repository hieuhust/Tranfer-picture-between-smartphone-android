package com.example.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.example.NetWork.NetMessage;
import com.example.cameagoogleguide.CameraApp;
import com.example.cameagoogleguide.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TransmitThread extends Thread {

	Handler UIHandler;
	private static Handler mHandler;
	static boolean isContinue = false;
	private static final String TAG = "Transmit Thread";

	private Context con;

	public void Restart() {
		Log.d("TransmitThread", "restarted");
		isContinue = true;
	}

	public static void End() {
		isContinue = false;
		Log.d("Transmit Thread", "end");
		Thread.currentThread().interrupt();
		if (CameraApp.sock != null) {
			try {
				CameraApp.sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return;
	}

	public TransmitThread(Context con, String Ip) {
		new StartSocket(Ip).start();
		this.con = con;
		Log.d("Transmit Thread", "created");
	}

	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public void run() {
		super.run();
		Looper.prepare();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message inputMessage) {
				switch (inputMessage.what) {
				case MessageService.SER2TRAN_NEW_MSG:
					Bundle b = new Bundle();
					b = inputMessage.getData();
					byte data[] = b
							.getByteArray(MessageService.MESSAGE2Service);

					Log.d(TAG, "data = " + data.length + "data[0]:" + data[0]
							+ "data[1]:" + data[1] + "data[2]:" + data[2]
							+ "data[3]:" + data[3]);
					// Log.d(TAG, "data = " + data[0] + ":" + data[1] + ":"
					// + data[2] + ":" + data[3] + ":" + data[4] + ":"
					// + data[5] + ":" + data[6] + ":" + data[7] + ":"
					// + data[8] + ":" + data[9] + ":" + data[10] + ":"+
					// data[11]);
					try {
						CameraApp.dos.write(data);
						CameraApp.dos.flush();
						data = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d(TAG, "wirte" + e.getMessage());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d(TAG, "wirte" + e.getMessage());
					}

					break;
				default:
					super.handleMessage(inputMessage);
					break;
				}
			}
		};
		Looper.loop();
	}

	public class StartSocket extends Thread {
		String IP = null;

		public StartSocket(String IP) {
			this.IP = IP;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				InetAddress serverAddr = InetAddress.getByName(this.IP);
				CameraApp.sock = new Socket(serverAddr, NetMessage.PORT);
				CameraApp.dis = new DataInputStream(new BufferedInputStream(
						CameraApp.sock.getInputStream()));
				CameraApp.dos = new DataOutputStream(new BufferedOutputStream(
						CameraApp.sock.getOutputStream()));

				// Thong bao la da ket noi Socket thanh cong
				Intent intent = new Intent();
				intent.setAction(MainActivity.action_connect_success);
				con.sendBroadcast(intent);
				// Open REceiveDataThread
				new ReceiveDataThread(con).start();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
