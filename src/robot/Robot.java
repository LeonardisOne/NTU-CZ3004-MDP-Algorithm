package robot;

import map.Map;
import map.MapConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utilities.CommMgr;
import utilities.MapDescriptor;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


/**
 * Represents the robot moving in the arena.
 * 
 * @author MDP Group 3
 */


public class Robot {
    private int posRow; // center cell
    private int posCol; // center cell
    private DIRECTION robotDir;
    private int speed;
    private final Sensor SRFrontLeft;       // north-facing front-left Short Range sensor
    private final Sensor SRFrontCenter;     // north-facing front-center Short Range sensor
    private final Sensor SRFrontRight;      // north-facing front-right Short Range sensor
    private final Sensor SRLeft;            // west-facing left Short Range sensor
    private final Sensor SRRight;           // east-facing right Short Range sensor
    private final Sensor LRLeft;            // west-facing left Long Range sensor
    private boolean reachedGoal;
    private final boolean actualBot;
    private boolean isRunning;
    private int wp_row= 0;
    private int wp_col= 0;
   /* public Robot(int row, int col, boolean actualBot) {
        this(row, col, actualBot, RobotConstants.START_DIR);
    }*/

    public Robot(int row, int col, boolean actualBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;

        this.actualBot = actualBot;
        this.isRunning = false;
        SRFrontLeft = new Sensor(0, 0, this.posRow + 1, this.posCol - 1, this.robotDir, "frontIR_2");//index1
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "frontIR_4");//index3
        SRFrontRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "frontIR_5");//index4
        SRLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, findNewDir(MOVEMENT.LEFT), "leftIR_1"); //index0
        SRRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, findNewDir(MOVEMENT.RIGHT), "rightIR_3");//index2
        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow, this.posCol - 1, findNewDir(MOVEMENT.LEFT), "leftIR_6");//index5
    }

    public void setWaypoint(int row, int col){
        this.wp_row = row;
        this.wp_col = col;
    }

    public int getWP_row(){
        return wp_row;
    }

    public int getWP_col(){
        return wp_col;
    }

    public void setRobotPos(int row, int col) {
        posRow = row;
        posCol = col;
    }

    public int getRobotPosRow() {
        return posRow;
    }

    public int getRobotPosCol() {
        return posCol;
    }

    public void setRobotDir(DIRECTION robotDir) {
        this.robotDir = robotDir;
    }

    public String getRobotStatus(){
        if(isRunning) 
            return "exploring";
        else
            return "idle";
    }
    
    public void setRobotStatus(boolean status){
        this.isRunning = status;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public DIRECTION getRobotCurDir() {
        return robotDir;
    }

    public boolean getActualBot() {
        return actualBot;
    }

    private void setReachedGoal() {
        if (this.getRobotPosRow() == MapConstants.GOAL_ROW && this.getRobotPosCol() == MapConstants.GOAL_COL)
            this.reachedGoal = true;
    }

    public boolean getReachedGoal() {
        return this.reachedGoal;
    }

    /**
     * Takes in a MOVEMENT and moves the robot accordingly by changing its position and direction. Sends the movement
     * if this.actualBot is set.
     */
    public void move(MOVEMENT m, boolean sendMoveToAndroid) {
        if (!actualBot) {
            // Emulate real movement by pausing execution.
            try {
                
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }

        //changePosOrDir(m);
        switch (m) {
            case FORWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow++;
                        break;
                    case EAST:
                        posCol++;
                        break;
                    case SOUTH:
                        posRow--;
                        break;
                    case WEST:
                        posCol--;
                        break;
                }
                break;
            case BACKWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow--;
                        break;
                    case EAST:
                        posCol--;
                        break;
                    case SOUTH:
                        posRow++;
                        break;
                    case WEST:
                        posCol++;
                        break;
                }
                break;
            case RIGHT:
            case LEFT:
                robotDir = findNewDir(m);
                break;
            case CALIBRATE:
                break;
            default:
                System.out.println("Error in Robot.move()!");
                break;
        }//end switch
        if (actualBot) sendMovement(m, sendMoveToAndroid);
        else {
            System.out.println("Move: " + MOVEMENT.print(m));
        }

        setReachedGoal();
    }

    /**
     * Takes in a MOVEMENT and change the position and direction of robot.
     */
    public void changePosOrDir(MOVEMENT m){
        switch (m) {
            case FORWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow++;
                        break;
                    case EAST:
                        posCol++;
                        break;
                    case SOUTH:
                        posRow--;
                        break;
                    case WEST:
                        posCol--;
                        break;
                }
                break;
            case BACKWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow--;
                        break;
                    case EAST:
                        posCol--;
                        break;
                    case SOUTH:
                        posRow++;
                        break;
                    case WEST:
                        posCol++;
                        break;
                }
                break;
            case RIGHT:
            case LEFT:
                robotDir = findNewDir(m);
                break;
            case CALIBRATE:
                break;
            default:
                System.out.println("Error in Robot.move()!");
                break;
        }
    }

    /*Overloaded method that calls this.move(MOVEMENT m, boolean sendMoveToAndroid = true).
     */
    /* public void move(MOVEMENT m) {
        this.move(m, true);
    } */

    /**
     * Sends a number instead of 'F' for multiple continuous forward movements.
     */
    public void moveForwardMultiCell(int count) {
        if (count == 1) {
            move(MOVEMENT.FORWARD, true);
        } else {
            CommMgr comm = CommMgr.getCommMgr();
            if (count == 10) {
                comm.sendMsg("0", CommMgr.INSTRUCTIONS);
            } else if (count < 10) {
                comm.sendMsg(Integer.toString(count), CommMgr.INSTRUCTIONS);
            }

            switch (robotDir) {
                case NORTH:
                    posRow += count;
                    break;
                case EAST:
                    posCol += count;
                    break;
                case SOUTH:
                    posRow += count;
                    break;
                case WEST:
                    posCol += count;
                    break;
            }

            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.ROBOT_POS);
        }
    }

    /**
     * Uses the CommMgr to send the next movement to the robot.
     */
    private void sendMovement(MOVEMENT m, boolean sendMoveToAndroid) {
        CommMgr comm = CommMgr.getCommMgr();
        comm.sendMsg(MOVEMENT.print(m) + "", CommMgr.INSTRUCTIONS);
        if (m != MOVEMENT.CALIBRATE && sendMoveToAndroid) {
            comm.sendMsg(this.getRobotPosCol() + "," + this.getRobotPosRow() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.ROBOT_POS);
        }
    }

    /**
     * Sets the sensors' position and direction values according to the robot's current position and direction.
     */
    public void setSensors() {
        switch (robotDir) {
            case NORTH:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow + 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                SRLeft.setSensor(this.posRow + 1, this.posCol - 1, findNewDir(MOVEMENT.LEFT));
                LRLeft.setSensor(this.posRow, this.posCol - 1, findNewDir(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow + 1, this.posCol + 1, findNewDir(MOVEMENT.RIGHT));
                break;
            case EAST:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol + 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                SRLeft.setSensor(this.posRow + 1, this.posCol + 1, findNewDir(MOVEMENT.LEFT));
                LRLeft.setSensor(this.posRow + 1, this.posCol, findNewDir(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow - 1, this.posCol + 1, findNewDir(MOVEMENT.RIGHT));
                break;
            case SOUTH:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow - 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                SRLeft.setSensor(this.posRow - 1, this.posCol + 1, findNewDir(MOVEMENT.LEFT));
                LRLeft.setSensor(this.posRow, this.posCol + 1, findNewDir(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow - 1, this.posCol - 1, findNewDir(MOVEMENT.RIGHT));
                break;
            case WEST:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol - 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                SRLeft.setSensor(this.posRow - 1, this.posCol - 1, findNewDir(MOVEMENT.LEFT));
                LRLeft.setSensor(this.posRow - 1, this.posCol, findNewDir(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow + 1, this.posCol - 1, findNewDir(MOVEMENT.RIGHT));
                break;
        }

    }

    /**
     * Uses the current direction of the robot and the given movement to find the new direction of the robot.
     */
    private DIRECTION findNewDir(MOVEMENT m) {
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir);
        } else {
            return DIRECTION.getPrevious(robotDir);
        }
    }

    /**
     * Calls the .sense() method of all the attached sensors and stores the received values in an integer array.
     *
     */
    public void sense(Map explorationMap, Map actualMap) {

        if (!actualBot) {
            SRFrontLeft.sense(explorationMap, actualMap);
            SRFrontCenter.sense(explorationMap, actualMap);
            SRFrontRight.sense(explorationMap, actualMap);
            SRLeft.sense(explorationMap, actualMap);
            SRRight.sense(explorationMap, actualMap);
            LRLeft.sense(explorationMap, actualMap);
            String[] mapStrings = MapDescriptor.createMapDescriptor_actual(explorationMap);
            

        } else {
            int[] result = new int[6];
            Arrays.fill(result, 0);
            System.out.println("testing sensor input");

            CommMgr comm = CommMgr.getCommMgr();
            String msg = comm.receiveMsg();
            System.out.println(msg);
            String[] msgArr = msg.split(";");
            System.out.println("msgArr index 0: "+msgArr[0]);
            System.out.println("msgArr index 1: "+msgArr[1]);
            System.out.println("msgArr index 2: "+msgArr[2]);
            System.out.println("msgArr index 2: "+msgArr[6]);
            /*if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
                result[0] = Integer.parseInt(msgArr[1].split("_")[1]);
                result[1] = Integer.parseInt(msgArr[2].split("_")[1]);
                result[2] = Integer.parseInt(msgArr[3].split("_")[1]);
                result[3] = Integer.parseInt(msgArr[4].split("_")[1]);
                result[4] = Integer.parseInt(msgArr[5].split("_")[1]);
                result[5] = Integer.parseInt(msgArr[6].split("_")[1]);
            }*/
            int [] temp =new int[6];
            temp = convertToInt(msgArr);
    
            if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
                System.out.println("testing assigning to result");
                result[0] = (int)(Math.ceil(temp[0]/10.0));
                System.out.println("result[0]: "+result[0]);
                result[1] = (int)(Math.ceil(temp[1]/10.0));
                System.out.println("result[1]: "+result[1]);
                result[2] = (int)(Math.ceil(temp[2]/10.0));
                System.out.println("result[2]: "+result[2]);
                result[3] = (int)(Math.ceil(temp[3]/10.0));
                System.out.println("result[3]: "+result[3]);
                result[4] = (int)(Math.ceil(temp[4]/10.0));
                System.out.println("result[4]: "+result[4]);
                result[5] = (int)(Math.ceil(temp[5]/10.0));
                System.out.println("result[5]: "+result[5]);
            }

            SRFrontLeft.sense(explorationMap, result[1]);
            SRFrontCenter.sense(explorationMap, result[3]);
            SRFrontRight.sense(explorationMap, result[4]);
            SRLeft.sense(explorationMap, result[0]);
            SRRight.sense(explorationMap, result[2]);
            LRLeft.sense(explorationMap, result[5]);

            String[] mapStrings;
            if(actualBot){
                mapStrings = MapDescriptor.createMapDescriptor_actual(explorationMap);
            }
            else{
                mapStrings = MapDescriptor.createMapDescriptor(explorationMap);
            }
            comm.sendMsg(mapStrings[0] + " " + mapStrings[1], CommMgr.MAP_STRING);
        }

        //return result;
    }

    public int[] convertToInt(String[] input){
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
