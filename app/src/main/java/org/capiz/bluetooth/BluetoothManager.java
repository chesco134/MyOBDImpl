package org.capiz.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;

import org.inspiratech.jcapiz.odbii.OBDIISetup;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Created by jcapiz on 14/09/15.
 */
public class BluetoothManager {

    private static final String MY_UUID = "4dd1cec3-6b0c-4036-a805-49b21ef84f9f";
    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 102;
    private Activity activity;
    private ServerManager sManager;
    private ClientManager cManager;
    private OBDIISetup cManagerOBDII;
    private ArrayAdapter<String> mArrayAdapter;
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("LAWAFOMECULIAOWEON123: ","Dispositivo descubierto: " + device.getName());
                // Add the name and address to an array adapter to show in a ListView
                try{
                    DevicePickerActivity a = (DevicePickerActivity)activity;
                    a.addDevice(device.getName() + "\n" + device.getAddress());
                }catch(ClassCastException e){
                    e.printStackTrace();
                }
            }
        }
    };
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);


    public BluetoothManager(Activity activity){
        this.activity = activity;
        mArrayAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1);
    }

    public ArrayAdapter<String> getmArrayAdapter(){
        return mArrayAdapter;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter;
    }

    public void enableBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void justTryToEnableBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean isBluetoothEnabled(){
        return mBluetoothAdapter.isEnabled();
    }

    public void loadPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private void logSomething(String toBeLogged){
        Log.d("BluetoothManager: ", toBeLogged);
    }

    public void registerReceiver(){
        activity.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        mBluetoothAdapter.startDiscovery();
        logSomething("Iniciando descubrimiento de dispositivos.");
    }

    public void unregisterReceiver(){
        activity.unregisterReceiver(mReceiver);
    }

    public void enableDiscoverability(){
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        activity.startActivity(discoverableIntent);
    }

    public void disableDiscoverability(){
        try{ mBluetoothAdapter.cancelDiscovery(); } catch(NullPointerException e){ e.printStackTrace(); }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startServerMode(){
        sManager = new ServerManager();
        sManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startClientMode(BluetoothDevice device, String... args){
        cManager = new ClientManager(device);
        cManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,args);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startClientModeOBDII(BluetoothDevice device, String... args){
        cManagerOBDII = new OBDIISetup(device,activity);
        cManagerOBDII.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,args);
    }

    public void stopServerActions(){
        if(sManager != null )
            sManager.stopActions();
    }

    public void stopClientActions(){
        if(cManager != null)
            cManager.stopActions();
        if(cManagerOBDII != null)
        	cManagerOBDII.stopActions();
    }

    private void logSomething(String... params){
        logSomething(params[0]);
        try{
            CustomBluetoothActivity a = (CustomBluetoothActivity)activity;
            a.logMessage(params[0]);
        }catch(ClassCastException e){
            e.printStackTrace();
        }
    }

    private class ServerManager extends AsyncTask<String,String,String> {

        private static final String MY_NAME = "Balalaika";
        private final BluetoothServerSocket serverSocket;

        public ServerManager(){
            BluetoothServerSocket zukam = null;
            try{
                zukam = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME,UUID.fromString(MY_UUID));
            } catch (IOException e){
                e.printStackTrace();
            }
            serverSocket = zukam;
        }

        @Override
        protected String doInBackground(String... args){
            String result = null;
            BluetoothSocket cliente = null;
            String[] params = new String[1];
            try{
                publishProgress("Servidor Preparado n.n");
                while(true){
                    publishProgress("Esperando cliente...");
                    cliente = serverSocket.accept();
                    try{ ((CustomBluetoothActivity)activity).makeServerAction(cliente); }
                    catch ( ClassCastException e ) { e.printStackTrace(); }
                    publishProgress("Cliente aceptado n.n");
                }
            }catch(IOException e){
                e.printStackTrace();
            }finally {
                stopActions();
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(String... params){
            logSomething(params);
        }

        public void stopActions(){
            try{
                serverSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private class ClientManager extends AsyncTask<String,String,String>{

        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ClientManager(BluetoothDevice device){
            this.device = device;
            BluetoothSocket temp = null;
            try{
                temp = this.device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            }catch(IOException e){
                e.printStackTrace();
            }
            socket = temp;
        }

        @Override
        protected String doInBackground(String... args){
            String result = null;
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name",URLEncoder.encode(args[0],"utf8"));
                jsonObject.put("lastName", URLEncoder.encode(args[1], "utf8"));
                String message = jsonObject.toString();
                socket.connect();
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                salida.write(message.getBytes());
                salida.flush();
                logSomething(message);
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                result = entrada.readUTF();
            }catch(JSONException | IOException e){
                e.printStackTrace();
                result = e.getMessage();
            }finally{
                stopActions();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String params){
            logSomething(params);
        }

        public void stopActions(){
            try{
                socket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}