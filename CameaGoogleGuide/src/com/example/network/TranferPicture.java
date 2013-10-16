package com.example.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class TranferPicture extends NetMessage {

	byte[] data;
	private Context con;

	public TranferPicture(int cmd_id) {
		super(cmd_id);
		// TODO Auto-generated constructor stub
	}

	public TranferPicture(Context con, int cmd_id, byte[] data) {
		super(cmd_id);
		// TODO Auto-generated constructor stub
		this.data = data;
		this.con = con;
	}

	@Override
	public void parseMsg(byte[] input) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] generateByteArray() {
		// TODO Auto-generated method stub
		byte[] dataSend = new byte[data.length + 5];
		dataSend[0] = (byte) NetMessage.CMD_TRANFER_PICTURE;
		// Them chieu dai cua du lieu vao mag
		byte[] lenghtData = intToByteArray(data.length);
		dataSend[1] = lenghtData[0];
		dataSend[2] = lenghtData[1];
		dataSend[3] = lenghtData[2];
		dataSend[4] = lenghtData[3];
		// Copy du lieu vao mang can chuyen
		System.arraycopy(data, 0, dataSend, 5, data.length);

		// In ra
//		try {
//			File myFile = new File(Environment
//					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//			"LogCamera.txt");
//			if(!myFile.exists())
//			myFile.createNewFile();
//			FileOutputStream fOut = new FileOutputStream(myFile);
//			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//			myOutWriter.append("lenght:"+ data.length + "\n");
//			myOutWriter.append("-dataSend[0]:" + Byte2Unsigned(dataSend[0]) + "-dataSend[1]:"
//					+ Byte2Unsigned(dataSend[1]) + "-dataSend[2]:" + Byte2Unsigned(dataSend[2])
//					+ "-dataSend[3]:" + Byte2Unsigned(dataSend[3]) + "-dataSend[4]:"
//					+ Byte2Unsigned(dataSend[4])+ "\n");
//			myOutWriter.append("lenght datasend:"+ dataSend.length + "\n");
//			myOutWriter.close();
//			fOut.close();
//		} catch (Exception e) {
//			Toast.makeText(con, e.getMessage(), Toast.LENGTH_SHORT).show();
//		}


		return dataSend;
	}

}
