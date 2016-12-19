
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.sam.sc;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BTMainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    public static final String TAG = "nRFUART";
    private static final int BT_MAIN = 123456;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static final int RC_HANDLE_BT_FINE_PERM = 2;
    private static int sTabPA = 0;
    private static int sTabPB = 0;
    private static int sTabPC = 0;
    private static int sTabPD = 0;
    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend;
    //Kota add 12/13/2016 fron this line*********************************************:
    private int mCurrentAmountPA = 0;
    private int mCurrentAmountPB = 0;
    private int mCurrentAmountPC = 0;
    private int mCurrentAmountPD = 0;
    private double PriceA = 10;
    private double PriceB = 5;
    private double PriceC = 15;
    private double PriceD = 50;
    private double TotalPrice = 0;
    private double WeightA = 1;
    private double WeightB = 2;
    private double WeightC = 3;
    private double WeightD = 4;
    private double TotalWeight = 0;


    private Button mbuttonF;
    private Button mbuttonR;
    private Button mbuttonL;
    private Button mbuttonS;
    private Button mbuttonB;

    private Button mcancelPA;
    private Button mcancelPB;
    private Button mcancelPC;
    private Button mcancelPD;
    private Button mbutton_check;
    //by this line

    private EditText edtMessage;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        //Kota add 12/13/2016

                        //
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        //Kota **************************
                        mbuttonB.setEnabled(true);
                        mbuttonF.setEnabled(true);
                        mbuttonL.setEnabled(true);
                        mbuttonS.setEnabled(true);
                        mbuttonR.setEnabled(true);

                        mcancelPA.setEnabled(true);
                        mcancelPB.setEnabled(true);
                        mcancelPC.setEnabled(true);
                        mcancelPD.setEnabled(true);
                        mbutton_check.setEnabled(true);
                        //**************************:
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        //KOTA **************************
                        mbuttonB.setEnabled(false);
                        mbuttonF.setEnabled(false);
                        mbuttonL.setEnabled(false);
                        mbuttonS.setEnabled(false);
                        mbuttonR.setEnabled(false);

                        mcancelPA.setEnabled(true);
                        mcancelPB.setEnabled(true);
                        mcancelPC.setEnabled(true);
                        mcancelPD.setEnabled(true);

                        mbutton_check.setEnabled(false);

                        //********************************
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            boolean flag = false;
                            if (text.contains("weight")) {
                                checkweight(text);
                            }
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };
    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private void checkweight(String text) {
        TextView weightState = (TextView) findViewById(R.id.weight);
        String regexString = Pattern.quote("<") + "(.*?\\d+)" + Pattern.quote(">");
        Pattern pattern = Pattern.compile(regexString);
// text contains the full text that you want to extract data
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String textInBetween = matcher.group(1); // Since (.*?) is capturing group 1
            System.out.println(textInBetween);
            String flag = null;
            Double ObtainedWeight = Double.parseDouble(textInBetween);
            Double Error = (TotalWeight - ObtainedWeight) % 10;
            System.out.println(Error);
            if (Error == 0) {
                flag = "true";
                weightState.setText("Weight matches!");
            } else {
                flag = "false";
                weightState.setText("Weight doesn't match!");
            }

            //setting flag to true for testing
            //when flag is false
            if (flag.contains("false")) {
                Toast toast = Toast.makeText(getApplicationContext(), "Weight Mismatch,Please Remove or Scan Item", Toast.LENGTH_SHORT);
                toast.show();

            }
            sendBTValue(flag);
        }

    }

    private void sendBTValue(String stringmessage) {
        byte[] value;
        try {

            //send data to service
            String message = stringmessage;
            value = message.getBytes("UTF-8");
            mService.writeRXCharacteristic(value);
            //Update the log with time stamp
            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
            edtMessage.setText("");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (rc == PackageManager.PERMISSION_GRANTED) {

        } else {
            requestBTPermission();
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        btnSend = (Button) findViewById(R.id.sendButton);
        //Kota add 12/13/2016 from this line***********************************

        mbuttonF = (Button) findViewById(R.id.buttonF);
        mbuttonB = (Button) findViewById(R.id.buttonB);
        mbuttonL = (Button) findViewById(R.id.buttonL);
        mbuttonR = (Button) findViewById(R.id.buttonR);
        mbuttonS = (Button) findViewById(R.id.buttonS);


        mcancelPA = (Button) findViewById(R.id.cancelPA);
        mcancelPB = (Button) findViewById(R.id.cancelPB);
        mcancelPC = (Button) findViewById(R.id.cancelPC);
        mcancelPD = (Button) findViewById(R.id.cancelPD);

        mbutton_check = (Button) findViewById(R.id.button_check);
        //by this line**********************************************************

        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btnConnectDisconnect.getText().equals("Connect")) {

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(BTMainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();

                        }
                    }
                }
            }
        });
        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        // Set initial UI state
