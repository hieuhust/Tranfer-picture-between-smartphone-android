package com.example.Service;

import java.io.IOException;
import java.io.InputStream;

import com.example.NetWork.NetMessage;
import com.example.cameagoogleguide.CameraApp;
import com.example.cameagoogleguide.MainActivity;
import com.example.save.SavePictureThread;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ReceiveDataThread extends Thread {

	public static final int STATE_RUNNING = 2;
	public static final int STATE_PAUSING = 1;
	public static final int STATE_END = 0;

	public static final String TAG = "ReceiveThead";
	public final static String ACTION_DISCONECT = "action_disconnect";
	public final static String ACTION_NUMBER = "action_numberpicture";
	
	public static int state;
	
	private Handler ServiceHandler;
	private InputStream is;
	private Context con;
	private int num;

	public ReceiveDataThread(Handler ServiceHandler) {

		this.ServiceHandler = ServiceHandler;
		state = STATE_RUNNING;
		Log.d("Receive Thread", "Created");
	}

	public ReceiveDataThread(Context con) {
		state = STATE_RUNNING;
		this.con = con;
		Log.d("Receive Thread", "Created");
	}

	public static void End() {
		state = STATE_END;
		Log.d("Receive Thread", "Service: end now");
		Thread.currentThread().interrupt();
		return;
	}

	public void Restart() {
		state = STATE_RUNNING;
		Log.d("Receive Thread", "restart");
	}

	public void Pause() {
		state = STATE_PAUSING;
		Log.d("Receive Thread", "state = pause");
	}

	@Override
	public void run() {
		super.run();

		if (state == STATE_END) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			Log.d("ReceiveThread", "I'm still alive!!!");
		}
		while (state != STATE_END) {

			if (state == STATE_RUNNING) {

				// Message Msg2Service = new Message();
				// Msg2Service.what = MessageService.RECEI2SER_NEW_MSG;
				// Bundle b = new Bundle();
				// b.putInt(MessageService.MESSAGE, 2);
				// Msg2Service.setData(b);
				// this.ServiceHandler.sendMessage(Msg2Service);
				// Log.d("Receive Thread","Sent msg to Service");
				// state = STATE_END;
				byte[] buf = new byte[4096];
				int numberBytes;
				try {
					numberBytes = CameraApp.dis.read(buf, 0, 5);
					if (numberBytes != -1) {
						// =========================================================
						// Convert ma hexa sang he thap phan de lay chieu dai
						int length = Byte2Unsigned(buf[1]) * 256 * 256 * 256
								+ Byte2Unsigned(buf[2]) * 256 * 256
								+ Byte2Unsigned(buf[3]) * 256
								+ Byte2Unsigned(buf[4]);
						int Cmd_id = Byte2Unsigned(buf[0]);
						// ===========================================================

						Log.d(TAG, "buf[1] = " + Byte2Unsigned(buf[1]));
						Log.d(TAG, "buf[2] = " + Byte2Unsigned(buf[2]));
						Log.d(TAG, "buf[3] = " + Byte2Unsigned(buf[3]));
						Log.d(TAG, "buf[4] = " + Byte2Unsigned(buf[4]));
						Log.d(TAG, "length =" + length);
						Log.d(TAG, "numberBytes =" + numberBytes);

						int bytesRead = 0;
						int n = 0;
						byte[] data = new byte[length];
						while (bytesRead < length) {
							if ((length - bytesRead) < buf.length) {
								n = CameraApp.dis.read(data, bytesRead,
										(length - bytesRead));
								bytesRead = bytesRead + n;
							} else {
								n = CameraApp.dis.read(data, bytesRead,
										buf.length);
								bytesRead = bytesRead + n;
							}

						}
						Log.d(TAG, "dataPicture = " + data.length
								+ " bytesRead =" + bytesRead);
						// Luu anh
						if (Cmd_id == NetMessage.CMD_TRANFER_PICTURE) {
							new SavePictureThread(data).start();
							num++;
							// Bao la so buc anh da nhan duoc
							Intent intent = new Intent();
							intent.setAction(MainActivity.action_numberPicture);
							intent.putExtra(ACTION_NUMBER, num);
							this.con.sendBroadcast(intent);
							
						}
						else if(Cmd_id == NetMessage.CMD_TRANFER_PARA){
							Intent intent = new Intent();
							intent.setAction(MainActivity.action_get_para);
							intent.putExtra(MainActivity.GET_PARA, data);
							this.con.sendBroadcast(intent);
						}

					} else {
						state = STATE_END;
						// bao la mat ket noi
						Intent intent = new Intent();
						intent.setAction(MainActivity.action_disconnect);						
						this.con.sendBroadcast(intent);
						Log.e(TAG, "matketnoi");
					}
					// state = STATE_END;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Log.d("ReceiveThead", " really end!");
		return;
	}

	public final static int Byte2Unsigned(byte a) {
		return a & 0xFF;
	}
}
