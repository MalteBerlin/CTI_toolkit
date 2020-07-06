package com.example.firebase;
/*
This is the class that contains the found Bluetooth low energy devices.
As it is registered from the result of the BLE scan it contains three parameters
Address, scan record and RSSI. From these parameters the name can also be identified
 */

import android.bluetooth.BluetoothDevice;


public class BTLE_Device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private byte[] scanRecord;

    //Set of setters and getter functions to get the data from the bluetooth device
    public BTLE_Device(BluetoothDevice bluetoothDevice){this.bluetoothDevice = bluetoothDevice;}

    public String getAddress(){return  bluetoothDevice.getAddress();}

    public byte[] getScanRecord(){return scanRecord;}

    public void setScanRecord(byte[] scanRecord){this.scanRecord =scanRecord;}

    public String getName(){return  bluetoothDevice.getName();}

    public void setRssi(int rssi){this.rssi = rssi;}

    public  int getRssi(){return rssi;}
}

