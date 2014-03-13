package com.tharindu.callerinfoplus;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tharindu.callerinfoplus.fragments.DashBoardFragment;
import com.tharindu.callerinfoplus.util.Database;
import com.tharindu.callerinfoplus.util.PreferenceHelp;

public class CallReceiver extends BroadcastReceiver
{

	Context context;
	int callType; // used to store and pass the call type from
					// searchInCallLogModified method
	String phonenumber;
	String contactName = " ";

	private PreferenceHelp prefHelp;
	private Database mDatabase;

	@Override
	public void onReceive( Context context, Intent intent )
	{

		this.context = context;
		Bundle bundle = intent.getExtras();
		String info;
		String callLogInfo = "";

		if (prefHelp == null)
		{
			prefHelp = new PreferenceHelp( context );
		}

		mDatabase = new Database( context );

		boolean knownEnabled = prefHelp.getPrefBool( DashBoardFragment.KEY_ENABLE_KNOWN );
		boolean unknownEnabled = prefHelp.getPrefBool( DashBoardFragment.KEY_ENABLE_UNKNOWN );
		boolean customNoteEnabled = prefHelp.getPrefBool( DashBoardFragment.KEY_ENABLE_CUSTOM_NOTE );
		boolean saveUnknownEnabled = prefHelp.getPrefBool( DashBoardFragment.KEY_ENABLE_SAVE_UNKNOWN );

		boolean known = false;

		if (knownEnabled || unknownEnabled || customNoteEnabled || saveUnknownEnabled)
		{

			if (null == bundle)
				return;

			// Log.i("IncomingCallReceiver", bundle.toString());

			String state = bundle.getString( TelephonyManager.EXTRA_STATE );

			// Log.i("IncomingCallReceiver", "State: " + state);

			if (state.equalsIgnoreCase( TelephonyManager.EXTRA_STATE_RINGING ))
			{
				phonenumber = bundle.getString( TelephonyManager.EXTRA_INCOMING_NUMBER );

				// Log.i("IncomingCallReceiver", "Incomng Number: " +
				// phonenumber);

				try
				{
					contactName = getContactDisplayNameByNumber( phonenumber );
					if (!contactName.equalsIgnoreCase( "stating_name_code1234" ))
						known = true;
				}
				catch (Exception e)
				{
					contactName = " error: failed to lookup contact ";
				}

				if ( ( known && knownEnabled ) || ( !known && unknownEnabled ))
				{

					String calledTime = " ";
					String calledTime2 = " ";
					try
					{
						calledTime = searchInCallLogModified( context, phonenumber );
						calledTime2 = searchInSavedUnknownList( phonenumber );
						if (!calledTime.equalsIgnoreCase( "starting_string" ))
						{

							// callLogInfo = Long.toString((System
							// .currentTimeMillis() - Long
							// .parseLong(calledTime)) );
							callLogInfo = prepareCallLogInfo( calledTime, callType );

						}
						else if (!calledTime2.equalsIgnoreCase( "starting_string" ))
						{

							callLogInfo = prepareCallLogInfo( calledTime2, -2 );

						}
						else
						{

							callLogInfo = "Caller has not called you before";
						}
					}
					catch (Exception e)
					{
						calledTime = "error: search in callLog failed";
					}

					if (customNoteEnabled)
					{
						callLogInfo = addCustomNote( callLogInfo, phonenumber );
					}

					if (!known && saveUnknownEnabled)
					{
						saveUnknownNumber( phonenumber );
					}

					showMyToast( callLogInfo );
					// showMyToastModified(context, callLogInfo, loops);

				}

				saveNote( phonenumber, callLogInfo );

			}
		}

	}

	private void saveNote( String phonenumber, String callLogInfo )
	{
		// saving last displayed toast
		prefHelp.savePref( DashBoardFragment.KEY_PREVIOUS_TOAST, "[" + phonenumber + "] \n" + callLogInfo );

	}

	private String searchInSavedUnknownList( String number )
	{

		String timeString = "starting_string";

		mDatabase.openForWrite();
		String s = mDatabase.getCallerDateFromSavedUnknown( number );
		mDatabase.close();
		if (s != null)
		{
			timeString = s;
		}

		return timeString;
	}

	private void saveUnknownNumber( String number )
	{

		long currentTime = System.currentTimeMillis();
		// SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
		String dateString = formatter.format( new Date( currentTime ) );

		mDatabase.openForWrite();
		mDatabase.insertSaveUnkownEntry( number, dateString );
		mDatabase.close();

	}

	private String addCustomNote( String info, String number )
	{

		mDatabase.openForWrite();
		String dataReturned = mDatabase.getNoteFromCustomNote( number );
		mDatabase.close();
		if (dataReturned != null)
		{
			info = info.concat( " \n\nNote: " + dataReturned );
		}
		return info;

	}

