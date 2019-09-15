package algorithms;


//import java.util.Iterator;
import java.util.LinkedList;
//import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Comparator;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utilities.CommMgr;

/**
 * Exploration algorithm for the robot.
 *
 * @author MDP Group 3
 */

public class ExplorationAlgo {
    private final Map exploredMap;
    private final Map actualMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private static int areaExplored = 0;
    private long startTime;
    private long endTime;
    private int lastCalibrate;
    private boolean calibrationMode;

    private LinkedList<Cell>[][] dfsNodes;
    private int[][] visited;
    //private Stack<Cell> stack;

    public ExplorationAlgo(Map exploredMap, Map actualMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.actualMap = actualMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
        dfsNodes = new LinkedList[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
        for(int row=0; row<MapConstants.NUM_ROWS; row++){
            for(int col=0; col<MapConstants.NUM_COLS; col++){
                dfsNodes[row][col] = new LinkedList<Cell>();
            }
        }
        visited = new int[MapConstants.NUM_ROWS][MapConstants.NUM_COLS];
        for(int[] row:visited){
            Arrays.fill(row, 1);
        }
        //stack = new Stack<Cell>();
    }

    /**
     * Increment the area explored by 1 when called.
     */
    public static void incAreaExplored(){
        areaExplored++;
    }

    public void addNeighbours(int row, int col, Cell cell){
        dfsNodes[row][col].add(cell);
    }

    public void doHeuristicExplore(){
        //stack = new Stack<Cell>();

        Cell prevCell = null;

        //stack.push(cell);

        //Stack<Cell> prevCells = new Stack<Cell>();

        while(areaExplored < coverageLimit && System.currentTimeMillis() < endTime){
            Cell currentCell = exploredMap.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());
            visited[currentCell.getRow()][currentCell.getCol()] *= 2;
            //visited[currentCell.getRow()][currentCell.getCol()]++;

            /* if(!visited[currentCell.getRow()][currentCell.getCol()]){
                visited[currentCell.getRow()][currentCell.getCol()] = true;
            } */

            getChildCells(currentCell);

            /* System.out.println("The queue elements:");
            Iterator<Cell> itr = queue.iterator(); 
            while (itr.hasNext()) {
                Cell temp = itr.next();
                System.out.println(temp.getRow() + "," + temp.getCol() + ":" + temp.getDistToNearestUnexplored()); 
            } */

            //Iterator<Cell> itr = dfsNodes[currentCell.getRow()][currentCell.getCol()].iterator();

            //boolean needBacktrack = true;

            //while(!queue.isEmpty()){
                //Cell childCell = itr.next();
                //Cell childCell = queue.poll();
                //System.out.println("row " + childCell.getRow() + " col " + childCell.getCol() + childHasUnexploredNeighbours(childCell.getRow(), childCell.getCol()));
                /* if(!visited[childCell.getRow()][childCell.getCol()]){
                    needBacktrack = false;
                    stack.push(childCell);
                    System.out.println("row:" + childCell.getRow() + "col:" + childCell.getCol());
                } */
                //needBacktrack = false;
                //stack.push(childCell);
                //System.out.println("row:" + childCell.getRow() + "col:" + childCell.getCol());}
            
            /* if(needBacktrack){
                Cell prevCell = prevCells.pop();
                stack.push(prevCell);
                System.out.println("row:" + prevCell.getRow() + "col:" + prevCell.getCol());
            } */

            //if(!queue.isEmpty()){
                Cell nextCell = getNextCell();
                MOVEMENT m = compareToGetDir(currentCell, nextCell);
                nextAction(m);
                System.out.println("Next row:" + nextCell.getRow() + "col:" + nextCell.getCol());
                System.out.println("Area explored: " + areaExplored);
                if(nextCell.equals(prevCell)){
                    visited[currentCell.getRow()][currentCell.getCol()] += 100;
                    System.out.println("dead end");
                }
                prevCell = currentCell;
                /* if(!needBacktrack){
                    prevCells.push(currentCell);
                } */
            //}

            dfsNodes[currentCell.getRow()][currentCell.getCol()].clear();

        }

        goToGoal();
        backToStart();
    }

    private Cell getNextCell(){
        Comparator<Cell> comparator = new UnexploredChildComparator();
        PriorityQueue<Cell> queue = new PriorityQueue<>(4, comparator);
        Cell currentCell = exploredMap.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());

        for(Cell c : dfsNodes[currentCell.getRow()][currentCell.getCol()]){
            int temp = c.getDistToNearestUnexplored() + visited[c.getRow()][c.getCol()];
            switch(compareToGetDir(currentCell, c)){
                case LEFT:
                case RIGHT:
                    c.setDistToNearestUnexplored(temp + 2);
                    break;
                case BACKWARD:
                    c.setDistToNearestUnexplored(temp + 4);
                    break;
                default:
                    c.setDistToNearestUnexplored(temp);
                    break;
            }
            queue.add(c);
        }

