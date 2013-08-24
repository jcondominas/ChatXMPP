package com.jcg.testchat.xmpp;

import java.util.ArrayList;
import java.util.Hashtable;

import org.jivesoftware.smack.RosterEntry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ThreadLoadAvatars extends Thread {

	private ChatController chat;
	private Hashtable<String, Bitmap> avatars;
	
	private volatile boolean finalize = false;

	public ThreadLoadAvatars(ChatController chat) {
		this.chat = chat;
		avatars = new Hashtable<String, Bitmap>();	
	}
	
	public void end(){		
		finalize = true;
	}

	@Override
	public void run() {
		try {
			
			//while (!finalize) {
				ArrayList<RosterEntry> roster = chat.getContacts();
				for (RosterEntry entry : roster) {
					if (!avatars.containsKey(entry.getUser())) {
						byte[] byteAvatar = chat.fetchAvatar(entry);
						if (byteAvatar != null) {
							Bitmap bmp = BitmapFactory.decodeByteArray(
									byteAvatar, 0, byteAvatar.length);
							avatars.put(entry.getUser(), bmp);
							chat.refreshContacts();
						}						
					}
					if(finalize) return;
				}
				
				if (!avatars.containsKey(chat.getUsername())) {
					byte[] byteAvatar = chat.fetchAvatar(null);
					if (byteAvatar != null) {
						Bitmap bmp = BitmapFactory.decodeByteArray(byteAvatar,
								0, byteAvatar.length);
						avatars.put(chat.getUsername(), bmp);
						chat.refreshContacts();
					}
				}
				
				Log.d(ChatController.TAG, "Number of loaded avatars = "+avatars.size());
				sleep(1);

			//}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
	}

	public Hashtable<String, Bitmap> getAvatars() {
		return avatars;
	}

}
