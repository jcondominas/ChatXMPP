package com.jcg.testchat.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.jivesoftware.smack.RosterEntry;
import com.example.testchat.R;
import com.jcg.testchat.xmpp.ChatController;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {
	
	private ArrayList<RosterEntry> contactList;
	private Activity ctx;
	private ChatController chat;
	private Hashtable<String, Integer> availabilities;
	
	

	public ContactListAdapter(ChatController chat, Activity context){
		this.chat=chat;
		sortData();
		ctx =context;
	}
	

	public void sortData() {
		// TODO Auto-generated method stub
		contactList =  chat.getContacts();
		availabilities = chat.getAvailabilites();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Collections.sort(contactList, new PresenceComparator());				
			}
		}).start();
	}
	
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();				
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return contactList.size();
	}

	@Override
	public RosterEntry getItem(int arg0) {
		// TODO Auto-generated method stub
		return contactList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int id, View oldView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(oldView == null){
			LayoutInflater inflater = (LayoutInflater) ctx.getLayoutInflater();
			oldView = inflater.inflate(R.layout.contact_item, null);
		}
		RosterEntry c = getItem(id);
		TextView contact = (TextView) oldView.findViewById(R.id.contactName);
		TextView status = (TextView) oldView.findViewById(R.id.status);
		ImageView img = (ImageView) oldView.findViewById(R.id.imgContact);
		Bitmap bmp = chat.getAvatar(c);
		if(bmp!=null){			
			img.setImageBitmap(bmp);
		}else{
			img.setImageResource(R.drawable.ic_noimg);
		}		
		
		String name = (c.getName()!=null)?c.getName():c.getUser();
		contact.setText(name);			
		String available = convertAvailability(chat.getUserAvailability(c));			
		status.setText(available);
		
		return oldView;
	}
	
	private String convertAvailability(int Availability){
		if(Availability==ChatController.USER_AVAILABLE) return "available";
		else if(Availability==ChatController.USER_AWAY) return "away";
		else return "offline";
	}
	
	class PresenceComparator implements Comparator<RosterEntry>{

		@Override
		public int compare(RosterEntry r1, RosterEntry r2) {
			// TODO Auto-generated method stub
			int value=0;
			if(availabilities.get(r1.getUser())<availabilities.get(r2.getUser()))
				value = -1;
			else if(availabilities.get(r1.getUser())>availabilities.get(r2.getUser()))
				value= 1;
			else if(r1.getName()!=null && r2.getName()!=null) value= r1.getName().compareTo(r2.getName());
			else if(r1.getName()==null && r2.getName()==null) value= r1.getUser().compareTo(r2.getUser());
			else if(r1.getName()==null && r2.getName()!=null) value= 1;
			else if(r1.getName()!=null && r2.getName()==null) value = -1;

			return value;
		}			
	}




}
