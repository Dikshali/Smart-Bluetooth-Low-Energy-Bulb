package com.practice.ble;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String TAG= "Main Activity";
    private final static int REQUEST_ENABLE_BT = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothAdapter bluetoothAdapter;
    Boolean btScanning = false;
    UUID SERVICE_ID = UUID.fromString("df500a63-02dd-c22b-1a3d-9c57281452e0");
    //UUID SERVICE_ID = UUID.fromString("df675fb2-174a-3ed4-17fe-3fc0a8c19cbd");
    UUID CHARACTERISTIC_BULB = UUID.fromString("fb959362-f26e-43a9-927c-7e17d8fb2d8d");
    UUID CHARACTERISTIC_TEMP = UUID.fromString("0ced9345-b31f-457d-a6a2-b3db9b03e39a");
    UUID CHARACTERISTIC_BEEP = UUID.fromString("ec958823-f26e-43a9-927c-7e17d8f32a90");
    Button beepBtn;
    TextView connStatus, temperature;
    ToggleButton bulbSwitch;

    BluetoothLeScanner btScanner;
    BluetoothGatt bluetoothGatt;
    BluetoothGattService gattService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        btScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect peripherals.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
        beepBtn = findViewById(R.id.beep_button);
        connStatus = findViewById(R.id.status_text_view);
        connStatus.setText("Scanning...");
        temperature = findViewById(R.id.temperature_text_view);
        bulbSwitch = findViewById(R.id.bulb_Switch);

        startScanning();

        bulbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BluetoothGattCharacteristic charac = gattService.getCharacteristic(CHARACTERISTIC_BULB);
                if (charac == null) {
                    Log.e("GATT", "char not found!");
                }
                if (isChecked) {
                    /*byte[] value = new byte[1];
                    value[0] = (byte) (1);
                    charac.setValue(value);
                    boolean status = bluetoothGatt.writeCharacteristic(charac);*/
                    //return status;
                    Toast.makeText(MainActivity.this, "Switch Blub ON : ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Switch Blub OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
        beepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            bluetoothGatt = result.getDevice().connectGatt(MainActivity.this, false, btleGattCallback);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "Scan Batch");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Scan Failed");
        }
    };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            switch (newState) {
                case 2:

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            connStatus.setText("Connected");
                        }
                    });
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {

                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                }
            });
            List<BluetoothGattService> gattServices = bluetoothGatt.getServices();

            for (BluetoothGattService gattService : gattServices) {

                final String uuid = gattService.getUuid().toString();
                Log.d(TAG,"Service discovered: " + uuid);
                if(!uuid.equals(SERVICE_ID.toString())){
                    continue;
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (final BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {

                    final String charUuid = gattCharacteristic.getUuid().toString();
                    Log.d(TAG,"Characteristic discovered for service: " + charUuid);
                    boolean read = bluetoothGatt.readCharacteristic(gattCharacteristic);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Log.d(TAG,"Characteristic discovered for service: "+charUuid+"\n");

                            /*if(charUuid.equals(CHARACTERISTIC_BULB.toString())){
                                byte[] val = gattCharacteristic.getValue();
                                //bulbSwitch.setChecked();
                                Log.d(TAG, "value : "+val[0]+" : "+val.toString());
                            }*/
                        }
                    });

                }
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String uuid = characteristic.getUuid().toString();
                String ch = CHARACTERISTIC_BULB.toString();
                if(uuid.equals(ch)){
                    byte[] val = characteristic.getValue();
                    int i = ((int) val[0]);
                    /*int i = ((val[0] & 0xff) | (val[1] << 8)) << 16 >> 16;*/
                    Log.d(TAG, +val[0]+"  == "+" : "+i+" ");
                    //bulbSwitch.setChecked();
                }
            }
        }
    };

    public void startScanning() {
        Log.d(TAG, "start scanning");
        btScanning = true;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_ID)).build();
                List<ScanFilter> filters = new ArrayList<>();
                filters.add(scanFilter);
                final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                Log.d("ok","starting....");
                btScanner.startScan(filters, settings, leScanCallback);
            }
        });
    }


    public void stopScanning() {
        Log.d(TAG, "stopping scanning");
        btScanning = false;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

}
