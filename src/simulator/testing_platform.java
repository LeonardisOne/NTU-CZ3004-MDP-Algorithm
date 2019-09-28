package simulator;

import java.util.*;

import javax.sound.midi.SysexMessage;

import java.lang.*;
import java.math.BigInteger;

public class testing_platform{
    public static void main(String[] arg) throws Exception{
        int[] result = new int[6];
        String msg = "SENSOR_DATA;-10.00;-10.00;-10.00;-10.00;-10.00;-10.00;";
        String testing = "10.00";
        String [] trytry =  testing.split("\\.");
        String a = trytry[0];
        String b = trytry[1];
        System.out.println("a: "+ a+" "+b);
        String s = " INSTR W";
        byte arr[] = s.getBytes("UTF8");
        for (byte x: arr) {
         System.out.print(x+" ");
      }
    }
    

    public static String removeTrailingZeros(String str){
		if (str == null){
		return null;}
		
		char[] chars = str.toCharArray();
		int length,index ;
		length = str.length();
		index = length -1;
		for (; index >=0;index--)
		{
		if (chars[index] != '0'){
		break;}
		}
		//return (index == length-1) ? str :str.substring(0,index+1);
		
		String myStr = (index == length-1) ? str :str.substring(0,index+1);
		//System.out.println("myStr is "+myStr);
		String finalStr = null;
		if(myStr.endsWith(".")){
			int len = myStr.length();
			finalStr = myStr.substring(0,len-1);
			System.out.println( "finalStr==>"+ finalStr);
		}
		return finalStr;
		
    }
    public static int[] convertToInt(String[] input){
        float[] temp = new float[6];

        for(int i = 0; i<6; i++){
            temp[i] = Float.parseFloat(input[i+1]);
        }

        int[] result = new int[6];
        for(int i = 0; i<6; i++){
            result[i] = (int)(temp[i]);
        }
        return result;
    }
    public static BigInteger testBigint(String temp){
        BigInteger hexBigIntegerExplored = new BigInteger(temp, 16);
        return hexBigIntegerExplored;
    }
}