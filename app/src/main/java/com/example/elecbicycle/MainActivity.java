package com.example.elecbicycle;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.elecbicycle.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

class PullerThread extends Thread {

    private BluetoothAdapter adapter;
    private BluetoothSocket mmSocket = null;
    private BluetoothDevice mmDevice = null;

    private BluetoothDevice target = null;

    private final UUID MY_UUID = UUID.fromString("d5105e83-4ccf-4eed-a5bc-a9992004dfec");

    public PullerThread(BluetoothDevice device) {
        //this.adapter = mgr.getAdapter();
        //if (!adapter.isEnabled()) {
        //    Log.println(Log.VERBOSE, "BT", "Adapter disabled, enabling...");
        //    adapter.enable();
        //}
        this.target = device;
        this.connect(this.target);
    }

    public boolean discoverDevices() {


        Set<BluetoothDevice> pairedDevices = this.adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                String _name = device.getName();
                String _addr = device.getAddress();
                Log.println(Log.VERBOSE, "BT", _name);
                if (Objects.equals(_name, "STIIIII")) {
                    this.target = device;
                    this.connect(this.target);
                    Log.println(Log.VERBOSE, "BT", "Found device");
                    return true;
                }
            }
        } else {
            Log.println(Log.WARN, "BT", "No device found");
        }
        return false;
    }

    public void connect(BluetoothDevice device) {
        BluetoothSocket tmp = null;

        this.mmDevice = device;

        try {
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
//            tmp = (BluetoothSocket) (mmDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class } ).invoke(device, 1));
            Log.println(Log.VERBOSE, "BT", "Connected to device !");
        } catch (Exception pointerShit) {
            Log.e("BT", "No target device to connect");
        }

        this.mmSocket = tmp;
    }


    private boolean tryToConnect() {
        while (true) {
            try {
                this.mmSocket.connect();
                return true;
            } catch (Exception e) {
                Log.e("BT", "Error running BlueTooth", e);
                try {
                    sleep(690);
                } catch (Exception _e) {}
            }
        }
    }
    public void run() {
        byte[] buffer = new byte[1024];
        this.tryToConnect();
        try {
            InputStream input = this.mmSocket.getInputStream();
            OutputStream output = this.mmSocket.getOutputStream();

            Log.println(Log.VERBOSE, "BT", "Loaded socket");

            while (true) {
                    Log.println(Log.VERBOSE, "BT", "Attempting to read data");
                    int d = input.read(buffer);
                    Log.println(Log.VERBOSE, "BT", ("Got Data: " + d));
                    Log.println(Log.VERBOSE, "BTBuffer", new String(buffer, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Log.e("BT", "Error running BT", e);
        }
    }

    public void close() {
        try {
            this.mmSocket.close();
        } catch (IOException e) {
            Log.e("ErrorClosing", "Error closing connection", e);
        }
    }
}

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PullerThread puller;


     void loadBluetooth() {
        puller = new PullerThread(getIntent().getExtras().getParcelable("btdevice"));
    }

    void showPct(int pourcent) {
        ((TextView)findViewById(R.id.txt_pourcentage)).setText("Pourcentage : " + toString(pourcent) + "%");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                loadBluetooth();
                this.puller.start();
            } else {
                Log.e("BT", "No bluetooth permission");
            }
        } catch (Exception e) {

        }
        showPct(69);
    }
}