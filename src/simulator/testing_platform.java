package simulator;

import java.util.*;

import javax.sound.midi.SysexMessage;

import java.lang.*;

public class testing_platform{
    public static void main(String[] arg){
        int[] result = new int[6];
        String msg = "SENSOR_DATA;-10.00;-10.00;-10.00;-10.00;-10.00;-10.00;";
        String[] msgArr = msg.split(";");
        System.out.println("msgArr index 0: "+msgArr[0]);
        System.out.println("msgArr index 1: "+msgArr[1]);
        System.out.println("msgArr index 2: "+msgArr[2]);
        System.out.println("msgArr index 6: "+msgArr[6]);
        System.out.println(Float.parseFloat(msgArr[1]));

        int [] temp =new int[6];
        temp = convertToInt(msgArr);

        if (msgArr[0].equals("SENSOR_DATA")) {
            result[0] = temp[0];
            System.out.println("result[0]: "+result[0]);
            result[1] = temp[1];
            System.out.println("result[1]: "+result[1]);
            result[2] = temp[2];
            System.out.println("result[2]: "+result[2]);
            result[3] = temp[3];
            System.out.println("result[3]: "+result[3]);
            result[4] = temp[4];
            System.out.println("result[4]: "+result[4]);
            result[5] = temp[5];
            System.out.println("result[5]: "+result[5]);

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
}