package com.example.firebase;
/*
The function for handling most of the interface output bar the graph within the main activity
 */

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.TextView;

import static java.lang.String.valueOf;

public class Interface_adapter {
    //Need to setup links to all the text views within the layout
    private TextView SD_logging;
    //Data output TextBoxes
    private TextView T1_label, T2_label, T3_label, T4_label, Pdiff_label;
    private TextView T1_Data, T2_Data, T3_Data, T4_Data, Pdiff_Data;
    private Boolean temperature_unit;
    //Data output Strings
    private String T1_Data_string, T2_Data_string, T3_Data_string, T4_Data_string, Counter_Data_String, Pdiff_Data_String, P_pos_neg_String;
    private String Sensor_Board = "Sensor_Board";
    private final static String TAG = Interface_adapter.class.getSimpleName();//Debugging tag

    public Interface_adapter(MainActivity mainActivity) {//The constructor of the interface adapter

        Log.d(TAG, "init: Do some setup");


        SD_logging = (TextView) mainActivity.findViewById(R.id.SD_logging);
        //Linking the labels to their layout Id's
        T1_label = (TextView) mainActivity.findViewById(R.id.T1_label);
        T2_label = (TextView) mainActivity.findViewById(R.id.T2_label);
        T3_label = (TextView) mainActivity.findViewById(R.id.T3_label);
        T4_label = (TextView) mainActivity.findViewById(R.id.T4_label);
        Pdiff_label = (TextView) mainActivity.findViewById(R.id.Pdiff_label);
        T1_Data = (TextView) mainActivity.findViewById(R.id.T1_data);
        T2_Data = (TextView) mainActivity.findViewById(R.id.T2_data);
        T3_Data = (TextView) mainActivity.findViewById(R.id.T3_data);
        T4_Data = (TextView) mainActivity.findViewById(R.id.T4_data);
        Pdiff_Data = (TextView) mainActivity.findViewById(R.id.Pdiff_data);

        Global_Settings global_settings = Global_Settings.getInstance();//Get access to the settings

        temperature_unit = global_settings.Get_temp_unit();
    }

    public void Clear() {//Clears the screen by setting blank test
        T1_Data.setText("");
        T2_Data.setText("");
        T3_Data.setText("");
        T4_Data.setText("");
        Pdiff_Data.setText("");
        T1_label.setText("");
        T2_label.setText("");
        T3_label.setText("");
        T4_label.setText("");
        Pdiff_Data.setText("");
    }

    public void Display(BluetoothDevice device, byte[] ScanRecord, int rssi, String Mode_spinner_value) {//Displays the latest value of the data from the SQLITE database
        Log.d(TAG, "addDevice: Counter value is :" + ScanRecord[7]);
        Counter_Data_String = Integer.toString(ScanRecord[7]);//Set the counter value from the advertisement
        //Combine the two bytes of data into an integer
        float T1 = (((ScanRecord[8]) & 0xFF) << 8 | (ScanRecord[9] & 0xFF));
        float T2 = (((ScanRecord[10]) & 0xFF) << 8 | (ScanRecord[11] & 0xFF));
        float T3 = (((ScanRecord[12]) & 0xFF) << 8 | (ScanRecord[13] & 0xFF));
        float T4 = (((ScanRecord[14]) & 0xFF) << 8 | (ScanRecord[15] & 0xFF));
        int P_pos_neg = ScanRecord[16];
        float Pdiff = (((ScanRecord[17]) & 0xFF) << 8 | (ScanRecord[18] & 0xFF));

        if(temperature_unit == false) {//C
            T1_Data_string = Float.toString(T1 / 100);//Get the actual temperature and set it to a readable string
            T2_Data_string = Float.toString(T2 / 100);//Get the actual temperature and set it to a readable string
            T3_Data_string = Float.toString(T3 / 100);//Get the actual temperature and set it to a readable string
            T4_Data_string = Float.toString(T4 / 100);//Get the actual temperature and set it to a readable string
            Pdiff_Data_String = Float.toString(Pdiff / 100);//Get the real pressure value
            // Pdiff_Data_String = Integer.toString(Pdiff);
            int Logging = (ScanRecord[19]);
            if (Logging == 1)//SD logging value
            {
                SD_logging.setText("Logging");
            } else {
                SD_logging.setText("Not logging");
            }
            if (P_pos_neg == 0) {
                P_pos_neg_String = "-";//Negative
            } else//1
            {
                P_pos_neg_String = "";//Positive
            }
            //Output the data to the interface

            T1_Data.setText(T1_Data_string + "\u2103");//Add a degree symbol
            T2_Data.setText(T2_Data_string + "\u2103");
            T3_Data.setText(T3_Data_string + "\u2103");
            T4_Data.setText(T4_Data_string + "\u2103");
            Pdiff_Data.setText(P_pos_neg_String + Pdiff_Data_String + "Pa");
        }
        else if (temperature_unit == true) {//F
            T1_Data_string = Float.toString(((T1 / 100)* (9/5)) +32);//Get the actual temperature and set it to a readable string
            T2_Data_string = Float.toString(((T2 / 100)* (9/5)) +32);//Get the actual temperature and set it to a readable string
            T3_Data_string = Float.toString(((T3 / 100)* (9/5)) +32);//Get the actual temperature and set it to a readable string
            T4_Data_string = Float.toString(((T4 / 100)* (9/5)) +32);//Get the actual temperature and set it to a readable string
            Pdiff_Data_String = Float.toString(Pdiff / 100);//Get the real pressure value
            // Pdiff_Data_String = Integer.toString(Pdiff);
            int Logging = (ScanRecord[19]);
            if (Logging == 1)//SD logging value
            {
                SD_logging.setText("Logging");
            } else {
                SD_logging.setText("Not logging");
            }
            if (P_pos_neg == 0) {
                P_pos_neg_String = "-";//Negative
            } else//1
            {
                P_pos_neg_String = "";//Positive
            }
            //Output the data to the interface

            T1_Data.setText(String.format("%.2f",((T1 / 100)* (9/5)) +32) + "\u2109");
            T2_Data.setText(String.format("%.2f",((T2 / 100)* (9/5)) +32) + "\u2109");
            T3_Data.setText(String.format("%.2f",((T3 / 100)* (9/5)) +32) + "\u2109");
            T4_Data.setText(String.format("%.2f",((T4 / 100)* (9/5)) +32) + "\u2109");

            //T1_Data.setText(T1_Data_string + "\u2109");//Add a degree symbol
            //T2_Data.setText(T2_Data_string + "\u2109");
            //T3_Data.setText(T3_Data_string + "\u2109");
            //T4_Data.setText(T4_Data_string + "\u2109");
            Pdiff_Data.setText(P_pos_neg_String + Pdiff_Data_String + "Pa");
        }

        T1_label.setText("T1");
        T2_label.setText("T2");
        T3_label.setText("T3");
        T4_label.setText("T4");
        Pdiff_label.setText("Pdiff");

    }

}



