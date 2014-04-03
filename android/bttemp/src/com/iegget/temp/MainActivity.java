package com.iegget.temp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.iegget.temp.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class MainActivity extends Activity {
    private static final String TAG = "bluetooth2";

    TextView txtTemp;
    Handler h;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:14:02:25:00:26";

    private int response;
    ViewFlipper viewFlipper;
    Button graphs;
    LinearLayout graphViewLayout;
    GraphView graphView;
    Context handler;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        handler = this;

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        graphs = (Button) findViewById(R.id.graphButton);
        graphs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                viewFlipper.setInAnimation(AnimationUtils.loadAnimation(handler, R.anim.slide_in_right));
                viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(handler, R.anim.slide_out_right));
                viewFlipper.showNext();
            }
        });

        GraphViewSeries series = new GraphViewSeries(new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(0, 0.0d)
        });

        graphView = new LineGraphView(this, "");
        graphView.addSeries(series);
        graphViewLayout = (LinearLayout) findViewById(R.id.graphViewLayout);
        graphViewLayout.addView(graphView);

        txtTemp = (TextView) findViewById(R.id.txtTemp);      // for display the received data from the Arduino

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear

                            try {
                                response = Integer.parseInt(sbprint);
                            } catch (NumberFormatException e) {
                                Log.d("INT", e.toString());
                            }

                            txtTemp.setText(String.format("%.1f °C", (double) (response) / 100.0, 3));            // update TextView
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        final ListView listview = (ListView) findViewById(R.id.listView);

        final ArrayList<Food> list = new ArrayList<Food>();

        list.add(new Food("Skinkesteik", "Sykt bra", 75, Food.Type.PORK));
        list.add(new Food("Roastbiff", "Ekstremt bra", 68, Food.Type.BEEF));
        list.add(new Food("Gåsa", "Mmm gåsa", 83, Food.Type.POULTRY));
        list.add(new Food("Lammesteik", "Ganske greit", 59, Food.Type.LAMB));
        list.add(new Food("Skinkesteik", "Sykt bra", 75, Food.Type.PORK));
        list.add(new Food("Roastbiff", "Ekstremt bra", 68, Food.Type.BEEF));
        list.add(new Food("Gåsa", "Mmm gåsa", 83, Food.Type.POULTRY));
        list.add(new Food("Lammesteik", "Ganske greit", 59, Food.Type.LAMB));
        list.add(new Food("Skinkesteik", "Sykt bra", 75, Food.Type.PORK));
        list.add(new Food("Roastbiff", "Ekstremt bra", 68, Food.Type.BEEF));
        list.add(new Food("Gåsa", "Mmm gåsa", 83, Food.Type.POULTRY));
        list.add(new Food("Lammesteik", "Ganske greit", 59, Food.Type.LAMB));

        final MyArrayAdapter adapter = new MyArrayAdapter(this, list);
        listview.setAdapter(adapter);

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}