package com.example.firebase;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Bind_device_list_adapter extends ArrayAdapter<BTLE_Device> {

    Activity bind_device_activity;
    int layoutResourceID;
    ArrayList<BTLE_Device> found_devices;

    public Bind_device_list_adapter(Activity activity, int resource, ArrayList<BTLE_Device> objects){
        super(activity.getApplicationContext(),resource, objects);
        this.bind_device_activity = activity;
        layoutResourceID = resource;
        found_devices = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) bind_device_activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        BTLE_Device device = found_devices.get(position);
        String name = device.getName();
        String address = device.getAddress();


        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        if (name != null && name.length() > 0) {
            tv_name.setText(device.getName());
        }
        else {
            tv_name.setText("CTI Device");
        }


        TextView tv_macaddr = (TextView) convertView.findViewById(R.id.tv_macaddr);
        if (address != null && address.length() > 0) {
            tv_macaddr.setText(device.getAddress());
        }
        else {
            tv_macaddr.setText("No Address");
        }
        return convertView;
    }
}
