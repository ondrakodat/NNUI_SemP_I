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
    private boolean isCropped = false;

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

    public void cropMaze() {
        if (isCropped) {
            return;
        }

        bitmap = findBordersOfMaze();
        setupLogicalMaze();
        isCropped = true;
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

        List<Integer> whiteColorLenghts = new ArrayList<>();
        List<Integer> nonWhiteColorLenghts = new ArrayList<>();

        boolean isCurrentlyWhite = isWhiteOrNot(bitmap.getRGB(0, rowToTest));
        boolean isFirstPixelWhite = isWhiteOrNot(bitmap.getRGB(0, rowToTest));
        boolean isLastPixelWhite = isWhiteOrNot(bitmap.getRGB(widthOfMaze - 1, rowToTest));

        for (int i = 1; i < widthOfMaze; i++) {
            boolean pixelIsWhite = isWhiteOrNot(bitmap.getRGB(i, rowToTest));

            if (pixelIsWhite == isCurrentlyWhite) {
                currentLenght++;
            } else {
                if(isCurrentlyWhite){
                    whiteColorLenghts.add(currentLenght);
                }else{
                    nonWhiteColorLenghts.add(currentLenght);
                }
                System.out.println("Actual lenght of color is : " + currentLenght);
                currentLenght = 1;
                isCurrentlyWhite = pixelIsWhite;
            }
        }
        if(isCurrentlyWhite)
            whiteColorLenghts.add(currentLenght);
        else
            nonWhiteColorLenghts.add(currentLenght);

        if (isFirstPixelWhite && isLastPixelWhite) {
            if(whiteColorLenghts.size() >= 2){
                whiteColorLenghts.removeFirst();
                whiteColorLenghts.removeLast();
            }else
                whiteColorLenghts.clear();;

        }


        List<Integer> allLengths = new ArrayList<>();
        allLengths.addAll(whiteColorLenghts);
        allLengths.addAll(nonWhiteColorLenghts);

        int[] lenghtsOfDiffColors = allLengths.stream()
                .mapToInt(Integer::intValue)
                .toArray();

        Sort.sortFromLowest(lenghtsOfDiffColors);

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

    private boolean isWhiteOrNot(int color) {
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;

        return red > 235 && green > 235 && blue > 235;
    }

    public void CreateNewLogicalMazeAfterTestingRowsLenght(){
        int mazeHeight = bitmap.getHeight();
        int mazeWidth = bitmap.getWidth();

        int expectedWidthOfOneCoridor = testFewRows(new int[]{mazeHeight/2, mazeHeight/3, mazeHeight/4, (int)(mazeHeight/4.5), (int)(mazeHeight/1.5), (int)(mazeHeight/1.6)});

        System.out.println("Original maze height : " + mazeHeight);
        System.out.println("Original maze width : " + mazeWidth);

        int newHeight = mazeHeight / expectedWidthOfOneCoridor;
        int newWidth = mazeWidth / expectedWidthOfOneCoridor;

        System.out.printf("New maze height : " + newHeight);
        System.out.printf("New maze widht : " + newWidth);
    }

    public BufferedImage findBordersOfMaze(){
        //TODO find line of top width and also bot width with left heigt line and right height line and make intersektor from it and be able to cut the image
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int topBorder = -1;
        int bottomBorder = -1;
        int leftBorder = -1;
        int rightBorder = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isWhiteOrNot(bitmap.getRGB(x, y))) {
                    topBorder = y;
                    break;
                }
            }

            if (topBorder != -1) {
                break;
            }
        }

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                if (!isWhiteOrNot(bitmap.getRGB(x, y))) {
                    bottomBorder = y;
                    break;
                }
            }

            if (bottomBorder != -1) {
                break;
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!isWhiteOrNot(bitmap.getRGB(x, y))) {
                    leftBorder = x;
                    break;
                }
            }

            if (leftBorder != -1) {
                break;
            }
        }

        for (int x = width - 1; x >= 0; x--) {
            for (int y = 0; y < height; y++) {
                if (!isWhiteOrNot(bitmap.getRGB(x, y))) {
                    rightBorder = x;
                    break;
                }
            }

            if (rightBorder != -1) {
                break;
            }
        }
        topBorder++;
        bottomBorder--;

        System.out.println("Horní hranice: " + topBorder);
        System.out.println("Dolní hranice: " + bottomBorder);
        System.out.println("Levá hranice: " + leftBorder);
        System.out.println("Pravá hranice: " + rightBorder);

        BufferedImage mazeWithDrawedBorders = bitmap.getSubimage(
                leftBorder,
                topBorder,
                rightBorder - leftBorder + 1,
                bottomBorder - topBorder + 1
        );

        return mazeWithDrawedBorders;
    }



    public void CreateReducedMaze() {

        cropMaze();

        int mazeHeight = bitmap.getHeight();
        int mazeWidth = bitmap.getWidth();

        int cellSize = testFewRows(new int[]{
                mazeHeight / 2,
                mazeHeight / 3,
                mazeHeight / 4
        });

        if (cellSize <= 0) {
            throw new IllegalStateException(
                    "Nepodařilo se zjistit velikost buňky."
            );
        }

        int newWidth = mazeWidth / cellSize;
        int newHeight = mazeHeight / cellSize;

        BufferedImage reducedMaze = new BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB
        );

        for (int x = 0; x < newWidth; x++) {
            for (int y = 0; y < newHeight; y++) {

                int whitePixels = 0;

                for (int px = x * cellSize;
                     px < (x + 1) * cellSize; px++) {

                    for (int py = y * cellSize;
                         py < (y + 1) * cellSize; py++) {

                        if (isWhiteOrNot(bitmap.getRGB(px, py))) {
                            whitePixels++;
                        }
                    }
                }

                int totalPixels = cellSize * cellSize;

                if (whitePixels > totalPixels / 2) {
                    reducedMaze.setRGB(
                            x, y, Color.WHITE.getRGB()
                    );
                } else {
                    reducedMaze.setRGB(
                            x, y, Color.BLACK.getRGB()
                    );
                }
            }
        }

        try {
            ImageIO.write(
                    reducedMaze,
                    "bmp",
                    new File("Bludiste-zmensene.bmp")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Velikost buňky: " + cellSize);
        System.out.println(
                "Rozměry zmenšeného bludiště: "
                        + newWidth + " × " + newHeight
        );
    }


}


