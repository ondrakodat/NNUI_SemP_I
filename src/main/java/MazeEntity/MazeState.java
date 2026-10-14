package MazeEntity;

import enums.Orientation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/*
* This class represents each state during discovering the best way to find our destination
* It includes moving forward, turning left or right or turning 180 degrees
* Move forward method is split into two separated smaller ones each for one axe of the 2-dimensional array
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MazeState {
    static final int MOVE_FORWARD_ONE_FIELD_COST = 5;
    static final int TURN_LEFT_90_DEGREES_COST = 2;
    static final int TURN_RIGHT_90_DEGREES_COST = 2;
    static final int TURN_180_DEGREES_COST = 3;

    private int totalCost;
    private int xPosition;
    private int yPosition;
    private MazeState previousState;
    private Orientation orientation = Orientation.SOUTH;


    public MazeState(int yPosition,int xPosition, Orientation orientation, MazeState previousState) {
        this.yPosition = yPosition;
        this.orientation = orientation;
        this.xPosition = xPosition;
        this.previousState = previousState;
    }



    public MazeState turnLeft90Degrees(){
        MazeState newState = new MazeState();
        int nextValue = orientation.ordinal() - 1;
        if(nextValue < 0)
            nextValue = 3;
        newState.setYPosition(yPosition);
        newState.setXPosition(xPosition);
        newState.setOrientation(Orientation.values()[nextValue]);
        newState.setTotalCost(totalCost + TURN_LEFT_90_DEGREES_COST);
        newState.setPreviousState(this);
        return newState;
    }

    public MazeState turnRight90Degrees(){
        MazeState newState = new MazeState();
        int nextValue = orientation.ordinal() + 1;
        if(nextValue >3)
            nextValue = 0;
        newState.setOrientation(Orientation.values()[nextValue]);
        newState.setTotalCost(totalCost + TURN_RIGHT_90_DEGREES_COST);
        newState.setXPosition(xPosition);
        newState.setYPosition(yPosition);
        newState.setPreviousState(this);
        return newState;
    }


    public MazeState turnAround180Degrees() {
        int nextValue = (orientation.ordinal() + 2) % 4;

        MazeState newState = new MazeState(
                totalCost + TURN_180_DEGREES_COST,
                xPosition,
                yPosition,
                this,
                Orientation.values()[nextValue]
        );


        return newState;
    }

    public MazeState moveForward(Maze maze){
        int x = xPosition;
        int y = yPosition;
        switch (orientation){
            case Orientation.EAST :
                    x = moveOnXAxiom(maze);
                    break;
            case Orientation.WEST :
                    x = moveOnXAxiom(maze);
                    break;
            case Orientation.NORTH :
                y =  moveOnYAxiom(maze);
                break;
            case Orientation.SOUTH :
                  y = moveOnYAxiom(maze);
                  break;
        }

        return new MazeState(
                totalCost + MOVE_FORWARD_ONE_FIELD_COST,
                x,
                y,
                this,
                orientation
        );
    }

    public int moveOnYAxiom(Maze maze){
        if(orientation == Orientation.NORTH){
            if(yPosition != 0){
                return yPosition - 1;
            }
            else{
                throw new RuntimeException("Cant move outside of the maze!");
            }
        }else{
            if(yPosition != maze.getLogicalMaze().length){
                return yPosition + 1;
            }else{
                throw new RuntimeException("Cant move outside of the maze!");
            }
        }
    }
    public int moveOnXAxiom(Maze maze){
        if(orientation == Orientation.WEST){
            if(xPosition != 0){
                return xPosition - 1;
            }
            else{
                throw new RuntimeException("Cant move outside of the maze!");
            }
        }else {
            if(xPosition != maze.getLogicalMaze().length){
                return xPosition + 1;
            }else{
                throw new RuntimeException("Cant move outside of the maze!");
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MazeState mazeState = (MazeState) o;
        return xPosition == mazeState.xPosition && yPosition == mazeState.yPosition && orientation == mazeState.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xPosition, yPosition, orientation);
    }
}
