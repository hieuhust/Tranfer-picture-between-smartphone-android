package com.example.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.widget.Toast;

public class Tranparameter extends NetMessage {

	private final static int Length = 36;
	private final static int Cmd = NetMessage.CMD_TRANFER_PARA;
	private byte data[];

	public Tranparameter(int cmd_id) {
		super(cmd_id);
		// TODO Auto-generated constructor stub
	}

	public Tranparameter(int cmd_id, byte[] data) {
		super(cmd_id);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	public void parseMsg(byte[] input) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] generateByteArray() {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[Length];
		buffer[0] = (byte) Cmd;
		byte[] lenghtData = intToByteArray(this.data.length);
		buffer[1] = lenghtData[0];
		buffer[2] = lenghtData[1];
		buffer[3] = lenghtData[2];
		buffer[4] = lenghtData[3];
		// Copy du lieu vao mang can chuyen
		System.arraycopy(data, 0, buffer, 5, data.length);

		// in ra de test
		try {
			File myFile = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"LogCameraPara.txt");
			if (!myFile.exists())
				myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append("lenght:" + data.length + "\n");
			for (int i = 0; i < buffer.length; i++) {
				myOutWriter.append(Byte2Unsigned(buffer[i]) + "  ");
			}
			myOutWriter.append("lenght buffer:" + buffer.length + "\n");
			myOutWriter.close();
			fOut.close();
		} catch (Exception e) {

		}

		return buffer;
	}

}
