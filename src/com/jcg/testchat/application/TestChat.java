package com.jcg.testchat.application;

import org.jivesoftware.smack.SmackAndroid;

import com.jcg.testchat.xmpp.ChatController;

import android.app.Application;


public class TestChat extends Application {
	public final ChatController chat = ChatController.getInstance();	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		SmackAndroid.init(this);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		chat.disconnect();
	}	
	
}
