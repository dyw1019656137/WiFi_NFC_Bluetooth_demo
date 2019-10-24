package com.dyw.wfnfcblue;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button wifiBtn, nfcBtn, blueToothBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    public void initView() {
        wifiBtn = (Button) findViewById(R.id.wifi);
        nfcBtn = (Button) findViewById(R.id.nfc);
        blueToothBtn = (Button) findViewById(R.id.bluetooth);

        wifiBtn.setOnClickListener(this);
        nfcBtn.setOnClickListener(this);
        blueToothBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wifi:
                startActivity(new Intent(MainActivity.this, WifiActivity.class));
                break;
            case R.id.nfc:
                startActivity(new Intent(MainActivity.this, NFCActivity.class));
                break;
            case R.id.bluetooth:
                startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
                break;
            default:
                break;
        }
    }
}
