package robot;

import map.Map;
import map.MapConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utilities.CommMgr;
import utilities.MapDescriptor;

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

    public Robot(int row, int col, boolean actualBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;

        this.actualBot = actualBot;

        SRFrontLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SRFL");
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRFC");
        SRFrontRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SRFR");
        SRLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, findNewDir(MOVEMENT.LEFT), "SRL");
        SRRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, findNewDir(MOVEMENT.RIGHT), "SRR");
        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow, this.posCol - 1, findNewDir(MOVEMENT.LEFT), "LRL");
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
            reachedGoal = true;
    }

    public boolean getReachedGoal() {
        return reachedGoal;
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

        if (actualBot) sendMovement(m, sendMoveToAndroid);
        else System.out.println("Move: " + MOVEMENT.print(m));

        setReachedGoal();
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
            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.ROBOT_POS);
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
        } else {
            int[] result = new int[6];

            CommMgr comm = CommMgr.getCommMgr();
            String msg = comm.receiveMsg();
            String[] msgArr = msg.split(";");

            if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
                result[0] = Integer.parseInt(msgArr[1].split("_")[1]);
                result[1] = Integer.parseInt(msgArr[2].split("_")[1]);
                result[2] = Integer.parseInt(msgArr[3].split("_")[1]);
                result[3] = Integer.parseInt(msgArr[4].split("_")[1]);
                result[4] = Integer.parseInt(msgArr[5].split("_")[1]);
                result[5] = Integer.parseInt(msgArr[6].split("_")[1]);
            }

            SRFrontLeft.senseActual(explorationMap, result[0]);
            SRFrontCenter.senseActual(explorationMap, result[1]);
            SRFrontRight.senseActual(explorationMap, result[2]);
            SRLeft.senseActual(explorationMap, result[3]);
            SRRight.senseActual(explorationMap, result[4]);
            LRLeft.senseActual(explorationMap, result[5]);

            String[] mapStrings = MapDescriptor.createMapDescriptor(explorationMap);
            comm.sendMsg(mapStrings[0] + " " + mapStrings[1], CommMgr.MAP_STRING);
        }

        //return result;
    }
}
