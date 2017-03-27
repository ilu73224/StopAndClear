package com.stc.stopandclear;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final String POKEMON_PKG_NAME = "com.nianticlabs.pokemongo";
    private static final int MS_TIME_TO_CLOSE = 1500;
    private static final String TAG = "StopAndClear";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String command = "am force-stop " + POKEMON_PKG_NAME + "\n";
        command += "rm -rf /data/data/" + POKEMON_PKG_NAME + "/cache/*\n";
        new mainTask().execute(command);
    }

    private class mainTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String command = params[0];
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream opt = new DataOutputStream(process.getOutputStream());
                opt.writeBytes(command);
                opt.writeBytes("exit\n");
                opt.flush();
                process.waitFor();
                if(0 == process.exitValue()) {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
                    StringBuilder log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(String.format("%s\n", line));
                    }
                    if(0 < log.length()) Log.d(TAG, "run \""+ command +"\" success result = \n " + log.toString());
                } else {
                    Log.d(TAG, "run \""+command+"\"failed exit value =  " + process.exitValue());
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()));
                    StringBuilder log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(String.format("%s\n", line));
                    }
                    if(0 < log.length()) Log.d(TAG, "run \""+command+"\" Error = \n " + log.toString());
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(Void result) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    finishAndRemoveTask();
                }
            }, MS_TIME_TO_CLOSE);
        }
    }
}
