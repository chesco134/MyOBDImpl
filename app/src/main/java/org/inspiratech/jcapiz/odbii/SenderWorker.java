package org.inspiratech.jcapiz.odbii;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.capiz.bluetooth.CustomBluetoothActivity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class SenderWorker extends Thread{

	public Handler mHandler;
	private Activity activity;
	
	public SenderWorker(Activity activity){
		this.activity = activity;
	}
	
	@Override
	public void run(){
		Looper.prepare();
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				String str = (String)msg.obj;
				String remoteServerIP = getServerURL();
				if(remoteServerIP != null)
					try {
						URLConnection con = new URL("http://" + remoteServerIP + 
								"/PrimalWebServer/LogService").openConnection();
						con.setDoOutput(true);
						DataOutputStream salida = new DataOutputStream(con.getOutputStream());
						salida.write(("tul="
								+ URLEncoder.encode(str,"utf8")).getBytes());
						salida.flush();
						logSomething("Enviamos: " + str);
					} catch (IOException e) {
						logSomething("Error: " + e.getMessage());
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					logSomething("Error al obtener dirección de servidor remoto.");
			}
		};
		Looper.loop();
	}
    
    private void logSomething(String smthg){
    	try{
    		((CustomBluetoothActivity) activity).logMessage(smthg);
    	}catch(ClassCastException e){
    		e.printStackTrace();
    	}
    }
	
	private String getServerURL(){
		String url = null;
		try{
			URLConnection con = new URL("http://votacionesipn.com/services/?tag=gimmeAddr").openConnection();
			DataInputStream entrada = new DataInputStream(con.getInputStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] chunk = new byte[512];
			int bytesLeidos;
			while((bytesLeidos = entrada.read(chunk))!=-1)
				baos.write(chunk, 0, bytesLeidos);
			JSONObject json = new JSONObject(baos.toString());
			baos.close();
			entrada.close();
			url = json.getString("content");
		}catch(IOException e){
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return url;
	}
}
