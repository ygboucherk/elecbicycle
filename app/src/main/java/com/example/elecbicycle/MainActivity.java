package com.example.elecbicycle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.elecbicycle.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

class PullerThread extends Thread {

    private BluetoothAdapter adapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    private final UUID MY_UUID = UUID.fromString("d5105e83-4ccf-4eed-a5bc-a9992004dfec");

    public PullerThread() {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.discoverDevices();
    }

    public void discoverDevices() {
        Set<BluetoothDevice> pairedDevices = this.adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                String _name = device.getName();
                String _addr = device.getAddress();
            }
        }
    }

    public void connect(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        InputStream tmpIn = null;

        this.mmDevice = device;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("ConnectError", "Error connecting BlueTooth", e);
        }

        this.mmSocket = tmp;
    }

    public void run() {
        this.adapter.cancelDiscovery();
        try {
            this.mmSocket.connect();
        } catch (IOException closeException) {
            Log.e("CloseError", "Error running BlueTooth", closeException);
        }
    }

    public void cancel() {
        try {
            this.mmSocket.close();
        } catch (IOException e) {
            Log.e("ErrorClosing", "Error closing connection", e);
        }
    }

    public void send(byte[] bytes) {
        try {
            OutputStream mmOut = this.mmSocket.getOutputStream();
            mmOut.write(bytes);
            mmOut.flush();
        } catch (IOException e) {
        }
    }

    public byte[] read() {
        try {
            InputStream mmIn = this.mmSocket.getInputStream();
            return mmIn.readAllBytes();
        } catch (IOException e) {
            return ("Error").getBytes(StandardCharsets.UTF_8);
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
        ((TextView)findViewById(R.id.battery_percentage)).setText("690000%");
    }

    void testPuller() {
        puller.send(("PERCENT").getBytes(StandardCharsets.UTF_8));
        byte[] received = puller.read();
        String receivedStr = new String(received, StandardCharsets.UTF_8);
        Toast toast = Toast.makeText(MainActivity.this, receivedStr, Toast.LENGTH_LONG);
        toast.show();
    }

}