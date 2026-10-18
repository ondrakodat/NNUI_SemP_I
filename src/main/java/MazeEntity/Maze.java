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
    private int cellSize;
    private int leftBorder;
    private int rightBorder;
    private int topBorder;
    private int bottomBorder;
    private String resourcePath = "/Bludiste.bmp";

    // Zachová kompatibilitu s AlgorithmA, které načítá aktuální bludiště bez argumentu.
    public void GetMazeFromFile() {
        GetMazeFromFile(resourcePath);
    }

    public void GetMazeFromFile(String path) {
        InputStream input = getClass().getResourceAsStream(path);
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
        resourcePath = path;
        findBordersOfMaze();
        setupLogicalMaze();
    }

//    public void setupLogicalMaze() {
//        /*
//        Added small deviation for white color so not all color != -1 are solid blocks
//        color is 4 bajts
//        1st - transparency
//        2nd - red
//        3rd - green
//        4th - blue
//         */
//        logicalMaze = new MazeTitle[bitmap.getWidth()][bitmap.getHeight()];
//        for (int x = 0; x < bitmap.getWidth(); x++) {
//            for (int y = 0; y < bitmap.getHeight(); y++) {
//                int colorOfPixel = bitmap.getRGB(x, y);
//                int red = (colorOfPixel >> 16) & 255;
//                int green = (colorOfPixel >> 8) & 255;
//                int blue = colorOfPixel & 255;
//
//                MazeTitle title = new MazeTitle();
//                title.setUpMazeTitle(x, y);
//
//                if (red > 235 && blue > 235 && green > 235) {
//                    title.setUpMazeAsNotSolidBlock();
//                } else {
//                    title.setUpMazeAsSolidBlock();
//                }
//                logicalMaze[x][y] = title;
//            }
//        }
//    }
        public void setupLogicalMaze() {
            cellSize = testFewRows(new int[]{
                    bitmap.getHeight() / 2,
                    bitmap.getHeight() / 3,
                    bitmap.getHeight() / 4
            });

            if (cellSize <= 0) {
                throw new IllegalStateException(
                        "Nepodařilo se určit velikost buňky."
                );
            }

            int mazeWidth = rightBorder - leftBorder + 1;
            int mazeHeight = bottomBorder - topBorder + 1;

            int columns = mazeWidth / cellSize;
            int rows = mazeHeight / cellSize;

            logicalMaze = new MazeTitle[columns][rows];

            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {

                    int pixelX = leftBorder + x * cellSize + cellSize / 2;
                    int pixelY = topBorder + y * cellSize + cellSize / 2;

                    int color = bitmap.getRGB(pixelX, pixelY);

                    MazeTitle title = new MazeTitle();
                    title.setUpMazeTitle(x, y);

                    if (isWhiteOrNot(color)) {
                        title.setUpMazeAsNotSolidBlock();
                    } else {
                        title.setUpMazeAsSolidBlock();
                    }

                    logicalMaze[x][y] = title;
                }
            }
            System.out.println("Rozměry obrázku: "
                    + bitmap.getWidth() + " × " + bitmap.getHeight());

            System.out.println("Hranice X: "
                    + leftBorder + " až " + rightBorder);

            System.out.println("Hranice Y: "
                    + topBorder + " až " + bottomBorder);

            System.out.println("Velikost buňky: " + cellSize);

            System.out.println("Logické bludiště: "
                    + columns + " × " + rows);

            System.out.println("Zbytek šířky: " + mazeWidth % cellSize);
            System.out.println("Zbytek výšky: " + mazeHeight % cellSize);
        }



