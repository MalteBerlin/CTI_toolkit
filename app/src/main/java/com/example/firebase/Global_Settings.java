package com.example.firebase;
/*
This file will be globally accessed to allow the settings to be used throughout the project
 */
public class Global_Settings {
    private boolean Advanced_gatt_settings;
    private int Text_Size;//1 small 2 medium 3 large
    private boolean Temp_unit;//false is C true is F
    private String Bound_device;
    private static Global_Settings instance = new Global_Settings();
    public static Global_Settings getInstance() {
        return instance;
    }
    public static void setInstance(Global_Settings instance) {
        Global_Settings.instance = instance;
    }
    private Global_Settings() {//Constructor
        Advanced_gatt_settings = false;//Off
        Text_Size = 2;//Medium
        Temp_unit =false;
        Bound_device = "Null";
    }
    // Getters
    public  boolean Get_Advanced_settings()
    {
        return Advanced_gatt_settings;
    }
    public int Get_Text_Size(){return Text_Size;}
    public boolean Get_temp_unit(){return Temp_unit;}
    public String Get_Bound_device(){return Bound_device;};
    //Setters
    public void Set_Text_Size(int Text_Size){this.Text_Size = Text_Size;}
    public void Set_Temp_Unit(boolean Temp_unit){this.Temp_unit = Temp_unit;}
    public void Set_Advanced_settings(Boolean advanced_settings) {this.Advanced_gatt_settings = advanced_settings;}
    public void Set_Bound_Device(String Bound_Device){this.Bound_device = Bound_Device;}
}