        return queue.poll();
    }

    /* private int childHasUnexploredNeighbours(int row, int col, DIRECTION moveDir){
        int unexploredCount=0;
        int neighbourRow=row;
        int neighbourCol=col;
        switch(moveDir){
            case NORTH:
                neighbourRow += 3;
                if(exploredMap.isCellValid(neighbourRow, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow, neighbourCol - 1)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol - 1).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow, neighbourCol + 1)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol + 1).getIsExplored()) unexploredCount++;
                } 
                break;
            case EAST:
                neighbourCol += 3;
                if(exploredMap.isCellValid(neighbourRow, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow - 1, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow - 1, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow + 1, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow + 1, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                break;
            case SOUTH:
                neighbourRow -= 3;
                if(exploredMap.isCellValid(neighbourRow, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow, neighbourCol - 1)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol - 1).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow, neighbourCol + 1)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol + 1).getIsExplored()) unexploredCount++;
                } 
                break;
            case WEST:
                neighbourCol -= 3;
                if(exploredMap.isCellValid(neighbourRow, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow - 1, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow - 1, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                if(exploredMap.isCellValid(neighbourRow + 1, neighbourCol)){
                    if(!exploredMap.getCell(neighbourRow + 1, neighbourCol).getIsExplored()) unexploredCount++;
                } 
                break;
        }
        return unexploredCount;
    } */

    private int calcUnexploredArea(int row, int col){
        int leastDistToUnexplored = 100;
        for (int i = 0; i < MapConstants.NUM_ROWS; i++) {
            for (int j = 0; j < MapConstants.NUM_COLS; j++) {
                if (!exploredMap.getCell(i, j).getIsExplored()) {
                    int manDist = findManhattanDistance(i, j, row, col);
                    if(leastDistToUnexplored > manDist){
                        leastDistToUnexplored = manDist;
                    }
                }
            }
        }
        return leastDistToUnexplored;
    }

    private int findManhattanDistance(int goalRow, int goalCol, int fromRow, int fromCol){
        return Math.abs(goalRow - fromRow) + Math.abs(goalCol - fromCol);
    }

    private MOVEMENT compareToGetDir(Cell currentCell, Cell nextCell){
        int rowDiff = nextCell.getRow() - currentCell.getRow();
        int colDiff = nextCell.getCol() - currentCell.getCol();
        
        switch(bot.getRobotCurDir()){
            case NORTH:
                if(colDiff==0){
                    if(rowDiff>0){
                        return MOVEMENT.FORWARD;
                    }
                    else{
                        return MOVEMENT.BACKWARD;
                    }
                }
                else if(colDiff>0){
                    return MOVEMENT.RIGHT;
                }
                else{
                    return MOVEMENT.LEFT;
                }
            case EAST:
                if(rowDiff==0){
                    if(colDiff>0){
                        return MOVEMENT.FORWARD;
                    }
                    else{
                        return MOVEMENT.BACKWARD;
                    }
                }
                else if(rowDiff>0){
                    return MOVEMENT.LEFT;
                }
                else{
                    return MOVEMENT.RIGHT;
                }
            case SOUTH:
                if(colDiff==0){
                    if(rowDiff<0){
                        return MOVEMENT.FORWARD;
                    }
                    else{
                        return MOVEMENT.BACKWARD;
                    }
                }
                else if(colDiff>0){
                    return MOVEMENT.LEFT;
                }
                else{
                    return MOVEMENT.RIGHT;
                }
            case WEST:
                if(rowDiff==0){
                    if(colDiff<0){
                        return MOVEMENT.FORWARD;
                    }
                    else{
                        return MOVEMENT.BACKWARD;
                    }
                }
                else if(rowDiff<0){
                    return MOVEMENT.LEFT;
                }
                else{
                    return MOVEMENT.RIGHT;
                }
            default:
                System.out.println(bot.getRobotCurDir());
                return MOVEMENT.ERROR;
        }
    }

    private void getChildCells(Cell cell){
        int row = cell.getRow();
        int col = cell.getCol();
        Cell childCell;
        if(areaExplored < 30){
            if(southCanMove()){
                childCell = exploredMap.getCell(row - 1, col);
                addNeighbours(row, col, childCell);
            }
            if(westCanMove()){
                childCell = exploredMap.getCell(row, col - 1);
                addNeighbours(row, col, childCell);
            }
            if(northCanMove()){
                childCell = exploredMap.getCell(row + 1, col);
                addNeighbours(row, col, childCell);
            }
            if(eastCanMove()){
                childCell = exploredMap.getCell(row, col + 1);
                addNeighbours(row, col, childCell);
            }
        }else{
            if(southCanMove()){
                int noOfUnexploredNeighbours = calcUnexploredArea(row - 1, col);
                //int noOfUnexploredNeighbours = childHasUnexploredNeighbours(row - 1, col, DIRECTION.SOUTH);
                childCell = exploredMap.getCell(row - 1, col);
                childCell.setDistToNearestUnexplored(noOfUnexploredNeighbours);
                addNeighbours(row, col, childCell);
            }
            if(westCanMove()){
                int noOfUnexploredNeighbours = calcUnexploredArea(row, col - 1);
                //int noOfUnexploredNeighbours = childHasUnexploredNeighbours(row, col - 1, DIRECTION.WEST);
                childCell = exploredMap.getCell(row, col - 1);
                childCell.setDistToNearestUnexplored(noOfUnexploredNeighbours);
                addNeighbours(row, col, childCell);
            }
            if(northCanMove()){
                int noOfUnexploredNeighbours = calcUnexploredArea(row + 1, col);
                //int noOfUnexploredNeighbours = childHasUnexploredNeighbours(row + 1, col, DIRECTION.NORTH);
                childCell = exploredMap.getCell(row + 1, col);
                childCell.setDistToNearestUnexplored(noOfUnexploredNeighbours);
                addNeighbours(row, col, childCell);
            }
            if(eastCanMove()){
                int noOfUnexploredNeighbours = calcUnexploredArea(row, col + 1);
                //int noOfUnexploredNeighbours = childHasUnexploredNeighbours(row, col + 1, DIRECTION.EAST);
                childCell = exploredMap.getCell(row, col + 1);
                childCell.setDistToNearestUnexplored(noOfUnexploredNeighbours);
                addNeighbours(row, col, childCell);
            }
        }
    }

    /**
     * Main method that is called to start the exploration.
     */
    public void runExploration() {
        boolean actualBot = bot.getActualBot();
        if (actualBot) {
            System.out.println("Starting calibration...");

            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.LEFT, false);
            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.CALIBRATE, false);
            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.LEFT, false);
            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.CALIBRATE, false);
            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.RIGHT, false);
            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.CALIBRATE, false);
            CommMgr.getCommMgr().receiveMsg();
            bot.move(MOVEMENT.RIGHT, false);

            while (true) {
                System.out.println("Waiting for START_EXPR...");
                String msg = CommMgr.getCommMgr().receiveMsg();
                String[] msgArr = msg.split(";");
                // if the 1st msg is "START_EXPR", exit while loop
                if (msgArr[0].equals(CommMgr.START_EXPR)) break;
            }
        }

        System.out.println("Starting exploration...");

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        if (actualBot) {
            CommMgr.getCommMgr().sendMsg(null, CommMgr.ROBOT_START);
        }
        //area of start & goal zone
        System.out.println("Explored Area: " + areaExplored);
        senseAndUpdate();

        //areaExplored = calculateAreaExplored();
        System.out.println("Explored Area: " + areaExplored);

        //explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());

        doHeuristicExplore();
    }

    /*
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    /* private void explorationLoop(int r, int c) {
        do {
            nextAction();

            //areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);

            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) {
                if (areaExplored >= 100) {
                    break;
                }
            }
        } while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);

        backToStart();
    } */

    /*
     * Determines the next move for the robot and executes it accordingly.
     */
    /* private void nextAction() {
        if (canMoveRight()) {
            moveBot(MOVEMENT.RIGHT);
            if (canMoveForward()) moveBot(MOVEMENT.FORWARD);
        } else if (canMoveForward()) {
            moveBot(MOVEMENT.FORWARD);
        } else if (canMoveLeft()) {
            moveBot(MOVEMENT.LEFT);
            if (canMoveForward()) moveBot(MOVEMENT.FORWARD);
        } else {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    } */

    private void nextAction(MOVEMENT m) {

        switch(m){
            case RIGHT:
                moveBot(MOVEMENT.RIGHT);
                moveBot(MOVEMENT.FORWARD);
                break;
            case FORWARD:
                moveBot(MOVEMENT.FORWARD);
                break;
            case LEFT:
                moveBot(MOVEMENT.LEFT);
                moveBot(MOVEMENT.FORWARD);
                break;
            case BACKWARD:
                moveBot(MOVEMENT.RIGHT);
                moveBot(MOVEMENT.RIGHT);
                moveBot(MOVEMENT.FORWARD);
                break;
            default:
                System.out.print("MOVEMENT: " + m);
                System.out.println(", DIR: " + bot.getRobotCurDir());
                System.out.println("Error in row: " + bot.getRobotPosRow() + ", col: " + bot.getRobotPosCol());
        }
    }

    /*
     * Returns true if the right side of the robot is free to move into.
     */
    /* private boolean canMoveRight() {
        /* switch (bot.getRobotCurDir()) {
            case NORTH:
                if(eastCanMove()) return 'N';
            case EAST:
                if(southCanMove()) return 'E';
            case SOUTH:
                if(westCanMove()) return 'S';
            case WEST:
                if(northCanMove()) return 'W';
        }
        return 'X'; */
        /* switch(bot.getRobotCurDir()){
            case NORTH:
                return eastCanMove();
            case EAST:
                return southCanMove();
            case SOUTH:
                return westCanMove();
            case WEST:
                return northCanMove();
        }
        return false;
    } */

    /*
     * Returns true if the robot is free to move forward.
     */
    /* private boolean canMoveForward() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return northCanMove();
            case EAST:
                return eastCanMove();
            case SOUTH:
                return southCanMove();
            case WEST:
                return westCanMove();
        }
        return false;
    } */

    /*
     * * Returns true if the left side of the robot is free to move into.
     */
    /* private boolean canMoveLeft() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return westCanMove();
            case EAST:
                return northCanMove();
            case SOUTH:
                return eastCanMove();
            case WEST:
                return southCanMove();
        }
        return false;
    } */

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northCanMove() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastCanMove() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southCanMove() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westCanMove() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void backToStart() {
        if (!bot.getReachedGoal() && coverageLimit == 300 && timeLimit == 3600) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, actualMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }

        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, actualMap);
        returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);

        System.out.println("Exploration complete!");
        //areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        if (bot.getActualBot()) {
            turnBotToDir(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotToDir(DIRECTION.SOUTH);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotToDir(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
        }
        turnBotToDir(DIRECTION.NORTH);
    }

    private void goToGoal(){
        if (!bot.getReachedGoal()) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, actualMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredNotObstacle(int r, int c) {
        if (exploredMap.isCellValid(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.isCellValid(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsVirtualWall() && !b.getIsObstacle());
        }
        return false;
    }

    /* Returns the number of cells explored in the grid.
     */
    /* private int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < MapConstants.NUM_ROWS; r++) {
            for (int c = 0; c < MapConstants.NUM_COLS; c++) {
                if (exploredMap.getCell(r, c).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    } */

    /**
     * Moves the bot, repaints the map and calls senseAndUpdate().
     */
    private void moveBot(MOVEMENT m) {
        bot.move(m, true);
        exploredMap.revalidate();
        if (m != MOVEMENT.CALIBRATE) {
            senseAndUpdate();
        } else {
            CommMgr commMgr = CommMgr.getCommMgr();
            commMgr.receiveMsg();
        }

        if (bot.getActualBot() && !calibrationMode) {
            calibrationMode = true;

            if (canCalibrateNow(bot.getRobotCurDir())) {
                lastCalibrate = 0;
                moveBot(MOVEMENT.CALIBRATE);
            } else {
                lastCalibrate++;
                if (lastCalibrate >= 5) {
                    DIRECTION targetDir = getCalibrationDir();
                    if (targetDir != null) {
                        lastCalibrate = 0;
                        calibrateBot(targetDir);
                    }
                }
            }

            calibrationMode = false;
        }
    }

    /**
     * Sets the bot's sensors, processes the sensor data and updates the map by repainting it.
     */
    private void senseAndUpdate() {
        bot.setSensors();
        bot.sense(exploredMap, actualMap);
        exploredMap.repaint(1,1,600,700);
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    private boolean canCalibrateNow(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (botDir) {
            case NORTH:
                return exploredMap.isObstacleOrWall(row + 2, col - 1) && exploredMap.isObstacleOrWall(row + 2, col) && exploredMap.isObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exploredMap.isObstacleOrWall(row + 1, col + 2) && exploredMap.isObstacleOrWall(row, col + 2) && exploredMap.isObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exploredMap.isObstacleOrWall(row - 2, col - 1) && exploredMap.isObstacleOrWall(row - 2, col) && exploredMap.isObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exploredMap.isObstacleOrWall(row + 1, col - 2) && exploredMap.isObstacleOrWall(row, col - 2) && exploredMap.isObstacleOrWall(row - 1, col - 2);
        }

        return false;
    }

    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
    private DIRECTION getCalibrationDir() {
        DIRECTION origDir = bot.getRobotCurDir();
        DIRECTION dirToCheck;

        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
        if (canCalibrateNow(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateNow(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateNow(dirToCheck)) return dirToCheck;

        return null;
    }

    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
    private void calibrateBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();

        turnBotToDir(targetDir);
        moveBot(MOVEMENT.CALIBRATE);
        turnBotToDir(origDir);
    }

    /**
     * Turns the robot to the required direction.
     */
    private void turnBotToDir(DIRECTION targetDir) {
        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
            } else {
                moveBot(MOVEMENT.LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }
}
