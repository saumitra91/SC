package com.example.sam.sc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vaibhavsharma on 12/13/16.
 */

public class AppMainActivity extends AppCompatActivity implements BTMainFragment.BTInterface {
  public static final String TAG = "nRFUART";
  private static final int BT_MAIN = 123456;
  private static final int REQUEST_SELECT_DEVICE = 1;
  private static final int REQUEST_ENABLE_BT = 2;
  private static final int UART_PROFILE_READY = 10;
  private static final int UART_PROFILE_CONNECTED = 20;
  private static final int UART_PROFILE_DISCONNECTED = 21;
  private static final int STATE_OFF = 10;
  private static final int RC_HANDLE_BT_FINE_PERM = 2;
  TextView mRemoteRssiVal;
  RadioGroup mRg;
  private int mState = UART_PROFILE_DISCONNECTED;
  private ViewPagerAdapter pagerAdapter;

  private static IntentFilter makeGattUpdateIntentFilter() {
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
    intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
    intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
    intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
    intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
    return intentFilter;
  }

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.app_main);
    final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
    final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
    pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
        tabLayout.setScrollPosition(tab.getPosition(), 0, false);
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {

      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
        tabLayout.setScrollPosition(tab.getPosition(), 0, false);
      }
    });
  }

  private void requestBTPermission() {
    Log.w(TAG, "BT permission is not granted. Requesting permission");

    final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.CAMERA)) {
      ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_BT_FINE_PERM);
      return;
    }

    final Activity thisActivity = this;

    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ActivityCompat.requestPermissions(thisActivity, permissions,
            RC_HANDLE_BT_FINE_PERM);
      }
    };
  }

  private void service_init() {
    if (pagerAdapter != null && pagerAdapter.getRegisteredFragment(1) != null) {
      BTMainFragment fragment = (BTMainFragment) pagerAdapter.getRegisteredFragment(1);
      Intent bindIntent = new Intent(this, UartService.class);
      bindService(bindIntent, fragment.mServiceConnection, Context.BIND_AUTO_CREATE);

      LocalBroadcastManager.getInstance(this).registerReceiver(fragment.UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy()");
    if (pagerAdapter != null && pagerAdapter.getRegisteredFragment(1) != null) {
      BTMainFragment fragment = (BTMainFragment) pagerAdapter.getRegisteredFragment(1);
      try {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fragment.UARTStatusChangeReceiver);
      } catch (Exception ignore) {
        Log.e(TAG, ignore.toString());
      }
      unbindService(fragment.mServiceConnection);
      fragment.mService.stopSelf();
      fragment.mService = null;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    if (pagerAdapter != null && pagerAdapter.getRegisteredFragment(1) != null) {
      BTMainFragment fragment = (BTMainFragment) pagerAdapter.getRegisteredFragment(1);
      if (!fragment.mBtAdapter.isEnabled()) {
        Log.i(TAG, "onResume - BT not enabled yet");
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
      }
    }

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }


  private void showMessage(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

  }

  @Override
  public void onBackPressed() {
    if (mState == UART_PROFILE_CONNECTED) {
      Intent startMain = new Intent(Intent.ACTION_MAIN);
      startMain.addCategory(Intent.CATEGORY_HOME);
      startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(startMain);
      showMessage("nRFUART's running in background.\n             Disconnect to exit");
    } else {
      new AlertDialog.Builder(this)
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setTitle(R.string.popup_title)
          .setMessage(R.string.popup_message)
          .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              finish();
            }
          })
          .setNegativeButton(R.string.popup_no, null)
          .show();
    }
  }

  @Override
  public void serviceInit() {
    service_init();
  }

  @Override
  public void requestBTPermissionInActivity() {
    requestBTPermission();
  }

  @Override
  public void showMessageInActivity(final String message) {
    showMessage(message);
  }

  @Override
  public void finishActivity() {
    finish();
  }
}
