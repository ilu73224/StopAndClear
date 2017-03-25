package com.stc.stopandclear;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final String POKEMON_PKG_NAME = "com.nianticlabs.pokemongo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Process pForceStop = Runtime.getRuntime().exec("su shell am force-stop " + POKEMON_PKG_NAME);
            pForceStop.waitFor();
            Process pClearCache = Runtime.getRuntime().exec("su shell rm -rf /data/data/"+POKEMON_PKG_NAME+"/cache/*");
            pClearCache.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 1500);
    }
}
