package org.capiz.bluetooth;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

import org.jcapiz.myobdimpl.R;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by jcapiz on 15/09/15.
 */
public class CustomBluetoothActivity extends AppCompatActivity {

	private static final int START_SERVER_ACTION = 1;
	private static final int START_CLIENT_ACTION = 2;
	private static final int START_CLIENT_TEST_ACTION = 3;
	private Button serverMode;
	private Button clientMode;
	private TextView logZone;
	private BluetoothManager manager;
	private int backButtonCount = 1;
	private boolean serverActionInProgress = false;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void makeServerAction(BluetoothSocket cliente) {
		new ServerActions(this, cliente)
				.executeOnExecutor(THREAD_POOL_EXECUTOR);
	}

	private void launchAction(int typeAction) {
		backButtonCount = 1;
		serverActionInProgress = true;
		enableModes(false);
		switch (typeAction) {
			case START_SERVER_ACTION:
				if (manager.isBluetoothEnabled()){
					manager.disableDiscoverability();
					manager.startServerMode();
				}else {
					manager.enableBluetooth();
				}
			break;
		default:
			launchClientAction(typeAction);
		}
	}

	private void launchClientAction(int clientAction){
		Intent i = new Intent(this, DevicePickerActivity.class);
		startActivityForResult(i, clientAction);
	}

	private void enableModes(boolean flag) {
		serverMode.setEnabled(flag);
		clientMode.setEnabled(flag);
	}

	public void logMessage(String message) {
		logZone.append("\n" + message);
	}

	public void putMessage(String message1, String message2) {
		logZone.append("\n" + message1 + " said: " + message2);
	}

	public void testCli(View view){
		launchAction(START_CLIENT_TEST_ACTION);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_activity);
		serverMode = (Button) findViewById(R.id.start_label);
		clientMode = (Button) findViewById(R.id.client_mode);
		logZone = (TextView) findViewById(R.id.log_field);
		manager = new BluetoothManager(this);
		if (manager.getBluetoothAdapter() == null) {
			Toast.makeText(this, "Bluetooth no disponible X.X",
					Toast.LENGTH_SHORT).show();
		} else {
			serverMode.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					launchAction(START_SERVER_ACTION);
				}
			});
			clientMode.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					launchAction(START_CLIENT_ACTION);
				}
			});
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("log_zone", logZone.getText().toString());
		outState.putInt("backButtonCount", backButtonCount);
		outState.putBoolean("serverActionInProgress", serverActionInProgress);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		logZone.setText(savedInstanceState.getString("log_zone"));
		backButtonCount = savedInstanceState.getInt("backButtonCount");
		serverActionInProgress = savedInstanceState
				.getBoolean("serverActionInProgress");
		enableModes(!serverActionInProgress);
	}

	@Override
	public void onBackPressed() {
		if (backButtonCount < 2) {
			serverActionInProgress = false;
			backButtonCount++;
			manager.stopClientActions();
			manager.stopClientActions();
			enableModes(true);
			Toast.makeText(this, "Presione una vez más para salir",
					Toast.LENGTH_SHORT).show();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (manager != null) {
			manager.stopServerActions();
			manager.stopClientActions();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case START_CLIENT_ACTION:
				manager.disableDiscoverability();
				manager.startClientModeOBDII(manager.getBluetoothAdapter()
						.getRemoteDevice(data.getStringExtra("device_addr")));
				break;
			case START_CLIENT_TEST_ACTION:
				manager.disableDiscoverability();
			manager.startClientMode(manager.getBluetoothAdapter()
					.getRemoteDevice(data.getStringExtra("device_addr")),
					"Juan", "Capiz");
				break;
			case BluetoothManager.REQUEST_ENABLE_BT:
				manager.startServerMode();
				manager.enableDiscoverability();
				break;
			default:
			}
		} else {
			if (requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
				enableModes(true);
			}
		}
	}
}