package com.tharindu.callerinfoplus;

import com.tharindu.callerinfoplus.fragments.CustomNoteFragment;
import com.tharindu.callerinfoplus.fragments.DashBoardFragment;
import com.tharindu.callerinfoplus.fragments.SaveUnknownFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	public MyFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new DashBoardFragment();
		case 1:
			return new CustomNoteFragment();
		case 2:
			return new SaveUnknownFragment();
		}
		return new Fragment();
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "DASHBOARD";
		case 1:
			return "CUSTOM NOTE";
		case 2:
			return "SAVED UNKNOWN";
		}
		return null;
	}

}
