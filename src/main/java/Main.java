import MazeEntity.Maze;
import MazeEntity.MazeState;
import algorithm.AlgorithmA;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Maze maze = new Maze();
        maze.GetMazeFromFile();
        maze.setupLogicalMaze();
//        BufferedImage imageOfMaze = maze.createPhysicalMazeFromLogicalState();
//
//        try {
//            ImageIO.write(imageOfMaze, "bmp", new File("Bludiste-output.bmp"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println("Cesta ulozeneho bludiste : " + new File("Bludiste-output.bmp").getAbsolutePath());

        AlgorithmA alg = new AlgorithmA(maze);
        alg.vypis();
//        MazeState state = new MazeState();
//        System.out.println(state.getOrientation().toString());
//        MazeState s = state.turnAround180Degrees();
//        System.out.println(s.getOrientation());
//        MazeState w = s.turnLeft90Degrees();
//        System.out.println(w.getOrientation());
//        MazeState around = w.turnAround180Degrees();
//        System.out.println(around.getOrientation());
//        MazeState last = around.turnRight90Degrees();
//        System.out.println(last.getOrientation());
        alg.Start();
    }
}
