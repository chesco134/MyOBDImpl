package org.capiz.bluetooth;

import org.jcapiz.myobdimpl.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by jcapiz on 19/09/15.
 */
public class DevicePickerActivity extends Activity {

    private ArrayAdapter<String> arrAdapter;
    private BluetoothManager manager;
    private ListView devicesList;

    public void addDevice(String device){
        arrAdapter.add(device);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_chooser);
        manager = new BluetoothManager(this);
        if( manager.getBluetoothAdapter() != null ) {
            devicesList = (ListView)findViewById(R.id.devices_list);
            arrAdapter = manager.getmArrayAdapter();
            devicesList.setAdapter(arrAdapter);
            devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String[] deviceData = ((TextView) view).getText().toString().split("\n");
                    String deviceAddr = deviceData[1];
                    Intent i = new Intent();
                    i.putExtra("device_addr", deviceAddr);
                    setResult(RESULT_OK, i);
                    finish();
                }
            });
            manager.registerReceiver();
            manager.loadPairedDevices();
            manager.justTryToEnableBluetooth();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        manager.unregisterReceiver();
    }
}
