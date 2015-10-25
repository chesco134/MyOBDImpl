package org.jcapiz.myobdimpl;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	private class Zukam extends AsyncTask<String,String,String>{
		
		@Override
		protected String doInBackground(String... args){
			String result = null;
			
			return result;
		}
	}
}
