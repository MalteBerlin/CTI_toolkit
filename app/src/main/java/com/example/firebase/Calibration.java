/*
This class allows the data that is being received from the sensor board to be converted to useful data using a calibration table
It also allows the de-calibration of a value for seeing the actual sensor data.

 */
package com.example.firebase;

public class Calibration {
    private float actual_temperature_value[] = {-50,-40,-30,-20,-10,0,10,20,30,40,50,60,70,80,90,100,110,120,130,140,150};//Actual temperature value
    private double gradient_temperature[] ={0.06452,0.06369,0.06410,0.06369,0.06369,0.06329,0.06289,0.06289,0.06289,0.06289,0.06250,0.06211,0.06211,0.06211,0.06211,0.06173,0.06173,0.06173,0.06098,0.06211};//M value
    private double inverse_gradient[] = {15.5,15.7,15.6,15.7,15.7,15.7,15.8,15.9,15.9,15.9,15.9,16.0,16.1,16.1,16.1,16.2,16.2,16.2,16.2,16.2,16.4,16.1};
    private int inverse_starter_value[] = {801,809,806,808,808,808,808,807,807,807,807,802,796,796,796,787,787,787,787,761,803};
    private int starter_value[]={26,181,338,494,651,808,966,1125,1284,1443,1602,1762,1923,2084,2245,2407,2569,2731,2893,3057,3218};//C value
    private String output_temperature;
    private String decimal_temperature;
    private double result;
    private double decimal_result;

    private float float_temp;
    public Calibration(){
    }//Constructor
    public String Get_temperature(int decimal_temp){
        //Input check
        if((decimal_temp >=26)&&(decimal_temp <=3057))//Minimum temperature value and the maximum temperature value
        {
            for(int i =0; i <= 19;i++) {//Loop all of the values in the array and check where the input value falls
                if ((decimal_temp >= starter_value[i])&&(decimal_temp <= starter_value[i+1])){
                    result = ((gradient_temperature[i])*(decimal_temp) + -50);//Apply the relevant gradient and add the default offset value
                }
            }
        }
        else{
            result= ((gradient_temperature[19])*(decimal_temp))+(-50);//Catch condition to use the last gradient value if the data falls outside of the range -50 to 140+ note 140+ needs to use the last value
        }
        output_temperature = String.valueOf(result);
        return output_temperature;//Return the value as a string for use+++
    }//Function to get the actual temperature via the calibration table
    public String Get_decimal(String temperature){
        float_temp= Float.parseFloat(temperature);//Convert the string value to a float value
        if((float_temp >=-50)&&(float_temp <=150))//Minimum temperature value and max value
        {
            for(int i =0; i <= 19;i++) {//Loop the instances of the data in the float arrays
                if ((float_temp >= actual_temperature_value[i])&&(float_temp <= actual_temperature_value[i+1])){//Check if the input data corresponds to the current data
                    decimal_result = ((inverse_gradient[i])*(float_temp) + inverse_starter_value[i]);//Use the relevant gradient and starting value
                }
            }
        }
        else{
            decimal_result = ((inverse_gradient[19])*(float_temp) + 803);//Default catch condition
        }
        decimal_temperature = String.valueOf(decimal_result);
        return decimal_temperature;//The definition of the decimal temperature
    }//Function to get the original sensor data by un-calibrating
}
