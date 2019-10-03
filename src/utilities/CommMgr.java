package utilities;

//import utilities.sendToandroid;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Communication manager to communicate with the rest of the system via the Raspberry Pi.
 *
 * @author MDP Group 3
 */

public class CommMgr {

    public static final String START_EXPR = "START_EXPR";           // Android --> PC
    public static final String START_FAST_PATH = "START_FAST_PATH"; // Android --> PC
    public static final String MAP_STRING = "MAP";                  // PC --> Android
    public static final String ROBOT_POS = "ROBOT_POS";             // PC --> Android
    public static final String ROBOT_STATUS = "ROBOT_STATUS";       //PC --> Android
    
    public static final String ROBOT_START = "ROBOT_START";         // PC --> Arduino
    public static final String INSTRUCTIONS = "INSTR";              // PC --> Arduino
    public static final String SENSOR_DATA = "SENSOR_DATA";               // Arduino --> PC
    public static final String HOST = "192.168.3.1";
    public static final int PORT = 9999;
    public String statusString="exploring";
    private static CommMgr commMgr = null;
    private static Socket socket = null;

    private BufferedWriter writer;
    private BufferedReader reader;

   // private sendToandroid sendAndroid = sendToandroid.getInstance();
    
    private PrintWriter _toRPi;
	private Scanner _fromRPi;
    private CommMgr() {
    }

    public static CommMgr getCommMgr() {
        if (commMgr == null) {
            commMgr = new CommMgr();
        }
        return commMgr;
    }

    public void startConnection() {
        System.out.println("Starting connection...");

        try {
            socket = new Socket(HOST, PORT);

            //_toRPi = new PrintWriter(socket.getOutputStream());
            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream()),StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Successfully connected!");

            return;
        } catch (UnknownHostException e) {
            System.out.println("startConnection() has UnknownHostException error!");
        } catch (IOException e) {
            System.out.println("startConnection() has IOException error!");
        } catch (Exception e) {
            System.out.println("startConnection() has an Exception error!");
            System.out.println(e.toString());
        }

        System.out.println("Failed to connect!");
    }

    public void endConnection() {
        System.out.println("Closing connection...");

        try {
            reader.close();

            if (socket != null) {
                socket.close();
                socket = null;
            }
            System.out.println("Connection closed!");
        } catch (IOException e) {
            System.out.println("endConnection() has IOException error!");
        } catch (NullPointerException e) {
            System.out.println("endConnection() has NullPointerException error!");
        } catch (Exception e) {
            System.out.println("endConnection() has an Exception error!");
            System.out.println(e.toString());
        }
    }

    public void sendMsg(String msg, String msgType) {
        System.out.println("Sending message ...");
        String mapString="",robotString="";
        try {
            String msgToSend;
            if (msg == null) {
                msgToSend = msgType + "\n";
            }
            else if(msgType.equals(ROBOT_STATUS)){
                System.out.println("testing if reach status point here");
                statusString = msg;
                msgToSend = "\n";
            }
            else if(msgType.equals(ROBOT_POS) || msgType.equals(MAP_STRING)) {
                if(msgType.equals(ROBOT_POS)){
                    robotString = msg;
                }
                if(msgType.equals(MAP_STRING)){
                    mapString = msg;
                }
                System.out.println("printing status string: "+ statusString);
                msgToSend = sendToAndroid(mapString, robotString, statusString);
            }
            /*else if (msgType.equals(ROBOT_POS)) {
                //msgToSend = msgType + " " + msg + "\n";

            }
            else if(msgType.equals(MAP_STRING)){
                msgToSend = msgType + " " + msg + " ";
            }*/
            else if(msgType.equals(INSTRUCTIONS)){
                TimeUnit.MILLISECONDS.sleep(500);
                msgToSend = msgType + " " + msg + "\n";
            } 
            else {
                msgToSend = msgType + "\n" + msg + "\n";
            }

            //byte [] encoded_message = convertUTF(msgToSend);
            //_toRPi.write(msgToSend);
            System.out.println("Sending message:\n" + msgToSend);
            writer.write(msgToSend);
            System.out.println("Message sent!");
            writer.flush();
        } catch (IOException e) {
            System.out.println("sendMsg() has IOException error!");
        } catch (Exception e) {
            System.out.println("sendMsg() has an Exception error!");
            System.out.println(e.toString());
        }
    }

    public String receiveMsg() {
        System.out.println("Receiving message...");

        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();

            if (input != null && input.length() > 0) {
                sb.append(input);
                System.out.println("Received msg: " + sb.toString());
                return sb.toString();
            }
        } catch (IOException e) {
            System.out.println("receiveMsg() has IOException error!");
        } catch (Exception e) {
            System.out.println("receiveMsg() has an Exception error!");
            System.out.println(e.toString());
        }

        return null;
    }
    
    public boolean isConnected(String msgType, String msg) {
        return socket.isConnected();
    }

    public String sendToAndroid(String mapString,String robotString, String statusString){
        String map = "\"map\":";
        String explored = "\"explored\":";
        String length = "\"length\":300,";
        String obstacle = "\"obstacle\":";
        String robot = "\"robot\":";
        String status = "\"status\":";
        String map_e = "",map_obs="";
        String mapToSend="";
        String robotToSend="";
        String statusToSend="";
        String robot_row="",robot_col="", robot_dir="";
        if(mapString.length()!=0)  {
            String [] temp = mapString.split(" ");
            map_e = temp[0];
            map_obs = temp[1];
            mapToSend = map+"[{"+explored+"\""+ map_e +"\","+length+obstacle+"\""+map_obs+"\""+"}]";
        }
        
        if(robotString.length()!=0){
            String [] temp1 = robotString.split(",");
            robot_row = temp1[0];
            robot_col = temp1[1];
            robot_dir = temp1[2];
            robotToSend = robot+"[{"+"\"x\":"+ robot_row + "," + "\"y\":" + robot_col + ","+ "\"direction\":"+ "\"" + robot_dir+"\"}],";
        }
        


        statusToSend = status+"[{"+status+"\""+statusString+"\""+"}]";

        String finalString ="";
        if(mapString.length()==0){
            finalString = "{"+robotToSend+statusToSend+"}";
        }
        else if(robotString.length()==0){
            finalString = "{"+mapToSend+"}";
        }
        
        return finalString;
    }//end sendToAndroid


    
}
