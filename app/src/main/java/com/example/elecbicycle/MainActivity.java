package com.example.elecbicycle;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

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

    public PullerThread() {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean discoverDevices() {
        Set<BluetoothDevice> pairedDevices = this.adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                String _name = device.getName();
                String _addr = device.getAddress();
                if (Objects.equals(_name, "STIIIII")) {
                    this.target = device;
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
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.println(Log.VERBOSE, "BT", "Connected to device !");
        } catch (IOException e) {
            Log.e("ConnectError", "Error connecting BlueTooth", e);
        } catch (NullPointerException pointerShit) {
            Log.e("BT", "No target device to connect");
        }

        this.mmSocket = tmp;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        if (this.discoverDevices()) {
            this.connect(this.target);
        }

        this.adapter.cancelDiscovery();
        try {
            this.mmSocket.connect();
        } catch (IOException closeException) {
            Log.e("BT", "Error running BlueTooth", closeException);
        }
        try {
            InputStream input = this.mmSocket.getInputStream();
            OutputStream output = this.mmSocket.getOutputStream();

            output.write("LEDON".getBytes(StandardCharsets.UTF_8));
            output.flush();

            output.write("PERCENT".getBytes(StandardCharsets.UTF_8));
            output.flush();

            Log.println(Log.VERBOSE, "BT", "Sent data");

            while (true) {
                int d = input.read(buffer);
                Log.println(Log.VERBOSE, "BT", ("Got Data: " + d));
            }
        } catch (IOException e) {}
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            this.puller = new PullerThread();
            this.puller.start();
        } else {
            Log.e("BT", "No bluetooth permission");
        }
    }
}