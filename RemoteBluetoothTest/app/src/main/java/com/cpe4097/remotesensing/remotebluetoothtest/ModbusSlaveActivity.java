package com.cpe4097.remotesensing.remotebluetoothtest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class ModbusSlaveActivity extends AppCompatActivity {
    public static String EXTRA_SLAVE_ADDRESS_NAMES = "slave_address_names";
    ArrayList<SlaveAdapter.ListItem> myItems = new ArrayList<>();

    //OK BUTTON PRESSED - SEND DATA ALONG
    View.OnClickListener sendNamesHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_SLAVE_ADDRESS_NAMES, myItems);
            finish();
        }
    };
    //BACK BUTTON PRESSED - SEND DATA ALONG
    //identical behavior to OK button. Change to 'keep old data' ???
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SLAVE_ADDRESS_NAMES, myItems);
        finish();
    }

    private class SlaveAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        class ListItem {
            String address;
            String friendlyName;

            void setAddress(String address) {
                this.address = address;
            }
            String getAddress() {
                return address;
            }
            void setFriendlyName(String friendlyName) {
                this.friendlyName = friendlyName;
            }
            String getFriendlyName() {
                return friendlyName;
            }
        }
        SlaveAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for(int i = 1; i < 248; i++) {
                ListItem listItem = new ListItem();
                listItem.setAddress(Integer.toString(i));
                listItem.setFriendlyName("Address" + i);
                myItems.add(listItem);
            }
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
                convertView = mInflater.inflate(R.layout.row_modbus_slave_address, null);
                holder.editTextFriendlyName = (EditText) convertView
                        .findViewById(R.id.editTextFriendlyName);
                holder.textViewAddress = (TextView) convertView
                        .findViewById(R.id.labelModbusSlaveAddress);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //Fill EditText with the value you have in data source
            holder.editTextFriendlyName.setText(myItems.get(position).getFriendlyName());
            holder.editTextFriendlyName.setId(position);
            holder.textViewAddress.setText(myItems.get(position).getAddress());

            //we need to update adapter once we finish with editing
            holder.editTextFriendlyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = v.getId();
                        final EditText etFriendlyName = (EditText) v;
                        myItems.get(position).setFriendlyName(etFriendlyName.getText().toString());
                    }
                }
            });

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modbus_slave);
        Button btSendNames = (Button) findViewById(R.id.btSendNames);
        btSendNames.setOnClickListener(sendNamesHandler);
        ListView modbusSlaveList = (ListView) findViewById(R.id.slaveList);
        SlaveAdapter slaveList = new SlaveAdapter();
        modbusSlaveList.setAdapter(slaveList);

    }
    private class ViewHolder {
        TextView textViewAddress;
        EditText editTextFriendlyName;
    }


}
