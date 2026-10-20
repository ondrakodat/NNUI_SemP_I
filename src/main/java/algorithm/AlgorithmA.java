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
    public interface SearchListener {
        void onExpanded(MazeState state);
        void onCompleted(List<MazeState> path, boolean found, int expandedStates);
    }

    private int startCostValue = 0;
    private int totalValue;
    private Maze maze;
    private MazeState startState;
    private MazeState actualState;
    private MazeState destinationState;
    private boolean foundDestination = false;
    private final boolean greedySearch;
    private final SearchListener searchListener;

    PriorityQueue<MazeState> openList = new PriorityQueue<>((MazeState c1, MazeState c2) -> Integer.compare(calculateEstimatedTotalCost(c1), calculateEstimatedTotalCost(c2)));
    Set<MazeState> closedList = new HashSet<>();
    List<MazeState> bestWay = new ArrayList<>();

    public AlgorithmA(Maze maze) {
        this(maze, false, null);
    }

    public AlgorithmA(Maze maze, boolean greedySearch, SearchListener searchListener) {
        this.maze = maze;
        this.greedySearch = greedySearch;
        this.searchListener = searchListener;
    }

    public void Start(int startX, int startY, int destinatioX, int destinationY){
        prepareEnvironment();
        MazeState[] userSelectedStates = GiveStartAndDestinationPosition(startX,startY,destinatioX,destinationY);
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
            if (searchListener != null) searchListener.onExpanded(actualState);

            MazeState leftState = actualState.turnLeft90Degrees();
            if(!openList.contains(leftState) && !closedList.contains(leftState))
                openList.add(leftState);

            MazeState rightState = actualState.turnRight90Degrees();
            if(!openList.contains(rightState) && !closedList.contains(rightState))
                openList.add(rightState);

            MazeState aroundState = actualState.turnAround180Degrees();
            if(!openList.contains(aroundState) && !closedList.contains(aroundState) )
                openList.add(aroundState);

            if (isForwardMoveWithinBounds(actualState)
                    && maze.isTransitionOpen(actualState.getXPosition(), actualState.getYPosition(), actualState.getOrientation())) {
                MazeState forwardState = actualState.moveForward(maze);
                if (maze.areValidEnterStates(forwardState.getXPosition(), forwardState.getYPosition())
                        && !openList.contains(forwardState)
                        && !closedList.contains(forwardState)) {
                    openList.add(forwardState);
                }
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
        maze.CreateNewLogicalMazeAfterTestingRowsLenght();
        maze.drawExploredArea(bestWay, closedList, startState, destinationState);
        if (searchListener != null) {
            searchListener.onCompleted(List.copyOf(bestWay), foundDestination, closedList.size());
        }
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
        System.out.printf("[PREPARE-BEFORE] maze@%08x logicalMaze@%08x, resource=%s, grid=%dx%d%n",
                System.identityHashCode(maze),
                System.identityHashCode(maze.getLogicalMaze()),
                maze.getResourcePath(), maze.getLogicalMaze().length,
                maze.getLogicalMaze()[0].length);
        maze.GetMazeFromFile();
        System.out.printf("[PREPARE-AFTER] maze@%08x logicalMaze@%08x, resource=%s, grid=%dx%d%n",
                System.identityHashCode(maze),
                System.identityHashCode(maze.getLogicalMaze()),
                maze.getResourcePath(), maze.getLogicalMaze().length,
                maze.getLogicalMaze()[0].length);
    }

    private boolean isForwardMoveWithinBounds(MazeState state) {
        int columns = maze.getLogicalMaze().length;
        int rows = maze.getLogicalMaze()[0].length;
        return switch (state.getOrientation()) {
            case NORTH -> state.getYPosition() > 0;
            case SOUTH -> state.getYPosition() < rows - 1;
            case WEST -> state.getXPosition() > 0;
            case EAST -> state.getXPosition() < columns - 1;
        };
    }

    public int calcuteHeuristic(MazeState state){
        int xDistance = Math.abs(state.getXPosition()- destinationState.getXPosition());
        int yDistance = Math.abs(state.getYPosition() - destinationState.getYPosition());
        return (xDistance + yDistance) * 5;
    }

    public int calculateEstimatedTotalCost(MazeState state){
        int heuristic = calcuteHeuristic(state);
        return greedySearch ? heuristic : state.getTotalCost() + heuristic;
    }

}
