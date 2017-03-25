package com.stc.stopandclear;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final String POKEMON_PKG_NAME = "com.nianticlabs.pokemongo";
    private static final int MS_TIME_TO_CLOSE = 1500;
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
                    finish();
                }
            }, MS_TIME_TO_CLOSE);
        }


    }
}
