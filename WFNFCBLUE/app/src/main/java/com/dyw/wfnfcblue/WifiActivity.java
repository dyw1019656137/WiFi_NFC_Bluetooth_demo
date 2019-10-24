package com.dyw.wfnfcblue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;

    private Button isOpenBtn, scanWifiBtn, getInfoBtn;

    private TextView tvResult;

    private WifiManager mWifiManager;

    private ConnectivityManager mConnectivityManager;

    private List<WifiConfiguration> mWifiConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        init();
    }

    public void init() {
        mContext = WifiActivity.this;


        isOpenBtn = (Button) findViewById(R.id.isopen);
        scanWifiBtn = (Button) findViewById(R.id.scanwifi);
        getInfoBtn = (Button) findViewById(R.id.getinfo);
        tvResult = (TextView) findViewById(R.id.result);

        isOpenBtn.setOnClickListener(this);
        scanWifiBtn.setOnClickListener(this);
        getInfoBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.isopen:
                setResult(isOpen() + "");
                break;
            case R.id.scanwifi:
                if (!isOpen()) {
                    toast("请打开WiFi");
                    setResult("请打开WiFi");
                } else {
                    setResult(ScanWifiInfo());
                }
                break;
            case R.id.getinfo:
                if (!isOpen()) {
                    toast("请打开WiFi");
                    setResult("请打开WiFi");
                } else {
                    setResult(getWifiInfo());
                }
                break;
            default:
                break;
        }
    }

    private boolean isOpen() {
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return mWifiManager != null && mWifiManager.isWifiEnabled();
    }

    private String ScanWifiInfo() {
        // 扫描热点,扫描时耗时操作，如果界面中需要展示进度条的话，建议将扫描操作放在子线程中操作
        boolean isSuccess = mWifiManager.startScan();
        // 得到扫描结果
        List<ScanResult> mWifiList = mWifiManager.getScanResults();
        // 得到配置好的网络连接,列表中可能出现重复的热点，并且可能是ssid为空的热点，根据需求情况 自行过滤
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();

        mWifiList = sortByLevel(mWifiList);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_").append(Integer.valueOf(i + 1).toString()).append(":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString()).append("信号强度：").append(mWifiList.get(i).level);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * 将搜索到的wifi根据信号从强到弱进行排序
     *
     * @param list
     * @return
     */
    private List<ScanResult> sortByLevel(List<ScanResult> list) {
        ScanResult temp = null;
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                //level属性即为强度
                if (list.get(i).level > list.get(j).level) {
                    temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }
        }
        return list;
    }

    private String getWifiInfo() {
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();

        NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String wifiName = wifiInfo.getExtraInfo();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("BSSID：").append(mWifiInfo.getBSSID()).append("\n")
                .append("SSID：").append(mWifiInfo.getSSID()).append("\n")
                .append("IpAddress：").append(mWifiInfo.getIpAddress()).append("\n")
                .append("MacAddress：").append(mWifiInfo.getMacAddress()).append("\n")
                .append("LinkSpeed：").append(mWifiInfo.getLinkSpeed()).append("\n")
                .append("HiddenSSID：").append(mWifiInfo.getHiddenSSID()).append("\n")
                .append("wifiName：").append(wifiName).append("\n");
        return String.valueOf(stringBuilder);
    }

    public void setResult(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText(info);
            }
        });
    }

    public void toast(String info) {
        Toast.makeText(mContext, info, Toast.LENGTH_SHORT).show();
    }
}
