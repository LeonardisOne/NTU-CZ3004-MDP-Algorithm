package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import simulator.UIlayout_v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

// @formatter:off
/**
 * Fastest path algorithm for the robot. Uses a version of the A* algorithm.
 *
 * g(n) = True path cost from START to n
 * h(n) = Heuristic path cost from n to GOAL
 * f(n) = g(n) + h(n)
 *
 * @author MDP Group 3
 */
// @formatter:on

public class FastestPathAlgo {
    private ArrayList<Cell> toVisit;        // array of Cells to be visited
    private ArrayList<Cell> visited;        // array of visited Cells
    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
    private Cell current;                   // current Cell
    private Cell[] neighbors;               // array of neighbors of current Cell
    private DIRECTION curDir;               // current direction of robot
    private double[][] truePathCost;        // array of true path cost from START to [row][col] i.e. g(n)
    private Robot bot;
    private Map exploredMap;
    private final Map actualMap;
    private int loopCounter;
    private boolean explorationMode;
    private DIRECTION prevDirection;
    private int display_timeElapsed;
    private UIlayout_v2 _ui = new UIlayout_v2();
    private boolean runTimer = true;
    public FastestPathAlgo(Map exploredMap, Robot bot) {
        this.actualMap = null;
        initObject(exploredMap, bot);
    }

    public FastestPathAlgo(Map exploredMap, Robot bot, Map actualMap) {
        this.actualMap = actualMap;
        this.explorationMode = true;
        initObject(exploredMap, bot);
    }

    /**
     * Initialise the FastestPathAlgo object.
     */
    private void initObject(Map map, Robot bot) {
        this.bot = bot;
        this.exploredMap = map;
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.current = map.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());
        this.curDir = bot.getRobotCurDir();
        this.truePathCost = new double[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];

        // Initialise truePathCost array
        for (int i = 0; i < MapConstants.NUM_ROWS; i++) {
            for (int j = 0; j < MapConstants.NUM_COLS; j++) {
                Cell cell = map.getCell(i, j);
                if (!canVisit(cell)) {
                    truePathCost[i][j] = RobotConstants.INFINITE_COST;
                } else {
                    truePathCost[i][j] = 0;
                }
            }
        }
        toVisit.add(current);

