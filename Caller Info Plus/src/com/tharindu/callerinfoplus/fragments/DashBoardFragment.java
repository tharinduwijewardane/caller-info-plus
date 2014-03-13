package com.tharindu.callerinfoplus.fragments;

import java.util.ArrayList;
import java.util.List;

import com.tharindu.callerinfoplus.util.PreferenceHelp;
import com.tharindu.callerinfoplus.R;
import com.tharindu.callerinfoplus.R.id;
import com.tharindu.callerinfoplus.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DashBoardFragment extends Fragment
{

	private PreferenceHelp prefHelp;

	private ToggleButton tbEnableKnown;
	private ToggleButton tbEnableUnknown;
	private Spinner spDuration;
	private Spinner spPosition;
	private ToggleButton tbEnableCustomNote;
	private ToggleButton tbEnableSaveUnknown;
	private TextView tvPreviosToast;

	public static final String KEY_ENABLE_KNOWN = "key_enable_known";
	public static final String KEY_ENABLE_UNKNOWN = "key_enable_unknown";
	public static final String KEY_DURATION = "key_duration";
	public static final String KEY_POSITION = "key_position";
	public static final String KEY_ENABLE_CUSTOM_NOTE = "key_enable_custom_note";
	public static final String KEY_ENABLE_SAVE_UNKNOWN = "key_enable_save_unknown";
	public static final String KEY_PREVIOUS_TOAST = "key_previous_toast";

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{

		View convertView = inflater.inflate( R.layout.fragment_dashboard, container, false );

		if (prefHelp == null)
		{
			prefHelp = new PreferenceHelp( getActivity() );
		}

		initUI( convertView );

		return convertView;
	}

	private void initUI( View convertView )
	{
		/* tbEnableKnown */
		tbEnableKnown = ( ToggleButton ) convertView.findViewById( R.id.tbEnableKnown );
		tbEnableKnown.setChecked( prefHelp.getPrefBool( KEY_ENABLE_KNOWN ) );
		tbEnableKnown.setOnCheckedChangeListener( new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
			{
				if (isChecked)
				{
					prefHelp.savePref( KEY_ENABLE_KNOWN, true );
				}
				else
				{
					prefHelp.savePref( KEY_ENABLE_KNOWN, false );
				}
			}
		} );

		/* tbEnableUnknown */
		tbEnableUnknown = ( ToggleButton ) convertView.findViewById( R.id.tbEnableUnknown );
		tbEnableUnknown.setChecked( prefHelp.getPrefBool( KEY_ENABLE_UNKNOWN ) );
		tbEnableUnknown.setOnCheckedChangeListener( new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
			{
				if (isChecked)
				{
					prefHelp.savePref( KEY_ENABLE_UNKNOWN, true );
				}
				else
				{
					prefHelp.savePref( KEY_ENABLE_UNKNOWN, false );
				}
			}
		} );

		/* spDuration */
		spDuration = ( Spinner ) convertView.findViewById( R.id.spDuration );
		List<Integer> listDuration = new ArrayList<Integer>();
		for (int i = 2; i <= 60; i += 2)
		{
			listDuration.add( i );
		}
		ArrayAdapter<Integer> dataAdapterDuration = new ArrayAdapter<Integer>( getActivity(),
				android.R.layout.simple_spinner_item, listDuration );
		dataAdapterDuration.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		spDuration.setAdapter( dataAdapterDuration );
		int selectedDuration = prefHelp.getPrefInt( KEY_DURATION );
		if (selectedDuration == 0)
		{
			spDuration.setSelection( ( 16 / 2 ) - 1 ); // default duration is 16. obtaining its position
		}
		else
		{
			spDuration.setSelection( ( selectedDuration / 2 ) - 1 ); // to get the position
		}
		spDuration.setOnItemSelectedListener( new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
			{
				prefHelp.savePref( KEY_DURATION, ( Integer ) spDuration.getSelectedItem() );
			}

			@Override
			public void onNothingSelected( AdapterView<?> arg0 )
			{
				// TODO Auto-generated method stub
			}
		} );

		/* spPosition */
		spPosition = ( Spinner ) convertView.findViewById( R.id.spPosition );
		List<Integer> listPosition = new ArrayList<Integer>();
		for (int i = 2; i <= 100; i += 2)
		{
			listPosition.add( i );
		}
		ArrayAdapter<Integer> dataAdapterPosition = new ArrayAdapter<Integer>( getActivity(),
				android.R.layout.simple_spinner_item, listPosition );
		dataAdapterPosition.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		spPosition.setAdapter( dataAdapterPosition );
		int selectedPosition = prefHelp.getPrefInt( KEY_POSITION );
		if (selectedPosition == 0)
		{
			spPosition.setSelection( ( 12 / 2 ) - 1 ); // default position is 12. obtaining its position
		}
		else
		{
			spPosition.setSelection( ( selectedPosition / 2 ) - 1 ); // to get the position
		}
		spPosition.setOnItemSelectedListener( new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
			{
				prefHelp.savePref( KEY_POSITION, ( Integer ) spPosition.getSelectedItem() );

				String position = prefHelp.getPrefString( DashBoardFragment.KEY_POSITION );
				int posValue = Integer.parseInt( position );
				DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
				posValue = posValue * displayMetrics.heightPixels / 100; // divide the device height into 100 and multiply
				Toast toast = Toast.makeText( getActivity(), "-- note goes --\n\t-- here --", Toast.LENGTH_SHORT );
				toast.setGravity( Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, posValue );
				toast.show();
			}

			@Override
			public void onNothingSelected( AdapterView<?> arg0 )
			{
				// TODO Auto-generated method stub
			}
		} );

		/* tbEnableCustomNote */
		tbEnableCustomNote = ( ToggleButton ) convertView.findViewById( R.id.tbEnableCustomNote );
		tbEnableCustomNote.setChecked( prefHelp.getPrefBool( KEY_ENABLE_CUSTOM_NOTE ) );
		tbEnableCustomNote.setOnCheckedChangeListener( new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
			{
				if (isChecked)
				{
					prefHelp.savePref( KEY_ENABLE_CUSTOM_NOTE, true );
				}
				else
				{
					prefHelp.savePref( KEY_ENABLE_CUSTOM_NOTE, false );
				}
			}
		} );

		/* tbEnableSaveUnknown */
		tbEnableSaveUnknown = ( ToggleButton ) convertView.findViewById( R.id.tbEnableSaveUnknown );
		tbEnableSaveUnknown.setChecked( prefHelp.getPrefBool( KEY_ENABLE_SAVE_UNKNOWN ) );
		tbEnableSaveUnknown.setOnCheckedChangeListener( new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
			{
				if (isChecked)
				{
					prefHelp.savePref( KEY_ENABLE_SAVE_UNKNOWN, true );
				}
				else
				{
					prefHelp.savePref( KEY_ENABLE_SAVE_UNKNOWN, false );
				}
			}
		} );

		/* previous toast */
		tvPreviosToast = ( TextView ) convertView.findViewById( R.id.tvPrevToast );
		String note = prefHelp.getPrefString( KEY_PREVIOUS_TOAST );
		if (!note.equals( "0" ))
		{
			tvPreviosToast.setText( "Previously displayed note:\n" + note );
		}
	}
}
