package com.jcg.testchat.adapters;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.RosterEntry;

import com.jcg.testchat.R;
import com.jcg.testchat.xmpp.ChatController;
import com.jcg.testchat.xmpp.MyMessage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {

	
	private ArrayList<MyMessage> messages;
	private Activity ctx;
	private ChatController chat;
	private Chat chatroom;

	public ChatAdapter(Activity ctx, ChatController chat, Chat chatroom) {
		this.ctx = ctx;
		this.chat = chat;
		this.chatroom = chatroom;
		messages = chat.getChat(chatroom);
	}
	
	public void setContext(Activity ctx){
		this.ctx=ctx;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messages.size();
	}

	@Override
	public MyMessage getItem(int arg0) {
		// TODO Auto-generated method stub
		return messages.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View oldView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(ctx == null) return oldView;
		
		MyMessage msg = getItem(position);
		Bitmap bmp=null;
		if (oldView == null || ( ((Integer)oldView.getTag()) != msg.outgoing)) {
			LayoutInflater inflater = (LayoutInflater) ctx.getLayoutInflater();
			
			if(msg.outgoing==MyMessage.OUTGOING_ITEM) {
				oldView = inflater.inflate(R.layout.chat_message_outgoing_item, null);
				oldView.setTag(MyMessage.OUTGOING_ITEM);
				bmp = chat.getAvatar(null);
			}
			else{
				oldView = inflater.inflate(R.layout.chat_message_ingoing_item, null);
				oldView.setTag(MyMessage.INGOING_ITEM);
				RosterEntry c = chat.getRoster().getEntry(chatroom.getParticipant());
				bmp = chat.getAvatar(c);
			}
			
		}
		
		TextView message = (TextView) oldView.findViewById(R.id.msg);
		message.setText(msg.msg);
		
		ImageView img = (ImageView) oldView.findViewById(R.id.imgContact);
		
		if(bmp!=null){			
			img.setImageBitmap(bmp);
		}
		// status.setText(getItem(id).getStatus().toString());

		return oldView;
	}

	public void setMessages(ArrayList<MyMessage> messages) {
		this.messages = messages;		
	}

}
