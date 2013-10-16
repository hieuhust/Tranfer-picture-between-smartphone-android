package com.example.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.cameagoogleguide.CameraApp;
import com.example.cameagoogleguide.MainActivity;
import com.example.network.NetMessage;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReceiveDataThread extends Thread {

	public static final int STATE_RUNNING = 2;
	public static final int STATE_PAUSING = 1;
	public static final int STATE_END = 0;

	public static final String TAG = "ReceiveThread";

	public static int state;
	private Handler ServiceHandler;
	private static ServerSocket servSock;
	private static int stateServerClient;

	public ReceiveDataThread(Handler ServiceHandler) {

		this.ServiceHandler = ServiceHandler;
		state = STATE_RUNNING;
		Log.d("Receive Thread", "Created");
	}

	public static void End() {
		state = STATE_END;
		Log.d("Receive Thread", "Service: end now");
		try {
			servSock.close();
			servSock = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		try {

			servSock = new ServerSocket(NetMessage.PORT);

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
				try {
					Thread.sleep(200);

					if (state == STATE_RUNNING) {
						// Code socket o day
						try {
							Socket sock = servSock.accept();														
							new processThread(sock, ServiceHandler).start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.d("Receive", "ExAccept: " + e.getMessage());
						}

					}
				} catch (InterruptedException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("ReceiveThead", " really end!");
		return;
	}
	
}