	private void showMyToast( String callLogInfo )
	{

		String duration = prefHelp.getPrefString( DashBoardFragment.KEY_DURATION );
		String position = prefHelp.getPrefString( DashBoardFragment.KEY_POSITION );

		int duraValue = Integer.parseInt( duration );

		int posValue = Integer.parseInt( position );

		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		posValue = posValue * displayMetrics.heightPixels / 100; // divide the device height into 100 and multiply

		int loops = duraValue / 2;
		int i = 0;

		Toast[] tst = new Toast[loops];
		for (int n = 0; n < loops; n++)
		{
			tst[n] = Toast.makeText( context, callLogInfo, Toast.LENGTH_SHORT );

			tst[n].setGravity( Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, posValue );
		}

		while (i < loops)
		{

			// LENGTH_SHORT is 2 seconds
			tst[i].show();

			i++;

		}

	}

	private String prepareCallLogInfo( String calledTime, int type )
	{

		long lastTime = Long.parseLong( calledTime );
		long currentTime = System.currentTimeMillis();
		long result = currentTime - lastTime;

		long millis = result;
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long months = days / 30;

		millis = millis % 1000;
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		days = days % 30;

		switch (type)
		{
		case Calls.INCOMING_TYPE:
			calledTime = "Called you ";
			break;
		case Calls.OUTGOING_TYPE:
			calledTime = "You dialed or called this  ";
			break;
		case Calls.MISSED_TYPE:
			calledTime = "Called you (missed)  ";
			break;
		case -2:
			calledTime = "(Saved recent unknown)  ";
			break;
		}

		if (months == 1)
			calledTime = calledTime.concat( "" + months + " month, " );
		else if (months > 0)
			calledTime = calledTime.concat( "" + months + " months, " );

		if (days == 1)
			calledTime = calledTime.concat( "" + days + " day, " );
		else if (days > 0)
			calledTime = calledTime.concat( "" + days + " days, " );

		if (hours == 1)
			calledTime = calledTime.concat( "" + hours + " hour, " );
		else if (hours > 0)
			calledTime = calledTime.concat( "" + hours + " hours, " );

		if (minutes == 1)
			calledTime = calledTime.concat( "" + minutes + " minute, " );
		else if (minutes > 0)
			calledTime = calledTime.concat( "" + minutes + " minutes, " );

		if (seconds == 1)
			calledTime = calledTime.concat( "" + seconds + " second " );
		else if (seconds > 0)
			calledTime = calledTime.concat( "" + seconds + " seconds " );

		calledTime = calledTime.concat( "ago." );

		return calledTime;

	}

	public String getContactDisplayNameByNumber( String number )
	{
		Uri uri = Uri.withAppendedPath( ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( number ) );
		String name = "stating_name_code1234";

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query( uri, new String[] { BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null );

		try
		{
			if (contactLookup != null && contactLookup.getCount() > 0)
			{
				contactLookup.moveToNext();
				name = contactLookup.getString( contactLookup.getColumnIndex( ContactsContract.Data.DISPLAY_NAME ) );
				// String contactId =
				// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		}
		finally
		{
			if (contactLookup != null)
			{
				contactLookup.close();
			}
		}

		return name;
	}

	public String searchInCallLog( Context context, String phonenumber )
	{

		Cursor c = context.getContentResolver().query( Calls.CONTENT_URI, null,
				Calls.NUMBER + "='" + phonenumber + "'", null, Calls.DATE + " DESC" );
		context.getContentResolver().cancelSync( Calls.CONTENT_URI );
		// this.startManagingCursor(c);
		String number = null;
		String date = "starting_string";
		int dateColumn = c.getColumnIndex( Calls.DATE );
		int numberColumn = c.getColumnIndex( Calls.NUMBER );
		if (c != null)
		{
			if (c.moveToFirst())
			{
				// do {
				// Get the field values
				date = c.getString( dateColumn );
				number = c.getString( numberColumn );
				// Log.d("NUmber", number);
				// Log.d("Date", date);

				// } while (c.moveToNext());
			}

			// int i = context.getContentResolver().delete(
			// Calls.CONTENT_URI,
			// Calls.NUMBER + "='" + number + "' and " + Calls.TYPE + "='"
			// + Calls.MISSED_TYPE + "'", null);

		}
		return date;
	}

	public String searchInCallLogModified( Context context, String phonenumber )
	{

		Cursor c = context.getContentResolver().query( Calls.CONTENT_URI, null,
				Calls.NUMBER + "='" + phonenumber + "'", null, Calls.DATE + " DESC" );
		context.getContentResolver().cancelSync( Calls.CONTENT_URI );
		// this.startManagingCursor(c);
		String number = null;
		String date = "starting_string";
		String type = "0";
		int dateColumn = c.getColumnIndex( Calls.DATE );
		int numberColumn = c.getColumnIndex( Calls.NUMBER );
		int typeColumn = c.getColumnIndex( Calls.TYPE );
		if (c != null)
		{
			if (c.moveToFirst())
			{
				// do {
				// Get the field values
				date = c.getString( dateColumn );
				number = c.getString( numberColumn );
				type = c.getString( typeColumn );
				// Log.d("NUmber", number);
				// Log.d("Date", date);

				// } while (c.moveToNext());
			}

			// int i = context.getContentResolver().delete(
			// Calls.CONTENT_URI,
			// Calls.NUMBER + "='" + number + "' and " + Calls.TYPE + "='"
			// + Calls.MISSED_TYPE + "'", null);

		}
		callType = Integer.parseInt( type );
		return date;

	}

}
