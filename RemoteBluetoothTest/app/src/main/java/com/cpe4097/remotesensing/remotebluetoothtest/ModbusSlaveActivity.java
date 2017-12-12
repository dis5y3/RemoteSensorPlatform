package com.cpe4097.remotesensing.remotebluetoothtest;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class ModbusSlaveActivity extends AppCompatActivity {
    public static final String TAG = "ModbusSlaveActivity";
    public static String EXTRA_NAMES = "slave_address_names";
    public static String EXTRA_IDS = "slave_address_ids";
    public static String EXTRA_MIN_DIFFS = "min_diffs";
    ArrayList<SlaveAdapter.ListItem> myItems = new ArrayList<>();
    ArrayList<Integer> myIDs = new ArrayList<>();
    ArrayList<Integer> myMinDiffs = new ArrayList<>();
    ArrayList<String> myFriendlyNames = new ArrayList<>();
    //FloatingActionButton myFab = null;

    //OK BUTTON PRESSED - SEND DATA ALONG
    View.OnClickListener sendNamesHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_NAMES, myFriendlyNames);
            intent.putExtra(EXTRA_IDS, myIDs);
            intent.putExtra(EXTRA_MIN_DIFFS, myMinDiffs);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
    //BACK BUTTON PRESSED - SEND DATA ALONG
    //identical behavior to OK button. Change to 'keep old data' ???
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NAMES, myFriendlyNames);
        intent.putExtra(EXTRA_IDS, myIDs);
        intent.putExtra(EXTRA_MIN_DIFFS, myMinDiffs);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class SlaveAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public void addItem(Integer count, String s, int i) {
            ListItem temp = new ListItem(count, s, i);
            myItems.add(count, temp);
            notifyDataSetChanged();
        }

        class ListItem {
            String friendlyName;
            Integer address, minDiff;

            public ListItem(Integer count, String s, Integer i) {
                this.address = count;
                this.friendlyName = s;
                this.minDiff = i;
                myFriendlyNames.add(s);
                myIDs.add(count);
                myMinDiffs.add(i);
            }

            void setAddress(Integer address) {
                this.address = address;
            }
            Integer getAddress() {
                return address;
            }
            void setFriendlyName(String friendlyName) {
                this.friendlyName = friendlyName;
            }
            String getFriendlyName() {
                return friendlyName;
            }
            void setMinDiff(Integer minDiff) {
                this.minDiff = minDiff;
            }
            Integer getMinDiff() {
                return minDiff;
            }

        }
        SlaveAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for(int i = 1; i < 2; i++) {
                ListItem listItem = new ListItem(i, "address " + i, 6);
                myFriendlyNames.add(Integer.toString(i)); //add just the friendly name to its own array for easier access outside of this activity
                myIDs.add(i);
                myMinDiffs.add(6);
            }
            Log.d(TAG, TextUtils.join(",",myIDs));
            notifyDataSetChanged();
        }
        public int getCount() {
            return myItems.size();
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.row_modbus_slave_address, parent,false);
                holder.editTextFriendlyName = convertView
                        .findViewById(R.id.editTextFriendlyName);
                holder.textViewAddress = convertView
                        .findViewById(R.id.labelModbusSlaveAddress);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //Fill EditText with the value you have in data source
            holder.editTextFriendlyName.setText(myItems.get(position).getFriendlyName());
            holder.editTextFriendlyName.setId(position);
            holder.editTextMinDiff.setId(position);
            holder.textViewAddress.setText(myItems.get(position).getAddress());

            //we need to update adapter once we finish with editing
            holder.editTextFriendlyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = v.getId();
                        final EditText etFriendlyName = (EditText) v;
                        myItems.get(position).setFriendlyName(etFriendlyName.getText().toString());
                        myFriendlyNames.set(position, etFriendlyName.getText().toString());
                        myIDs.set(position, position);
                    }
                }
            });

            holder.editTextMinDiff.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = view.getId();
                        final EditText etMinDiff = (EditText) view;
                        myItems.get(position).setFriendlyName(etMinDiff.getText().toString());
                        myMinDiffs.set(position, Integer.parseInt(etMinDiff.getText().toString()));
                        myIDs.set(position, position);
                    }
                }
            });
            return convertView;
        }
    }

    /*View.OnClickListener fabHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ListView modbusSlaveList = findViewById(R.id.slaveList);
            ListAdapter tempLA = modbusSlaveList.getAdapter();
            SlaveAdapter.ListItem newItem = new SlaveAdapter(this, SlaveAdapter.class).ListItem();
            newItem.setAddress(myItems.size());
            newItem.setMinDiff(6);
            newItem.setFriendlyName("address " + newItem.getAddress());
            myItems.add(newItem);

        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modbus_slave);
        Button btSendNames = findViewById(R.id.btSendNames);
        btSendNames.setOnClickListener(sendNamesHandler);
        final ListView modbusSlaveList = findViewById(R.id.slaveList);
        final SlaveAdapter slaveList = new SlaveAdapter();
        modbusSlaveList.setAdapter(slaveList);
        /*myFab = findViewById(R.id.myFAB);
        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slaveList.addItem(slaveList.getCount(), "address " + slaveList.getCount(), 6);
            }
        });*/
    }

    private class ViewHolder {
        TextView textViewAddress;
        EditText editTextFriendlyName;
        EditText editTextMinDiff;
    }
}