//    private void printMazeSize() {
//    }


    public BufferedImage createPhysicalMazeFromLogicalState() {
        BufferedImage result = new BufferedImage(
                bitmap.getWidth(),
                bitmap.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // Zachováme původní obrázek včetně bílého okolí.
        Graphics2D g = result.createGraphics();
        g.drawImage(bitmap, 0, 0, null);

        // Každou logickou buňku vykreslíme přes původní buňku.
        for (int x = 0; x < logicalMaze.length; x++) {
            for (int y = 0; y < logicalMaze[x].length; y++) {
                MazeTitle title = logicalMaze[x][y];

                int pixelX = leftBorder + x * cellSize;
                int pixelY = topBorder + y * cellSize;

                if (title.isSolidBlock()) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }

                g.fillRect(pixelX, pixelY, cellSize, cellSize);
            }
        }

        g.dispose();
        return result;
    }


    private void drawCell(BufferedImage image, int x, int y, Color color) {
        int startX = leftBorder + x * cellSize;
        int startY = topBorder + y * cellSize;

        for (int px = startX; px < startX + cellSize; px++) {
            for (int py = startY; py < startY + cellSize; py++) {
                if (px < image.getWidth() && py < image.getHeight()) {
                    image.setRGB(px, py, color.getRGB());
                }
            }
        }
    }




    public boolean areValidEnterStates(int x, int y) {
        if (x < 0 || x >= logicalMaze.length
                || y < 0 || y >= logicalMaze[x].length) {
            System.out.println("Zadaná pozice je mimo bludiště.");
            return false;
        }

        MazeTitle titleForCheck = logicalMaze[x][y];

        if (titleForCheck.isSolidBlock()) {
            System.out.println(
                    "Zadaná pozice je zeď. X: " + x + ", Y: " + y
            );
            return false;
        }

        return true;
    }


//    public void cropMaze() {
//        if (isCropped) {
//            return;
//        }
//
//        bitmap = findBordersOfMaze();
//        setupLogicalMaze();
//        isCropped = true;
//    }

    public BufferedImage drawExploredArea(List<MazeState> bestWay, Set<MazeState> closedSet, MazeState startState, MazeState destinationState) {
        BufferedImage result = createPhysicalMazeFromLogicalState();
        for (MazeState state : closedSet) {
            drawCell(result, state.getXPosition(),
                    state.getYPosition(), Color.BLUE);
        }

        for (MazeState state : bestWay) {
            drawCell(result, state.getXPosition(),
                    state.getYPosition(), Color.RED);
        }

        drawCell(result, startState.getXPosition(),
                startState.getYPosition(), Color.GREEN);

        drawCell(result, destinationState.getXPosition(),
                destinationState.getYPosition(), Color.YELLOW);
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

    public void findBordersOfMaze(){
        //TODO find line of top width and also bot width with left heigt line and right height line and make intersektor from it and be able to cut the image
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        topBorder = -1;
        bottomBorder = -1;
        leftBorder = -1;
        rightBorder = -1;

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

        System.out.println("Horní hranice: " + topBorder);
        System.out.println("Dolní hranice: " + bottomBorder);
        System.out.println("Levá hranice: " + leftBorder);
        System.out.println("Pravá hranice: " + rightBorder);



    }


    /*
    public void CreateReducedMaze() {

        cropMaze();

        int mazeHeight = bitmap.getHeight();
        int mazeWidth = bitmap.getWidth();

        int cellSize = testFewRows(new int[]{
                mazeHeight / 2,
                mazeHeight / 3,
                mazeHeight / 4
        });

        System.out.println("Rozměry oříznutého obrázku: "
                + mazeWidth + " × " + mazeHeight);

        System.out.println("Zbytek šířky po rozdělení: "
                + mazeWidth % cellSize);

        System.out.println("Zbytek výšky po rozdělení: "
                + mazeHeight % cellSize);

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
                        int totalPixels = cellSize * cellSize;

                        if (x == 10 && y == 10) {
                            System.out.println(
                                    "Buňka (" + x + ", " + y + ")"
                                            + ", začátek: (" + (x * cellSize)
                                            + ", " + (y * cellSize) + ")"
                                            + ", bílé pixely: " + whitePixels
                                            + "/" + totalPixels
                            );
                        }

                        if (isWhiteOrNot(bitmap.getRGB(px, py))) {
                            whitePixels++;
                        }
                    }
                }

                int totalPixels = cellSize * cellSize;

                if (whitePixels > 0 && whitePixels < totalPixels) {
                    System.out.println(
                            "Smíšená buňka x=" + x + ", y=" + y
                                    + ", bílé pixely=" + whitePixels
                                    + "/" + totalPixels
                    );
                }

                if (whitePixels >= totalPixels / 2) {
                    reducedMaze.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    reducedMaze.setRGB(x, y, Color.BLACK.getRGB());
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
    */

}


