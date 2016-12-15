package com.example.sam.sc;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by vaibhavsharma on 12/13/16.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
  private final SparseArray<WeakReference<Fragment>> registeredFragments = new SparseArray<>();

  public ViewPagerAdapter(final FragmentManager supportFragmentManager) {
    super(supportFragmentManager);
  }

  @Override
  public CharSequence getPageTitle(final int position) {
    if (position == 0) {
      return "List";
    }
    return "Bluetooth";
  }

  @Override
  public Fragment getItem(final int pos) {
    if (pos == 0) {
      return MainFragment.newInstance();
    }
    return BTMainFragment.newInstance();
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    final Fragment fragment = (Fragment) super.instantiateItem(container, position);
    registeredFragments.put(position, new WeakReference<>(fragment));
    return fragment;
  }

  @Override
  public void destroyItem(final ViewGroup container, final int position, final Object object) {
    registeredFragments.remove(position);
    super.destroyItem(container, position, object);
  }

  @Override
  public int getCount() {
    return 2;
  }

  @Nullable
  public Fragment getRegisteredFragment(final int position) {
    final WeakReference<Fragment> wr = registeredFragments.get(position);
    if (wr != null) {
      return wr.get();
    } else {
      return null;
    }
  }

  @Override
  public void notifyDataSetChanged() {
    super.notifyDataSetChanged();
  }
}
