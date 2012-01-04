package com.BeetelRockBattery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SyncSettings extends ListActivity {

	private static final String ACCOUNT_KEY = "account";
	private ArrayList<Account> mSystemAccounts = new ArrayList<Account>();
	private UserListAdapter mAdapter = null;
	private HashMap<String, AuthenticatorDescription> mAuthDesc = new HashMap<String, AuthenticatorDescription>();
	private AccountManager mAm = null;
	private SyncAdapterType[] mSATypes = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_settings);
		
		mAm = AccountManager.get(this);
		mSATypes = ContentResolver.getSyncAdapterTypes();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		LoadAccountTasks task = new LoadAccountTasks();
		task.execute(this);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Account account = mSystemAccounts.get((int)id);
		Intent intent = new Intent("android.settings.ACCOUNT_SYNC_SETTINGS");
		intent.putExtra(ACCOUNT_KEY, account);
		try {
			startActivityForResult(intent, 1);
		} catch (Exception e) {
			Toast.makeText(this, R.string.feature_not_supported, Toast.LENGTH_LONG).show();
		}
	}
	
	private class LoadAccountTasks extends AsyncTask<SyncSettings, Integer, Integer> {

		@Override
		protected Integer doInBackground(SyncSettings... ss) {
			
			if(mAm != null) {
				mSystemAccounts.clear();
				Account[] accounts = mAm.getAccounts();
				for(Account account : accounts) {
					mSystemAccounts.add(account);
				}
				
				mAuthDesc.clear();
				AuthenticatorDescription[] authDesc = mAm.getAuthenticatorTypes();
				for(int i = 0; i < authDesc.length; i++) {
					mAuthDesc.put(authDesc[i].type, authDesc[i]);
				}
			}
			
			return mSystemAccounts.size();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			onAccountTaskFinish();
		}
	}
	
	private void onAccountTaskFinish() {
		if(mAdapter != null) 
			mAdapter = null;
		mAdapter = new UserListAdapter(SyncSettings.this, R.layout.account_list_item, mSystemAccounts);
		setListAdapter(mAdapter);
	}
	
	private ArrayList<String> getAccountAuthenticatorLists(Account account) {
		ArrayList<String> authenticators = new ArrayList<String>();
		for(int i = 0; i < mSATypes.length; i++) {
			if(mSATypes[i].accountType.equals(account.type)) {
				authenticators.add(mSATypes[i].authority);
			}
		}
		return authenticators;
	}
	
	private void refreshAccountSyncSettings(CheckBox cb, Account account) {
		cb.setChecked(false);
		ArrayList<String> authenticators = getAccountAuthenticatorLists(account);
		//Log.e("Battery", "refreshAccountSyncSettings size = " + authenticators.size());
		if(authenticators.size() > 0) {
			for(String authenticator : authenticators) {
				if(ContentResolver.getSyncAutomatically(account, authenticator)) {
					cb.setChecked(true);
					//Log.e("Battery", "refreshAccountSyncSettings = true");
				}
			}
		}
	}

/***************************************************/
	
	private class SystemAccount {
		public String mName;
		public String mType;
		public int mIcon;
	}

	private class ViewDataHolder extends ViewDataBinder {
		public TextView mName;
		public TextView mType;
	    public ImageView mIcon;
	    public CheckBox mCheckSync;
	    
	    public ViewDataHolder( TextView name,
    						TextView type, 
    						ImageView icon,
    						CheckBox checkSync) {
	    	this.mType = type;
	    	this.mName = name;
	    	this.mIcon = icon;
	    	this.mCheckSync = checkSync;
	    }
    } //ViewHolde
	
	private class UserListAdapter extends ClickableListAdapter {

		public UserListAdapter(Context context, int viewid, List objects) {
			super(context, viewid, objects);
		}

		// this function updates the state of the list item
		// and its items. So write your logic here
		@Override
		protected void bindHolder(ViewDataBinder vdb) {
			ViewDataHolder vdh = (ViewDataHolder)vdb;
			Account data = (Account)vdb.data;
			if(data != null) {
				vdh.mName.setText(data.name);
				vdh.mType.setText(data.type);
				vdh.mIcon.setBackgroundDrawable(getDrawableForType(data.type));
				refreshAccountSyncSettings(vdh.mCheckSync, data);
			}
		}

		@Override
		protected ViewDataBinder createHolder(View v) {
			ViewDataHolder viewDataHolder;
			TextView name = (TextView)v.findViewById(R.id.account_name);
			TextView type = (TextView)v.findViewById(R.id.account_type);
			ImageView icon = (ImageView)v.findViewById(R.id.account_image);
			CheckBox checkSync = (CheckBox)v.findViewById(R.id.checkSync);
			
			viewDataHolder = new ViewDataHolder(name, type, icon, checkSync);
			
			checkSync.setOnCheckedChangeListener(new ClickableListAdapter.OnCheckedChangeListner(viewDataHolder){

				@Override
				public void onCheckedChanged(View v, ViewDataBinder vdb,
						boolean isChecked) {
					ViewDataHolder vdh = (ViewDataHolder)vdb;
					Account data = (Account)vdb.data;
					if(data != null) {
						ArrayList<String> authenticators =  getAccountAuthenticatorLists(data);
						if(authenticators.size() > 0) {
							for(String authenticator : authenticators) {
								ContentResolver.setSyncAutomatically(data, authenticator, isChecked);
							}
						}
					}
				}
			});
			
			return viewDataHolder;
		}
    }
	
	private Drawable getDrawableForType(final String accountType) 
	{
		Drawable icon = null;
		if (mAuthDesc.containsKey(accountType))	{
			try { 
				AuthenticatorDescription desc = (AuthenticatorDescription) mAuthDesc.get(accountType);
				Context authContext = createPackageContext(desc.packageName, 0);
				icon = authContext.getResources().getDrawable(desc.iconId);
			} catch (PackageManager.NameNotFoundException e) {
				// TODO: place holder icon for missing account icons?
				Log.w(BatteryWidget.TAG, "No icon for account type " + accountType); 
			} 
		}
		return icon;
	}
	
}
