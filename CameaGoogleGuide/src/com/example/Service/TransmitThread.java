package com.example.Service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.example.cameagoogleguide.CameraApp;
import com.example.cameagoogleguide.MainActivity;
import com.example.network.NetMessage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TransmitThread extends Thread {

	Handler UIHandler;
	private static Handler mHandler;
	private static final String TAG = "Transmit Thread";
	static boolean isContinue = false;

	public void Restart() {
		Log.d("TransmitThread", "restarted");
		isContinue = true;
	}

	public static void End() {
		isContinue = false;
//		mHandler.getLooper().quit();
		Log.d("Transmit Thread", "end");
		Thread.currentThread().interrupt();
		return;
	}

	public TransmitThread() {

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
					Log.d(TAG, " data  ");
					byte[] data;
					Bundle b = new Bundle();
					b = inputMessage.getData();
					data = b.getByteArray(MessageService.MESSAGE_send_data_2ser);
//					Log.d(TAG, "lenght data of Activity= " + data.length);

					//=============================================================
					// Gui du lieu di
					byte[] buf = new byte[4092];
					int bytesRead = 0;
					try {
						while (bytesRead < data.length) {
							if((data.length - bytesRead) < buf.length){
								System.arraycopy(data, bytesRead, buf, 0, (data.length - bytesRead));
								CameraApp.dos.write(buf, 0, (data.length - bytesRead));
								bytesRead = data.length;
							}
							else{
							System.arraycopy(data, bytesRead, buf, 0, buf.length);
							CameraApp.dos.write(buf);
							bytesRead = bytesRead + buf.length;
							}
						}
						
						CameraApp.dos.flush();
						Log.d(TAG, "lenght data of Activity= " + data.length);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MessageService.SER2TRAN_MSG_PARA:					
					Bundle bPara = new Bundle();
					bPara = inputMessage.getData();
					byte[] para = bPara.getByteArray(MainActivity.PARAMETER);
					try {
						CameraApp.dos.write(para);
						CameraApp.dos.flush();
						Log.e(TAG, "write" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e(TAG, "Exception" + e.getMessage());
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


}
