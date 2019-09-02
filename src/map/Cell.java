package map;

import algorithms.ExplorationAlgo;

/**
 * Represents each cell in the map grid.
 *
 * @author MDP Group 3
 */

public class Cell {
    private final int row;
    private final int col;
    private boolean isObstacle;
    private boolean isVirtualWall;
    private boolean isExplored;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        isObstacle = false;
        isVirtualWall = false;
        isExplored = false;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setIsObstacle(boolean val) {
        isObstacle = val;
    }

    public boolean getIsObstacle() {
        return isObstacle;
    }

    public void setVirtualWall(boolean val) {
        if (val || (row != 0 && row != MapConstants.NUM_ROWS - 1 && col != 0 && col != MapConstants.NUM_COLS - 1)) {
            isVirtualWall = val;
        }
    }

    public boolean getIsVirtualWall() {
        return isVirtualWall;
    }

    public void setIsExplored(boolean val) {
        isExplored = val;
    }

    public void setAndIncIsExplored(boolean val){
        //increment area explored when an unexplored cell is explored
        if(!isExplored && val==true){
            ExplorationAlgo.incAreaExplored();
        }
        isExplored = val;
    }

    public boolean getIsExplored() {
        return isExplored;
    }
}
