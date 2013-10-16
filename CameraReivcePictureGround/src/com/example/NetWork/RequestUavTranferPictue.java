package com.example.NetWork;

import com.example.cameagoogleguide.MainActivity;

public class RequestUavTranferPictue extends NetMessage {

	private int[] size;
	private int quality, flashMode, scenceMode, numberPicture, interval;

	public RequestUavTranferPictue(int cmd_id) {
		super(NetMessage.CMD_REQUEST_UAV_TRANFER_PICTURE);
		// TODO Auto-generated constructor stub
	}

	public RequestUavTranferPictue(int[] size, int quality, int flashMode,
			int scenceMode, int numberPicture, int interval) {
		super(NetMessage.CMD_REQUEST_UAV_TRANFER_PICTURE);
		// TODO Auto-generated constructor stub
		this.size = size;
		this.quality = quality;
		this.flashMode = flashMode;
		this.scenceMode = scenceMode;
		this.numberPicture = numberPicture;
		this.interval = interval * 10;// Vi  don vi cua byte chuyen di la 1/10s
	}

	@Override
	public byte[] generateToByte() {
		// TODO Auto-generated method stub
		byte[] tranfer = new byte[12];
		tranfer[0] = (byte) NetMessage.CMD_REQUEST_UAV_TRANFER_PICTURE;
		tranfer[1] = 0;
		tranfer[2] = 9;
		// with
		tranfer[3] = (byte) size[0];
		tranfer[4] = (byte) size[1];
		// height
		tranfer[5] = (byte) size[2];
		tranfer[6] = (byte) size[3];
		//quality
		tranfer[7] = (byte) quality;
		//flashMode
		tranfer[8] = (byte) flashMode;
		//SceneMode
		tranfer[9] = (byte) scenceMode;
		//number picture
		tranfer[10] = (byte) numberPicture;
		tranfer[11] = (byte) interval;

		return tranfer;
	}

}
