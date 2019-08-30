package map;

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
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public void setIsObstacle(boolean val) {
        this.isObstacle = val;
    }

    public boolean getIsObstacle() {
        return this.isObstacle;
    }

    public void setVirtualWall(boolean val) {
        if (val || (row != 0 && row != MapConstants.NUM_ROWS - 1 && col != 0 && col != MapConstants.NUM_COLS - 1)) {
            this.isVirtualWall = val;
        }
    }

    public boolean getIsVirtualWall() {
        return this.isVirtualWall;
    }

    public void setIsExplored(boolean val) {
        this.isExplored = val;
    }

    public boolean getIsExplored() {
        return this.isExplored;
    }
}
