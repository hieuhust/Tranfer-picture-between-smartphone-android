package com.example.Service;

import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class testThread extends Thread{
	public static final String TAG = " testThread";
	
	public testThread() {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Log.e(TAG, "=))");
		while (true) {
			int a=1;
		}
	}
	

}
