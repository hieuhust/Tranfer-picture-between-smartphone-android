package com.example.NetWork;

public abstract class  NetMessage {
	
	public final static int CMD_NOTHING = 0x00;
	public final static int CMD_REQUEST_UAV_TRANFER_PICTURE = 0xA0;
	public final static int CMD_TRANFER_PICTURE = 0xA1;
	public final static int CMD_TRANFER_PARA = 0xA4;
	public final static int CMD_REQUEST_PARA = 0xA5;
	public final static int PROCESS_PARA = 0xB6;
	
	public final static int PORT = 6868;
	
	int cmd_Id =0;
	public NetMessage(int cmd_id){
		this.cmd_Id = cmd_id;
	}
	public abstract byte[] generateToByte();
	public final static int Byte2Unsigned(byte a) {
		return a & 0xFF;
	}

}
