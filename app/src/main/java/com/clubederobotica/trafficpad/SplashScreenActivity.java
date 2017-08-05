package com.clubederobotica.trafficpad;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                abrirTelaInicial();
            }
        }, 2000);
    }

    public void abrirTelaInicial(){
        Intent mudarActivity = new Intent(SplashScreenActivity.this, TelaInicialActivity.class);
        finish();
        startActivity(mudarActivity);
    }
}
