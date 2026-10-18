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
    private MazeTitle[][] logicalMaze;

    public void GetMazeFromFile() {
        InputStream input = getClass().getResourceAsStream("/Bludiste_2.bmp");
        if (input == null) {
            throw new RuntimeException("File does not exist!");
        }

        try (input) {
            bitmap = ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (bitmap == null) {
            throw new RuntimeException("File could not be read!");
        }
        printMazeSize();
        setupLogicalMaze();
    }

    public void setupLogicalMaze() {
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

    public BufferedImage createPhysicalMazeFromLogicalState() {
        BufferedImage bmtMaze = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                MazeTitle title = logicalMaze[x][y];
                if (title.isSolidBlock())
                    bmtMaze.setRGB(x, y, Color.BLACK.getRGB());
                else {
                    bmtMaze.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return bmtMaze;
    }

    public boolean areValidEnterStates(int x, int y) throws ArrayIndexOutOfBoundsException {
        MazeTitle titleForCheck = logicalMaze[x][y];
        int xCoordinates = titleForCheck.getXCoordinates();
        int yCoordinates = titleForCheck.getYCoordinates();
        int bordersOfX = logicalMaze.length;
        int bordersOfY = logicalMaze[x].length;
        if (xCoordinates >= bordersOfX || xCoordinates < 0) {
            System.out.println("Chosen position is not inside of maze with X position");
            return false;
        } else if (yCoordinates >= bordersOfY || yCoordinates < 0) {
            System.out.println("Chosen position is not inside of maze with Y position");
            return false;
        } else if (titleForCheck.isSolidBlock()) {
            System.out.println("Chosen position is solid block. Position x : " + x + ", position y : " + y);
            return false;
        } else
            return true;
    }

    public BufferedImage drawExploredArea(List<MazeState> bestWay, Set<MazeState> closedSet, MazeState startState, MazeState destinationState) {
        BufferedImage result = createPhysicalMazeFromLogicalState();
        for (MazeState state : closedSet) {
            int x = state.getXPosition();
            int y = state.getYPosition();
            result.setRGB(x, y, Color.BLUE.getRGB());
        }
        for (MazeState state : bestWay) {
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

    public int findCellSize(int rowToTest) {
        if (rowToTest < 0 || rowToTest >= bitmap.getHeight()) {
            System.out.println("Neplatný řádek: " + rowToTest);
            return 0;
        }

        int widthOfMaze = bitmap.getWidth();
        //int heightOfMaze = bitmap.getHeight();

        int currentLenght = 1;
        int previouisColor = 0;

        List<Integer> lenghtOFDifferentColor = new ArrayList<>();
        int swappedColors = 0;

        for (int i = 1; i < widthOfMaze; i++) {
            previouisColor = bitmap.getRGB(i - 1, rowToTest);
            if (isSameColor(bitmap.getRGB(i, rowToTest), previouisColor)) {
                currentLenght++;
            } else {
                lenghtOFDifferentColor.add(currentLenght);
                System.out.println("Actual lenght of color is : " + currentLenght);
                swappedColors++;
                currentLenght = 1;
            }
        }
        lenghtOFDifferentColor.add(currentLenght);
        swappedColors++;

        if (lenghtOFDifferentColor.size() < 3) {
            return 0;
        }

        lenghtOFDifferentColor.removeFirst();
        lenghtOFDifferentColor.removeLast();

        int[] lenghtsOfDiffColors = lenghtOFDifferentColor.stream().mapToInt(Integer::intValue).toArray();

        swappedColors = swappedColors - 2;

        Sort.sortFromLowest(lenghtsOfDiffColors);

        System.out.println(swappedColors + "S timto se bude pocitat");

        Arrays.stream(lenghtsOfDiffColors).forEach(System.out::println);

        int lenghtOfFoundColorLines = lenghtsOfDiffColors.length;

        int bestLength = 0;
        int bestCount = 0;

        for (int i = 0; i < lenghtsOfDiffColors.length; i++) {
            int currentCount = 0;

            for (int j = 0; j < lenghtsOfDiffColors.length; j++) {
                if (Math.abs(lenghtsOfDiffColors[i] - lenghtsOfDiffColors[j]) <= 1) {
                    currentCount++;
                }
            }

            if (currentCount > bestCount) {
                bestCount = currentCount;
                bestLength = lenghtsOfDiffColors[i];
            }
        }
        System.out.println("Odhad šířky: " + bestLength);
        System.out.println("Počet podobných délek: " + bestCount);
        return bestLength;

    }

    public int testFewRows(int[] arrayToTest) {
        int expectedVal = 0;
        int validrows = 0;

        for (int i = 0; i < arrayToTest.length; i++) {
            int cellSize = findCellSize(arrayToTest[i]);
            System.out.println("Řádek " + arrayToTest[i]
                    + " - odhad: " + cellSize);

            if(cellSize>0){
                expectedVal += cellSize;
                validrows++;
            }
        }
        if(validrows == 0){
            System.out.println("0 platných řádků k výpočtu");
            return 0;
        }

        int midlleValue = expectedVal / validrows;

        System.out.println("Expected Value of row coridor width is : " + midlleValue);

        return midlleValue;
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

    public void CreateNewLogicalMazeAfterTestingRowsLenght(){
        int mazeHeight = bitmap.getHeight();
        int mazeWidth = bitmap.getWidth();

        int expectedWidthOfOneCoridor = testFewRows(new int[]{2, 5, 6, 10, 15, 20, 100, 200, 250, 230, 240, 220, 150, 130});

        System.out.println("Original maze height : " + mazeHeight);
        System.out.println("Original maze width : " + mazeWidth);

        int newHeight = mazeHeight / expectedWidthOfOneCoridor;
        int newWidth = mazeWidth / expectedWidthOfOneCoridor;

        System.out.printf("New maze height : " + newHeight);
        System.out.printf("New maze widht : " + newWidth);
    }

    public void findBordersOfMaze(){
        //TODO find line of top width and also bot width with left heigt line and right height line and make intersektor from it and be able to cut the image

    }


}


