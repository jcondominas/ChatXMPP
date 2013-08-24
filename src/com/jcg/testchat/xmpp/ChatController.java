package com.jcg.testchat.xmpp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import com.jcg.testchat.Activities.ChatPages;
import com.jcg.testchat.Activities.ContactList;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

public class ChatController {

	public static final String TAG = "TestChat/ChatController";

	private static final String GTALK_SERVER = "talk.google.com";
	private static final int GTALK_PORT = 5222;
	private static final String GTALK_SERVICE = "gmail.com";

	public static final int USER_AVAILABLE = 0;
	public static final int USER_AWAY = 1;
	public static final int USER_OFFLINE = 2;

	public static final int MODE_OAUTH2 = 0;
	public static final int MODE_PLAIN = 1;
	
	private static ChatController chat=null;

	private String username;
	private String token;

	/*
	 * chatList stores all the created chats. When a chat is recreated it is
	 * substituted to ensure it is stored the most updated chat object linked
	 * with a determined participant
	 */
	private ArrayList<Chat> chatList;
	private Hashtable<String, Integer> chatIndexes;
	
	/*
	 * chatMessages stores all the messages per participant.
	 */	
	private Hashtable<String, ArrayList<MyMessage>> chatMessages;
	
	
	/*
	 * All the avatars are loaded in a separated thread and stored as Bitmap. 
	 * 
	 */
	private ThreadLoadAvatars loadAvatars;

	

	private XMPPConnection connection;
	private int loginMode;

	private ChatPages chatActivity;
	private ContactList contactListActivity;

	static{
		chat = new ChatController();
	}

	private ChatController() {
		chatList = new ArrayList<Chat>();
		chatMessages = new Hashtable<String, ArrayList<MyMessage>>();
		chatIndexes = new Hashtable<String, Integer>();
		
	}

	public boolean isConnected() {
		if (connection != null) {
			return connection.isConnected();
		}
		return false;
	}

	public boolean isAuthenticated() {
		if (connection != null) {
			if (connection.isConnected())
				return connection.isAuthenticated();
		}
		return false;
	}

	public void setChatSettings(String username, String password, int loginMode) {
		this.username = username;
		this.token = password;
		this.loginMode = loginMode;
	}

	public void setChatPagesActivity(ChatPages activity) {
		chatActivity = activity;
	}

	public String getParticipant(int index) {
		Chat chat = chatList.get(index);
		return chat.getParticipant();
	}

	public int participantIndex(String participant) {
		return chatIndexes.get(participant);
	}

	public ContactList getContactListActivity() {
		return contactListActivity;
	}

	public void setContactListActivity(ContactList contactListActivity) {
		this.contactListActivity = contactListActivity;
	}

	public ArrayList<Chat> getChatList() {
		return chatList;
	}

