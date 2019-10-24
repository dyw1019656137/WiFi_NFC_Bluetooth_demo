package com.dyw.wfnfcblue;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NFCActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;

    private Button isSupportBtn, readBtn, writeBtn;

    private TextView tvResult;

    private NfcAdapter mNfcAdapter;

    private PendingIntent mPendingIntent;

    private IntentFilter[] mFilters;

    private String[][] mTechLists;

    private String TAG = "NFCActivity-----：";

    private String mNfcTagName = "com.android.mms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        init();

        initNFC();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSupportNFC()) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSupportNFC()) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void init() {
        mContext = NFCActivity.this;
        isSupportBtn = (Button) findViewById(R.id.issupport);
        readBtn = (Button) findViewById(R.id.read);
        writeBtn = (Button) findViewById(R.id.write);
        tvResult = (TextView) findViewById(R.id.result);

        isSupportBtn.setOnClickListener(this);
        readBtn.setOnClickListener(this);
        writeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.issupport:
                if (isSupportNFC()) {
                    setResult("该设备支持NFC，且已打开NFC功能");
                } else {
                    toast("该设备不支持NFC，或未启用NFC功能");
                    setResult("该设备不支持NFC，或未启用NFC功能");
                }
                break;
            case R.id.read:
                break;
            case R.id.write:
                break;
            default:
                break;
        }
    }

    private boolean isSupportNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        if (mNfcAdapter == null) {
            return false;
        } else {
            return mNfcAdapter.isEnabled();
        }
    }

    private void initNFC() {
        // 探测到NFC卡片后，必须以FLAG_ACTIVITY_SINGLE_TOP方式启动Activity，
        // 或者在AndroidManifest.xml中设置launchMode属性为singleTop或者singleTask，
        // 保证无论NFC标签靠近手机多少次，Activity实例都只有一个。
        Intent intent = new Intent(this, NFCActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // 声明一个NFC卡片探测事件的相应动作
        mPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            // 定义一个过滤器（检测到NFC卡片）
            mFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};
        } catch (Exception e) {
            Log.d(TAG, "initNFC: " + e.getMessage());
            e.printStackTrace();
        }
        // 读标签之前先确定标签类型
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}, {IsoDep.class.getName()}};
        Log.d(TAG, "initNFC: ");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String card_info = "";
        // 获取到本次启动的action
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF类型
                || action.equals(NfcAdapter.ACTION_TECH_DISCOVERED) // 其他类型
                || action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) { // 未知类型
            // 从intent中读取NFC卡片内容
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // 获取NFC卡片的序列号
            byte[] ids = tag.getId();
            card_info = String.format("卡片的序列号为: %s", getHex(ids));
            setResult(card_info + "\n" + readNFCCard(tag));

            writeNFCTag(tag);
        } else {
            Log.d(TAG, "onNewIntent: 未识别");
            setResult("未识别");
        }
    }

    public String readNFCCard(Tag tag) {
        MifareClassic classic = MifareClassic.get(tag);
        String info;
        try {
            classic.connect(); // 连接卡片数据
            int type = classic.getType(); //获取TAG的类型
            String typeDesc;
            if (type == MifareClassic.TYPE_CLASSIC) {
                typeDesc = "传统类型";
            } else if (type == MifareClassic.TYPE_PLUS) {
                typeDesc = "增强类型";
            } else if (type == MifareClassic.TYPE_PRO) {
                typeDesc = "专业类型";
            } else {
                typeDesc = "未知类型";
            }
            info = String.format("\t卡片类型：%s\n\t扇区数量：%d\n\t分块个数：%d\n\t存储空间：%d字节",
                    typeDesc, classic.getSectorCount(), classic.getBlockCount(), classic.getSize());
        } catch (Exception e) {
            e.printStackTrace();
            info = e.getMessage();
        } finally { // 无论是否发生异常，都要释放资源
            try {
                classic.close(); // 释放卡片数据
            } catch (Exception e) {
                e.printStackTrace();
                info = e.getMessage();
            }
        }
        return info;
    }

    public void writeNFCTag(Tag tag) {
        if (tag == null) {
            return;
        }
        final NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createApplicationRecord(mNfcTagName)});
        int size = ndefMessage.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return;
                }
                if (ndef.getMaxSize() < size) {
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                toast("写入数据成功");
            } else {
                final NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    format.connect();
                    format.format(ndefMessage);
                } else {
                    toast("写入数据失败");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast("Exception:" + e.getMessage());
        }
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
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
