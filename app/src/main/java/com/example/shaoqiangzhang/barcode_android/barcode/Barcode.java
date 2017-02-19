package com.example.shaoqiangzhang.barcode_android.barcode;

import android.view.KeyEvent;

import com.example.shaoqiangzhang.barcode_android.exception.DevicePairedNotFoundException;

/**
 * @author Shaoqiang.Zhang
 * Created by Shaoqiang.Zhang on 2016/12/16.
 */

public interface Barcode {

    boolean hasConnectBarcode() throws DevicePairedNotFoundException;

    boolean isInputDeviceExist(String deviceName);

    void analysisKeyEvent(KeyEvent event);

    char getInputCode(KeyEvent event);

    void checkLetterStatus(KeyEvent event);

    void scanSuccessCallBack();

    boolean isEventFromBarCode(KeyEvent event);
}
