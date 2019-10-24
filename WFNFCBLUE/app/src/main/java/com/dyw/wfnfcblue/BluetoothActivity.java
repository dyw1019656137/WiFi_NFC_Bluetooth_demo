package com.dyw.wfnfcblue;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;

    private Button isSupportBtn, scanBlueBtn, searchBtn;

    private TextView tvResult;

    private ListView lv;

    private BluetoothAdapter mBluetoothAdapter;

    private ArrayAdapter<String> mArrayAdapter;

    private String TAG = "BluetoothActivity";

    // 广播接收发现蓝牙设备
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 把名字和地址取出来
                mArrayAdapter.add("\t设备名：" + device.getName() + "\n\tAddress：" + device.getAddress());
                mArrayAdapter.notifyDataSetChanged();
                Log.d(TAG, "onReceive: " + device.getName());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        init();

        // 注册广播接收器。接收蓝牙发现讯息
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1);
        lv.setAdapter(mArrayAdapter);
    }

    private void init() {
        mContext = BluetoothActivity.this;
        isSupportBtn = (Button) findViewById(R.id.issupport);
        scanBlueBtn = (Button) findViewById(R.id.scanblue);
        searchBtn = (Button) findViewById(R.id.search);
        tvResult = (TextView) findViewById(R.id.result);
        lv = (ListView) findViewById(R.id.lv);

        isSupportBtn.setOnClickListener(this);
        scanBlueBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.issupport:
                if (isSupportBluetooth()) {
                    setResult("该设备支持蓝牙，且已打开蓝牙功能");
                } else {
                    toast("该设备不支持蓝牙，或未启用蓝牙功能");
                    setResult("该设备不支持蓝牙，或未启用蓝牙功能");
                }
                break;
            case R.id.scanblue:
                if (isSupportBluetooth()) {
                    if (mBluetoothAdapter.startDiscovery()) {
                        setResult("启动蓝牙扫描设备中...");
                    }
                } else {
                    toast("该设备不支持蓝牙，或未启用蓝牙功能");
                    setResult("该设备不支持蓝牙，或未启用蓝牙功能");
                }
                break;
            case R.id.search:
                if (isSupportBluetooth()) {
                    String info = getPairedBtDevices();
                    if (TextUtils.isEmpty(info)) {
                        setResult("无已配对设备");
                    } else {
                        setResult(info);
                    }
                } else {
                    toast("该设备不支持蓝牙，或未启用蓝牙功能");
                    setResult("该设备不支持蓝牙，或未启用蓝牙功能");
                }
                break;
            default:
                break;
        }
    }

    private boolean isSupportBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return mBluetoothAdapter.isEnabled();
        }
    }

    /**
     * 查询本地蓝牙适配器已配对的蓝牙设备
     *
     * @return
     */
    private String getPairedBtDevices() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // 把名字和地址取出来添加到适配器中
                stringBuilder.append("\t设备名：").append(device.getName()).append("\n\tAddress：").append(device.getAddress()).append("\n");
            }
            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public void setResult(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(info);
            }
        });
    }

    public void toast(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, info, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
