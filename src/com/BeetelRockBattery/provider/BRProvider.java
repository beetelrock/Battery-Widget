package com.BeetelRockBattery.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;

public class BRProvider {

	private static final String DATA_BASE_NAME = "beetelRockBattery.db";	
	private static final int DATA_BASE_VERSION = 1;
	
	private static final String AUTHORITY = "BeetelRockBattery";
	
	private static final String DATA_BASE_TABLE_BATTERY = "battery";
	private static final int DATA_BASE_TABLE_BATTERY_CODE = 1;
	public static class BatteryColumns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DATA_BASE_TABLE_BATTERY);		
		public static final String STATUS = "status";
		public static final String LEVEL = "valus";
		public static final String DATE_TIME = "date_time";
		
		public static final int COL_ID = 0;
		public static final int COL_STATUS = 1;
		public static final int COL_LEVEL = 2;
		public static final int COL_DATE_TIME = 3;
	}
	public static final String[] sBatteryProjectionMap;
	static {
		sBatteryProjectionMap = new String[]{BatteryColumns.ID, 
											BatteryColumns.STATUS,
											BatteryColumns.LEVEL,
											BatteryColumns.DATE_TIME};
	}
	
	private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURLMatcher.addURI(AUTHORITY, DATA_BASE_TABLE_BATTERY, DATA_BASE_TABLE_BATTERY_CODE);
	}
	
	private static BRProvider INSTANCE = null;
	private SQLiteDatabase mDb;
	private BRProviderDbHelper mDbHelper;
	private Context mCtx;
	
	public static BRProvider getProvider(Context ctx) throws Exception {
		if(INSTANCE == null) {
			INSTANCE = new BRProvider(ctx);
			
			try {
				INSTANCE.open();
			} catch(SQLException e) {
				INSTANCE = null;
				throw e;
			}
		}		
		return INSTANCE;
	}
	
	public static void releaseProvider() {
		INSTANCE.close();
		INSTANCE = null;
	}
	
	private BRProvider(Context ctx) {
		mCtx = ctx;
	}
	
	private BRProvider open() throws SQLException {
		
		mDbHelper = new BRProviderDbHelper(mCtx, DATA_BASE_NAME, null, DATA_BASE_VERSION);
		try {
			mDb = mDbHelper.getWritableDatabase();
		}
		catch(SQLException ex) {
			mDb = mDbHelper.getReadableDatabase();
		}
		return this;
	}
	
	private void close() {
		mDb.close();
	}
	
	public Uri insert(Uri url, ContentValues values) throws Exception {
		int match = sURLMatcher.match(url);
		long rowId = -1;
		String tableName = null;
		switch(match) {
		case DATA_BASE_TABLE_BATTERY_CODE:
			tableName = DATA_BASE_TABLE_BATTERY;
			break;
		default:
			throw new Exception("Invalid table URI = " + url);
		}
		
		rowId = mDbHelper.insertInternal(tableName, values, mDb);
		return  Uri.parse("content://" + url.getAuthority() + "/" + tableName + "/" + rowId);
	}
	
	public Cursor query(Uri url, String selection, String[] selectionArgs, String order) throws Exception {
		int match = sURLMatcher.match(url);
		String tableName = null;
		String[] projection = null;
		switch(match) {
		case DATA_BASE_TABLE_BATTERY_CODE:
			tableName = DATA_BASE_TABLE_BATTERY;
			projection = sBatteryProjectionMap;
			break;
		default:
			throw new Exception("Invalid table URI = " + url);
		}
		return mDbHelper.queryInternal(tableName, projection, selection, selectionArgs, order, mDb);
	}
	
	public int update(Uri url, String selection, ContentValues values) throws Exception {
		int match = sURLMatcher.match(url);
		String tableName = null;
		String[] projection = null;
		switch(match) {
		case DATA_BASE_TABLE_BATTERY_CODE:
			tableName = DATA_BASE_TABLE_BATTERY;
			projection = sBatteryProjectionMap;
			break;
		default:
			throw new Exception("Invalid table URI = " + url);
		}
		return mDbHelper.updateInternal(tableName, projection, selection, values, mDb);
	}
	
	public int delete(Uri url, String selection) throws Exception {
		int match = sURLMatcher.match(url);
		String tableName = null;
		switch(match) {
		case DATA_BASE_TABLE_BATTERY_CODE:
			tableName = DATA_BASE_TABLE_BATTERY;
			break;
		default:
			throw new Exception("Invalid table URI = " + url);
		}
		return mDbHelper.deleteInternal(tableName, selection, mDb);
	}
	
	private class BRProviderDbHelper extends SQLiteOpenHelper {

		public BRProviderDbHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			bootStrapDatabase(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			dropAllTables(db);
			bootStrapDatabase(db);
		}
		
		private void bootStrapDatabase(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + DATA_BASE_TABLE_BATTERY + " ( " +
					BRProvider.BatteryColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					BRProvider.BatteryColumns.STATUS + " INTEGER NOT NULL, " +
					BRProvider.BatteryColumns.LEVEL + " INTEGER NOT NULL, " +
					BRProvider.BatteryColumns.DATE_TIME + " INTEGER NOT NULL " +
					");");
		}
		
		private void dropAllTables(SQLiteDatabase db) {
			db.execSQL("drop table if exists " + DATA_BASE_TABLE_BATTERY);
		}
		
		public long insertInternal(String tableName, ContentValues values, SQLiteDatabase db) {
			return db.insert(tableName, null, values);
		}
		
		public Cursor queryInternal(String tableName, String[] projection, String selection, String[] selectionArgs, String order, SQLiteDatabase db) {
			return db.query(tableName, projection, selection, selectionArgs, null, null, order);
		}
		
		public int updateInternal(String tableName, String[] projection, String whereClause, ContentValues values, SQLiteDatabase db) {
			return db.update(tableName, values, whereClause, null);
		}
		
		public int deleteInternal(String tableName, String whereClause, SQLiteDatabase db ) {
			return db.delete(tableName, whereClause, null);
		}
	} //SmartTalkyDbHelper

}
