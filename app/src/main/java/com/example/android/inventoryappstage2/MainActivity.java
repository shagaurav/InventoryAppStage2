package com.example.android.inventoryappstage2;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // Splash screen timer
        int SPLASH_TIME_OUT = 3000;
        new Handler().postDelayed( new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start the app home activity
                Intent homeIntent = new Intent( MainActivity.this, CatalogActivity.class );
                startActivity( homeIntent );

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT );
    }
}

