package MazeEntity;

import enums.Orientation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MazeState {
    private int totalCost;
    private int xPosition;
    private int yPosition;
    private Orientation orientation;
}
