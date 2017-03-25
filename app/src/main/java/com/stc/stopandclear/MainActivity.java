package com.stc.stopandclear;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final String POKEMON_PKG_NAME = "com.nianticlabs.pokemongo";
    private static final int MS_TIME_TO_CLOSE = 1500;
    private static final String TAG = "StopAndClear";
    private static final int INVALID_STACKID = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new mainTask().execute();
    }

    private class mainTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Process pForceStop = Runtime.getRuntime().exec("su shell am force-stop " + POKEMON_PKG_NAME);
                pForceStop.waitFor();
                Process pClearCache = Runtime.getRuntime().exec("su shell rm -rf /data/data/"+POKEMON_PKG_NAME+"/cache/*");
                pClearCache.waitFor();
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
        void tryClearInRecentApp(){
            try {
                // Run the command
                Process pGetStackList = Runtime.getRuntime().exec("su shell am stack list");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(pGetStackList.getInputStream()));

                // Grab the results
                StringBuilder log = new StringBuilder();
                String line;
                int pokemonStackId = INVALID_STACKID;
                int tmpStackId=INVALID_STACKID;
                String sPrefix = "Stack id=";
                String sPostfix = " bounds=[";
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.contains(sPrefix)){
                        String sTmpStackId = line.substring(line.indexOf(sPrefix)+sPrefix.length(),
                                line.indexOf(sPostfix));
                        tmpStackId = Integer.parseInt(sTmpStackId);
                        Log.d(TAG, String.format("tmpStackId = %d\n", tmpStackId));
                    } else if (line.contains(POKEMON_PKG_NAME)){
                        pokemonStackId = tmpStackId;
                        Log.d(TAG, String.format("get %s, tmpStackId = %d, pokemonStackId = %d\n",
                                POKEMON_PKG_NAME, tmpStackId, pokemonStackId));
                    }
                    log.append(String.format("%s\n", line));
                }
                Log.d(TAG, "am stack list report :\n " + log.toString());
                Log.d(TAG, "pokemonStackId = " + pokemonStackId);
                if(INVALID_STACKID != pokemonStackId){
                    Process pClearCache = Runtime.getRuntime().exec("su shell am stack remove " + pokemonStackId);
                    pClearCache.waitFor();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

    }
}
