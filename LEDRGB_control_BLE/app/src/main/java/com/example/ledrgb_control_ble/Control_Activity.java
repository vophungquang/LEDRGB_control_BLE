package com.example.ledrgb_control_ble;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class Control_Activity extends Activity {

    //private final UUID TX_ID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private String sDeviceName;
    private String sDeviceAddress;

    private String LIST_NAME = "NAME";
    private String LIST_UUID = "UUID";

    private boolean isConnected = false;

    private TextView connectionStatus;
    private SeekBar sbRed,sbGreen,sbBlue;
    private int[] RGBFrame = {0,0,0};

    private RadioGroup radioGroup;
    private RadioButton nohueBtn;
    private RadioButton hueBtn;
    private RadioButton buttonOFF;
    private RadioButton rb;

    private ToggleButton tg;
    private LinearLayout Fcontrol;
    private TableLayout colorList;

    private BluetoothGattCharacteristic characteristicTX;

    private BluetoothLeService bluetoothLeService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if(!bluetoothLeService.initialize()){
                finish();
            }
            bluetoothLeService.connect(sDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                connectionStatus.setText("Connected");
            }else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                isConnected = false;
                connectionStatus.setText("Disconnected");
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            //currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        sDeviceName = intent.getStringExtra("name");
        sDeviceAddress = intent.getStringExtra("address");

        ((TextView) findViewById(R.id.txtNameDevice)).setText(sDeviceName);
        ((TextView) findViewById(R.id.txtAddressDevice)).setText(sDeviceAddress);
        connectionStatus = findViewById(R.id.txtState);
        sbRed = findViewById(R.id.Redseekbar);
        sbGreen = findViewById(R.id.Greenseekbar);
        sbBlue = findViewById(R.id.Blueseekbar);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        nohueBtn = (RadioButton) findViewById(R.id.noHueBtn);
        hueBtn = (RadioButton) findViewById(R.id.hueBtn);
        buttonOFF = (RadioButton) findViewById(R.id.buttonOFF);

        tg = (ToggleButton) findViewById(R.id.btnOnOff);
        setTableButtonsListener(findViewById(R.id.colorList));


        Fcontrol = findViewById(R.id.Fcontrol);
        colorList = findViewById(R.id.colorList);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.noHueBtn:
                                 characteristicTX.setValue("SBL");
                                 bluetoothLeService.writeCharacteristic(characteristicTX);
                        ((Button)findViewById(R.id.buttonOFF)).setEnabled(true);
                        ((Button)findViewById(R.id.buttonRED)).setEnabled(true);
                        ((Button)findViewById(R.id.buttonGREEN)).setEnabled(true);
                        ((Button)findViewById(R.id.buttonBLUE)).setEnabled(true);
                        sbBlue.setEnabled(true);
                        sbGreen.setEnabled(true);
                        sbRed.setEnabled(true);
                        Fcontrol.setVisibility(View.INVISIBLE);
                        colorList.setVisibility(View.VISIBLE);
                                 break;
                    case R.id.hueBtn:
                        characteristicTX.setValue("BLN");
                        bluetoothLeService.writeCharacteristic(characteristicTX);
                        ((Button)findViewById(R.id.buttonOFF)).setEnabled(false);
                        ((Button)findViewById(R.id.buttonRED)).setEnabled(false);
                        ((Button)findViewById(R.id.buttonGREEN)).setEnabled(false);
                        ((Button)findViewById(R.id.buttonBLUE)).setEnabled(false);
                        sbBlue.setEnabled(false);
                        sbGreen.setEnabled(false);
                        sbRed.setEnabled(false);
                        colorList.setEnabled(false);
                        Fcontrol.setVisibility(View.VISIBLE);
                        colorList.setVisibility(View.INVISIBLE);
                        break;

                    case R.id.buttonOFF:
                        sbRed.setProgress(0);
                        sbGreen.setProgress(0);
                        sbBlue.setProgress(0);
                        RGBFrame[0] = 0;
                        RGBFrame[1] = 0;
                        RGBFrame[2] = 0;
                        sendRGBFrame();
                        break;
                }
            }
        });

        tg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        bluetoothLeService.disconnect();
                    } else{
                        bluetoothLeService.connect(sDeviceAddress);
                    }
            }
        });

        ((Button)findViewById(R.id.buttonRED)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sbRed.setProgress(255);
                sbGreen.setProgress(0);
                sbBlue.setProgress(0);
                RGBFrame[0] = 255;
                RGBFrame[1] = 0;
                RGBFrame[2] = 0;
                sendRGBFrame();
                nohueBtn.setChecked(true);
            }
        });
        ((Button)findViewById(R.id.buttonGREEN)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sbRed.setProgress(0);
                sbGreen.setProgress(255);
                sbBlue.setProgress(0);
                RGBFrame[0] = 0;
                RGBFrame[1] = 255;
                RGBFrame[2] = 0;
                sendRGBFrame();
                nohueBtn.setChecked(true);
                

            }
        });
        ((Button)findViewById(R.id.buttonBLUE)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sbRed.setProgress(0);
                sbGreen.setProgress(0);
                sbBlue.setProgress(255);
                RGBFrame[0] = 0;
                RGBFrame[1] = 0;
                RGBFrame[2] = 255;
                sendRGBFrame();
                nohueBtn.setChecked(true);
            }
        });
        setSeekBarsListener(sbRed,0);
        setSeekBarsListener(sbGreen,1);
        setSeekBarsListener(sbBlue,2);

        ((SeekBar)findViewById(R.id.frequenceSb)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int fre = 10000;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fre = progress + 10000;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(Control_Activity.this,""+ fre,Toast.LENGTH_SHORT).show();
                if(isConnected){
                    characteristicTX.setValue("ND" + fre);
                    bluetoothLeService.writeCharacteristic(characteristicTX);
                }
            }
        });

        timerHandle();

        Intent gattSeviceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattSeviceIntent,serviceConnection,BIND_AUTO_CREATE);
    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver,makeGattUpdateIntentFilter());
        if(bluetoothLeService != null){
            bluetoothLeService.connect(sDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bluetoothLeService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void setSeekBarsListener(SeekBar seekBar, int position){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RGBFrame[position] = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendRGBFrame();
                nohueBtn.setChecked(true);
            }
        });
    }

    public void setTableButtonsListener(TableLayout table)
    {
        for(int i= 0 ;i < table.getChildCount();i++)
        {
            TableRow row = (TableRow) table.getChildAt(i);
            for(int j = 0 ;j < row.getChildCount();j++)
            {
                Button button = (Button) row.getChildAt(j);
                ColorDrawable color = (ColorDrawable) button.getBackground();
                ;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int colorId = color.getColor();
                        RGBFrame[0] = (colorId>>16) & 0xFF;
                        RGBFrame[1] = (colorId >> 8) & 0xFF;
                        RGBFrame[2] = (colorId >> 0) & 0xFF;
                        sbRed.setProgress(RGBFrame[0]);
                        sbGreen.setProgress(RGBFrame[1]);
                        sbBlue.setProgress(RGBFrame[2]);
                        sendRGBFrame();
                    }
                });
            }
        }
    }

    void timerHandle()
    {
        Switch state = findViewById(R.id.ledStateSw);
        TextView duration = findViewById(R.id.durationtxt);
        Button start = findViewById(R.id.startTimerbtn);
        ProgressBar progress = findViewById((R.id.progress));
        state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    characteristicTX.setValue("1");
                }
                else{
                    characteristicTX.setValue("0");
                }
                bluetoothLeService.writeCharacteristic(characteristicTX);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (duration.getText().length() < 1)
                        Toast.makeText(Control_Activity.this, "Nhập vào thời gian", Toast.LENGTH_SHORT).show();
                    else {
                        int time = Integer.parseInt(duration.getText().toString())*1000;
                        progress.setMax((int) time);
                        progress.setProgress((int) time);

                        new CountDownTimer(time,100) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                progress.setProgress((int) millisUntilFinished);
                            }
                            @Override
                            public void onFinish() {
                                state.setChecked(!state.isChecked());
                                progress.setProgress(0);
                                state.setEnabled(true);
                                start.setEnabled(true);
                            }
                        }.start();
                        state.setEnabled(false);
                        start.setEnabled(false);
                    }

            }
        });
    }

    private void sendRGBFrame()
    {
        String red = "" + RGBFrame[0]/100 + "" + RGBFrame[0]%100/10 + "" + RGBFrame[0]%100%10;
        String green = "" + RGBFrame[1]/100 + "" + RGBFrame[1]%100/10 + "" + RGBFrame[1]%100%10;
        String blue = "" + RGBFrame[2]/100 + "" + RGBFrame[2]%100/10 + "" + RGBFrame[2]%100%10;

        String frame = "RGB" + red + green + blue;
        byte[] tx = frame.getBytes();
        if(isConnected){
            characteristicTX.setValue(tx);
            bluetoothLeService.writeCharacteristic(characteristicTX);
        }
    }
}