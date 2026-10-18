package MazeEntity;

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
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import enums.Orientation;

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
    private int gridOriginX;
    private int gridOriginY;
    private boolean corridorCellGrid;
    private boolean[][][] openTransitions;

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
            int sampledRunSize = testFewRows(new int[]{
                    topBorder + (bottomBorder - topBorder) / 2,
                    topBorder + (bottomBorder - topBorder) / 3,
                    topBorder + (bottomBorder - topBorder) / 4
            });

            corridorCellGrid = resourcePath.endsWith("/bludiste_nosorozec.png");
            openTransitions = null;
            if (corridorCellGrid) {
                int[] geometry = detectCorridorGeometry();
                cellSize = geometry[0];
                int firstCenterX = leftBorder + geometry[1];
                int firstCenterY = topBorder + geometry[2];
                gridOriginX = firstCenterX - cellSize / 2;
                gridOriginY = firstCenterY - cellSize / 2;
            } else {
                cellSize = sampledRunSize;
                gridOriginX = leftBorder;
                gridOriginY = topBorder;
            }

            if (cellSize <= 0) {
                throw new IllegalStateException(
                        "Nepodařilo se určit velikost buňky."
                );
            }

            int columns;
            int rows;
            if (corridorCellGrid) {
                columns = (rightBorder - cellSize / 2 - (gridOriginX + cellSize / 2)) / cellSize + 1;
                rows = (bottomBorder - cellSize / 2 - (gridOriginY + cellSize / 2)) / cellSize + 1;
            } else {
                int mazeWidth = rightBorder - leftBorder + 1;
                int mazeHeight = bottomBorder - topBorder + 1;
                columns = mazeWidth / cellSize;
                rows = mazeHeight / cellSize;
            }
            if (columns <= 0 || rows <= 0) {
                throw new IllegalStateException("Odvozená logická mřížka nemá žádná pole.");
            }

            logicalMaze = new MazeTitle[columns][rows];

            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {

                    int pixelX = gridOriginX + x * cellSize + cellSize / 2;
                    int pixelY = gridOriginY + y * cellSize + cellSize / 2;

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

            System.out.println("Počátek logické mřížky: " + gridOriginX + ", " + gridOriginY);
            System.out.println("Zbytek šířky: " + (rightBorder - leftBorder + 1) % cellSize);
            System.out.println("Zbytek výšky: " + (bottomBorder - topBorder + 1) % cellSize);
            if (corridorCellGrid) buildOpenTransitions();
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

        // In the corridor-center representation the walls are transitions
        // between cells, so repainting each logical cell would erase them.
        if (corridorCellGrid) {
            g.dispose();
            return result;
        }

        // Rasterizace logických buněk nesmí přetéct do okolí bludiště,
        // pokud rozměry detekované oblasti nejsou násobkem cellSize.
        g.clipRect(leftBorder, topBorder,
                rightBorder - leftBorder + 1,
                bottomBorder - topBorder + 1);

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
        if (x < 0 || y < 0 || x >= logicalMaze.length
                || y >= logicalMaze[x].length) {
            return;
        }

        int startX = gridOriginX + x * cellSize;
        int startY = gridOriginY + y * cellSize;
        if (corridorCellGrid) {
            int centerX = startX + cellSize / 2;
            int centerY = startY + cellSize / 2;
            int diameter = Math.max(3, cellSize / 4);
            int markerX = centerX - diameter / 2;
            int markerY = centerY - diameter / 2;
            Graphics2D g = image.createGraphics();
            g.setColor(color);
            g.fillOval(markerX, markerY, diameter, diameter);
            g.dispose();
            return;
        }
        int endX = Math.min(Math.min(startX + cellSize, rightBorder + 1), image.getWidth());
        int endY = Math.min(Math.min(startY + cellSize, bottomBorder + 1), image.getHeight());
        int clippedStartX = Math.max(Math.max(startX, leftBorder), 0);
        int clippedStartY = Math.max(Math.max(startY, topBorder), 0);

        for (int px = clippedStartX; px < endX; px++) {
            for (int py = clippedStartY; py < endY; py++) {
                image.setRGB(px, py, color.getRGB());
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
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            String caller = trace.length > 2 ? trace[2].toString() : "neznámý";
            System.out.printf("[VALIDATE-WALL] maze@%08x logicalMaze@%08x cell@%08x, x=%d, y=%d, title=(%d,%d), solid=%s, resource=%s, caller=%s%n",
                    System.identityHashCode(this),
                    System.identityHashCode(logicalMaze),
                    System.identityHashCode(titleForCheck),
                    x, y, titleForCheck.getXCoordinates(),
                    titleForCheck.getYCoordinates(),
                    titleForCheck.isSolidBlock(), resourcePath, caller);
            System.out.println(
                    "Zadaná pozice je zeď. X: " + x + ", Y: " + y
            );
            return false;
        }

        return true;
    }

    public boolean isTransitionOpen(int x, int y, Orientation direction) {
        if (openTransitions == null) return true;
        if (x < 0 || y < 0 || x >= openTransitions.length || y >= openTransitions[x].length) {
            return false;
        }
        return openTransitions[x][y][direction.ordinal()];
    }

    public boolean isPassableImagePixel(int x, int y) {
        return x >= 0 && y >= 0 && x < bitmap.getWidth() && y < bitmap.getHeight()
                && isWhiteOrNot(bitmap.getRGB(x, y));
    }

    private int[] detectCorridorGeometry() {
        int wallX = dominantRunWidth(true, false);
        int passageX = dominantRunWidth(true, true);
        int wallY = dominantRunWidth(false, false);
        int passageY = dominantRunWidth(false, true);
        int spacingX = dominantWallSpacing(true, wallX, wallX + passageX);
        int spacingY = dominantWallSpacing(false, wallY, wallY + passageY);
        if (spacingX <= 0 || spacingY <= 0
                || Math.abs(spacingX - spacingY) > Math.max(2, Math.round(Math.min(spacingX, spacingY) * .03f))) {
            throw new IllegalStateException("Rozteč průchodů není v obou osách jednoznačně pravidelná.");
        }
        int spacing = Math.round((spacingX + spacingY) / 2.0f);
        int phaseX = findGridPhase(true, spacing);
        int phaseY = findGridPhase(false, spacing);
        System.out.printf("[CORRIDOR-GEOMETRY] wall=%d/%d px, passage=%d/%d px, pitch=%d/%d px, phase=%d/%d px%n",
                wallX, wallY, passageX, passageY, spacingX, spacingY, phaseX, phaseY);
        return new int[]{spacing, phaseX, phaseY};
    }

    private int dominantRunWidth(boolean alongX, boolean whiteRuns) {
        Map<Integer, Integer> widths = new HashMap<>();
        int fixedStart = alongX ? topBorder : leftBorder;
        int fixedEnd = alongX ? bottomBorder : rightBorder;
        int scanStart = alongX ? leftBorder : topBorder;
        int scanEnd = alongX ? rightBorder : bottomBorder;
        int scanStep = Math.max(1, (fixedEnd - fixedStart + 1) / 400);
        for (int fixed = fixedStart; fixed <= fixedEnd; fixed += scanStep) {
            int runStart = scanStart;
            boolean current = isWhiteOrNot(pixelAt(alongX, runStart, fixed));
            for (int position = scanStart + 1; position <= scanEnd + 1; position++) {
                boolean next = position <= scanEnd && isWhiteOrNot(pixelAt(alongX, position, fixed));
                if (position > scanEnd || next != current) {
                    if (current == whiteRuns && position - runStart >= 5) {
                        widths.merge(position - runStart, 1, Integer::sum);
                    }
                    runStart = position;
                    current = next;
                }
            }
        }
        return modalWidth(widths);
    }

    private int dominantWallSpacing(boolean alongX, int wallWidth, int expectedPitch) {
        if (wallWidth <= 0 || expectedPitch <= wallWidth) return 0;
        Map<Integer, Integer> gaps = new HashMap<>();
        int fixedStart = alongX ? topBorder : leftBorder;
        int fixedEnd = alongX ? bottomBorder : rightBorder;
        int scanStart = alongX ? leftBorder : topBorder;
        int scanEnd = alongX ? rightBorder : bottomBorder;
        int scanStep = Math.max(1, (fixedEnd - fixedStart + 1) / 400);
        int widthTolerance = Math.max(2, wallWidth / 10);
        for (int fixed = fixedStart; fixed <= fixedEnd; fixed += scanStep) {
            int runStart = scanStart;
            boolean current = isWhiteOrNot(pixelAt(alongX, runStart, fixed));
            int previousWallCenter = -1;
            for (int position = scanStart + 1; position <= scanEnd + 1; position++) {
                boolean next = position <= scanEnd && isWhiteOrNot(pixelAt(alongX, position, fixed));
                if (position > scanEnd || next != current) {
                    int runWidth = position - runStart;
                    if (!current && Math.abs(runWidth - wallWidth) <= widthTolerance) {
                        int center = runStart + (runWidth - 1) / 2;
                        if (previousWallCenter >= 0) {
                            int gap = center - previousWallCenter;
                            if (gap >= expectedPitch * .7 && gap <= expectedPitch * 1.3) {
                                gaps.merge(gap, 1, Integer::sum);
                            }
                        }
                        previousWallCenter = center;
                    }
                    runStart = position;
                    current = next;
                }
            }
        }
        return modalWidth(gaps);
    }

    private int modalWidth(Map<Integer, Integer> values) {
        int bestWidth = 0;
        int bestCount = 0;
        for (int candidate : values.keySet()) {
            int count = 0;
            for (int delta = -2; delta <= 2; delta++) count += values.getOrDefault(candidate + delta, 0);
            if (count > bestCount) {
                bestWidth = candidate;
                bestCount = count;
            }
        }
        return bestWidth;
    }

    private int findGridPhase(boolean alongX, int pitch) {
        int start = alongX ? leftBorder : topBorder;
        int end = alongX ? rightBorder : bottomBorder;
        int otherStart = alongX ? topBorder : leftBorder;
        int otherEnd = alongX ? bottomBorder : rightBorder;
        int otherStep = Math.max(1, pitch / 15);
        int bestPhase = -1;
        double bestScore = -1;
        for (int phase = pitch / 2; phase < pitch; phase++) {
            int firstCenter = start + phase;
            if (firstCenter - pitch / 2 < start || firstCenter + pitch / 2 > end) continue;
            long clearanceSum = 0;
            long samples = 0;
            for (int center = firstCenter; center + pitch / 2 <= end; center += pitch) {
                for (int other = otherStart; other <= otherEnd; other += otherStep) {
                    int x = alongX ? center : other;
                    int y = alongX ? other : center;
                    samples++;
                    if (!isWhiteOrNot(bitmap.getRGB(x, y))) continue;
                    for (int sign : new int[]{-1, 1}) {
                        for (int distance = 1; distance <= pitch / 2; distance++) {
                            int sampleX = alongX ? x + sign * distance : x;
                            int sampleY = alongX ? y : y + sign * distance;
                            if (!isWhiteOrNot(bitmap.getRGB(sampleX, sampleY))) break;
                            clearanceSum++;
                        }
                    }
                }
            }
            double score = samples == 0 ? 0 : (double) clearanceSum / samples;
            if (score > bestScore) {
                bestScore = score;
                bestPhase = phase;
            }
        }
        if (bestPhase < 0) throw new IllegalStateException("Nepodařilo se určit středy průchozích polí.");
        return bestPhase;
    }

    private void buildOpenTransitions() {
        int columns = logicalMaze.length;
        int rows = logicalMaze[0].length;
        openTransitions = new boolean[columns][rows][Orientation.values().length];
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                if (x + 1 < columns) {
                    setTransitionPair(x, y, Orientation.EAST, x + 1, y, Orientation.WEST);
                }
                if (y + 1 < rows) {
                    setTransitionPair(x, y, Orientation.SOUTH, x, y + 1, Orientation.NORTH);
                }
            }
        }
    }

    private void setTransitionPair(int x, int y, Orientation direction,
                                   int nextX, int nextY, Orientation opposite) {
        int startX = gridOriginX + x * cellSize + cellSize / 2;
        int startY = gridOriginY + y * cellSize + cellSize / 2;
        int endX = gridOriginX + nextX * cellSize + cellSize / 2;
        int endY = gridOriginY + nextY * cellSize + cellSize / 2;
        int white = 0;
        int total = 0;
        int length = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
        for (int step = 1; step < length; step++) {
            int px = startX + (endX - startX) * step / length;
            int py = startY + (endY - startY) * step / length;
            total++;
            if (isWhiteOrNot(bitmap.getRGB(px, py))) white++;
        }
        // Open passages have almost entirely white cross-sections; a wall occupies
        // roughly one third of a pitch, leaving a wide gap between both classes.
        boolean open = !logicalMaze[x][y].isSolidBlock()
                && !logicalMaze[nextX][nextY].isSolidBlock()
                && total > 0 && (double) white / total >= .9;
        openTransitions[x][y][direction.ordinal()] = open;
        openTransitions[nextX][nextY][opposite.ordinal()] = open;
    }

    private int pixelAt(boolean alongX, int position, int fixed) {
        return bitmap.getRGB(alongX ? position : fixed, alongX ? fixed : position);
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
        if (rowToTest < topBorder || rowToTest > bottomBorder
                || rowToTest >= bitmap.getHeight()) {
            System.out.println("Neplatný řádek: " + rowToTest);
            return 0;
        }

        Map<Integer, Integer> runCounts = new HashMap<>();
        int runStart = leftBorder;
        boolean current = isWhiteOrNot(bitmap.getRGB(runStart, rowToTest));
        for (int x = leftBorder + 1; x <= rightBorder + 1; x++) {
            boolean next = x <= rightBorder
                    && isWhiteOrNot(bitmap.getRGB(x, rowToTest));
            if (x > rightBorder || next != current) {
                runCounts.merge(x - runStart, 1, Integer::sum);
                runStart = x;
                current = next;
            }
        }

        // Ignore 1–3px antialiasing fragments only for rasterized, gray-edged images.
        boolean hasAntialiasing = false;
        for (int x = leftBorder; x <= rightBorder && !hasAntialiasing; x++) {
            int color = bitmap.getRGB(x, rowToTest);
            int red = (color >> 16) & 255, green = (color >> 8) & 255, blue = color & 255;
            hasAntialiasing = red == green && green == blue && red > 0 && red < 255;
        }
        int bestLength = 0;
        int bestCount = 0;
        for (int candidate : runCounts.keySet()) {
            if (hasAntialiasing && candidate <= 3) continue;
            int count = runCounts.entrySet().stream()
                    .filter(entry -> Math.abs(entry.getKey() - candidate) <= 1)
                    .mapToInt(Map.Entry::getValue).sum();
            if (count > bestCount || (count == bestCount && candidate < bestLength)) {
                bestLength = candidate;
                bestCount = count;
            }
        }
        System.out.println("Odhad šířky: " + bestLength);
        System.out.println("Počet podobných délek: " + bestCount);
        return bestLength;

    }

    public int testFewRows(int[] arrayToTest) {
        List<Integer> validSizes = new ArrayList<>();

        for (int i = 0; i < arrayToTest.length; i++) {
            int cellSize = findCellSize(arrayToTest[i]);
//            System.out.println("Řádek " + arrayToTest[i]
//                    + " - odhad: " + cellSize);

            if (cellSize > 0) validSizes.add(cellSize);
        }
        if (validSizes.isEmpty()) {
            System.out.println("0 platných řádků k výpočtu");
            return 0;
        }
        validSizes.sort(Comparator.naturalOrder());
        return validSizes.get(validSizes.size() / 2);
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
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixelCount = Math.multiplyExact(width, height);
        BitSet visited = new BitSet(pixelCount);
        List<ComponentBounds> components = new ArrayList<>();
        for (int start = 0; start < pixelCount; start++) {
            if (visited.get(start)) continue;
            int startX = start % width, startY = start / width;
            if (isWhiteOrNot(bitmap.getRGB(startX, startY))) { visited.set(start); continue; }
            int[] queue = new int[1024];
            int head = 0, tail = 0;
            queue[tail++] = start;
            visited.set(start);
            int minX = startX, maxX = startX, minY = startY, maxY = startY;
            while (head < tail) {
                int index = queue[head++];
                int x = index % width, y = index / width;
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx, ny = y + dy;
                    if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                    int neighbor = ny * width + nx;
                    if (visited.get(neighbor)) continue;
                    visited.set(neighbor);
                    if (isWhiteOrNot(bitmap.getRGB(nx, ny))) continue;
                    if (tail == queue.length) queue = Arrays.copyOf(queue, queue.length * 2);
                    queue[tail++] = neighbor;
                }
            }
            components.add(new ComponentBounds(tail, minX, minY, maxX, maxY));
        }
        ComponentBounds largest = components.stream()
                .max(Comparator.comparingInt(component -> component.pixels))
                .orElseThrow(() -> new IllegalStateException("V obrázku nebyla nalezena oblast bludiště."));
        leftBorder = largest.minX; rightBorder = largest.maxX;
        topBorder = largest.minY; bottomBorder = largest.maxY;
        // Rasterized mazes may have multiple overlapping foreground parts; detached art stays out.
        for (ComponentBounds component : components) {
            boolean overlaps = component.maxX >= largest.minX && component.minX <= largest.maxX
                    && component.maxY >= largest.minY && component.minY <= largest.maxY;
            if (overlaps) {
                leftBorder = Math.min(leftBorder, component.minX);
                rightBorder = Math.max(rightBorder, component.maxX);
                topBorder = Math.min(topBorder, component.minY);
                bottomBorder = Math.max(bottomBorder, component.maxY);
            }
        }
        System.out.println("Horní hranice: " + topBorder);
        System.out.println("Dolní hranice: " + bottomBorder);
        System.out.println("Levá hranice: " + leftBorder);
        System.out.println("Pravá hranice: " + rightBorder);
    }

    private record ComponentBounds(int pixels, int minX, int minY, int maxX, int maxY) {}
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


