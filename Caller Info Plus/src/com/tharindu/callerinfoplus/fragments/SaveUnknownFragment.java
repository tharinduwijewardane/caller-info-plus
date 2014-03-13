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
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SaveUnknownFragment extends ListFragment
{
	
	private final String NUMBER = "number";
	private final String DATE = "date";
	
	private Database mDatabase;
	private SimpleAdapter mAdapter;
	private ArrayList<Map<String, String>> itemList;

	private Button bDeleteAll;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{

		View convertView = inflater.inflate( R.layout.fragment_save_unknown, container, false );

		mDatabase = new Database( getActivity() );
		initUI( convertView );

		// setHasOptionsMenu(true);

		buildListData();
		String[] from = { NUMBER, DATE }; // columns in listview
		int[] to = { android.R.id.text1, android.R.id.text2 }; // text styles

		mAdapter = new SimpleAdapter( getActivity(), itemList, android.R.layout.simple_list_item_2, from, to );
		setListAdapter( mAdapter );

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

		bDeleteAll = ( Button ) convertView.findViewById( R.id.bDeleteAllSaveUnknown );
		bDeleteAll.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				showConfirmDialog( "all", -1 ); // -1 is sent to deleteAll when click ok button
			}
		} );
		
	}

	private void buildListData()
	{

		itemList = new ArrayList<Map<String, String>>();
		
		mDatabase.openForWrite();
		List<ThreeStrings> threeStringsList = mDatabase.readSaveUnkwonData();
		mDatabase.close();

		for (ThreeStrings ss : threeStringsList)
		{
			itemList.add( putItem( ss.getString2(), ss.getString3() ) );
		}

	}

	private HashMap<String, String> putItem( String number, String date )
	{
		HashMap<String, String> item = new HashMap<String, String>();
		item.put( NUMBER, number );
		item.put( DATE, date );
		return item;
	}

	private void deleteItem( int position )
	{
		mDatabase.openForWrite();
		List<ThreeStrings> threeStringsList = mDatabase.readSaveUnkwonData();
		String id = threeStringsList.get( position ).getString1();
		mDatabase.deleteSaveUnkownEntry( id ); // delete from database
		mDatabase.close();

		itemList.remove( position ); // delete from listview
		mAdapter.notifyDataSetChanged(); // updates listview
	}

	private void deleteAll()
	{
		mDatabase.openForWrite();
		mDatabase.deleteSaveUnkownAll(); // delete all from database
		mDatabase.close();

		itemList.clear(); // delete from listview
		mAdapter.notifyDataSetChanged(); // updates listview
	}

	private void showConfirmDialog( String text, final int position )
	{

		final Dialog dialog = new Dialog( SaveUnknownFragment.this.getActivity() );

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
		String text = item.get( NUMBER );
		copyToClipBoard( text );
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
	{
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu( menu, inflater );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		// TODO Auto-generated method stub
		Toast.makeText( getActivity().getApplicationContext(), "test", Toast.LENGTH_SHORT ).show();
		return super.onOptionsItemSelected( item );
	}

}
