package org.inspiratech.jcapiz.odbii;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.capiz.bluetooth.CustomBluetoothActivity;

public class OBDIISetup extends AsyncTask<String,String,String>{

	private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket socket;
    private final BluetoothDevice device;
	Activity activity;
	private SenderWorker rpmSender;
	private SenderWorker speedSender;
	private Message rpmMessage;
	private Message speedMessage;

    public OBDIISetup(BluetoothDevice device, Activity activity){
        this.device = device;
		this.activity = activity;
        BluetoothSocket temp = null;
		try {
			// Instantiate a BluetoothSocket for the remote device and connect it.
			temp = this.device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
		} catch (IOException e1) {
		}
        socket = temp;
        rpmSender = new SenderWorker(activity);
        speedSender = new SenderWorker(activity);
    }

    @Override
    protected String doInBackground(String... args){
        String result = null;
		RPMCommand engineRpmCommand = new RPMCommand();
		SpeedCommand speedCommand = new SpeedCommand();
		rpmSender.start();
		speedSender.start();
        BluetoothSocket sockFallback = null;
        try {
            socket.connect();
        }catch(IOException e) {
            Log.e("SKULL", "There was an error while establishing Bluetooth connection. Falling back..", e);
            publishProgress("There was an error while establishing Bluetooth connection. Falling back..\n" + e.getMessage());
            Class<?> clazz = socket.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                sockFallback.connect();
                socket = sockFallback;
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | IOException e2) {
                Log.e("SKULL", "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                result = "Couldn't fallback while establishing Bluetooth connection. Stopping app..\n" + e2.getMessage();
                //stopActions();
            }
        }
        try{

            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(0).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            publishProgress(e.getMessage());
        }
        try {
            while (!Thread.currentThread().isInterrupted())
            {
                engineRpmCommand.run(socket.getInputStream(),socket.getOutputStream());
                speedCommand.run(socket.getInputStream(),socket.getOutputStream());
                //wait(2000);
                // TODO handle commands result
                rpmMessage = rpmSender.mHandler.obtainMessage();
                rpmMessage.obj = "RPM: " + engineRpmCommand.getResult();

                speedMessage = speedSender.mHandler.obtainMessage();
                speedMessage.obj = "Speed: " + speedCommand.getResult();

                rpmSender.mHandler.sendMessage(rpmMessage);
                speedSender.mHandler.sendMessage(speedMessage);
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = e.getMessage();
        }
		stopActions();
        return result;
    }

	@Override
	public void onPostExecute(String result){
		publishSomething(result);
	}

    @Override
    public void onProgressUpdate(String... args){
        ((CustomBluetoothActivity) activity).logMessage(args[0]);
    }

    public void publishSomething(String result){
        ((CustomBluetoothActivity) activity).logMessage(result);
    }

    public void stopActions(){
    	Thread.currentThread().interrupt();
    }
}
