package com.example.liron.fbhack;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    int REQUST_ENABLE_BT = 4;

    OkHttpClient client = new OkHttpClient();
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    Call get(String url, String json, Callback callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

//            Toast.makeText(getApplicationContext(), action, Toast.LENGTH_SHORT).show();
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                mBluetoothAdapter.startDiscovery();
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                final int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                if (deviceName == null)
                    return;
                if (//rssi > -65 &&
                 deviceName.equals("estimote"))
                {
                    // TODO: make http request


                    String url = "http://python-service.us-east-2.elasticbeanstalk.com/bleRequest/1/"
                            + (-rssi);

                    get(url, "", new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    final String res = response.body().string();
                                   runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           Toast.makeText(getApplicationContext(), deviceName + ": " + rssi + "\n id: "
                                                           + res,
                                                   Toast.LENGTH_SHORT).show();
                                       }
                                   });

                                }
                            });
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // my code
        // register bluetooth

        int MY_PERMISSIONS_REQUEST = 200;
        int permissions=ContextCompat.checkSelfPermission (this,Manifest.permission.ACCESS_FINE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST);


        // check if bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled())
        {
            // TODO bluetooth adapter is not enabled.
            Intent enableByIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableByIntent, REQUST_ENABLE_BT);
        }


        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter);
        // start discovery
//        if (mBluetoothAdapter.startDiscovery())
//        {
//            Toast.makeText(getApplicationContext(), "Discovery Started",
//                    Toast.LENGTH_LONG).show();
//        }

        // some button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter.startDiscovery())
                {
                    Toast.makeText(getApplicationContext(),"Starting discovery...",
                            Toast.LENGTH_SHORT).show();
                }

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
