package MazeEntity;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class MazeTitle{
    private int xCoordinates;
    private int yCoordinates;
    private boolean isSolidBlock;
    private Color color;

    public void setUpMazeTitle(int x, int y){
        this.setXCoordinates(x);
        this.setYCoordinates(y);
    }
    public void setUpMazeAsSolidBlock(){
        this.setColor(Color.BLACK);
        this.setSolidBlock(true);
    }
    public void setUpMazeAsNotSolidBlock(){
        this.setColor(Color.WHITE);
        this.setSolidBlock(false);
    }
}


