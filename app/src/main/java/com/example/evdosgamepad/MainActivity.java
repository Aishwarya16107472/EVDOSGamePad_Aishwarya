package com.example.evdosgamepad;

import android.Manifest;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.devadvance.circularseekbar.CircularSeekBar;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLEGamepad";
    private static final UUID SERVICE_UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb");

    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic dataCharacteristic;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView accelView;
    private BluetoothDevice connectedDevice;
    private  static int direction=0,YAxisDirection=0;
    private float accelX = 0f;
    private float accelY = 0f;
    private float accelZ = 0f; //why do we need z axis?

    private int throttleValue = 127;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelView = findViewById(R.id.accelerometerText);
        Button buttonA = findViewById(R.id.btnA);

        buttonA.setOnClickListener(v -> sendData("Button A Pressed"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //sdk.int>= version_codes.s?
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE // what is bluetooth advertise?
                    }, 1);
        }

        setupAccelerometer();
        //Aishwarya's changes
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startBleServer();

//break
        // --- Setup for your NEW Buttons (place this in onCreate) ---

// Find the forward button from your layout file
        Button forwardButton = findViewById(R.id.btnX);

// Set the listener for the backward button
        forwardButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if the button is being pressed down
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // TODO: Set direction to forward (e.g., direction = 1;)
                    direction = 1;
                }
                // Check if the button is being released
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // TODO: Set direction to neutral (e.g., direction = 0;)
                    direction = 0;
                }
                YAxisDirection = direction * throttleValue;

                // TODO: Create your message and call sendData() here
                //sendData(String.format("%.2f,%.2f,%.2f,%d", accelX, accelY, accelZ, throttleValue));
                String message = String.format(Locale.ROOT,"steer:%.2f,speed:%d", accelX, YAxisDirection);
                sendData(message);
                // Return true to indicate you've handled the touch event
                return true;
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (direction == 1) {
                    direction = 0;
                    Toast.makeText(MainActivity.this, "forward Toggled OFF", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "forward Button TOGGLED OFF(OnClickListener");

                } else {
                    direction = 1;
                    Toast.makeText(MainActivity.this, "forward Toggled ON", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "forward Button TOGGLED ON(OnClickListener");

                }
                String message = String.format(Locale.ROOT,"steer:%.2f,speed:%d", accelX, YAxisDirection);
                sendData(message);
            }
        });

// Find the backward button from your layout file
        Button backwardButton = findViewById(R.id.btnB);

// Set the listener for the backward button
        backwardButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if the button is being pressed down
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // TODO: Set direction to backward (e.g., direction = -1;)
                    direction = -1;
                }
                // Check if the button is being released
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // TODO: Set direction to neutral (e.g., direction = 0;)
                    direction = 0;
                }
                YAxisDirection = direction * throttleValue;

                // TODO: Create your message and call sendData() here
                //sendData(String.format("%.2f,%.2f,%.2f,%d", accelX, accelY, accelZ, throttleValue));
                String message = String.format(Locale.ROOT,"steer:%.2f,speed:%d", accelX, YAxisDirection);
                sendData(message);
                // Return true to indicate you've handled the touch event
                return true;
            }
        });
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (direction == -1) {
                    direction = 0;
                    Toast.makeText(MainActivity.this, "Backward Toggled OFF", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Backward Button TOGGLED OFF(OnClickListener");

                } else {
                    direction = -1;
                    Toast.makeText(MainActivity.this, "Backward Toggled ON", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Backward Button TOGGLED ON(OnClickListener");

                }
                String message = String.format(Locale.ROOT,"steer:%.2f,speed:%d", accelX, YAxisDirection);
                sendData(message);
            }
        });

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
                throttleValue = progress;

                sendData(String.format(Locale.ROOT,"%.2f,%.2f,%.2f,%d", accelX, accelY, accelZ, throttleValue));
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                seekBar.setProgress(127);
                throttleValue = 127;
                sendData(String.format(Locale.ROOT,"%.2f,%.2f,%.2f,%d", accelX, accelY, accelZ, throttleValue));
            }
        });


    }
//method that sends data to the CAR
    private void setupAccelerometer() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener accelListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accelX = event.values[0];
                    accelY = event.values[1];
                    accelZ = event.values[2];
                    // Update the display
                    String displayText = String.format(Locale.ROOT,"X: %.2f\nY: %.2f\nZ: %.2f", accelX, accelY, accelZ);
                    runOnUiThread(() -> accelView.setText(displayText));

                    // Send full message over Bluetooth
                    String message = String.format(Locale.ROOT,"%.2f,%.2f,%.2f,%d", accelX, accelY, accelZ, throttleValue);
                    sendData(message);
                }
            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(accelListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void startBleServer() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not available or not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            Toast.makeText(this, "BLE advertising not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .setIncludeDeviceName(true)
                .build();

        advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "BLE Advertising Started", Toast.LENGTH_SHORT).show());
                Log.i(TAG, "BLE Advertising started");
            }

            @Override
            public void onStartFailure(int errorCode) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "BLE Advertising Failed", Toast.LENGTH_SHORT).show());
                Log.e(TAG, "BLE Advertising failed: " + errorCode);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gattServer = bluetoothManager.openGattServer(this, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connectedDevice = device;
                    Log.i(TAG, "Device connected: " + device.getAddress());
                } else {
                    connectedDevice = null;
                    Log.i(TAG, "Device disconnected");
                }
            }
        });

        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        dataCharacteristic = new BluetoothGattCharacteristic(
                CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        service.addCharacteristic(dataCharacteristic);
        gattServer.addService(service);
    }

    private void sendData(String message) {
        if (connectedDevice == null || gattServer == null || dataCharacteristic == null) return;
        dataCharacteristic.setValue(message.getBytes(StandardCharsets.UTF_8));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gattServer.notifyCharacteristicChanged(connectedDevice, dataCharacteristic, false);
    }
}