package com.training.hms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.training.hms.account.AccountActivity;
import com.training.hms.ads.AdsActivity;
import com.training.hms.iap.IapActivity;
import com.training.hms.location.LocationActivity;
import com.training.hms.ml.MLActivity;
import com.training.hms.push.PushActivity;
import com.training.hms.safetydetect.SafetyDetectActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initClickListener();
    }

    private void initClickListener() {
        findViewById(R.id.account_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
            }
        });

        findViewById(R.id.push_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PushActivity.class));
            }
        });
        findViewById(R.id.iap_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IapActivity.class));
            }
        });

        findViewById(R.id.location_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocationActivity.class));
            }
        });
        findViewById(R.id.safety_detect_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SafetyDetectActivity.class));
            }
        });

        findViewById(R.id.ml_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MLActivity.class));
            }
        });

        findViewById(R.id.ads_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdsActivity.class));
            }
        });
    }
}