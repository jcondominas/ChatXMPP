package com.jcg.testchat.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

public class MyMessage {

	public static final int OUTGOING_ITEM = 0;
	public static final int INGOING_ITEM = 1;
	
	public String msg;
	public Chat chat;
	public int outgoing;
	
	public MyMessage(Chat chat, Message msg, int outgoing){
		this.chat = chat;
		this.msg=msg.getBody();
		this.outgoing=outgoing;
	}
	
	public MyMessage(Chat chat, String msg, int outgoing){
		this.chat = chat;
		this.msg=msg;
		this.outgoing=outgoing;
	}
	
	
}