//Kota add 12/13/2016 from this line***************************************************************


        mbuttonF.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "forward\r\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        mbuttonB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "back\r\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        mbuttonL.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "left\r\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        mbuttonR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "right\r\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        mbuttonS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "stop\r\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        mbutton_check.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView checkView = (TextView) findViewById(R.id.listcheck);
                checkView.setText("Thank you!!");
                TotalWeight = WeightA * mCurrentAmountPA + WeightB * mCurrentAmountPB + WeightC * mCurrentAmountPC + WeightD * mCurrentAmountPD;
                TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;

                sTabPA = 0;
                sTabPB = 0;
                sTabPC = 0;
                sTabPD = 0;
                mCurrentAmountPA = sTabPA;
                mCurrentAmountPB = sTabPB;
                mCurrentAmountPC = sTabPC;
                mCurrentAmountPD = sTabPD;
                TextView textViewA = (TextView) findViewById(R.id.listPA);
                textViewA.setText("Chapstick x " + sTabPA + "\n$" + mCurrentAmountPA * PriceA + "0");
                TextView textViewB = (TextView) findViewById(R.id.listPB);
                textViewB.setText("Deodorant x " + sTabPB + "\n$" + mCurrentAmountPB * PriceB + "0");
                TextView textViewC = (TextView) findViewById(R.id.listPA);
                textViewC.setText("Chapstick x " + sTabPC + "\n$" + mCurrentAmountPC * PriceC + "0");
                TextView textViewD = (TextView) findViewById(R.id.listPD);
                textViewD.setText("Cards x " + sTabPD + "\n$" + mCurrentAmountPD * PriceD + "0");
                //       String message = "check\r\n";
                //       byte[] value;
                //       try{
                //           //send data to service
                //           value = message.getBytes("UTF-8");
                //           mService.writeRXCharacteristic(value);
                //           //Update the log with time stamp
                //           String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //           listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                //           messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //           edtMessage.setText("");
                //       } catch (UnsupportedEncodingException e) {
                //           // TODO Auto-generated catch block
                //           e.printStackTrace();
                //       }

            }
        });


                //Bluetooth part*******************************:
                //    String message = "10\r\n";
                //    byte[] value;
                //    try{
                //        //send data to service
                //        value = message.getBytes("UTF-8");
                //        mService.writeRXCharacteristic(value);
                //        //Update the log with time stamp
                //        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //        listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                //        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //        edtMessage.setText("");
                //    } catch (UnsupportedEncodingException e) {
                //        // TODO Auto-generated catch block
                //       e.printStackTrace();
                //   }
                //***************************************************

        mcancelPA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sTabPA--;
                mCurrentAmountPA = sTabPA;
                TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                TextView textView = (TextView) findViewById(R.id.listPA);
                textView.setText("Hand Sanitizer x " + sTabPA + "\n$" + mCurrentAmountPA * PriceA + "0");
                TextView checkView = (TextView) findViewById(R.id.listcheck);
                checkView.setText("You will pay $" + TotalPrice + "0");

                //      String message = "-10\r\n";
                //      byte[] value;
                //      try{
                //          //send data to service
                //          value = message.getBytes("UTF-8");
                //          mService.writeRXCharacteristic(value);
                //          //Update the log with time stamp
                //          String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //          listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                //          messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //          edtMessage.setText("");
                //      } catch (UnsupportedEncodingException e) {
                //          // TODO Auto-generated catch block
                //          e.printStackTrace();
                //      }
            }
        });


        //       String message = "20\r\n";
        //       byte[] value;
        //       try{
        //           //send data to service
        //           value = message.getBytes("UTF-8");
        //           mService.writeRXCharacteristic(value);
        //           //Update the log with time stamp
        //           String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        //           listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
        //           messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
        //           edtMessage.setText("");
        //       } catch (UnsupportedEncodingException e) {
        //           // TODO Auto-generated catch block
        //           e.printStackTrace();
        //       }

        mcancelPB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sTabPB--;
                mCurrentAmountPB = sTabPB;
                TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                TextView textView = (TextView) findViewById(R.id.listPB);
                textView.setText("Deodorant x " + sTabPB + "\n$" + mCurrentAmountPB * PriceB + "0");
                TextView checkView = (TextView) findViewById(R.id.listcheck);
                checkView.setText("You will pay $" + TotalPrice + "0");

                //      String message = "-20\r\n";
                //      byte[] value;
                //      try{
                //           //send data to service
                //          value = message.getBytes("UTF-8");
                //          mService.writeRXCharacteristic(value);
                //          //Update the log with time stamp
                //          String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //          listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                //          messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //          edtMessage.setText("");
                //      } catch (UnsupportedEncodingException e) {
                //          // TODO Auto-generated catch block
                //          e.printStackTrace();
                //      }

            }
        });

        //       String message = "30\r\n";
        //       byte[] value;
        //       try{
        //           //send data to service
        //           value = message.getBytes("UTF-8");
        //           mService.writeRXCharacteristic(value);
        //           //Update the log with time stamp
        //           String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        //           listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
        //           messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
        //           edtMessage.setText("");
        //       } catch (UnsupportedEncodingException e) {
        //           // TODO Auto-generated catch block
        //           e.printStackTrace();
        //       }

        mcancelPC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sTabPC--;
                mCurrentAmountPC = sTabPC;
                TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                TextView textView = (TextView) findViewById(R.id.listPC);
                textView.setText("Chapstick x " + sTabPC + "\n$" + mCurrentAmountPC * PriceC + "0");
                TextView checkView = (TextView) findViewById(R.id.listcheck);
                checkView.setText("You will pay $" + TotalPrice + "0");

                //       String message = "-30\r\n";
                //       byte[] value;
                //       try{
                //           //send data to service
                //           value = message.getBytes("UTF-8");
                //           mService.writeRXCharacteristic(value);
                //           //Update the log with time stamp
                //           String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //           listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                //           messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //           edtMessage.setText("");
                //       } catch (UnsupportedEncodingException e) {
                //           // TODO Auto-generated catch block
                //           e.printStackTrace();
                //       }

            }
        });


        //      String message = "40\r\n";
        //      byte[] value;
        //      try{
        //          //send data to service
        //          value = message.getBytes("UTF-8");
        //          mService.writeRXCharacteristic(value);
        //          //Update the log with time stamp
        //          String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        //          listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
        //          messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
        //          edtMessage.setText("");
        //      } catch (UnsupportedEncodingException e) {
        //          // TODO Auto-generated catch block
        //          e.printStackTrace();
        //       }

        mcancelPD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sTabPD--;
                mCurrentAmountPD = sTabPD;
                TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                TextView textView = (TextView) findViewById(R.id.listPD);
                textView.setText("Random\n       " + mCurrentAmountPD);
                TextView checkView = (TextView) findViewById(R.id.listcheck);
                checkView.setText("You will pay $" + TotalPrice);

                //     String message = "-40\r\n";
                //     byte[] value;
                //     try{
                //         //send data to service
                //         value = message.getBytes("UTF-8");
                //         mService.writeRXCharacteristic(value);
                //         //Update the log with time stamp
                //         String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //         listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                //         messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //         edtMessage.setText("");
                //     } catch (UnsupportedEncodingException e) {
                //         // TODO Auto-generated catch block
                //         e.printStackTrace();
                //     }

            }
        });

