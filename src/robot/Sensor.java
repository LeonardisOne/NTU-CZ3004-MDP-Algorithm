package robot;

import map.Map;
import robot.RobotConstants.DIRECTION;

/**
 * Represents a sensor mounted on the robot.
 *
 * @author MDP Group 3
 */

public class Sensor {
    private final int lowerRange;
    private final int upperRange;
    private int sensorPosRow;
    private int sensorPosCol;
    private DIRECTION sensorDir;
    private final String name;

    public Sensor(int lowerRange, int upperRange, int row, int col, DIRECTION dir, String name) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.sensorPosRow = row;
        this.sensorPosCol = col;
        this.sensorDir = dir;
        this.name = name;
    }

    public void setSensor(int row, int col, DIRECTION dir) {
        this.sensorPosRow = row;
        this.sensorPosCol = col;
        this.sensorDir = dir;
    }

    /**
     * Returns the number of cells to the nearest detected obstacle or -1 if no obstacle is detected.
     */
    public void sense(Map exploredMap, Map actualMap) {
        switch (sensorDir) {
            case NORTH:
                processSensorVal(exploredMap, actualMap, 1, 0);
                break;
            case EAST:
                processSensorVal(exploredMap, actualMap, 0, 1);
                break;
            case SOUTH:
                processSensorVal(exploredMap, actualMap, -1, 0);
                break;
            case WEST:
                processSensorVal(exploredMap, actualMap, 0, -1);
                break;
        }
        //return -1;
    }

    /**
     * Overloads sense() method for use in actual robot.
     * 
     * Uses the sensor direction and given value from the actual sensor to update the map.
     */
    public void sense(Map exploredMap, int sensorVal) {
        switch (sensorDir) {
            case NORTH:
                processSensorVal(exploredMap, sensorVal, 1, 0);
                break;
            case EAST:
                processSensorVal(exploredMap, sensorVal, 0, 1);
                break;
            case SOUTH:
                processSensorVal(exploredMap, sensorVal, -1, 0);
                break;
            case WEST:
                processSensorVal(exploredMap, sensorVal, 0, -1);
                break;
        }
    }

    /**
     * Sets the correct cells in virtual arena to explored and/or obstacle according to the simulated sensor value.
     */
    private void processSensorVal(Map exploredMap, Map actualMap, int rowInc, int colInc) {
        int row = sensorPosRow;
        int col = sensorPosCol;

        // Check if starting point is valid for sensors with lowerRange > 1.
        if (lowerRange > 1) {
            for (int i = 1; i < lowerRange; i++) {
                row += rowInc;
                col += colInc;

                if (!exploredMap.isCellValid(row, col)) return;
                if (actualMap.getCell(row, col).getIsObstacle()) return;
            }
            row = sensorPosRow;
            col = sensorPosCol;
        }

        // Check if anything is detected by the sensor and return that value.
        for (int i = lowerRange; i <= upperRange; i++) {
            row += rowInc;
            col += colInc;

            if (!exploredMap.isCellValid(row, col)) return;

            exploredMap.getCell(row, col).setIsExplored(true);

            if (actualMap.getCell(row, col).getIsObstacle()) {
                exploredMap.setObstacleCell(row, col, true);
                return;
            }
        }
    }

    /**
     * Overloads processSensorVal() method for use in actual robot.
     * 
     * Sets the correct cells to explored and/or obstacle according to the actual sensor value.
     */
    private void processSensorVal(Map exploredMap, int sensorVal, int rowInc, int colInc) {
        if (sensorVal <=0) return;  // return value for LR sensor if obstacle before lowerRange

        int row = sensorPosRow;
        int col = sensorPosCol;

        // If above fails, check if starting point is valid for sensors with lowerRange > 1.
        for (int i = 1; i < lowerRange; i++) {
            row += rowInc;
            col += colInc;

            if (!exploredMap.isCellValid(row, col)) return;
            if (exploredMap.getCell(row, col).getIsObstacle()) return;
        }

        row = sensorPosRow;
        col = sensorPosCol;

        // Update map according to sensor's value.
        for (int i = lowerRange; i <= upperRange; i++) {
            row += rowInc;
            col += colInc;

            if (!exploredMap.isCellValid(row, col)) continue;

            exploredMap.getCell(row, col).setIsExplored(true);

            if (sensorVal == i) {
                exploredMap.setObstacleCell(row, col, true);
                break;
            }

            // Override previous obstacle value if front sensors detect no obstacle.
            if (exploredMap.getCell(row, col).getIsObstacle()) {
                if (name.equals("frontIR_2") || name.equals("frontIR_4") || name.equals("frontIR_5")) {
                    exploredMap.setObstacleCell(row, col, false);
                } else {
                    break;
                }
            }
        }
    }
}
