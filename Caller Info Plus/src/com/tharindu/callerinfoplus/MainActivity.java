package com.tharindu.callerinfoplus;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.tharindu.callerinfoplus.R;
import com.tharindu.callerinfoplus.fragments.SaveUnknownFragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class MainActivity extends FragmentActivity
{

	private MyFragmentPagerAdapter mFragmentPagerAdapter;
	private ViewPager mViewPager;

	AdView adView;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// initialization
		mFragmentPagerAdapter = new MyFragmentPagerAdapter( getSupportFragmentManager() );
		mViewPager = ( ViewPager ) findViewById( R.id.pager );
		mViewPager.setAdapter( mFragmentPagerAdapter );
		mViewPager.setOffscreenPageLimit( 2 );

		// Create the adView
		adView = new AdView( this, AdSize.BANNER, "xxx" );

		// Lookup your LinearLayout assuming it's been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = ( LinearLayout ) findViewById( R.id.linForAd );

		// Add the adView to it
		layout.addView( adView );

		// Initiate a generic request to load it with an ad
		adView.loadAd( new AdRequest() );

	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		super.onOptionsItemSelected( item );
		switch (item.getItemId())
		{

		case R.id.about:
			
			final Dialog dialog = new Dialog( this );
			dialog.setContentView( R.layout.dialog_about );
			dialog.setTitle( "About" );
			dialog.show();
			
			break;

		}
		return true;
	}

}