// by this line*********************************************************************
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
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void Get_Barcode(View view) {

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
//            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
//            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

        startActivityForResult(intent, MainActivity.RC_BARCODE_CAPTURE);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.RC_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {

                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        if (barcode.displayValue.equals("036800236707")) {
                            // statusMessage.setText(R.string.barcode_success);
                            //MainActivity.barcodeValue.setText(barcode.displayValue);

                            sTabPA++;
                            mCurrentAmountPA = sTabPA;
                            TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                            TextView checkView = (TextView) findViewById(R.id.listcheck);
                            checkView.setText("You will pay $" + TotalPrice + "0");
                            TextView textView = (TextView) findViewById(R.id.listPA);
                            textView.setText("Hand Sanitizer x " + sTabPA + "\n$" + mCurrentAmountPA * PriceA + "0");
                        }
                        if (barcode.displayValue.equals("012044038918")) {
                            // statusMessage.setText(R.string.barcode_success);
                            //MainActivity.barcodeValue.setText(barcode.displayValue);
                            sTabPB++;
                            mCurrentAmountPB = sTabPB;
                            TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                            TextView checkView = (TextView) findViewById(R.id.listcheck);
                            checkView.setText("You will pay $" + TotalPrice + "0");
                            TextView textView = (TextView) findViewById(R.id.listPB);
                            textView.setText("Deodorant x " + sTabPB + "\n$" + mCurrentAmountPB * PriceB + "0");
                        }
                        if (barcode.displayValue.equals("792850157996")) {
                            // statusMessage.setText(R.string.barcode_success);
                            //MainActivity.barcodeValue.setText(barcode.displayValue);
                            sTabPC++;
                            mCurrentAmountPC = sTabPC;
                            TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                            TextView checkView = (TextView) findViewById(R.id.listcheck);
                            checkView.setText("You will pay $" + TotalPrice + "0");
                            TextView textView = (TextView) findViewById(R.id.listPC);
                            textView.setText("Chapstick x " + sTabPC + "\n$" + mCurrentAmountPC * PriceC + "0");
                        }
                        if (barcode.displayValue.equals("097257783381")) {
                            // statusMessage.setText(R.string.barcode_success);
                            //MainActivity.barcodeValue.setText(barcode.displayValue);
                            sTabPD++;
                            mCurrentAmountPD = sTabPD;
                            TotalPrice = PriceA * mCurrentAmountPA + PriceB * mCurrentAmountPB + PriceC * mCurrentAmountPC + PriceD * mCurrentAmountPD;
                            TextView checkView = (TextView) findViewById(R.id.listcheck);
                            checkView.setText("You will pay $" + TotalPrice + "0");
                            TextView textView = (TextView) findViewById(R.id.listPD);
                            textView.setText("Cards x " + sTabPD + "\n$" + mCurrentAmountPD * PriceD + "0");
                        }

                    }
                }
                break;
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

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

}
