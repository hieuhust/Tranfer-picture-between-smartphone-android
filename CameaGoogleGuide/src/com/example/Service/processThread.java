package com.example.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.cameagoogleguide.CameraApp;

public class processThread extends Thread {
	Socket sock = null;

	public static final String TAG = "processClientThread";

	private static int stateServerClient;
	public static final int STATE_RUNNING = 2;
	public static final int STATE_PAUSING = 1;
	public static final int STATE_END = 0;
	private Handler ServiceHandler;

	public processThread(Socket s, Handler handler) {
		this.sock = s;
		ServiceHandler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		int lenght10 = 0;
		int lenght1 = 0;
		int lenght = 0;
		try {

			CameraApp.dis = new DataInputStream(new BufferedInputStream(
					sock.getInputStream()));
			CameraApp.dos = new DataOutputStream(new BufferedOutputStream(
					sock.getOutputStream()));
			stateServerClient = STATE_RUNNING;
			while (stateServerClient != STATE_END) {
				try {
					byte[] buf = new byte[1000];
					CameraApp.dis.read(buf, 0, 3);
					Log.d(TAG, "Receive: " + buf[0]);

					// Lay chieu dai cua ban tin
					lenght10 = buf[1];
					lenght1 = buf[2];
					lenght = lenght10 * 10 + lenght1;
					// Tiep tuc doc tuy nhien bo phan chieu dai di
					try {
						CameraApp.dis.read(buf, 1, lenght);

						byte data[] = new byte[lenght + 1];
						for (int i = 0; i < lenght + 1; i++) {
							data[i] = buf[i];
						}
						if (lenght != 0) {
							// Chuyen sang ben Service
							Message Msg2Service = new Message();
							Msg2Service.what = MessageService.RECEI2SER_NEW_MSG;
							Bundle b = new Bundle();
							b.putByteArray(MessageService.MESSAGE, data);
							Msg2Service.setData(b);
							ServiceHandler.sendMessage(Msg2Service);
						} else {
							stateServerClient = STATE_END;
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e(TAG, "exRead:" + e.getMessage());
						stateServerClient = STATE_END;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "exRead1:" + e.getMessage());
					stateServerClient = STATE_END;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "exCreat:" + e.getMessage());
		}
	}

}
