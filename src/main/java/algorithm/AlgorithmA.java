package algorithm;

/*
* Finding the most effective way to get from start to end
* Using openList and closedList
* OpenList contains states which are waiting for discovery
* ClosedList contains already found states to avoid discovering them again
* The beginning of algorithm will start with adding start state of our state, and then we'll choose
*       the cheapest way to continue exploring new states.
* When we move to another state we add it to our closed list.
* When we are on our new state we discover all the new possible ways to continue discovering next state.
* And just like that we continue to discover our final destination.
* Input should be start position and destination from terminal or from gui
*/

import MazeEntity.Maze;
import MazeEntity.MazeState;
import enums.Orientation;

public class AlgorithmA {
    private int startCostValue = 0;
    private int totalValue;
    private Maze maze;

    public AlgorithmA(Maze maze) {
        this.maze = maze;
    }


    public void vypis(){
        System.out.println(maze.getLogicalMaze().length);

    }

    private void GiveStartAndDestinationPosition(int xStart, int yStart, int xDestination, int YDestination){


        MazeState startPosition = new MazeState(startCostValue, xStart, yStart,null , Orientation.NORTH);
        MazeState destiantionPosition = new MazeState(totalValue, xDestination, YDestination,null , Orientation.NORTH);
    }


    private boolean isDestinationReached(MazeState acutalPosition, MazeState finalDestination){
        if(acutalPosition.getXPosition() == finalDestination.getXPosition()
                &&
                acutalPosition.getYPosition() == finalDestination.getYPosition())
            return true;
        else
            return false;
    }
}
