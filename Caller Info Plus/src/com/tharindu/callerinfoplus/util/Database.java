package com.tharindu.callerinfoplus.util;

import java.util.ArrayList;
import java.util.List;

import com.tharindu.callerinfoplus.objects.ThreeStrings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Database
{

	private static final String DATABASE_NAME = "callerinfoplus_db";
	private static final String TABLE_NAME_CUSTOM_NOTE = "custom_note";
	private static final String TABLE_NAME_SAVE_UNKNOWN = "save_unknown";
	private static final int DATABASE_VERSION = 1;

	private DbHelper mDbHelper;
	private final Context mContext;
	private SQLiteDatabase mSqLiteDatabase;

	public static final String KEY_ROWID = "_id";

	public static final String KEY_NUMBER = "number";
	public static final String KEY_NOTE = "note";

	public static final String KEY_CALLER_NUMBER = "caller_number";
	public static final String KEY_CALLER_TIME = "caller_time";

	// public static final String KEY_TOTAL = "total";

	private static class DbHelper extends SQLiteOpenHelper
	{

		private static final String TABLE_CREATE = "create table " + TABLE_NAME_CUSTOM_NOTE + " (" + KEY_ROWID
				+ " integer primary key autoincrement, " + KEY_NUMBER + " varchar(20), " + KEY_NOTE + " text);";

		private static final String TABLE_CREATE_2 = "create table " + TABLE_NAME_SAVE_UNKNOWN + " (" + KEY_ROWID
				+ " integer primary key autoincrement, " + KEY_CALLER_NUMBER + " varchar(20), " + KEY_CALLER_TIME
				+ " varchar(20));";

		// private static final String TABLE_CREATE = "CREATE TABLE "
		// + DATABASE_TABLE_NAME + " (" + KEY_MONTH + " TEXT, "
		// + KEY_TOTAL + " TEXT);";

		DbHelper(Context context)
		{
			super( context, DATABASE_NAME, null, DATABASE_VERSION );
		}

		@Override
		public void onCreate( SQLiteDatabase db )
		{
			db.execSQL( TABLE_CREATE );
			db.execSQL( TABLE_CREATE_2 );

			db.execSQL( "INSERT INTO " + TABLE_NAME_CUSTOM_NOTE + " (" + KEY_NUMBER + "," + KEY_NOTE
					+ ") VALUES ('0711234567', 'example note');" );

			db.execSQL( "INSERT INTO " + TABLE_NAME_SAVE_UNKNOWN + " (" + KEY_CALLER_NUMBER + "," + KEY_CALLER_TIME
					+ ") VALUES ('0111111111', '2014-01-01 16:29');" );

		}

		@Override
		public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
		{
			db.execSQL( "drop table if exists " + TABLE_NAME_CUSTOM_NOTE );
			db.execSQL( "drop table if exists " + TABLE_NAME_SAVE_UNKNOWN );
			onCreate( db );
		}

	}

	public Database(Context cn)
	{
		mContext = cn;
	}

	public Database openForWrite()
	{
		mDbHelper = new DbHelper( mContext );
		mSqLiteDatabase = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		mDbHelper.close();
		mSqLiteDatabase.close();
	}

	public long insertCustomNoteEntry( String phoneNumber, String note )
	{

		ContentValues cv = new ContentValues();

		cv.put( KEY_NUMBER, phoneNumber );
		cv.put( KEY_NOTE, note );

		return mSqLiteDatabase.insert( TABLE_NAME_CUSTOM_NOTE, null, cv );
	}

	public long insertSaveUnkownEntry( String callerPhoneNumber, String callerDate )
	{

		ContentValues cv = new ContentValues();

		cv.put( KEY_CALLER_NUMBER, callerPhoneNumber );
		cv.put( KEY_CALLER_TIME, callerDate );

		return mSqLiteDatabase.insert( TABLE_NAME_SAVE_UNKNOWN, null, cv );
	}

	public int deleteCustomNoteEntry( String id )
	{

		return mSqLiteDatabase.delete( TABLE_NAME_CUSTOM_NOTE, KEY_ROWID + " = " + id, null );
	}

	public int deleteSaveUnkownEntry( String id )
	{

		return mSqLiteDatabase.delete( TABLE_NAME_SAVE_UNKNOWN, KEY_ROWID + " = " + id, null );
	}

	public int deleteCustomNoteAll()
	{

		return mSqLiteDatabase.delete( TABLE_NAME_CUSTOM_NOTE, null, null );
	}

	public int deleteSaveUnkownAll()
	{

		return mSqLiteDatabase.delete( TABLE_NAME_SAVE_UNKNOWN, null, null );
	}

	public String getCallerDateFromSavedUnknown( String number )
	{
		String[] columns = new String[] { KEY_ROWID, KEY_CALLER_NUMBER, KEY_CALLER_TIME };
		Cursor c = mSqLiteDatabase.query( TABLE_NAME_SAVE_UNKNOWN, columns, KEY_CALLER_NUMBER + " = " + number, null,
				null, null, KEY_CALLER_TIME );

		int iRowId = c.getColumnIndex( KEY_ROWID );
		int iCallerNumber = c.getColumnIndex( KEY_CALLER_NUMBER );
		int iCallerTime = c.getColumnIndex( KEY_CALLER_TIME );

		c.moveToLast();
		if (!c.isAfterLast())
		{
			return c.getString( iCallerTime );
		}
		else
		{
			return null;
		}

	}

	public String getNoteFromCustomNote( String number )
	{
		String[] columns = new String[] { KEY_ROWID, KEY_NUMBER, KEY_NOTE };
		Cursor c = mSqLiteDatabase.query( TABLE_NAME_CUSTOM_NOTE, columns, KEY_NUMBER + " = '" + number + "'", null,
				null, null, KEY_ROWID );

		int iRowId = c.getColumnIndex( KEY_ROWID );
		int iNumber = c.getColumnIndex( KEY_NUMBER );
		int iNote = c.getColumnIndex( KEY_NOTE );

		c.moveToFirst();
		if (!c.isAfterLast())
		{
			return c.getString( iNote );
		}
		else
		{
			return null;
		}

	}

	public List<ThreeStrings> readCustomNoteData()
	{

		String[] columns = new String[] { KEY_ROWID, KEY_NUMBER, KEY_NOTE };
		Cursor c = mSqLiteDatabase.query( TABLE_NAME_CUSTOM_NOTE, columns, null, null, null, null, KEY_NUMBER );

		int iRowId = c.getColumnIndex( KEY_ROWID );
		int iNumber = c.getColumnIndex( KEY_NUMBER );
		int iNote = c.getColumnIndex( KEY_NOTE );

		List<ThreeStrings> threeStringsList = new ArrayList<ThreeStrings>();

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
		{
			// String id = c.getString(iRowId);
			threeStringsList
					.add( new ThreeStrings( c.getString( iRowId ), c.getString( iNumber ), c.getString( iNote ) ) );
		}

		return threeStringsList;

	}

	public List<ThreeStrings> readSaveUnkwonData()
	{

		String[] columns = new String[] { KEY_ROWID, KEY_CALLER_NUMBER, KEY_CALLER_TIME };
		Cursor c = mSqLiteDatabase.query( TABLE_NAME_SAVE_UNKNOWN, columns, null, null, null, null, KEY_CALLER_TIME
				+ " DESC" );

		int iRowId = c.getColumnIndex( KEY_ROWID );
		int iCallerNumber = c.getColumnIndex( KEY_CALLER_NUMBER );
		int iCallerTime = c.getColumnIndex( KEY_CALLER_TIME );

		List<ThreeStrings> threeStringsList = new ArrayList<ThreeStrings>();

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
		{
			// String id = c.getString(iRowId);
			threeStringsList.add( new ThreeStrings( c.getString( iRowId ), c.getString( iCallerNumber ), c
					.getString( iCallerTime ) ) );
		}

		return threeStringsList;

	}
}
