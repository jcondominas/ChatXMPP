package com.jcg.testchat.activities;

import org.jivesoftware.smack.RosterEntry;

import com.jcg.testchat.R;
import com.jcg.testchat.adapters.ContactListAdapter;
import com.jcg.testchat.application.TestChat;
import com.jcg.testchat.xmpp.ChatController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ContactList extends Activity {

	private static final String TAG = "TestChat/ContactListActivity";
	private static final int REFRESH_CONTACT_LIST = 0;
	private static final int REFRESH_AVATARS = 1;
	private ChatController chat;
	private ListView mContactlist;
	private Context ctx;
	private ContactListAdapter adapter;
	private Handler handler;
	private boolean isGoingHome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		chat = ((TestChat) getApplication()).chat;
		isGoingHome = false;
		ctx = this;
		mContactlist = (ListView) findViewById(R.id.lwContactList);
		adapter = new ContactListAdapter(chat, this);
		mContactlist.setAdapter(adapter);
		
		mContactlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				ContactListAdapter adapt = (ContactListAdapter) mContactlist
						.getAdapter();
				RosterEntry contact = adapt.getItem(position);
				chat.startChat(contact);
				Intent i = new Intent(ctx, ChatPages.class);
				Bundle args = new Bundle();
				args.putString(ChatPages.CONTACT_CHAT, contact.getUser());
				i.putExtra("bundle", args);
				startActivity(i);
			}
		});

		handler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case REFRESH_CONTACT_LIST:
					adapter.sortData();
					adapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
				
				adapter.notifyDataSetChanged();
			}
		};

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		chat.setContactListActivity(null);
		if(isGoingHome){
			//logging out
			chat.disconnect();
		}
	}	

	@Override
	public void onBackPressed() {
		// If Back is pressed, go HOME, instead of log out. Only log out if is pushed Home button
		super.onBackPressed();
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		chat.setContactListActivity(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			updateRoster(item);
			return true;
		case android.R.id.home:
			isGoingHome=true;
			return super.onOptionsItemSelected(item);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void updateRoster(MenuItem item) {
		Log.d(TAG, "update roster");
	}

	public void refreshContactList() {
		Message msg = handler.obtainMessage();
		msg.what = REFRESH_CONTACT_LIST;
		msg.sendToTarget();
	}

	public void refreshAvatars() {
		Message msg = handler.obtainMessage();
		msg.what = REFRESH_AVATARS;
		msg.sendToTarget();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}

}
