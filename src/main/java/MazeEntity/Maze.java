package MazeEntity;

import algorithm.Sort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.List;

/*
* This class represents our Maze which we load from directory.
* Then we make logical version of our 'physical' (image) version.
* We make two dimension array of MazeTitle.
* And also we make copy of our original maze to ensure we got exactly the same one.
*/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Maze {
    private BufferedImage bitmap;
    private MazeTitle [][] logicalMaze;

    public void GetMazeFromFile(){
        InputStream input = getClass().getResourceAsStream("/Bludiste.bmp");
        if(input == null){
            throw new RuntimeException("File does not exist!");
        }

        try(input){
            bitmap = ImageIO.read(input);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        if(bitmap == null){
            throw new RuntimeException("File could not be read!");
        }
        printMazeSize();
        setupLogicalMaze();
    }

    public void setupLogicalMaze(){
        /*
        Added small deviation for white color so not all color != -1 are solid blocks
        color is 4 bajts
        1st - transparency
        2nd - red
        3rd - green
        4th - blue
         */
        logicalMaze = new MazeTitle[bitmap.getWidth()][bitmap.getHeight()];
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int colorOfPixel = bitmap.getRGB(x, y);
                int red = (colorOfPixel >> 16) & 255;
                int green = (colorOfPixel >> 8) & 255;
                int blue = colorOfPixel & 255;

                MazeTitle title = new MazeTitle();
                title.setUpMazeTitle(x, y);

                if (red > 235 && blue > 235 && green > 235) {
                    title.setUpMazeAsNotSolidBlock();
                } else {
                    title.setUpMazeAsSolidBlock();
                }
                logicalMaze[x][y] = title;
            }
        }
    }

    private void printMazeSize() {
    }

    public BufferedImage createPhysicalMazeFromLogicalState(){
        BufferedImage bmtMaze = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                MazeTitle title = logicalMaze[x][y];
                if(title.isSolidBlock())
                    bmtMaze.setRGB(x,y, Color.BLACK.getRGB());
                else{
                    bmtMaze.setRGB(x,y,Color.WHITE.getRGB());
                }
            }
        }
        return bmtMaze;
    }

    public boolean areValidEnterStates(int x, int y) throws ArrayIndexOutOfBoundsException{
        MazeTitle titleForCheck = logicalMaze[x][y];
        int xCoordinates = titleForCheck.getXCoordinates();
        int yCoordinates = titleForCheck.getYCoordinates();
        int bordersOfX = logicalMaze.length;
        int bordersOfY = logicalMaze[x].length;
        if(xCoordinates >= bordersOfX || xCoordinates < 0) {
            System.out.println("Chosen position is not inside of maze with X position");
            return false;
        }
        else if(yCoordinates >= bordersOfY || yCoordinates < 0){
            System.out.println("Chosen position is not inside of maze with Y position");
            return false;
        }
        else if(titleForCheck.isSolidBlock()){
            System.out.println("Chosen position is solid block. Position x : " + x + ", position y : " + y);
            return false;
        }else
            return true;
    }

    public BufferedImage drawExploredArea(List<MazeState> bestWay, Set<MazeState> closedSet, MazeState startState, MazeState destinationState){
        BufferedImage result = createPhysicalMazeFromLogicalState();
        for (MazeState state : closedSet){
            int x = state.getXPosition();
            int y = state.getYPosition();
            result.setRGB(x, y, Color.BLUE.getRGB());
        }
        for (MazeState state : bestWay){
            int x = state.getXPosition();
            int y = state.getYPosition();

            result.setRGB(x, y, Color.RED.getRGB());
        }
        result.setRGB(startState.getXPosition(), startState.getYPosition(), Color.GREEN.getRGB());
        result.setRGB(destinationState.getXPosition(), destinationState.getYPosition(), Color.YELLOW.getRGB());
        try {
            ImageIO.write(result, "bmp", new File("Bludiste-prosle.bmp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public int findCellSize(){
        int widthOfMaze = bitmap.getWidth();
        //int heightOfMaze = bitmap.getHeight();

        int currentLenght = 1;
        int previouisColor = 0;

        int randomRowOfMaze = 2;

        List<Integer> lenghtOFDifferentColor = new ArrayList<>();
        int swappedColors = 0;

        for (int i = 1; i < widthOfMaze; i++) {
            previouisColor = bitmap.getRGB(i - 1, randomRowOfMaze);
            if(isSameColor(bitmap.getRGB(i, randomRowOfMaze), previouisColor)){
                currentLenght++;
            }else{
                lenghtOFDifferentColor.add(currentLenght);
                System.out.println("Actual lenght of color is : " + currentLenght);
                swappedColors++;
                currentLenght = 1;
            }
        }
        lenghtOFDifferentColor.add(currentLenght);
        swappedColors++;

        lenghtOFDifferentColor.removeFirst();
        lenghtOFDifferentColor.removeLast();

        int [] lenghtsOfDiffColors = lenghtOFDifferentColor.stream().mapToInt(Integer::intValue).toArray();

        swappedColors = swappedColors - 2;

        Sort.sortFromLowest(lenghtsOfDiffColors);

        System.out.println(swappedColors + "S timto se bude pocitat");

        Arrays.stream(lenghtsOfDiffColors).forEach(System.out::println);

        return swappedColors;
    }

    private boolean isSameColor(int firstColor, int secondColor) {
        int red1 = (firstColor >> 16) & 255;
        int green1 = (firstColor >> 8) & 255;
        int blue1 = firstColor & 255;

        int red2 = (secondColor >> 16) & 255;
        int green2 = (secondColor >> 8) & 255;
        int blue2 = secondColor & 255;

        boolean firstIsWhite = red1 > 235 && green1 > 235 && blue1 > 235;
        boolean secondIsWhite = red2 > 235 && green2 > 235 && blue2 > 235;

        return firstIsWhite == secondIsWhite;
    }






}


