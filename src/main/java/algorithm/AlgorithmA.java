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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class AlgorithmA {
    private int startCostValue = 0;
    private int totalValue;
    private Maze maze;
    private MazeState startState;
    private MazeState actualState;
    private MazeState destinationState;



    public AlgorithmA(Maze maze) {
        this.maze = maze;
    }

    public void Start(){
        prepareEnvironment();

        PriorityQueue<MazeState> openList = new PriorityQueue<>();
        List<MazeState> closedList = new ArrayList<>();

        MazeState[] userSelectedStates = GiveStartAndDestinationPosition(0,0,5,5);
        startState = userSelectedStates[0];
        destinationState = userSelectedStates[1];
        actualState = startState;


        openList.add(actualState);


    }

    public void vypis(){
        System.out.println(maze.getLogicalMaze().length);
    }

    private MazeState[] GiveStartAndDestinationPosition(int xStart, int yStart, int xDestination, int yDestination){
        MazeState[] states = new MazeState[2];
        MazeState startPosition = new MazeState(startCostValue, xStart, yStart,null , Orientation.NORTH);
        MazeState destiantionPosition = new MazeState(totalValue, xDestination, yDestination,null , Orientation.NORTH);
        //Check if blocks aren't solid blocks or throw run time exception
        if(!maze.areValidEnterStates(xStart, yStart))
            throw new RuntimeException("Cant choose" + xStart + yStart + " as destination");
        if(!maze.areValidEnterStates(xDestination,yDestination))
            throw new RuntimeException("Cant choose" + xDestination + yDestination + " as destination");

        states[0] = startPosition;
        states[1] = destiantionPosition;

        return states;
    }


    private boolean isDestinationReached(){
        if(actualState.getXPosition() == destinationState.getXPosition()
                &&
                actualState.getYPosition() == destinationState.getYPosition())
            return true;
        else
            return false;
    }

    private void prepareEnvironment(){
        maze.GetMazeFromFile();
    }
}
