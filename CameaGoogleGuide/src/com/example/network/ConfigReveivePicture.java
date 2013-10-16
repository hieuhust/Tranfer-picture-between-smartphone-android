package com.example.network;

public class ConfigReveivePicture extends NetMessage {

	int width = 0;
	int height = 0;
	int quality = 0;
	int FlashMode = 0;
	int SceneMode = 0;
	int NumberOfPicture = 0;
	int Delay = 0;

	public ConfigReveivePicture(int cmd_id) {
		super(cmd_id);
	}

	public Boolean isNetworkReveivePicture(byte[] input) {
		if (getCmd_id(input) == NetMessage.CMD_RECEIVE_PICTURE)
			return true;
		return false;
	}

	@Override
	public void parseMsg(byte[] input) {
		// TODO Auto-generated method stub
		int index = 1;
		width = (int) input[index] * 100 + (int) input[index + 1];
		System.out.println("Width = " + width);
		index = index + 2;
		height = (int) input[index] * 100 + (int) input[index + 1];
		System.out.println("height = " + height);
		index = index + 2;
		quality = (int) input[index];
		System.out.println("qulity = " + quality);
		index++;
		FlashMode = (int) input[index];
		System.out.println("FlashMode = " + FlashMode);
		index++;
		SceneMode = (int) input[index];
		System.out.println("SceneMode = " + SceneMode);
		index++;
		NumberOfPicture = (int) input[index];
		System.out.println("NumberPicture = " + NumberOfPicture);
		index++;
		Delay = (int) input[index];
		System.out.println("Delay = " + Delay);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getQuality() {
		return quality;
	}

	public int getFlashMode() {
		return FlashMode;
	}

	public int getNumberPicture() {
		return NumberOfPicture;
	}

	public int getDelay() {
		return Delay;
	}
	public int getSceneMode() {
		return SceneMode;
	}

	@Override
	public byte[] generateByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

}
