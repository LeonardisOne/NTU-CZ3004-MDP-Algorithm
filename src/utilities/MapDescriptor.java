package utilities;

import map.Map;
import map.MapConstants;

import java.io.*;

/**
 * Helper methods for reading & generating map strings.
 *
 * Use binary number (0 and 1) to represent false and true respectively.
 * Part 1: 0 if unexplored and 1 if explored. All cells are represented.
 * Part 2: 0 if not an obstacle and 1 if an obstacle. Only explored cells are represented.
 *
 * @author MDP Group 3
 */

public class MapDescriptor {
    /**
     * Reads filename.txt (which contains the map descriptor) from disk and loads it into the passed Map object. Uses binary number (0 and 1) to
     * identify if a cell is an obstacle.
     * 
     * The filename.txt must be saved under "maps" directory in the same directory as src directory.
     */
    public static void loadMap(Map map, String filename) {
        //use try-with-resources statement to ensure that each resource is closed at the end of the statement.
        try (InputStream inputStream = new FileInputStream("maps/" + filename + ".txt");
        BufferedReader mapReader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String line = mapReader.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = mapReader.readLine();
            }

            String bin = sb.toString();
            int binPtr = 0;

			for (int row = 0; row < MapConstants.NUM_ROWS; row++) {
				for (int col = 0; col < MapConstants.NUM_COLS; col++) {
					if (row == 0 || col == 0 || row == MapConstants.NUM_ROWS - 1 || col == MapConstants.NUM_COLS - 1) {
						map.reachable[row][col] = false;
					} else {
						map.reachable[row][col] = true;
					}
				}
			}
            for (int row = MapConstants.NUM_ROWS - 1; row >= 0; row--) {
                for (int col = 0; col < MapConstants.NUM_COLS; col++) {
                    if (bin.charAt(binPtr) == '1'){ 
                    map.blocked[row][col] = true;
                    map.setObstacleCell(row, col, true);
                    map.notReachable(row, col);
                    }
                    binPtr++;
                }
            }

            //map.setAllExplored();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to convert a binary string to a hex string.
     */
    private static String binToHex(String bin) {
        int decimal = Integer.parseInt(bin, 2);

        return Integer.toHexString(decimal);
    }

    /**
     * Method to generate Part 1 & Part 2 map descriptor strings from the passed Map object.
     */
    public static String[] createMapDescriptor(Map map) {
        String[] descriptor = new String[2];

        StringBuilder Part1 = new StringBuilder();
        StringBuilder Part1_bin = new StringBuilder();
        Part1_bin.append("11");
        for (int r = 0; r < MapConstants.NUM_ROWS; r++) {
            for (int c = 0; c < MapConstants.NUM_COLS; c++) {
                if (map.getCell(r, c).getIsExplored())
                    Part1_bin.append("1");
                else
                    Part1_bin.append("0");

                if (Part1_bin.length() == 4) {
                    Part1.append(binToHex(Part1_bin.toString()));
                    Part1_bin.setLength(0);
                }
            }
        }
        Part1_bin.append("11");
        Part1.append(binToHex(Part1_bin.toString()));
        System.out.println("P1: " + Part1.toString());
        descriptor[0] = Part1.toString();

        StringBuilder Part2 = new StringBuilder();
        StringBuilder Part2_bin = new StringBuilder();
        for (int r = 0; r < MapConstants.NUM_ROWS; r++) {
            for (int c = 0; c < MapConstants.NUM_COLS; c++) {
                if (map.getCell(r, c).getIsExplored()) {
                    if (map.getCell(r, c).getIsObstacle())
                        Part2_bin.append("1");
                    else
                        Part2_bin.append("0");

                    if (Part2_bin.length() == 4) {
                        Part2.append(binToHex(Part2_bin.toString()));
                        Part2_bin.setLength(0);
                    }
                }
            }
        }
        if (Part2_bin.length() > 0) Part2.append(binToHex(Part2_bin.toString()));
        System.out.println("P2: " + Part2.toString());
        descriptor[1] = Part2.toString();

        return descriptor;
    }
}
