package com.example.ledrgb_control_ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DevicesScan_Activity extends Activity {
    
    private BluetoothAdapter bluetoothAdapter;
    private BLEDeviceListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 5000;

    private Button ScanBtn;
    private Button StopBtn;
    private ListView scanResult;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Thiết bị không hỗ trợ BLE!", Toast.LENGTH_LONG).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        ScanBtn = (Button) findViewById(R.id.btnScan);
        StopBtn = (Button) findViewById(R.id.btnStop);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth không hỗ trợ" ,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ScanBtn.setOnClickListener(v -> {
            listAdapter.clear();
            scanDevice(true);
        });

        StopBtn.setOnClickListener(v -> scanDevice(false));
        scanResult = findViewById(R.id.listDevice);
        listAdapter = new BLEDeviceListAdapter(R.layout.ble_row,this);
        scanResult.setAdapter(listAdapter);
        scanResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DevicesScan_Activity.this, Control_Activity.class);
                BluetoothDevice device = listAdapter.getDevice(position);
                intent.putExtra("name",device.getName());
                intent.putExtra("address",device.getAddress());

                bluetoothAdapter.stopLeScan(leScanCallback);
                startActivity(intent);
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listAdapter.clear();
                scanDevice(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },SCAN_PERIOD);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
        scanDevice(true);
    }

    private void scanDevice(final boolean enable){
        if(enable){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(ScanBtn != null && StopBtn != null){
                        ScanBtn.setEnabled(true);
                        StopBtn.setEnabled(false);
                    }
                    findViewById(R.id.scanning).setVisibility(View.INVISIBLE);
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            },SCAN_PERIOD);
            ScanBtn.setEnabled(false);
            StopBtn.setEnabled(true);
            findViewById(R.id.scanning).setVisibility(View.VISIBLE);
            bluetoothAdapter.startLeScan(leScanCallback);
        }else {
            if(ScanBtn != null && StopBtn != null){
                ScanBtn.setEnabled(true);
                StopBtn.setEnabled(false);
                findViewById(R.id.scanning).setVisibility(View.VISIBLE);
            }
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listAdapter.addDevice(device);
                    listAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanDevice(false);
        listAdapter.clear();
    }


}