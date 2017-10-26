//PARTS OF THIS CODE ARE TAKEN AND MODIFIED FROM THE BLUETOOTH CHAT ANDROID EXAMPLE
//SEE BELOW COPYRIGHT
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cpe4097.remotesensing.remotebluetoothtest;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MODIFY_ADDRESS_NAMES = 9;

    private String mConnectedDeviceName = null; //Name of connected device
    //private StringBuffer mOutStringBuffer = null; //String buffer for outgoing messages
    private BluetoothAdapter mBTAdapter = null; //Local BT Adapter
    private BluetoothSerialService mBTService = null; //Member object for BT Services
    //private String address = "B8:27:EB:0D:E5:7F"; //Travis' RPi
    private String address = null;
    //INFO: ^ Run 'hcitool dev' on a pi to find BT MAC Address, change the above to match
    private ArrayList<String> modbusSlaveAddressList;
    //UI elements that need to be non-local
    private Button btSend = null;
    private Button btConfigSlaveAddressNames = null;
    private TextView connectStatus = null;
    private EditText dataPollingFrequency = null;
    private EditText dataSiteID = null;
    private EditText dataAPIIntervalMin = null;
    private EditText dataAPIIntervalMax = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Define UI Elements
        Button btConnect = findViewById(R.id.btConnect);
        btSend = findViewById(R.id.btSend);
        btConfigSlaveAddressNames = findViewById(R.id.btConfigSlaveAddressNames);
        connectStatus = findViewById(R.id.lbConnectStatus);
        dataPollingFrequency = findViewById(R.id.etPollingFrequency);
        dataSiteID = findViewById(R.id.etSiteID);
        dataAPIIntervalMin = findViewById(R.id.etAPIIntervalMin);
        dataAPIIntervalMax = findViewById(R.id.etAPIIntervalMax);

        //Set initial states for things we can't handle in XML
        btSend.setEnabled(false); //Disable all non-connection stuff, don't want user thinking they can do anything without connecting
        btConfigSlaveAddressNames.setEnabled(false);
        dataPollingFrequency.setEnabled(false);
        dataSiteID.setEnabled(false);
        dataAPIIntervalMin.setEnabled(false);
        dataAPIIntervalMax.setEnabled(false);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); //Initialize Adapter
        mBTService = new BluetoothSerialService(getApplicationContext(), mHandler);
        if (mBTAdapter == null) {
            //device does not support Bluetooth!
            Toast.makeText(getApplicationContext(), R.string.no_bluetooth, Toast.LENGTH_LONG).show();
            finish();
        }

        View.OnClickListener connectHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch activity to select device (gets a MAC address)
                if (mBTService.getState() == BluetoothSerialService.STATE_NONE) {
                    // Launch the DeviceListActivity to see devices and do scan
                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }
                else //already got a device? kill it all
                if (mBTService.getState() == BluetoothSerialService.STATE_CONNECTED) {
                    mBTService.stop();
                    mBTService.start();
                }
                //do connection stuff
                BluetoothDevice mBTDevice = mBTAdapter.getRemoteDevice(address);
                mBTService.connect(mBTDevice);

                Log.d(TAG, "getState() = " + mBTService.getState());
                //Wait for connection
                new GetConnectionStatusTask().execute();
            }
        };
        View.OnClickListener sendDataHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), R.string.sending_data, Toast.LENGTH_SHORT).show();
                //build our string loosely based on Amplisine Config Model for (hopefully) easy use by other device components
                String message = TextUtils.join(",",modbusSlaveAddressList); //format Address name list, then add the other data below
                message = "{SiteID: " +dataSiteID.getText().toString() + ", Registers: [" + message +
                        "], DataPollingFrequency: " + dataPollingFrequency.getText().toString() +
                        " MinimumAPIInterval: " + dataAPIIntervalMin.getText().toString() +
                        " MaximumAPIInterval: " + dataAPIIntervalMax.getText().toString();
                sendMessage(message);
                Log.d(TAG, message);
            }
        };
        View.OnClickListener modbusSlaveAddressNameHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "opening listview", Toast.LENGTH_SHORT).show();
                Intent listIntent = new Intent(getApplicationContext(), ModbusSlaveActivity.class);
                startActivityForResult(listIntent, MODIFY_ADDRESS_NAMES);
            }
        };
                //set (now defined) OnClickListeners
        btConnect.setOnClickListener(connectHandler);
        btSend.setOnClickListener(sendDataHandler);
        btConfigSlaveAddressNames.setOnClickListener(modbusSlaveAddressNameHandler);

    }
    //The Voids are doInBackground, onProgressUpdate, and onPostExecute, respectively
    @SuppressLint("StaticFieldLeak")
    private class GetConnectionStatusTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //noinspection StatementWithEmptyBody
            while(mBTService.getState() == BluetoothSerialService.STATE_CONNECTING){
                //do nothing, wait for connection
            }
            return null;
        }

        //I don't know why I need 'useless' but it doesn't work without it.
        protected void onPostExecute(Void useless) {
            //Check connection status, do nothing if not connected (but notify user)
            if(mBTService.getState() == BluetoothSerialService.STATE_NONE) {
                Toast.makeText(getApplicationContext(), R.string.unable_to_connect, Toast.LENGTH_SHORT).show();
            }
            //Enable UI Controls if connection successful
            else if (mBTService.getState() == BluetoothSerialService.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), R.string.connected_to, Toast.LENGTH_SHORT).show();
                    btSend.setEnabled(true);
                    btConfigSlaveAddressNames.setEnabled(true);
                    dataPollingFrequency.setEnabled(true);
                    dataSiteID.setEnabled(true);
                    dataAPIIntervalMin.setEnabled(true);
                    dataAPIIntervalMax.setEnabled(true);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if(!mBTAdapter.isEnabled()) { //Request Enable if not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    //pull the device's MAC address for use elsewhere
                    try{
                        //noinspection ConstantConditions
                        address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    } catch(NullPointerException e){
                        Log.d(TAG, "NullPointerException: " + e.getLocalizedMessage());
                    }
                }
            case MODIFY_ADDRESS_NAMES:
                //When ModbusSlaveActivity returns with a new list of names
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        //noinspection ConstantConditions
                        modbusSlaveAddressList = data.getExtras().getStringArrayList(ModbusSlaveActivity.EXTRA_SLAVE_ADDRESS_NAMES);
                    } catch(NullPointerException e){
                        Log.d(TAG, "NullPointerException: " + e.getLocalizedMessage());
                    }
                }
        }
    }

    //Adapted from BluetoothChat example, mostly sets connection status string
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            connectStatus.setText(getString(R.string.connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothSerialService.STATE_CONNECTING:
                            connectStatus.setText(R.string.connecting);
                            break;
                        case BluetoothSerialService.STATE_LISTEN:
                        case BluetoothSerialService.STATE_NONE:
                            connectStatus.setText(R.string.not_connected);
                            break;
                    }
                    break;
                /*case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    break;*/
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    //Toast.makeText(getApplicationContext(), "Connected to "
                            //+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBTService.getState() != BluetoothSerialService.STATE_CONNECTED) {
            //Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBTService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            dataPollingFrequency.setText("");
            dataSiteID.setText("");
            mBTService.stop(); //disconnect? let's see if it works
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == BluetoothSerialService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBTService.start();
            }
        }
    }
    protected void onDestroy() {
        super.onDestroy(); //the usual stuff in a constructor
        //Close the connection
        if(mBTService != null){
            mBTService.stop();
        }
    }

}