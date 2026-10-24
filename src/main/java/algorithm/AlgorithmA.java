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
import java.util.concurrent.atomic.AtomicBoolean;

public class AlgorithmA {
    public interface SearchListener {
        void onExpanded(MazeState state);
        void onCompleted(List<MazeState> path, boolean found, int expandedStates);
        default void onCompleted(List<MazeState> path, boolean found, int expandedStates, int generatedStates) {
            onCompleted(path, found, expandedStates);
        }
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
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private boolean hasStarted;

    PriorityQueue<MazeState> openList = new PriorityQueue<>(this::compareStates);
    Set<MazeState> closedList = new HashSet<>();
    Map<MazeState, Integer> bestKnownCost = new HashMap<>();
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
        if (hasStarted) cancelled.set(false);
        hasStarted = true;
        openList.clear();
        closedList.clear();
        bestKnownCost.clear();
        bestWay.clear();
        foundDestination = false;
        prepareEnvironment();
        MazeState[] userSelectedStates = GiveStartAndDestinationPosition(startX,startY,destinatioX,destinationY);
        startState = userSelectedStates[0];
        destinationState = userSelectedStates[1];
        actualState = startState;


        openList.add(actualState);
        bestKnownCost.put(actualState, actualState.getTotalCost());
        int expandedStates = 0;

        while(!openList.isEmpty() && !cancelled.get()){
            actualState = openList.poll();
            if (actualState.getTotalCost() != bestKnownCost.getOrDefault(actualState, Integer.MAX_VALUE)) continue;
            if (closedList.contains(actualState)) continue;
            if(isDestinationReached()){
                System.out.println("Final destination state reached " + actualState.getTotalCost());
                foundDestination = true;
                break;
            }
            closedList.add(actualState);
            expandedStates++;
            if (searchListener != null) searchListener.onExpanded(actualState);

            relax(actualState.turnLeft90Degrees());
            relax(actualState.turnRight90Degrees());
            relax(actualState.turnAround180Degrees());

            if (isForwardMoveWithinBounds(actualState)
                    && maze.isTransitionOpen(actualState.getXPosition(), actualState.getYPosition(), actualState.getOrientation())) {
                MazeState forwardState = actualState.moveForward(maze);
                if (maze.areValidEnterStates(forwardState.getXPosition(), forwardState.getYPosition())) relax(forwardState);
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
            searchListener.onCompleted(List.copyOf(bestWay), foundDestination, expandedStates, bestKnownCost.size());
        }
    }

    /** Requests a cooperative stop; the search finishes after its current expansion callback. */
    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    private void relax(MazeState candidate) {
        int known = bestKnownCost.getOrDefault(candidate, Integer.MAX_VALUE);
        if (candidate.getTotalCost() >= known) return;
        bestKnownCost.put(candidate, candidate.getTotalCost());
        // Reopening is safe for both A* and greedy mode, and handles future
        // changes to the heuristic without relying on PriorityQueue decrease-key.
        closedList.remove(candidate);
        openList.add(candidate); // old entries become stale and are skipped when polled
    }

    private int compareStates(MazeState left, MazeState right) {
        int comparison = Integer.compare(calculateEstimatedTotalCost(left), calculateEstimatedTotalCost(right));
        if (comparison == 0) comparison = Integer.compare(calcuteHeuristic(left), calcuteHeuristic(right));
        if (comparison == 0) comparison = Integer.compare(left.getYPosition(), right.getYPosition());
        if (comparison == 0) comparison = Integer.compare(left.getXPosition(), right.getXPosition());
        if (comparison == 0) comparison = Integer.compare(left.getOrientation().ordinal(), right.getOrientation().ordinal());
        return comparison;
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
