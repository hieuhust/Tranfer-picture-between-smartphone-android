package com.example.NetWork;

public class ProcessPara extends NetMessage {

	private int cmd_id;

	private int flash_auto = 0;
	private int flash_off = 0;
	private int flash_on = 0;
	private int flash_torch = 0;
	private int scene_action = 0;
	private int scene_auto = 0;
	private int scene_landscape = 0;
	private int scene_night = 0;
	private int scene_night_portrait = 0;
	private int scene_portrait = 0;
	private int scene_sport = 0;
	private int[] widthByte = new int[2];
	private int[] heightByte = new int[2];
	private int[] width = new int[5];
	private int[] height = new int[5];

	public ProcessPara(int cmd_id) {
		super(cmd_id);
		// TODO Auto-generated constructor stub
		this.cmd_Id = cmd_id;
	}

	@Override
	public byte[] generateToByte() {
		// TODO Auto-generated method stub
		return null;
	}

	public void parse(byte[] data) {
		flash_auto = data[0];
		flash_off = data[1];
		flash_on = data[2];
		flash_torch = data[3];
		scene_action = data[4];
		scene_auto = data[5];
		scene_landscape = data[6];
		scene_night = data[7];
		scene_portrait = data[8];
		scene_sport = data[9];
		scene_night_portrait = data[10];
		int index = 0;
		for (int i = 0; i < 5; i++) {
			widthByte[0] = NetMessage.Byte2Unsigned(data[11  +index]);
			widthByte[1] = NetMessage.Byte2Unsigned(data[12  +index]);
			heightByte[0] = NetMessage.Byte2Unsigned(data[13 +index]);
			heightByte[1] = NetMessage.Byte2Unsigned(data[14 +index]);
			width[i] = widthByte[0] * 256 + widthByte[1];
			height[i] = heightByte[0] * 256 + heightByte[1];
			index =index+4;
		}
		int a =0;
	}

	public int getFlashAuto() {
		return flash_auto;
	}

	public int getFlashOff() {
		return flash_off;
	}

	public int getFlashON() {
		return flash_on;
	}

	public int getFlashTorch() {
		return flash_torch;
	}

	public int getSceneAction() {
		return scene_action;
	}

	public int getSceneAuto() {
		return scene_auto;
	}

	public int getSceneLandscape() {
		return scene_landscape;
	}

	public int getSceneNight() {
		return scene_night;
	}

	public int getSceneNightPortrait() {
		return scene_night_portrait;
	}

	public int getScenePortrait() {
		return scene_portrait;
	}

	public int getSceneSport() {
		return scene_sport;
	}

	public int[] getWidth() {
		return width;
	}

	public int[] getHeight() {
		return height;
	}

}