        // Initialise starting point
        truePathCost[bot.getRobotPosRow()][bot.getRobotPosCol()] = 0;
        this.loopCounter = 0;
    }

    /**
     * Returns true if the cell can be visited.
     */
    private boolean canVisit(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall();
    }

    /**
     * Returns the Cell inside toVisit with the minimum f(n).
     */
    private Cell minCostCell(int goalRow, int getCol) {
        int size = toVisit.size();
        double minCost = RobotConstants.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            double gCost = truePathCost[(toVisit.get(i).getRow())][(toVisit.get(i).getCol())];
            double cost = gCost + estCost(toVisit.get(i), goalRow, getCol);
            if (cost < minCost) {
                minCost = cost;
                result = toVisit.get(i);
            }
        }

        return result;
    }

    /**
     * Returns the heuristic cost i.e. h(n) from a given Cell to a given [goalRow, goalCol] in the maze.
     */
    private double estCost(Cell b, int goalRow, int goalCol) {
        // Heuristic: The no. of moves will be equal to the difference in the row and column values.
        double pathCost = (Math.abs(goalCol - b.getCol()) + Math.abs(goalRow - b.getRow())) * RobotConstants.MOVE_COST;

        if (pathCost == 0) return 0;

        // Heuristic: If b is not in the same row or column, one turn will be needed.
        double turnCost = 0;
        if (goalCol - b.getCol() != 0 || goalRow - b.getRow() != 0) {
            turnCost = RobotConstants.TURN_COST;
        }

        return pathCost + turnCost;
    }

    /**
     * Returns the target direction of the bot from [botR, botC] to target Cell.
     */
    private DIRECTION getTargetDir(int botR, int botC, DIRECTION botDir, Cell target) {
        if (botC - target.getCol() > 0) {
            return DIRECTION.WEST;
        } else if (target.getCol() - botC > 0) {
            return DIRECTION.EAST;
        } else {
            if (botR - target.getRow() > 0) {
                return DIRECTION.SOUTH;
            } else if (target.getRow() - botR > 0) {
                return DIRECTION.NORTH;
            } else {
                return botDir;
            }
        }
    }

    /**
     * Get the actual turning cost from one DIRECTION to another.
     */
    private double getTurnCost(DIRECTION a, DIRECTION b) {
        int numOfTurn = Math.abs(a.ordinal() - b.ordinal());
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * RobotConstants.TURN_COST);
    }

    /**
     * Calculate the actual cost of moving from Cell a to Cell b (assuming both are neighbors).
     */
    private double trueCost(Cell a, Cell b, DIRECTION aDir) {
        double moveCost = RobotConstants.MOVE_COST; // one movement to neighbor

        double turnCost;
        DIRECTION targetDir = getTargetDir(a.getRow(), a.getCol(), aDir, b);
        turnCost = getTurnCost(aDir, targetDir);

        return moveCost + turnCost;
    }

    /**
     * Find the fastest path from the robot's current position to [goalRow, goalCol].
     */
    public String runFastestPath(int goalRow, int goalCol) {
        if(goalRow!=RobotConstants.GOAL_ROW && goalCol!=RobotConstants.GOAL_COL){
            if(exploredMap.isCellObstacle(goalRow, goalCol-1)){
                goalCol = goalCol+1;
            }//left side of waypoint is obstacle
            else if(exploredMap.isCellObstacle(goalRow, goalCol+1)){
                goalCol = goalCol-1;
            }
            else if(exploredMap.isCellObstacle(goalRow+1, goalCol+1)){
                goalCol = goalCol-1;
            }


        }
        System.out.println("Calculating fastest path from (" + current.getRow() + ", " + current.getCol() + ") to goal (" + goalRow + ", " + goalCol + ")...");
        
        Stack<Cell> path;
        do {
            loopCounter++;

            // Get cell with minimum cost from toVisit and assign it to current.
            current = minCostCell(goalRow, goalCol);
            // Point the robot in the direction of current from the previous cell.
            if (parents.containsKey(current)) {
                curDir = getTargetDir(parents.get(current).getRow(), parents.get(current).getCol(), curDir, current);
            }

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("Goal visited. Path found!");
                path = getPath(goalRow, goalCol);
                printFastestPath(path);
                return executePath(path, goalRow, goalCol);
            }

            // Setup neighbors of current cell. [Top, Bottom, Left, Right].
            if (exploredMap.isCellValid(current.getRow() + 1, current.getCol())) {
                neighbors[0] = exploredMap.getCell(current.getRow() + 1, current.getCol());
                if (!canVisit(neighbors[0])) {
                    neighbors[0] = null;
                }
            }
            if (exploredMap.isCellValid(current.getRow() - 1, current.getCol())) {
                neighbors[1] = exploredMap.getCell(current.getRow() - 1, current.getCol());
                if (!canVisit(neighbors[1])) {
                    neighbors[1] = null;
                }
            }
            if (exploredMap.isCellValid(current.getRow(), current.getCol() - 1)) {
                neighbors[2] = exploredMap.getCell(current.getRow(), current.getCol() - 1);
                if (!canVisit(neighbors[2])) {
                    neighbors[2] = null;
                }
            }
            if (exploredMap.isCellValid(current.getRow(), current.getCol() + 1)) {
                neighbors[3] = exploredMap.getCell(current.getRow(), current.getCol() + 1);
                if (!canVisit(neighbors[3])) {
                    neighbors[3] = null;
                }
            }

            // Iterate through neighbors and update the g(n) values of each.
            for (int i = 0; i < 4; i++) {
                if (neighbors[i] != null) {
                    if (visited.contains(neighbors[i])) {
                        continue;
                    }

                    if (!(toVisit.contains(neighbors[i]))) {
                        parents.put(neighbors[i], current);
                        truePathCost[neighbors[i].getRow()][neighbors[i].getCol()] = truePathCost[current.getRow()][current.getCol()] + trueCost(current, neighbors[i], curDir);
                        toVisit.add(neighbors[i]);
                    } else {
                        double currentGScore = truePathCost[neighbors[i].getRow()][neighbors[i].getCol()];
                        double newGScore = truePathCost[current.getRow()][current.getCol()] + trueCost(current, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            truePathCost[neighbors[i].getRow()][neighbors[i].getCol()] = newGScore;
                            parents.put(neighbors[i], current);
                        }
                    }
                }
            }
            prevDirection = bot.getRobotCurDir();
        } while (!toVisit.isEmpty());
        System.out.println("Path not found!");
        return null;
    }

    public void startTimer(){
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                display_timeElapsed++;
                
                _ui.setTimer(display_timeElapsed);
                
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
}

    /**
     * Generates path in reverse using the parents HashMap.
     */
    private Stack<Cell> getPath(int goalRow, int goalCol) {
        Stack<Cell> actualPath = new Stack<>();
        Cell temp = exploredMap.getCell(goalRow, goalCol);

        while (true) {
            actualPath.push(temp);
            temp = parents.get(temp);
            if (temp == null) {
                break;
            }
        }

        return actualPath;
    }

    
    /**
     * Executes the fastest path and returns a StringBuilder object with the path steps.
     */
    private String executePath(Stack<Cell> path, int goalRow, int goalCol) {
        StringBuilder outputString = new StringBuilder();

        Cell temp = path.pop();
        DIRECTION targetDir;

        ArrayList<MOVEMENT> movements = new ArrayList<>();

        Robot tempBot;
        if(goalRow == RobotConstants.GOAL_ROW && goalCol == RobotConstants.GOAL_COL){
            tempBot = new Robot(bot.getWP_row(), bot.getWP_col(), false);
            tempBot.setRobotDir(prevDirection);
        }
        else{
            tempBot = new Robot(1, 1, false);
        }
        //Robot tempBot = new Robot(1, 1, false);
        tempBot.setSpeed(0);
        System.out.println("temp bot row:"+tempBot.getRobotPosRow()+" col: "+ tempBot.getRobotPosCol() );
        while ((tempBot.getRobotPosRow() != goalRow) || (tempBot.getRobotPosCol() != goalCol)) {
            if (tempBot.getRobotPosRow() == temp.getRow() && tempBot.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
            }

            targetDir = getTargetDir(tempBot.getRobotPosRow(), tempBot.getRobotPosCol(), tempBot.getRobotCurDir(), temp);

            MOVEMENT m;
            if (tempBot.getRobotCurDir() != targetDir) {
                m = getTargetMove(tempBot.getRobotCurDir(), targetDir);
            } else {
                m = MOVEMENT.FORWARD;
            }

            System.out.println("Movement " + MOVEMENT.print(m) + " from (" + tempBot.getRobotPosRow() + ", " + tempBot.getRobotPosCol() + ") to (" + temp.getRow() + ", " + temp.getCol() + ")");

            tempBot.move(m, true);
            movements.add(m);
            outputString.append(MOVEMENT.print(m));
        }

        if (!bot.getActualBot() || explorationMode) {
            for (MOVEMENT x : movements) {
                if (x == MOVEMENT.FORWARD) {
                    if (!canMoveForward()) {
                        System.out.println("Early termination of fastest path execution.");
                        return "T";
                    }
                }

                bot.move(x, true);
                this.exploredMap.repaint();

                // During exploration, use sensor data to update exploredMap.
                if (explorationMode) {
                    bot.setSensors();
                    bot.sense(this.exploredMap, this.actualMap);
                    this.exploredMap.revalidate();
                }
            }
        } else {
            int fCount = 0;
            for (MOVEMENT x : movements) {
                if (x == MOVEMENT.FORWARD) {
                    fCount++;
                    if (fCount == 10) {
                        bot.moveForwardMultiCell(fCount);
                        fCount = 0;
                        exploredMap.revalidate();
                    }
                } else if (x == MOVEMENT.RIGHT || x == MOVEMENT.LEFT) {
                    if (fCount > 0) {
                        bot.moveForwardMultiCell(fCount);
                        fCount = 0;
                        exploredMap.revalidate();
                    }

                    bot.move(x, true);
                    exploredMap.revalidate();
                }
            }//end for(MOVEMENT x : movements)

            if (fCount > 0) {
                bot.moveForwardMultiCell(fCount);
                exploredMap.revalidate();
            }
        }

        System.out.println("\nMovements: " + outputString.toString());
        return outputString.toString();
    }

    /**
     * Returns true if the robot can move forward one cell with the current heading.
     */
    private boolean canMoveForward() {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (bot.getRobotCurDir()) {
            case NORTH:
                if (!exploredMap.isCellObstacle(row + 2, col - 1) && !exploredMap.isCellObstacle(row + 2, col) && !exploredMap.isCellObstacle(row + 2, col + 1)) {
                    return true;
                }
                break;
            case EAST:
                if (!exploredMap.isCellObstacle(row + 1, col + 2) && !exploredMap.isCellObstacle(row, col + 2) && !exploredMap.isCellObstacle(row - 1, col + 2)) {
                    return true;
                }
                break;
            case SOUTH:
                if (!exploredMap.isCellObstacle(row - 2, col - 1) && !exploredMap.isCellObstacle(row - 2, col) && !exploredMap.isCellObstacle(row - 2, col + 1)) {
                    return true;
                }
                break;
            case WEST:
                if (!exploredMap.isCellObstacle(row + 1, col - 2) && !exploredMap.isCellObstacle(row, col - 2) && !exploredMap.isCellObstacle(row - 1, col - 2)) {
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Returns the movement to execute to get from one direction to another.
     */
    private MOVEMENT getTargetMove(DIRECTION a, DIRECTION b) {
        switch (a) {
            case NORTH:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.ERROR;
                    case SOUTH:
                        return MOVEMENT.LEFT;
                    case WEST:
                        return MOVEMENT.LEFT;
                    case EAST:
                        return MOVEMENT.RIGHT;
                }
                break;
            case SOUTH:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.LEFT;
                    case SOUTH:
                        return MOVEMENT.ERROR;
                    case WEST:
                        return MOVEMENT.RIGHT;
                    case EAST:
                        return MOVEMENT.LEFT;
                }
                break;
            case WEST:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.RIGHT;
                    case SOUTH:
                        return MOVEMENT.LEFT;
                    case WEST:
                        return MOVEMENT.ERROR;
                    case EAST:
                        return MOVEMENT.LEFT;
                }
                break;
            case EAST:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.LEFT;
                    case SOUTH:
                        return MOVEMENT.RIGHT;
                    case WEST:
                        return MOVEMENT.LEFT;
                    case EAST:
                        return MOVEMENT.ERROR;
                }
        }
        return MOVEMENT.ERROR;
    }

    /**
     * Prints the fastest path from the Stack object.
     */
    private void printFastestPath(Stack<Cell> path) {
        System.out.println("\nLooped " + loopCounter + " times.");
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ") --> ");
            else System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ")");
        }

        System.out.println("\n");
    }

    /**
     * Prints all the current g(n) values for the cells.
     */
    /* public void printGCosts() {
        for (int i = 0; i < MapConstants.NUM_ROWS; i++) {
            for (int j = 0; j < MapConstants.NUM_COLS; j++) {
                System.out.print(truePathCost[MapConstants.NUM_ROWS - 1 - i][j]);
                System.out.print(";");
            }
            System.out.println("\n");
        }
    } */
}
