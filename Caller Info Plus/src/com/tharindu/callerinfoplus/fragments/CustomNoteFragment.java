package com.tharindu.callerinfoplus.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tharindu.callerinfoplus.objects.ThreeStrings;
import com.tharindu.callerinfoplus.util.Database;
import com.tharindu.callerinfoplus.R;
import com.tharindu.callerinfoplus.R.layout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class CustomNoteFragment extends ListFragment
{

	private final String NUMBER = "number";
	private final String NOTE = "note";

	private final int CONTACT_PICKER_RESULT = 111;
	private final int CALL_LOG_PICKER_RESULT = 222;

	private Database mDatabase;
	private SimpleAdapter mAdapter;
	private ArrayList<Map<String, String>> itemList;

	private Button bFromContacts;
	private Button bFromCallLog;
	private Button bSave;
	private EditText etNumber;
	private EditText etNote;
	private Button bDeleteAll;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{

		View convertView = inflater.inflate( R.layout.fragment_custom_note, container, false );

		mDatabase = new Database( getActivity() );
		initUI( convertView );

		buildListView();

		return convertView;
	}

	@Override
	public void onActivityCreated( Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );

		// long click listener
		getListView().setOnItemLongClickListener( new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick( AdapterView<?> parent, View view, final int position, long id )
			{
				HashMap<String, String> item = ( HashMap<String, String> ) parent.getItemAtPosition( position ); // get a row
				String text = "\"" + item.get( NUMBER ) + "\"";
				showConfirmDialog( text, position );
				return true;
			}

		} );

	}

	private void initUI( View convertView )
	{

		bFromContacts = ( Button ) convertView.findViewById( R.id.bFromContacts );
		bFromContacts.setOnClickListener( new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				Intent contactPickerIntent = new Intent( Intent.ACTION_PICK,
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI );
				startActivityForResult( contactPickerIntent, CONTACT_PICKER_RESULT );
			}
		} );

		bFromCallLog = ( Button ) convertView.findViewById( R.id.bFromCallLog );
		bFromCallLog.setOnClickListener( new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				showCallLogDialog();
			}
		} );

		etNumber = ( EditText ) convertView.findViewById( R.id.etNumberCustomNote );
		etNote = ( EditText ) convertView.findViewById( R.id.etNoteCustomNote );

		bSave = ( Button ) convertView.findViewById( R.id.bSaveNote );
		bSave.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				String number = etNumber.getText().toString();
				String note = etNote.getText().toString();
				if (number.equals( "" ) || note.equals( "" ))
				{
					return; // if fields are empty
				}

				mDatabase.openForWrite();
				mDatabase.insertCustomNoteEntry( number, note );
				mDatabase.close();

				buildListView(); // build the list view again with new data
				mAdapter.notifyDataSetChanged();

				etNumber.setText( "" ); // clear fields
				etNote.setText( "" );
				Toast.makeText( getActivity().getApplicationContext(), "saved", Toast.LENGTH_SHORT ).show();
			}
		} );

		bDeleteAll = ( Button ) convertView.findViewById( R.id.bDeleteAllCustomNote );
		bDeleteAll.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				showConfirmDialog( "all", -1 ); // -1 is sent to deleteAll when click ok button
			}
		} );
	}

	private void showCallLogDialog()
	{

		String[] callLogFields = { android.provider.CallLog.Calls._ID, android.provider.CallLog.Calls.NUMBER,
				android.provider.CallLog.Calls.CACHED_NAME /* im not using the name but you can */};
		String viaOrder = android.provider.CallLog.Calls.DATE + " DESC";
		// String WHERE = android.provider.CallLog.Calls.NUMBER + " >0"; /* filter out private/unknown numbers */

		final Cursor callLog_cursor = getActivity().getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, callLogFields, null, null, viaOrder );

		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

		android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		{
			public void onClick( DialogInterface dialogInterface, int item )
			{
				callLog_cursor.moveToPosition( item );

				etNumber.setText( callLog_cursor.getString( callLog_cursor
						.getColumnIndex( android.provider.CallLog.Calls.NUMBER ) ) );

				callLog_cursor.close();

			}
		};
		builder.setCursor( callLog_cursor, listener, android.provider.CallLog.Calls.NUMBER );
		builder.setTitle( "Choose from Call Log" );
		builder.create().show();
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		// TODO Auto-generated method stub
		super.onActivityResult( requestCode, resultCode, data );

		if (resultCode == CustomNoteFragment.this.getActivity().RESULT_OK)
		{
			switch (requestCode)
			{
			case CONTACT_PICKER_RESULT:
				// handle contact results

				Uri result = data.getData();
				// Log.d("My", "Got a result: " + result.toString());

				// get the phone number id from the Uri
				String id = result.getLastPathSegment();

				// query the phone numbers for the selected phone number id
				Cursor c = CustomNoteFragment.this.getActivity().getContentResolver()
						.query( Phone.CONTENT_URI, null, Phone._ID + "=?", new String[] { id }, null );

				int phoneIdx = c.getColumnIndex( Phone.NUMBER );

				if (c.getCount() == 1)
				{ // contact has a single phone number. get the only phone number
					if (c.moveToFirst())
					{
						String phone = c.getString( phoneIdx );
						Log.d( "My", "Got phone number: " + phone );

						etNumber.setText( phone ); // do something with
													// the phone number

					}
					else
					{
						// Log.w("My", "No results");
					}
				}

				break;
			}
		}
		else
		{
			// gracefully handle failure
			Log.d( "My", "Warning: activity result not ok" );
		}

	}

	private void buildListView()
	{
		String[] from = { NUMBER, NOTE };
		int[] to = { android.R.id.text1, android.R.id.text2 };

		itemList = new ArrayList<Map<String, String>>();
		mAdapter = new SimpleAdapter( getActivity(), itemList, android.R.layout.simple_list_item_2, from, to );

		mDatabase.openForWrite();
		List<ThreeStrings> threeStringsList = mDatabase.readCustomNoteData();
		mDatabase.close();

		for (ThreeStrings ss : threeStringsList)
		{
			itemList.add( putItem( ss.getString2(), ss.getString3() ) );
		}

		setListAdapter( mAdapter );
	}

	private HashMap<String, String> putItem( String number, String note )
	{
		HashMap<String, String> item = new HashMap<String, String>();
		item.put( NUMBER, number );
		item.put( NOTE, note );
		return item;
	}

	private void deleteItem( int position )
	{
		mDatabase.openForWrite();
		List<ThreeStrings> threeStringsList = mDatabase.readCustomNoteData();
		String id = threeStringsList.get( position ).getString1();
		mDatabase.deleteCustomNoteEntry( id ); // delete from database
		mDatabase.close();

		itemList.remove( position ); // delete from listview
		mAdapter.notifyDataSetChanged(); // updates listview
	}

	private void deleteAll()
	{
		mDatabase.openForWrite();
		mDatabase.deleteCustomNoteAll(); // delete all from database
		mDatabase.close();

		itemList.clear(); // delete from listview
		mAdapter.notifyDataSetChanged(); // updates listview
	}

	private void showConfirmDialog( String text, final int position )
	{
		final Dialog dialog = new Dialog( CustomNoteFragment.this.getActivity() );

		dialog.setContentView( R.layout.dialog_delete );

		dialog.setTitle( "Delete " + text + " ?" );
		dialog.show();

		Button bDelete = ( Button ) dialog.findViewById( R.id.bDelete );
		bDelete.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				if (position == -1) // sent -1 to execute deleteAll
				{
					deleteAll();
				}
				else
				{
					deleteItem( position );
				}
				dialog.dismiss();
			}
		} );

		Button bCancel = ( Button ) dialog.findViewById( R.id.bCancel );
		bCancel.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				dialog.dismiss();
			}
		} );
	}

	@SuppressLint("NewApi")
	private void copyToClipBoard( String string )
	{
		int sdk = android.os.Build.VERSION.SDK_INT; // get the sdk version

		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB)
		{ // if old version
			android.text.ClipboardManager clipboard = ( android.text.ClipboardManager ) getActivity().getSystemService(
					getActivity().CLIPBOARD_SERVICE );
			clipboard.setText( string );
		}
		else
		{ // if new version
			android.content.ClipboardManager clipboard = ( android.content.ClipboardManager ) getActivity()
					.getSystemService( getActivity().CLIPBOARD_SERVICE );
			android.content.ClipData clip = android.content.ClipData.newPlainText( "text label", string );
			clipboard.setPrimaryClip( clip );
		}
		Toast.makeText( getActivity().getApplicationContext(), "copied: " + string, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onListItemClick( ListView l, View v, int position, long id )
	{
		// Do something when a list item is clicked
		HashMap<String, String> item = ( HashMap<String, String> ) this.getListAdapter().getItem( position );
		String text = item.get( NOTE );
		copyToClipBoard( text );
	}
}
