package com.example.NetWork;

public class RequestSendPara extends NetMessage {
	private int length = 4;
	private int cmd_id;
	public RequestSendPara(int cmd_id) {
		super(cmd_id);
		this.cmd_Id =cmd_id;
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] generateToByte() {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[length];
		buffer[0] = (byte)this.cmd_Id;
		buffer[1] =0;
		buffer[2] =1;
		buffer[3] =1;
		return buffer;
		
	}

}
