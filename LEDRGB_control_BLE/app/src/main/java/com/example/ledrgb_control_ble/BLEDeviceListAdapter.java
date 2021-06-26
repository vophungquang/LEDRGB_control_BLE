package com.example.ledrgb_control_ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BLEDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> BLEDevices = new ArrayList<BluetoothDevice>();
    private int layout;
    private Context context;

    public BLEDeviceListAdapter(int layout, Context context) {
        this.layout = layout;
        this.context = context;
    }

    public void addDevice(BluetoothDevice device){
        if(!BLEDevices.contains(device)){
            BLEDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position){
        return BLEDevices.get(position);
    }

    public void clear(){
        BLEDevices.clear();
    }

    @Override
    public int getCount() {
        return BLEDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return getDevice(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layout,null);

        TextView name = (TextView) convertView.findViewById(R.id.txtName);
        TextView address  = (TextView) convertView.findViewById(R.id.txtAddress);

        BluetoothDevice device = BLEDevices.get(position);
        final String D_name = device.getName();
        if(D_name != null && D_name.length() > 0) name.setText(D_name);
        else name.setText("No name");
        address.setText(device.getAddress());
        return convertView;
    }
}
