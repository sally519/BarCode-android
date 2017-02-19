package com.example.shaoqiangzhang.barcode_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.example.shaoqiangzhang.barcode_android.barcode.BarCodeImpl;
import com.example.shaoqiangzhang.barcode_android.exception.DevicePairedNotFoundException;

public class MainActivity extends AppCompatActivity implements BarCodeImpl.OnScanSuccessListener{

    private BarCodeImpl barCodeImpl = new BarCodeImpl();
    private static final String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //注册监听
        barCodeImpl.setOnGunKeyPressListener(this);
    }

    @Override
    public void onScanSuccess(String barcode) {
        Log.e(TAG,"扫描到的内容为："+barcode);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (barCodeImpl.isEventFromBarCode(event)) {
            barCodeImpl.analysisKeyEvent(event);
            return true;
        }
        Log.e("keycode",event.getKeyCode()+"");
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            barCodeImpl.hasConnectBarcode();
        } catch (DevicePairedNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "badcode枪未连接！");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barCodeImpl.onComplete();
    }
}
