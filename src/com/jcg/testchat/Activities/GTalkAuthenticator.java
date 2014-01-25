package com.jcg.testchat.Activities;

import java.io.IOException;

import com.jcg.testchat.R;
import com.jcg.testchat.application.TestChat;
import com.jcg.testchat.xmpp.ChatController;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GTalkAuthenticator extends Activity {

	private static final String TAG = "TestChat/GTalkOAuth2";
	
	private static final String ACCOUNT_TYPE = "com.google";

	private AccountManager mAccountManager;
	private String mToken;
	private Account googleAccount;
	
	private ChatController chat;
	
	
	private Context ctx;
	private ProgressDialog pd;
	private Button login;
	private EditText username;
	private EditText password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.activity_gtalkauthenticator);
		chat = ((TestChat)getApplication()).chat;
		username = (EditText) findViewById(R.id.edUsername);
		password = (EditText) findViewById(R.id.edPassword);
		login = ((Button)findViewById(R.id.btLogin));
		login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String user =  username.getText().toString();
				String pass = password.getText().toString();
				
				pd = ProgressDialog.show(ctx, "GTalk Login", "Log in, please wait");
				new GTalkLogin(user,pass, ChatController.MODE_PLAIN).execute();
			}
		});
		
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(chat.isAuthenticated()){
			Intent i = new Intent(ctx, ContactList.class);
			startActivity(i);		
		}else if(chat.isConnected()){
			chat.disconnect();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void GtalkAuth(View v) {
		mAccountManager = AccountManager.get(this);
		getAuthToket();
	}

	public void getAuthToket() {
		if(mAccountManager.getAccountsByType(ACCOUNT_TYPE).length>0){
			googleAccount = mAccountManager.getAccountsByType(ACCOUNT_TYPE)[0];

			Bundle options = new Bundle();
			mAccountManager.getAuthToken(googleAccount,
					"oauth2:https://www.googleapis.com/auth/googletalk",
					options, this, new OnTokenAcquired(), new Handler(
							new OnError()));
		}else{
			Toast.makeText(this, "Google account not found. Please log in using your Gmail username and Gmail password. Thank you", Toast.LENGTH_LONG).show();
		}
	}

	private class OnError implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.e(TAG, "Gtalk Auth error: " + msg.toString());
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Log.d(TAG,"Credentials correctly configured. Get token again. Request code "+requestCode);
			mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, null);
			getAuthToket();
		}
	}
	
	private class GTalkLogin extends AsyncTask<Void, Void, Boolean>{		

		private String username;
		private String password;
		private int LoginMode;
		
		public GTalkLogin(String user, String pass,int mode){
			this.username = user;
			this.password = pass;
			this.LoginMode= mode;
		}
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			ChatController chat = ((TestChat) getApplication()).chat;
			chat.setChatSettings(this.username, this.password,this.LoginMode);
			return chat.connection();			
			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if(result){				
				Log.d(TAG, "gtalk log in correct");	
				
				Intent i = new Intent(ctx, ContactList.class);
				startActivity(i);				
				pd.dismiss();
			}else{
				if(LoginMode == ChatController.MODE_OAUTH2) Toast.makeText(ctx, "Problem to log in using Google credentials. Please try again", Toast.LENGTH_LONG).show();
				else Toast.makeText(ctx, "Username or Password incorrect.", Toast.LENGTH_LONG).show();
				pd.dismiss();
				Log.d(TAG, "Gtalk log in incorrect");		
			}			
		}	
	}

	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

		@Override
		public void run(AccountManagerFuture<Bundle> arg0) {
			// TODO Auto-generated method stub
			try {
				Bundle result = arg0.getResult();

				Intent i = (Intent) result.get(AccountManager.KEY_INTENT);
				if (i != null) {
					Log.d(TAG,"More credentials are needed. Starting activity "+i.toString());
					startActivityForResult(i, 1);
					return;
				}
				mToken = result.getString(AccountManager.KEY_AUTHTOKEN);
				Log.d(TAG, "generated Token: "+mToken);			
				pd = ProgressDialog.show(ctx, "GTalk Login", "Log in, please wait");
				new GTalkLogin(googleAccount.name, mToken,ChatController.MODE_OAUTH2).execute();
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "Operaton cancelled by user");
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "Authenticator failed to respond");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "Network problem");
				e.printStackTrace();
			}
		}
	}


}
