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
                getSensorVal(exploredMap, actualMap, 1, 0);
                break;
            case EAST:
                getSensorVal(exploredMap, actualMap, 0, 1);
                break;
            case SOUTH:
                getSensorVal(exploredMap, actualMap, -1, 0);
                break;
            case WEST:
                getSensorVal(exploredMap, actualMap, 0, -1);
                break;
        }
        //return -1;
    }

    /**
     * Sets the appropriate obstacle cell in the map and returns the row or column value of the obstacle cell. Returns
     * -1 if no obstacle is detected.
     */
    private int getSensorVal(Map exploredMap, Map actualMap, int rowInc, int colInc) {
        // Check if starting point is valid for sensors with lowerRange > 1.
        if (lowerRange > 1) {
            for (int i = 1; i < this.lowerRange; i++) {
                int row = this.sensorPosRow + (rowInc * i);
                int col = this.sensorPosCol + (colInc * i);

                if (!exploredMap.isCellValid(row, col)) return i;
                if (actualMap.getCell(row, col).getIsObstacle()) return i;
            }
        }

        // Check if anything is detected by the sensor and return that value.
        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.isCellValid(row, col)) return i;

            exploredMap.getCell(row, col).setIsExplored(true);

            if (actualMap.getCell(row, col).getIsObstacle()) {
                exploredMap.setObstacleCell(row, col, true);
                return i;
            }
        }

        // Else, return -1.
        return -1;
    }

    /**
     * Uses the sensor direction and given value from the actual sensor to update the map.
     */
    public void senseActual(Map exploredMap, int sensorVal) {
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
     * Sets the correct cells to explored and/or obstacle according to the actual sensor value.
     */
    private void processSensorVal(Map exploredMap, int sensorVal, int rowInc, int colInc) {
        if (sensorVal == 0) return;  // return value for LR sensor if obstacle before lowerRange

        // If above fails, check if starting point is valid for sensors with lowerRange > 1.
        for (int i = 1; i < this.lowerRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.isCellValid(row, col)) return;
            if (exploredMap.getCell(row, col).getIsObstacle()) return;
        }

        // Update map according to sensor's value.
        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.isCellValid(row, col)) continue;

            exploredMap.getCell(row, col).setIsExplored(true);

            if (sensorVal == i) {
                exploredMap.setObstacleCell(row, col, true);
                break;
            }

            // Override previous obstacle value if front sensors detect no obstacle.
            if (exploredMap.getCell(row, col).getIsObstacle()) {
                if (name.equals("SRFL") || name.equals("SRFC") || name.equals("SRFR")) {
                    exploredMap.setObstacleCell(row, col, false);
                } else {
                    break;
                }
            }
        }
    }
}
