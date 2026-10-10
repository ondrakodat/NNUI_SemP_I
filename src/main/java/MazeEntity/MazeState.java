    package MazeEntity;

    import enums.Orientation;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

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
        private Orientation orientation = Orientation.SOUTH;


        public MazeState(int yPosition, Orientation orientation, int xPosition) {
            this.yPosition = yPosition;
            this.orientation = orientation;
            this.xPosition = xPosition;
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

            return newState;
        }


        public MazeState turnAround180Degrees() {
            int nextValue = (orientation.ordinal() + 2) % 4;

            MazeState newState = new MazeState(
                    totalCost + TURN_180_DEGREES_COST,
                    xPosition,
                    yPosition,
                    Orientation.values()[nextValue]
            );

            return newState;
        }

        public MazeState moveForward(){
            int x = xPosition;
            int y = yPosition;
            switch (orientation){
                case Orientation.EAST :
                        x = moveOnXAxiom();
                        break;
                case Orientation.WEST :
                        x = moveOnXAxiom();
                        break;
                case Orientation.NORTH :
                    y =  moveOnYAxiom();
                    break;
                case Orientation.SOUTH :
                      y = moveOnYAxiom();
                      break;
            }

            return new MazeState(
                    totalCost + MOVE_FORWARD_ONE_FIELD_COST,
                    x,
                    y,
                    orientation
            );
        }

        public int moveOnYAxiom(){
            if(orientation == Orientation.NORTH){
                if(yPosition != 0){
                    return yPosition - 1;
                }
                else{
                    throw new RuntimeException("Cant move outside of the maze!");
                }
            }else{
                if(yPosition != 33){
                    return yPosition + 1;
                }else{
                    throw new RuntimeException("Cant move outside of the maze!");
                }
            }
        }
        public int moveOnXAxiom(){
            if(orientation == Orientation.WEST){
                if(xPosition != 0){
                    return xPosition - 1;
                }
                else{
                    throw new RuntimeException("Cant move outside of the maze!");
                }
            }else {
                if(xPosition != 33){
                    return xPosition + 1;
                }else{
                    throw new RuntimeException("Cant move outside of the maze!");
                }
            }

        }

    }
