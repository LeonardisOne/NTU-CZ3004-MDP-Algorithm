package simulator;

import java.util.*;

import javax.sound.midi.SysexMessage;
import utilities.CommMgr;
import java.lang.*;
import java.math.BigInteger;

public class testing_platform{
    public static void main(String[] arg) throws Exception{
        CommMgr temp = CommMgr.getCommMgr();
        //temp.sendToAndroid();
        String msg = "SENSOR_DATA;-10.00;-10.00;-10.00;-10.00;-10.00;-10.00;";
        String obstacleString = "020004000800f00000000000000000000000000000000000000000000000000000000000000";
        String exploredMapString = "fffffffffffffffffffffffffffffffffffffffffffdfffbffffffffffffffffffffffffffff";
        
        String result = trytry(exploredMapString);
        System.out.println("testing result"+result);
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
    public static String trytry(String obstacleString){
        
        boolean isMapD = false;

        if(obstacleString.length() > 300){
            obstacleString = obstacleString.substring(2, obstacleString.length()-2);
            isMapD = true;
        } //means it is map descriptor

            

        BigInteger hex = new BigInteger(obstacleString,16);
        String bin = hex.toString(2);

        while(bin.length()<300)
            bin = "0" + bin;

        String resultString = "";

        for (int i=0; i<bin.length(); i=i+15) {
            int j=0;
            String subString = "";
            while (j<15) {
                if(j+i >= bin.length())
                    break;
                subString = subString + bin.charAt(j+i);
                
                j++;
            }
            resultString = subString + resultString;
        }

        if(!isMapD){
            
            return resultString;
        }
        else{
                resultString = "11" + resultString + "11";
                return resultString;
        }
    }
    
}