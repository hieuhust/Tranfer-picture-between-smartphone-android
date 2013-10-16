package com.example.network;

import java.nio.ByteBuffer;

public abstract class NetMessage {
	public final static int CMD_NOTHING = 0x00;
	public final static int CMD_RECEIVE_PICTURE = 0xA0;
	public final static int CMD_TRANFER_PICTURE = 0xA1;
	public final static int CMD_TRANFER_PARA = 0xA4;
	public final static int CMD_REQUEST_PARA = 0xA5;


	public final static int PORT = 6868;
	
	int cmd_id;
	//byte position
		public final static int cmdIdPos = 0;
	public NetMessage(int cmd_id){
		this.cmd_id = cmd_id;
	}
	public int getCmdMessage(){
		return cmd_id;
	}
	public static int getCmd_id(byte[] input){
		if(input == null) return CMD_NOTHING;
		if(input.length<=1)
			return CMD_NOTHING;
		else
			return Byte2Unsigned(input[cmdIdPos]);
		
	}
	public final static int Byte2Unsigned(byte a){
		return a & 0xFF;
	}
	public byte[] intToByteArray(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}
	public abstract void parseMsg(byte[] input);
	public abstract byte[]generateByteArray();
		
}
