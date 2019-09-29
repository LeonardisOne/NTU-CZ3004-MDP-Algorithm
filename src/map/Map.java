package map;

import robot.Robot;
import robot.RobotConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the entire map grid for the arena.
 *
 * @author MDP Group 3
 */

public class Map extends JPanel {
    private final Cell[][] grid;
    private final Robot bot;
    
	public int rowToReach = RobotConstants.START_ROW;
	public  int colToReach = RobotConstants.START_COL;
    // blocked grid represents the real(explored) map
    public boolean[][] blocked;

	// explored grid stores the cell status
    public boolean[][] explored;
    
	// grid that the robot center can reach
    public boolean[][] reachable;
    
    

    /**
     * Initialises a Map object with a grid of Cell objects.
     */
    public Map(Robot bot) {
		blocked = new boolean[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
		explored = new boolean[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
        reachable = new boolean[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
        initialize();

        this.bot = bot;

        grid = new Cell[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col] = new Cell(row, col);

                // Set the virtual walls of the arena
                if (row == 0 || col == 0 || row == MapConstants.NUM_ROWS - 1 || col == MapConstants.NUM_COLS - 1) {
                    grid[row][col].setVirtualWall(true);
                }
            }
        }
    }

    //initialize all the rows and coloumns to false
	public void initialize() {
		for (int row = 0; row < MapConstants.NUM_ROWS; row++) {
			for (int col = 0; col < MapConstants.NUM_COLS; col++) {
				blocked[row][col] = false;
				explored[row][col] = false;
				reachable[row][col] = false;
			}
		}
    }

    public void notReachable(int row, int col) {
		reachable[row][col] = false;
		if (row == MapConstants.NUM_ROWS - 1) {
			if (col == MapConstants.NUM_COLS - 1) {
				reachable[row][col - 1] = false;
				reachable[row - 1][col] = false;
				reachable[row - 1][col - 1] = false;
			} else if (col == 0) {
				reachable[row][col + 1] = false;
				reachable[row - 1][col] = false;
				reachable[row - 1][col + 1] = false;
			} else {
				reachable[row][col - 1] = false;
				reachable[row][col + 1] = false;
				reachable[row - 1][col] = false;
				reachable[row - 1][col - 1] = false;
				reachable[row - 1][col + 1] = false;
			}
		} else if (row == 0) {
			if (col == MapConstants.NUM_COLS - 1) {
				reachable[row][col - 1] = false;
				reachable[row + 1][col] = false;
				reachable[row + 1][col - 1] = false;
			} else if (col == 0) {
				reachable[row][col + 1] = false;
				reachable[row + 1][col] = false;
				reachable[row + 1][col + 1] = false;
			} else {
				reachable[row][col - 1] = false;
				reachable[row][col + 1] = false;
				reachable[row + 1][col] = false;
				reachable[row + 1][col - 1] = false;
				reachable[row + 1][col + 1] = false;
			}
		} else {
			if (col == MapConstants.NUM_COLS - 1) {
				reachable[row][col - 1] = false;
				reachable[row - 1][col] = false;
				reachable[row - 1][col - 1] = false;
				reachable[row + 1][col] = false;
				reachable[row + 1][col - 1] = false;
			} else if (col == 0) {
				reachable[row][col + 1] = false;
				reachable[row - 1][col] = false;
				reachable[row - 1][col + 1] = false;
				reachable[row + 1][col] = false;
				reachable[row + 1][col + 1] = false;
			} else {
				reachable[row][col - 1] = false;
				reachable[row][col + 1] = false;
				reachable[row - 1][col] = false;
				reachable[row - 1][col - 1] = false;
				reachable[row - 1][col + 1] = false;
				reachable[row + 1][col] = false;
				reachable[row + 1][col - 1] = false;
				reachable[row + 1][col + 1] = false;
			}
		}
	}



    /**
     * Returns true if the row and column values are valid.
     */
    public boolean isCellValid(int row, int col) {
        return row >= 0 && col >= 0 && row < MapConstants.NUM_ROWS && col < MapConstants.NUM_COLS;
    }

    /**
     * Returns true if the row and column values are in the start zone.
     */
    private boolean inStartZone(int row, int col) {
        return row >= 0 && row <= MapConstants.START_ROW && col >= 0 && col <= MapConstants.START_COL;
    }

    /**
     * Returns true if the row and column values are in the goal zone.
     */
    private boolean inGoalZone(int row, int col) {
        return row >= MapConstants.GOAL_ROW - 1 && row <= MapConstants.GOAL_ROW + 1 && col >= MapConstants.GOAL_COL - 1 && col <= MapConstants.GOAL_COL + 1;
    }

    /**
     * Returns the cell with specified row and column values in the grid.
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns true if a cell is an obstacle.
     */
    public boolean isCellObstacle(int row, int col) {
        return grid[row][col].getIsObstacle();
    }

    /**
     * Returns true if a cell is a virtual wall.
     */
    public boolean isCellVirtualWall(int row, int col) {
        return grid[row][col].getIsVirtualWall();
    }

    /**
     * Sets all cells in the grid to an explored state.
     */
    public void setAllExplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setIsExplored(true);
            }
        }
    } 

    /**
     * Sets all cells in the grid to an unexplored state except for the START & GOAL zone.
     */
    public void setAllUnexplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (inStartZone(row, col) || inGoalZone(row, col)) {
                    grid[row][col].setIsExplored(true);
                } else {
                    grid[row][col].setIsExplored(false);
                }
            }
        }
    } 

    /**
     * Sets all cells in the START & GOAL zone to explored.
     */
    public void setExploredArea() {
        for (int row = 0; row <= MapConstants.START_ROW; row++) {
            for (int col = 0; col <= MapConstants.START_COL; col++) {
                grid[row][col].setIsExplored(true);
                grid[MapConstants.GOAL_ROW + 1 - row][MapConstants.GOAL_COL + 1 -col].setIsExplored(true);
            }
        }
    }

    /**
     * Sets all cells in the START & GOAL zone to explored and increment the explored area for each explored cell.
     */
    public void setAndIncExploredArea() {
        for (int row = 0; row <= MapConstants.START_ROW; row++) {
            for (int col = 0; col <= MapConstants.START_COL; col++) {
                grid[row][col].setAndIncIsExplored(true);
            }
        }
    }

    /**
     * Sets a cell as an obstacle and the surrounding cells as virtual walls or resets the cell and surrounding
     * virtual walls.
     */
    public void setObstacleCell(int row, int col, boolean isObstacle) {
        if (isObstacle && (inStartZone(row, col) || inGoalZone(row, col)))
            System.out.println("Error in obstacle detection!");

        grid[row][col].setIsObstacle(isObstacle);

        if (row >= 1) {
            grid[row - 1][col].setVirtualWall(isObstacle);            // bottom cell

            if (col < MapConstants.NUM_COLS - 1) {
                grid[row - 1][col + 1].setVirtualWall(isObstacle);    // bottom-right cell
            }

            if (col >= 1) {
                grid[row - 1][col - 1].setVirtualWall(isObstacle);    // bottom-left cell
            }
        }

        if (row < MapConstants.NUM_ROWS - 1) {
            grid[row + 1][col].setVirtualWall(isObstacle);            // top cell

            if (col < MapConstants.NUM_COLS - 1) {
                grid[row + 1][col + 1].setVirtualWall(isObstacle);    // top-right cell
            }

            if (col >= 1) {
                grid[row + 1][col - 1].setVirtualWall(isObstacle);    // top-left cell
            }
        }

        if (col >= 1) {
            grid[row][col - 1].setVirtualWall(isObstacle);            // left cell
        }

        if (col < MapConstants.NUM_COLS - 1) {
            grid[row][col + 1].setVirtualWall(isObstacle);            // right cell
        }
    }// end set obstacle cell

    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean isObstacleOrWall(int row, int col) {
        return !isCellValid(row, col) || getCell(row, col).getIsObstacle();
    }

	public boolean isBlocked(int row, int col) {
		if (row >= 0 && row < MapConstants.NUM_ROWS && col >= 0 && col < MapConstants.NUM_COLS) {
			return blocked[row][col];
		} else {
			return true;
		}
    }
    //paint waypoint
    public void paintWP(int row, int col){
        Color wp_color = GraphicsConstants.C_waypoint;
    }
    /**
     * Overrides JComponent's paintComponent() method. It creates a 2-D array of _DisplayCell objects
     * to store the current map state. Then, it paints the cells for the grid with the appropriate colors as
     * well as the robot on-screen.
     */
    public void paintComponent(Graphics g) {
        // Create a 2-D array of _DisplayCell objects for rendering.
        _DisplayCell[][] _mapCells = new _DisplayCell[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
        for (int mapRow = 0; mapRow < MapConstants.NUM_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.NUM_COLS; mapCol++) {
                _mapCells[mapRow][mapCol] = new _DisplayCell(mapCol * GraphicsConstants.CELL_SIZE, mapRow * GraphicsConstants.CELL_SIZE, GraphicsConstants.CELL_SIZE);
            }
        }

        // Paint the cells with the appropriate colors.
        for (int mapRow = 0; mapRow < MapConstants.NUM_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.NUM_COLS; mapCol++) {
                Color cellColor;

                if (inStartZone(mapRow, mapCol))
                    cellColor = GraphicsConstants.C_START;
                else if (inGoalZone(mapRow, mapCol))
                    cellColor = GraphicsConstants.C_GOAL;
                else {
                    if (!grid[mapRow][mapCol].getIsExplored())
                        cellColor = GraphicsConstants.C_UNEXPLORED;
                    else if (grid[mapRow][mapCol].getIsObstacle())
                        cellColor = GraphicsConstants.C_OBSTACLE;
                    else
                        cellColor = GraphicsConstants.C_FREE;
                }

                g.setColor(cellColor);
                g.fillRect(_mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET, _mapCells[mapRow][mapCol].cellY, _mapCells[mapRow][mapCol].cellSize, _mapCells[mapRow][mapCol].cellSize);

            }
        }
        //paint waypoint
        g.setColor(GraphicsConstants.C_waypoint);
        int wp_row = bot.getWP_row();
        int wp_col = bot.getWP_col();
        if(wp_row!=1 && wp_col!=1)
            g.fillRect(_mapCells[wp_row][wp_col].cellX + GraphicsConstants.MAP_X_OFFSET, _mapCells[wp_row][wp_col].cellY, _mapCells[wp_row][wp_col].cellSize, _mapCells[wp_row][wp_col].cellSize);
            
        
        // Paint the robot on-screen.
        g.setColor(GraphicsConstants.C_ROBOT);
        int r = bot.getRobotPosRow();
        int c = bot.getRobotPosCol();
        g.fillOval((c - 1) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - (r * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);

        // Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsConstants.C_ROBOT_DIR);
        RobotConstants.DIRECTION d = bot.getRobotCurDir();
        switch (d) {
            case NORTH:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 15, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case EAST:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 20 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case SOUTH:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 20, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case WEST:
                g.fillOval(c * GraphicsConstants.CELL_SIZE - 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
        }
    }

    private class _DisplayCell {
        public final int cellX;
        public final int cellY;
        public final int cellSize;

        public _DisplayCell(int borderX, int borderY, int borderSize) {
            this.cellX = borderX + GraphicsConstants.CELL_LINE_WEIGHT;
            this.cellY = GraphicsConstants.MAP_H - (borderY - GraphicsConstants.CELL_LINE_WEIGHT);
            this.cellSize = borderSize - (GraphicsConstants.CELL_LINE_WEIGHT * 2);
        }
    }
}
