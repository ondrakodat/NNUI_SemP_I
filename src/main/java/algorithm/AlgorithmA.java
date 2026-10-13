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

import java.util.*;

public class AlgorithmA {
    private int startCostValue = 0;
    private int totalValue;
    private Maze maze;
    private MazeState startState;
    private MazeState actualState;
    private MazeState destinationState;
    private boolean foundDestination = false;

    PriorityQueue<MazeState> openList = new PriorityQueue<>((MazeState c1, MazeState c2) -> Integer.compare(calculateEstimatedTotalCost(c1), calculateEstimatedTotalCost(c2)));
    Set<MazeState> closedList = new HashSet<>();
    List<MazeState> bestWay = new ArrayList<>();

    public AlgorithmA(Maze maze) {
        this.maze = maze;
    }

    public void Start(){
        prepareEnvironment();
        MazeState[] userSelectedStates = GiveStartAndDestinationPosition(5,3,5,31);
        startState = userSelectedStates[0];
        destinationState = userSelectedStates[1];
        actualState = startState;


        openList.add(actualState);

        while(!openList.isEmpty()){
            actualState = openList.poll();
            if(isDestinationReached()){
                System.out.println("Final destination state reached " + actualState.getTotalCost());
                foundDestination = true;
                break;
            }
            closedList.add(actualState);

            MazeState leftState = actualState.turnLeft90Degrees();
            if(!openList.contains(leftState) && !closedList.contains(leftState))
                openList.add(leftState);

            MazeState rightState = actualState.turnRight90Degrees();
            if(!openList.contains(rightState) && !closedList.contains(rightState))
                openList.add(rightState);

            MazeState aroundState = actualState.turnAround180Degrees();
            if(!openList.contains(aroundState) && !closedList.contains(aroundState) )
                openList.add(aroundState);

            MazeState forwardState = actualState.moveForward();
            if(maze.areValidEnterStates(forwardState.getXPosition(), forwardState.getYPosition()) && !openList.contains(forwardState) && !closedList.contains(forwardState)){
                openList.add(forwardState);
            }
        }
        if (foundDestination) {
            MazeState state = actualState;

            while (state != null) {
                bestWay.add(state);
                state = state.getPreviousState();
            }

            Collections.reverse(bestWay);
        }
        maze.drawExploredArea(bestWay, closedList, startState, destinationState);
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

    public int calcuteHeuristic(MazeState state){
        int xDistance = Math.abs(state.getXPosition()- destinationState.getXPosition());
        int yDistance = Math.abs(state.getYPosition() - destinationState.getYPosition());
        return (xDistance + yDistance) * 5;
    }

    public int calculateEstimatedTotalCost(MazeState state){
        return state.getTotalCost() + calcuteHeuristic(state);
    }

}
