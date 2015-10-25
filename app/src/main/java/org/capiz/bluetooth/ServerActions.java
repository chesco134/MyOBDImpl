package org.capiz.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

/**
 * Created by jcapiz on 15/09/15.
 */
public class ServerActions extends AsyncTask<String,String,String> {

    private Activity activity;
    private InputStream iStream;
    private OutputStream oStream; // Podríamos necesitar mandar algo al cliente.
    private final BluetoothSocket socket;

    public ServerActions(Activity activity, BluetoothSocket socket){
        this.activity = activity;
        this.socket = socket;
        InputStream entrada = null;
        OutputStream salida = null;
        try {
            entrada = socket.getInputStream();
            salida = socket.getOutputStream();
        }catch(IOException e){
            e.printStackTrace();
        }
        iStream = entrada;
        oStream = salida;
    }

    @Override
    protected String doInBackground(String... args){
        String result;
        byte[] buffer = new byte[512];
        int bytesRead;
        ByteArrayOutputStream baos;
        String[] params = new String[2];
        DataInputStream entrada = new DataInputStream(iStream);
        try{
            baos = new ByteArrayOutputStream();
            Log.d("MAMAMIA: ","Estamos por leer algo...");
            bytesRead = entrada.read(buffer);
            baos.write(buffer,0,bytesRead);
            Log.d("MAMAMIA: ", "Leimos: " + baos.toString());
            JSONObject json = new JSONObject(baos.toString());
            baos.close();
            params[0] = URLDecoder.decode(json.getString("name"), "utf8");
            params[1] = URLDecoder.decode(json.getString("lastName"),"utf8");
            publishProgress(params);
            result = json.toString();
            DataOutputStream salida = new DataOutputStream(oStream);
            salida.writeUTF("ZuKam!!");
            salida.flush();
        }catch(JSONException | IOException e){
            e.printStackTrace();
            result = "Finished working n.n";
            Log.d("ServerActions: ", result);
        }finally {
            try{
                socket.close();
            }catch(IOException ex){
                ex.printStackTrace();
                Log.d("ServerActions: ", "¿Fallo al cerrar el socket cliente?");
            }
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(String... params){
        try{
            CustomBluetoothActivity mActivity = (CustomBluetoothActivity)activity;
            if(params.length == 2)
                mActivity.putMessage(params[0],params[1]);
            else
                Toast.makeText(activity,"Ups! Algo salió mal!",Toast.LENGTH_SHORT).show();
        }catch(ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String result){
        Toast.makeText(activity,result,Toast.LENGTH_SHORT).show();
    }
}