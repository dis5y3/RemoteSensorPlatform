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

import java.io.IOException;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    //Default BT UUID = 00001101-0000-1000-8000-00805f9b34fb
    private final UUID deviceUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private static final String TAG = "MainActivity";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private String mConnectedDeviceName = null; //Name of connected device
    private StringBuffer mOutStringBuffer = null; //String buffer for outgoing messages
    private BluetoothAdapter mBTAdapter = null; //Local BT Adapter
    private BluetoothSerialService mBTService = null; //Member object for BT Services
    private String address = "B8:27:EB:B4:9F:3D"; //TODO: this needs to not be
    //^ Run 'hcitool dev' on a pi to find BT MAC Address, change the above to match
    //UI elements
    private Button btConnect = null;
    private Button btSend = null;
    private TextView connectStatus = null;
    private EditText userData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Define buttons
        btConnect = (Button) findViewById(R.id.btConnect);
        btSend = (Button) findViewById(R.id.btSend);
        connectStatus = (TextView) findViewById(R.id.lbConnectStatus);
        userData = (EditText) findViewById(R.id.etData);
        //Set initial states for things we can't handle in XML
        btSend.setEnabled(false); //don't want to send data without a connection, that kills the app
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); //Initialize Adapter
        mBTService = new BluetoothSerialService(getApplicationContext(), mHandler);
        if (mBTAdapter == null) {
            //device does not support Bluetooth!
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        }

        View.OnClickListener connectHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
                //do connection stuff
//                Intent selectDevice = new Intent(getApplicationContext(), DeviceListActivity.class);
//                startActivityForResult(selectDevice,REQUEST_CONNECT_DEVICE_SECURE);
                BluetoothDevice mBTDevice = mBTAdapter.getRemoteDevice(address);
                mBTService.connect(mBTDevice);
                if(mBTService.getState() == BluetoothSerialService.STATE_CONNECTED) {
                    Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                btSend.setEnabled(true);
                }
            }
        };
        View.OnClickListener sendDataHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"sending",Toast.LENGTH_SHORT).show();
                String message = userData.getText().toString();
                sendMessage(message);
            }
        };
        //set (now defined) OnClickListeners
        btConnect.setOnClickListener(connectHandler);
        btSend.setOnClickListener(sendDataHandler);
    }
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if(!mBTAdapter.isEnabled()) { //Request Enable if not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else if(mBTService == null) {
            //?
        }
    }

//    public void onActivityResult(int resultCode, Intent data) {
//        // When DeviceListActivity returns with a device to connect
//        Toast.makeText(getApplicationContext(),"onActivityResult called",Toast.LENGTH_SHORT).show();
//        if (resultCode == Activity.RESULT_OK) {
//            connectDevice(data);
//        }
//    }

//    /**
//     * Establish connection with other device
//     *
//     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
//     */
//    private void connectDevice(Intent data) {
//        // Get the device MAC address
//        String address = data.getExtras()
//                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//        // Get the BluetoothDevice object
//        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
//        // Attempt to connect to the device
//        mBTService.connect(device);
//    }
    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");
//
//        // Initialize the array adapter for the conversation thread
//        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
//
//        mConversationView.setAdapter(mConversationArrayAdapter);
//
//
//        // Initialize the compose field with a listener for the return key
//        mOutEditText.setOnEditorActionListener(mWriteListener);
//

        // Initialize the send button with a listener that for click events
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getCurrentFocus();
                if (null != view) {
                    String message = userData.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBTService = new BluetoothSerialService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            connectStatus.setText(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothSerialService.STATE_CONNECTING:
                            connectStatus.setText(R.string.title_connecting);
                            break;
                        case BluetoothSerialService.STATE_LISTEN:
                        case BluetoothSerialService.STATE_NONE:
                            connectStatus.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBTService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            userData.setText("");
        }
    }


/*    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
        return true;
    }*/
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
        //Do extra stuff for cleanup
        if(mBTService != null){
            mBTService.stop();
        }
    }
}