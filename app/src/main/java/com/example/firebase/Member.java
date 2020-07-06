package com.example.firebase;
/*
    A class used to send data to the firebase database.
    This is due to the fact that the firebase database needs the data passing into it via a class structure.
    The data that is sent are all the member variables.
    In this case it is the Counter, T1,T2,T3,P1 and P2.
    A collection of setters and getters are present
 */
public class Member {
    private String T1_Data_string;
    private String T2_Data_string;
    private String T3_Data_string;
    private String T4_Data_string;
    private String Pdiff_Data_string;
    private String Time_string;


    //The above strings are laid out as the database helper's data is laid out.

    public Member(){}//Constructor, does not need to be populated
    //Getters
    public String getT1_Data_string(){
        return T1_Data_string;    }//Get the t1 data string
    public String getT2_Data_string(){
        return T2_Data_string;    }//Get the t2 data string
    public String getT3_Data_string(){
        return T3_Data_string;    }//Get the t3 data string
    public String getT4_Data_string(){
        return  T4_Data_string;}//Get the t4 data string
    public String getPdiff_Data_string(){return Pdiff_Data_string;}
    public String getTime_string(){return Time_string;}//Get the time string


    //Setters
    public void setT1_Data_string(String t1_Data_string){T1_Data_string = t1_Data_string.trim();
    }
    public void setT2_Data_string(String t2_Data_string){T2_Data_string = t2_Data_string.trim();
    }
    public void setT3_Data_string(String t3_Data_string){T3_Data_string = t3_Data_string.trim();
    }
    public void setT4_Data_string(String t4_Data_string){T4_Data_string = t4_Data_string.trim();
    }
    public void setPdiff_Data_string(String pdiff_Data_string){
        Pdiff_Data_string = pdiff_Data_string.trim();}
    public void setTime_string(String time_string){Time_string = time_string;}

}
