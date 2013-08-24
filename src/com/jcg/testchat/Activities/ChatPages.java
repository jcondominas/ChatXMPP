package com.jcg.testchat.activities;

import java.util.ArrayList;
import java.util.Hashtable;


import org.jivesoftware.smack.Chat;

import com.jcg.testchat.R;
import com.jcg.testchat.adapters.ChatAdapter;
import com.jcg.testchat.application.TestChat;
import com.jcg.testchat.xmpp.ChatController;



import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ChatPages extends FragmentActivity {

	private ChatController chat;
	private ChatPages ctx;
	private ViewPager vw;
	private ChatListAdapater adapter;
	private EditText input;

	private Hashtable<Chat, ChatAdapter> chatAdapters;

	private static Handler handler;

	private int CurrentPagePosition;
	public static final int REFRESH_CHAT_LIST = 0;
	public static final int REFRESH_CHAT_ROOM = 1;
	protected static final String CONTACT_CHAT = "contact";

	@Override
	protected void onCreate(Bundle savedInstances) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstances);
		setContentView(R.layout.activity_chat);
		chat = ((TestChat) getApplication()).chat;
		chatAdapters = new Hashtable<Chat, ChatAdapter>();
		adapter = new ChatListAdapater(getSupportFragmentManager());
		vw = (ViewPager) findViewById(R.id.vpChatList);
		vw.setAdapter(adapter);
		ctx = this;

		input = (EditText) findViewById(R.id.inputText);

		input.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView et, int actionId,
					KeyEvent keyevent) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					ctx.sendMessage(et.getText().toString());
					et.setText("");
					return true;
				}
				return false;
			}
		});

		handler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Chat chatroom = (Chat) msg.obj;
				if(chatroom == null) return;
				switch (msg.what) {
				case REFRESH_CHAT_LIST:
					adapter.notifyDataSetChanged();
					break;
				case REFRESH_CHAT_ROOM:
					ChatAdapter chatadapter = chatAdapters.get(chatroom);
					if (chatadapter != null)
						chatadapter.notifyDataSetChanged();
					break;
				}
			}
		};

		String participant = getIntent().getBundleExtra("bundle").getString(
				CONTACT_CHAT);
		CurrentPagePosition = chat.participantIndex(participant);
		vw.setCurrentItem(CurrentPagePosition);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		chat.setContactListActivity(null);
	}	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		chat.setChatPagesActivity(this);
	}	

	protected void sendMessage(String msg) {

		chat.sendMessage(chat.getChatList().get(vw.getCurrentItem()), msg);
	}

	public void refreshChatList() {
		int state = REFRESH_CHAT_LIST;
		Message msg = handler.obtainMessage(state);
		msg.sendToTarget();
	}

	public void refreshChatRoom(Chat chatroom) {
		int state = REFRESH_CHAT_ROOM;
		Message msg = handler.obtainMessage(state, chatroom);
		msg.sendToTarget();
	}

	public void refreshChatRoom() {
		int p = vw.getCurrentItem();
		ArrayList<Chat> chats = chat.getChatList();
		Chat chatroom=null;
		if(chats.size()>0)
			chatroom = chats.get(p);
		int state = REFRESH_CHAT_ROOM;
		Message msg = handler.obtainMessage(state, chatroom);
		msg.sendToTarget();
	}

	public class ChatListAdapater extends FragmentStatePagerAdapter {

		public ChatListAdapater(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			Chatroom room = new Chatroom();
			Chat chatroom = chat.getChatList().get(position);
			ChatAdapter adapter = new ChatAdapter(ctx, chat, chatroom);
			chatAdapters.put(chatroom, adapter);
			room.adapter = adapter;
			Bundle args = new Bundle();
			args.putInt(Chatroom.POSITION_CHAT_CREATED, position);
			room.setArguments(args);
			return room;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			int size = chat.getChatList().size();
			return size;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return chat.getParticipant(position);
		}

	}

	public static class Chatroom extends Fragment {
		public static final String POSITION_CHAT_CREATED = "ChatCreatedPosition";

		public ChatAdapter adapter;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_chat, container, false);
			ListView list = (ListView) v.findViewById(R.id.lwChat);
			list.setAdapter(adapter);
			return v;
		}

	}

}