	public boolean connection() {
		
		ConnectionConfiguration ConnectionConf;
		
		if(this.loginMode==MODE_OAUTH2)  ConnectionConf = getConfigtOAuth2();
		else ConnectionConf = getConfigPlain();

		connection = new XMPPConnection(ConnectionConf);

		try {
			connection.connect();
			connection.login(username, token, "testchat");
			
			
			loadAvatars = new ThreadLoadAvatars(ChatController.getInstance());
			rosterListener();

							
			loadAvatars.start();
			
			ChatManager chatManager = connection.getChatManager();
			chatManager.addChatListener(new ChatManagerListener() {

				@Override
				public void chatCreated(Chat chat, boolean createdLocally) {
					Log.d(TAG, "Chat created with " + chat.getParticipant()
							+ ". Created locally?" + createdLocally);

					String participant = chat.getParticipant();

					if (!chatIndexes.containsKey(participant)) {
						chatMessages.put(participant,
								new ArrayList<MyMessage>());
						chatIndexes.put(participant, chatList.size());
						chatList.add(chat);
					} else {
						int i = chatIndexes.get(participant);

						chatList.remove(i);
						chatList.add(i, chat);
					}

					if (!createdLocally) {
						chat.addMessageListener(new messageRecieved());
						if (chatActivity != null)
							chatActivity.refreshChatList();
					}
				}
			});

			Log.d(TAG, "xmpp connection established. Presence available");
			Log.d(TAG, "Login succesful");
			return true;
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	private ConnectionConfiguration getConfigtOAuth2() {
		SASLAuthentication.registerSASLMechanism(GTalkSASLMechanism.NAME,
				GTalkSASLMechanism.class);
		SASLAuthentication.supportSASLMechanism(GTalkSASLMechanism.NAME, 0);
		ConnectionConfiguration ConnectionConf = new ConnectionConfiguration(
				GTALK_SERVER, GTALK_PORT, GTALK_SERVICE);
		ConnectionConf.setSASLAuthenticationEnabled(true);
		ConnectionConf.setSendPresence(true);
		ConnectionConf.setSecurityMode(SecurityMode.enabled);
		ConnectionConf.setReconnectionAllowed(true);
		SmackConfiguration.setPacketReplyTimeout(10000);

		// from Stackoverflow. It reduces some exceptions when smack opens the
		// android keystore.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ConnectionConf.setTruststoreType("AndroidCAStore");
			ConnectionConf.setTruststorePassword(null);
			ConnectionConf.setTruststorePath(null);
		} else {
			ConnectionConf.setTruststoreType("BKS");
			String path = System.getProperty("javax.net.ssl.trustStore");
			if (path == null)
				path = System.getProperty("java.home") + File.separator + "etc"
						+ File.separator + "security" + File.separator
						+ "cacerts.bks";
			ConnectionConf.setTruststorePath(path);
		}
		return ConnectionConf;
	}
	
	private ConnectionConfiguration getConfigPlain(){
		SASLAuthentication.unregisterSASLMechanism(GTalkSASLMechanism.NAME);
		SASLAuthentication.unsupportSASLMechanism(GTalkSASLMechanism.NAME);
		ConnectionConfiguration ConnectionConf = new ConnectionConfiguration(
				GTALK_SERVER, GTALK_PORT, GTALK_SERVICE);
		ConnectionConf.setSendPresence(true);
		ConnectionConf.setSecurityMode(SecurityMode.enabled);
		ConnectionConf.setReconnectionAllowed(true);
		SmackConfiguration.setPacketReplyTimeout(10000);		
		return ConnectionConf;
	}

	public String getUsername() {
		return username;
	}

	public void rosterListener() {
		// TODO Auto-generated method stub
		connection.getRoster().addRosterListener(new RosterListener() {
			
			@Override
			public void presenceChanged(Presence presence) {
				if (contactListActivity != null)
					contactListActivity.refreshContactList();
				
			}

			@Override
			public void entriesAdded(Collection<String> entries) {
				// TODO Auto-generated method stub
				Log.d(TAG, "entries added " + entries.size());
				connection.getRoster();

				if (contactListActivity != null)
					contactListActivity.refreshContactList();
				
			}

			@Override
			public void entriesDeleted(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void entriesUpdated(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}
		});

	}

	public byte[] fetchAvatar(RosterEntry entry) {
		if (isAuthenticated()) {
			String user;
			if (entry == null)
				user = this.username;
			else
				user = entry.getUser();
			VCard card = new VCard();
			try {
				card.load(connection, user);

				byte[] result = card.getAvatar();
				return result;
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Bitmap getAvatar(RosterEntry entry) {
		if (entry != null) {
			return loadAvatars.getAvatars().get(entry.getUser());
		} else {
			return loadAvatars.getAvatars().get(username);
		}
	}

	public ArrayList<RosterEntry> getContacts() {
		ArrayList<RosterEntry> result = new ArrayList<RosterEntry>(connection
				.getRoster().getEntries());
		return result;
	}

	public Chat getChat(String participant) {
		int i = chatIndexes.get(participant);
		return chatList.get(i);
	}

	public boolean sendMessage(Chat chat, String message) {

		Log.d(TAG, "Send message to: " + chat.getParticipant());
		Log.d(TAG, "Message: " + message);
		MyMessage mymsg = new MyMessage(chat, message, MyMessage.OUTGOING_ITEM);
		ArrayList<MyMessage> messages = chatMessages.get(chat.getParticipant());

		try {
			chat.sendMessage(message);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if (messages.size() > 0
				&& (messages.get(messages.size() - 1).outgoing == MyMessage.OUTGOING_ITEM))
			messages.get(messages.size() - 1).msg = messages.get(messages
					.size() - 1).msg + "\n" + message;
		else
			messages.add(mymsg);

		if (chatActivity != null)
			chatActivity.refreshChatRoom(chat);
		return true;
	}

	public class messageRecieved implements MessageListener {

		@Override
		public void processMessage(Chat chat, Message msg) {
			Log.d(TAG, "Message from: " + chat.getParticipant());
			Log.d(TAG, "Message: " + msg.getBody());
			if (msg.getType() == Message.Type.chat && msg.getBody() != null) {
				MyMessage mymsg = new MyMessage(chat, msg,
						MyMessage.INGOING_ITEM);
				ArrayList<MyMessage> messages = chatMessages.get(chat
						.getParticipant());
				if (messages.size() > 0
						&& (messages.get(messages.size() - 1).outgoing == MyMessage.INGOING_ITEM))
					messages.get(messages.size() - 1).msg = messages
							.get(messages.size() - 1).msg
							+ "\n"
							+ msg.getBody();
				else
					messages.add(mymsg);
				if (chatActivity != null)
					chatActivity.refreshChatRoom(chat);
			}
		}
	}

	public boolean startChat(RosterEntry contact) {
		// TODO Auto-generated method stub
		connection.getChatManager().createChat(contact.getUser(),
				new messageRecieved());
		return true;
	}

	public ArrayList<MyMessage> getChat(Chat chat) {
		ArrayList<MyMessage> result = chatMessages.get(chat.getParticipant());
		return result;
	}

	public Roster getRoster() {
		// TODO Auto-generated method stub
		return connection.getRoster();
	}

	public int getUserAvailability(RosterEntry c) {
		Presence p = connection.getRoster().getPresence(c.getUser());

		if (p.isAvailable() && !p.isAway())
			return USER_AVAILABLE;
		else if (p.isAway())
			return USER_AWAY;
		else
			return USER_OFFLINE;
	}

	public void refreshContacts() {

		if (loadAvatars.getAvatars().size() == connection.getRoster()
				.getEntries().size()) {
			loadAvatars.end();
			loadAvatars.interrupt();
		}

		if (this.contactListActivity != null)
			this.contactListActivity.refreshAvatars();
		if (this.chatActivity != null)
			this.chatActivity.refreshChatRoom();
	}

	public void disconnect() {
		Log.d(TAG,"Chat disconnected");
		
		connection.disconnect();
		loadAvatars.end();
		chatIndexes = new Hashtable<String, Integer>();
		chatList = new ArrayList<Chat>();
		chatMessages = new Hashtable<String, ArrayList<MyMessage>>();
			
	}

	public Hashtable<String, Integer> getAvailabilites() {
		Collection<RosterEntry> rosterentry = connection.getRoster().getEntries();
		Hashtable<String, Integer> presences = new Hashtable<String, Integer>();
		for(RosterEntry entry:rosterentry){
			presences.put(entry.getUser(), getUserAvailability(entry));
		}
		return presences;
	}

	public static ChatController getInstance() {
		// TODO Auto-generated method stub
		return ChatController.chat;
	}

}
