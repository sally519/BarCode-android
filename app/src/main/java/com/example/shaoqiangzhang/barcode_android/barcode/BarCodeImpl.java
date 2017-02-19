package com.example.shaoqiangzhang.barcode_android.barcode;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;


import com.example.shaoqiangzhang.barcode_android.exception.DevicePairedNotFoundException;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Shaoqiang.Zhang
 * @see BarCodeImpl 继承Barcode接口，功能是读取手机的外围设备输入的内容，并解析内容后通过OnScanSuccessListener接口回调回你想要的地方
 * Created by Shaoqiang.Zhang on 2016/12/16.
 */

public class BarCodeImpl implements Barcode {

    private final static long MESSAGE_DELAY = 500;             //延迟500ms，判断扫码是否完成。
    private StringBuffer mStringBufferResult;                  //扫码内容
    private boolean mCaps;                                     //大小写区分
    private OnScanSuccessListener mOnScanSuccessListener;
    private String mDeviceName;
    private Runnable mScanningFishedRunnable;

    private Handler mHandler = new Handler();

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    /**
     * @return BarCode枪是否已经连接
     * @throws DevicePairedNotFoundException 手机没有蓝牙设备或者未配对任何蓝牙设备时所抛出的异常
     */
    @Override
    public boolean hasConnectBarcode() throws DevicePairedNotFoundException {

        //TODO 添加特殊设备的判断
        if (isInputDeviceExist("mtk-kpd")) {
            return true;
        }

        if (mBluetoothAdapter == null) {
            throw new DevicePairedNotFoundException("该设备没有蓝牙模块！");
        }

        Set<BluetoothDevice> blueDevices = mBluetoothAdapter.getBondedDevices();

        if (blueDevices == null || blueDevices.size() <= 0) {
            throw new DevicePairedNotFoundException("手机尚未配对任何蓝牙设备！");
        }

        for (Iterator<BluetoothDevice> iterator = blueDevices.iterator(); iterator.hasNext(); ) {
            BluetoothDevice bluetoothDevice = iterator.next();

            //TODO 添加PDA模块的判断

            if (bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL) {
                mDeviceName = bluetoothDevice.getName();
                return isInputDeviceExist(mDeviceName);
            }

        }
        return false;
    }

    /**
     * @param deviceName 设备的名称，即对应的参数为--PERIPHERAL的设备，在android里如果设备参数是PERIPHERAL代表着这是一个外部设备（与手机是一种组合的关系）
     * @return 检测是否有输入设备的存在
     */
    @Override
    public boolean isInputDeviceExist(String deviceName) {
//        int[] deviceIds = InputDevice.getDeviceIds();
//
//        for (int id : deviceIds) {
//            Log.e("InputDevice", InputDevice.getDevice(id).getName());
//            if (InputDevice.getDevice(id).getName().equals(deviceName)) {
//                return true;
//            }
//        }
        return true;
    }

    /**
     * @param event 一个输入事件,对输入事件解析的逻辑
     */
    @Override
    public void analysisKeyEvent(KeyEvent event) {
        Log.e("barcode", "analysisKeyEvent()");
        int keyCode = event.getKeyCode();
        //字母大小写判断
        checkLetterStatus(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            char aChar = getInputCode(event);
            Log.e("zifu", (aChar != 0) + "");
            if (aChar != 0) {
                Log.e("zifu", aChar + "");
                mStringBufferResult.append(aChar);
            }

//            mHandler.removeCallbacks(mScanningFishedRunnable);
//            mHandler.postDelayed(mScanningFishedRunnable, MESSAGE_DELAY);

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.post(mScanningFishedRunnable);
            }
        }
    }

    /**
     * @param event 一个输入事件，获取输入的内容
     */
    @Override
    public char getInputCode(KeyEvent event) {
        int keyCode = event.getKeyCode();

        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else {
            //其他符号
            switch (keyCode) {
                case KeyEvent.KEYCODE_LEFT_BRACKET:
                    aChar = mCaps ? '{' : '[';
                    break;
                case KeyEvent.KEYCODE_PERIOD:
                    aChar = '.';
                    break;
                case KeyEvent.KEYCODE_MINUS:
                    aChar = mCaps ? '_' : '-';
                    break;
                case KeyEvent.KEYCODE_SLASH:
                    aChar = '/';
                    break;
                case KeyEvent.KEYCODE_BACKSLASH:
                    aChar = mCaps ? '|' : '\\';
                    break;
                default:
                    aChar = 0;
                    break;
            }
        }
        return aChar;
    }

    /**
     * @param event 一个输入事件，方法会判断这个事件是否是 shift 按键，据此得知输入的是否是大写内容
     */
    @Override
    public void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }

    /**
     * 当扫面成功后的回调,内容发送给接口
     */
    @Override
    public void scanSuccessCallBack() {
        String barcode = mStringBufferResult.toString();
        if (mOnScanSuccessListener != null && !barcode.equals(""))
            mOnScanSuccessListener.onScanSuccess(barcode);
        mStringBufferResult.setLength(0);
    }

    /**
     * 过滤：检测接受到的事件是否来自BarCode枪
     */
    @Override
    public boolean isEventFromBarCode(KeyEvent event) {
        //bypass handling for some sepcial key,e.g: BACK KEY
        if (isKeyBypass(event)) {
            return false;
        }

        //TODO 添加特殊设备的判断
        if (isInputDeviceExist("mtk-kpd")) {
            return true;
        }
        return event.getDevice().getName().equals(mDeviceName);
    }

    /**
     * @see OnScanSuccessListener 内部自定义的接口，接受外围设备内容后会发送给方法 onScanSuccess
     */
    public interface OnScanSuccessListener {
        void onScanSuccess(String barcode);
    }

    /**
     * @param onScanSuccessListener 将外部创建的接口赋值给mOnScanSuccessListener
     */
    public void setOnGunKeyPressListener(OnScanSuccessListener onScanSuccessListener) {
        mOnScanSuccessListener = onScanSuccessListener;
        mStringBufferResult = new StringBuffer();
        mScanningFishedRunnable = new Runnable() {
            @Override
            public void run() {
                scanSuccessCallBack();
            }
        };
    }

    /**
     * 在生命周期结束的时候调用，释放内存
     */
    public void onComplete() {
        mHandler.removeCallbacks(mScanningFishedRunnable);
        mOnScanSuccessListener = null;
    }

    private boolean isKeyBypass(KeyEvent event) {
        int keycode = event.getKeyCode();
        if (keycode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return false;
        }

    }
}
