package algorithms;

import java.util.Comparator;
import map.Cell;

public class UnexploredChildComparator implements Comparator<Cell>{
    @Override
    public int compare(Cell x, Cell y){
        if(x.getDistToNearestUnexplored() < y.getDistToNearestUnexplored())
            return -1;
        else if(x.getDistToNearestUnexplored() > y.getDistToNearestUnexplored())
            return 1;
        else return 0;
    }
}