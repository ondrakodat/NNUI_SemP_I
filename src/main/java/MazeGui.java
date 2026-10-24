import MazeEntity.Maze;
import MazeEntity.MazeTitle;
import algorithm.AlgorithmA;
import enums.Orientation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MazeGui extends Application {
    private final Maze maze = new Maze();
    private Canvas canvas;
    private ChoiceBox<MazeOption> imageChoice;
    private ChoiceBox<String> algorithmChoice;
    private ChoiceBox<String> clickMode;
    private ChoiceBox<Orientation> orientationChoice;
    private Slider speed;
    private Label startLabel, goalLabel, runState, details;
    private Button runButton, stopButton;
    private Integer startX, startY, goalX, goalY;
    private BufferedImage resultImage;
    private boolean[][] animatedExplored;
    private boolean[][] animatedPath;
    private final AtomicInteger stepDelayMillis = new AtomicInteger(120);
    private Image sourceImage;
    private volatile boolean running;
    private volatile AlgorithmA activeSearch;
    private final AtomicBoolean stopRequested = new AtomicBoolean();
    private String selectedMazePath = "/Bludiste.bmp";

    @Override
    public void start(Stage stage) {
        try {
            maze.GetMazeFromFile(selectedMazePath);
            sourceImage = toFxImage(maze.getBitmap());
        } catch (RuntimeException e) {
            showError("Bludiště se nepodařilo načíst", e.getMessage());
            return;
        }

        canvas = new Canvas(800, 650);
        canvas.widthProperty().addListener((o, a, b) -> drawMaze());
        canvas.heightProperty().addListener((o, a, b) -> drawMaze());
        canvas.setOnMouseClicked(event -> setPoint(event.getX(), event.getY()));

        VBox controls = buildControls();
        StackPane mazePane = new StackPane(canvas);
        mazePane.setMinSize(450, 350);
        mazePane.setStyle("-fx-background-color: #101827; -fx-background-radius: 12;");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setLeft(controls);
        root.setCenter(mazePane);
        BorderPane.setMargin(mazePane, new Insets(0, 0, 0, 18));
        root.setStyle("-fx-background-color: #0b1220;");

        Scene scene = new Scene(root, 1180, 760);
        stage.setTitle("Maze Solver | NNUI1");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
        drawMaze();
    }

    private VBox buildControls() {
        Label title = new Label("Maze Solver");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Semestrální práce · NNUI1");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        imageChoice = new ChoiceBox<>();
        imageChoice.getItems().setAll(List.of(
                new MazeOption("Bludiste.bmp", "/Bludiste.bmp"),
                new MazeOption("Bludiste_2.bmp", "/Bludiste_2.bmp"),
                new MazeOption("Bludiste_barevne.png", "/Bludiste_barevne.png"),
                new MazeOption("bludiste_nosorozec.png", "/bludiste_nosorozec.png")
        ));
        imageChoice.setValue(imageChoice.getItems().getFirst());
        imageChoice.setMaxWidth(Double.MAX_VALUE);
        imageChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.resourcePath().equals(selectedMazePath)) {
                loadMaze(newValue);
            }
        });

        algorithmChoice = new ChoiceBox<>();
        algorithmChoice.getItems().addAll("A*", "Hladový");
        algorithmChoice.setValue("A*");
        algorithmChoice.setMaxWidth(Double.MAX_VALUE);

        clickMode = new ChoiceBox<>();
        clickMode.getItems().addAll("Vypnuto", "Nastavit start", "Nastavit cíl");
        clickMode.setValue("Nastavit start");
        clickMode.setMaxWidth(Double.MAX_VALUE);

        startLabel = info("Start: nenastaven");
        goalLabel = info("Cíl: nenastaven");
        orientationChoice = new ChoiceBox<>();
        orientationChoice.getItems().addAll(Orientation.values());
        orientationChoice.setValue(Orientation.NORTH);
        orientationChoice.setDisable(true);
        orientationChoice.setMaxWidth(Double.MAX_VALUE);
        Label orientationHint = hint("Algoritmus A* nastavuje počáteční orientaci pevně na NORTH.");

        speed = new Slider(10, 1000, 120);
        speed.setShowTickLabels(true);
        speed.setShowTickMarks(true);
        speed.setMajorTickUnit(250);
        speed.setMinorTickCount(4);
        speed.setBlockIncrement(10);
        Label speedHint = hint("Prodleva mezi kroky: 120 ms");
        speed.valueProperty().addListener((observable, oldValue, newValue) -> {
            int delay = (int) Math.round(newValue.doubleValue());
            stepDelayMillis.set(delay);
            speedHint.setText("Prodleva mezi kroky: " + delay + " ms");
        });

        runButton = button("Spustit hledání", "#2563eb");
        stopButton = button("Zastavit hledání", "#475569");
        Button newSearch = button("Nové hledání", "#0f766e");
        stopButton.setDisable(true);
        runButton.setOnAction(e -> runSearch());
        newSearch.setOnAction(e -> resetSearch());
        stopButton.setOnAction(e -> stopSearch());

        runState = info("Připraveno · klikněte na start a cíl");
        details = info("Cena: N/A   Prohledané: N/A   Vygenerované: N/A\nČas: N/A   Faktor větvení: N/A");
        Label limitation = hint("A* i hladový algoritmus zobrazují prohledávání postupně. Běh probíhá na pozadí, aby GUI zůstalo responzivní.");

        VBox box = new VBox(9,
                title, subtitle, section("BLUDIŠTĚ"), imageChoice,
                section("ALGORITMUS"), algorithmChoice,
                section("VSTUPNÍ BODY"), clickMode, startLabel, goalLabel,
                section("ORIENTACE"), orientationChoice, orientationHint,
                section("RYCHLOST ANIMACE"), speed, speedHint,
                runButton, stopButton, newSearch,
                section("VÝSLEDEK"), runState, details, limitation);
        box.setPadding(new Insets(18));
        box.setPrefWidth(320);
        box.setMinWidth(300);
        box.setStyle("-fx-background-color: #111c2e; -fx-background-radius: 12;");
        return box;
    }

    private void loadMaze(MazeOption option) {
        String previousPath = selectedMazePath;
        try {
            maze.GetMazeFromFile(option.resourcePath());
            sourceImage = toFxImage(maze.getBitmap());
            selectedMazePath = option.resourcePath();
            resetSearch();
            drawMaze();
        } catch (RuntimeException e) {
            imageChoice.getItems().stream()
                    .filter(item -> item.resourcePath().equals(previousPath))
                    .findFirst()
                    .ifPresent(item -> imageChoice.setValue(item));
            showError("Bludiště se nepodařilo načíst", e.getMessage());
        }
    }

    private void resetSearch() {
        if (running) {
            runState.setText("Hledání právě probíhá. Počkejte na jeho dokončení.");
            return;
        }
        resultImage = null;
        animatedExplored = null;
        animatedPath = null;
        startX = null;
        startY = null;
        goalX = null;
        goalY = null;
        startLabel.setText("Start: nenastaven");
        goalLabel.setText("Cíl: nenastaven");
        clickMode.setValue("Nastavit start");
        runState.setText("Připraveno · nejprve vyberte start, potom cíl");
        details.setText("Cena: N/A   Prohledané: N/A   Vygenerované: N/A\nČas: N/A   Faktor větvení: N/A");
        drawMaze();
    }

    private void setPoint(double px, double py) {
        if ("Vypnuto".equals(clickMode.getValue()) || running) return;
        int columns = maze.getLogicalMaze().length;
        int rows = maze.getLogicalMaze()[0].length;
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double imageScale = Math.min(canvasWidth / maze.getBitmap().getWidth(),
                canvasHeight / maze.getBitmap().getHeight());
        double imageWidth = maze.getBitmap().getWidth() * imageScale;
        double imageHeight = maze.getBitmap().getHeight() * imageScale;
        double imageLeft = (canvasWidth - imageWidth) / 2;
        double imageTop = (canvasHeight - imageHeight) / 2;
        System.out.printf("[CLICK] GUI=(%.2f, %.2f), imageArea=%.2f×%.2f, scale=%.5f, offset=(%.2f, %.2f)%n",
                px, py, imageWidth, imageHeight, imageScale, imageLeft, imageTop);
        if (px < imageLeft || py < imageTop || px >= imageLeft + imageWidth
                || py >= imageTop + imageHeight) return;
        double sourcePixelX = (px - imageLeft) / imageScale;
        double sourcePixelY = (py - imageTop) / imageScale;
        if (sourcePixelX < maze.getLeftBorder() || sourcePixelX >= maze.getRightBorder() + 1.0
                || sourcePixelY < maze.getTopBorder() || sourcePixelY >= maze.getBottomBorder() + 1.0) {
            return;
        }
        double gridX = (sourcePixelX - maze.getGridOriginX()) / maze.getCellSize();
        double gridY = (sourcePixelY - maze.getGridOriginY()) / maze.getCellSize();
        int x = (int) gridX;
        int y = (int) gridY;
        if (x < 0 || y < 0 || x >= columns || y >= rows) return;
        int sourceX = (int) sourcePixelX;
        int sourceY = (int) sourcePixelY;
        int centerX = maze.getGridOriginX() + x * maze.getCellSize() + maze.getCellSize() / 2;
        int centerY = maze.getGridOriginY() + y * maze.getCellSize() + maze.getCellSize() / 2;
        int sampledRgb = maze.getBitmap().getRGB(centerX, centerY);
        int red = (sampledRgb >> 16) & 255;
        int green = (sampledRgb >> 8) & 255;
        int blue = sampledRgb & 255;
        boolean sampledAsWhite = red > 235 && green > 235 && blue > 235;
        boolean classifiedAsWall = maze.getLogicalMaze()[x][y].isSolidBlock();
        System.out.printf("[CLICK] maze@%08x logicalMaze@%08x, grid=(sloupec %d, řádek %d), sourcePixel=(%d, %d), cellCenter=(%d, %d), centerRGB=#%06X, isWhite=%s, logical=%s%n",
                System.identityHashCode(maze), System.identityHashCode(maze.getLogicalMaze()),
                x, y, sourceX, sourceY, centerX, centerY, sampledRgb & 0x00ffffff,
                sampledAsWhite, classifiedAsWall ? "ZEĎ" : "CHODBA");
        boolean validateClickedPixel = maze.isCorridorCellGrid()
                || maze.getResourcePath().endsWith("/Bludiste_barevne.png");
        if (validateClickedPixel && !maze.isPassableImagePixel(sourceX, sourceY)) {
            runState.setText("Klikněte do světlé průchozí části chodby.");
            return;
        }
        if (!maze.areValidEnterStates(x, y)) {
            runState.setText("Neplatná pozice: vybrali jste stěnu.");
            return;
        }
        if ("Nastavit start".equals(clickMode.getValue())) {
            startX = x; startY = y; startLabel.setText("Start: (" + x + ", " + y + ")");
            clickMode.setValue("Nastavit cíl");
        } else {
            goalX = x; goalY = y; goalLabel.setText("Cíl: (" + x + ", " + y + ")");
            clickMode.setValue("Vypnuto");
        }
        resultImage = null;
        animatedExplored = null;
        animatedPath = null;
        runState.setText("Připraveno");
        drawMaze();
    }

    private void runSearch() {
        if (running) return;
        if (startX == null || goalX == null) {
            runState.setText("Nejprve kliknutím nastavte start i cíl.");
            return;
        }
        if (startX.equals(goalX) && startY.equals(goalY)) {
            runState.setText("Start a cíl musí být různé pozice.");
            return;
        }
        running = true;
        stopRequested.set(false);
        runButton.setDisable(true);
        stopButton.setDisable(false);
        imageChoice.setDisable(true);
        algorithmChoice.setDisable(true);
        resultImage = null;
        animatedExplored = new boolean[maze.getLogicalMaze().length][maze.getLogicalMaze()[0].length];
        animatedPath = new boolean[maze.getLogicalMaze().length][maze.getLogicalMaze()[0].length];
        final int searchStartX = startX;
        final int searchStartY = startY;
        final int searchGoalX = goalX;
        final int searchGoalY = goalY;
        final boolean greedy = "Hladový".equals(algorithmChoice.getValue());
        final long searchStartedAt = System.nanoTime();
        final AtomicReference<AlgorithmA> runSearchRef = new AtomicReference<>();
        drawMaze();
        runState.setText(greedy ? "Běží hladové hledání…" : "Běží A*…");
        details.setText("Cena: N/A   Prohledané: N/A   Vygenerované: N/A\nČas: N/A   Faktor větvení: N/A");
        Thread worker = new Thread(() -> {
            try {
                AlgorithmA.SearchListener listener = new AlgorithmA.SearchListener() {
                    private int expandedStates;

                    @Override
                    public void onExpanded(MazeEntity.MazeState state) {
                        expandedStates++;
                        updateOnFxThreadAndWait(() -> {
                            animatedExplored[state.getXPosition()][state.getYPosition()] = true;
                            runState.setText((greedy ? "Hladové hledání" : "A*")
                                    + " · prohledáno " + expandedStates + " stavů");
                            drawMaze();
                        });
                        try {
                            Thread.sleep(stepDelayMillis.get());
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    @Override
                    public void onCompleted(List<MazeEntity.MazeState> path, boolean found, int expanded) {
                        completeSearch(path, found, expanded, -1);
                    }

                    @Override
                    public void onCompleted(List<MazeEntity.MazeState> path, boolean found,
                                            int expanded, int generated) {
                        completeSearch(path, found, expanded, generated);
                    }

                    private void completeSearch(List<MazeEntity.MazeState> path, boolean found,
                                                int expanded, int generated) {
                        updateOnFxThreadAndWait(() -> {
                            for (MazeEntity.MazeState state : path) {
                                animatedPath[state.getXPosition()][state.getYPosition()] = true;
                            }
                            boolean cancelled = stopRequested.get();
                            runState.setText(cancelled ? "Hledání zastaveno"
                                    : found ? "Cesta nalezena" : "Cesta nebyla nalezena");
                            stopButton.setDisable(true);
                            int cost = path.isEmpty() ? 0 : path.get(path.size() - 1).getTotalCost();
                            long elapsedMs = (System.nanoTime() - searchStartedAt) / 1_000_000;
                            int depth = found ? path.size() - 1 : -1;
                            String branching = found && generated >= 0
                                    ? String.format(java.util.Locale.ROOT, "%.3f", effectiveBranchingFactor(generated, depth))
                                    : "N/A";
                            details.setText("Cena: " + (found ? cost : "N/A")
                                    + "   Prohledané: " + expanded
                                    + "   Vygenerované: " + (generated >= 0 ? generated : "N/A")
                                    + "   Kroky cesty: " + (found ? depth : "N/A")
                                    + "\nČas: " + elapsedMs + " ms   Algoritmus: "
                                    + (greedy ? "hladový" : "A*")
                                    + "   Efektivní faktor větvení: " + branching);
                            drawMaze();
                        });
                    }
                };
                AlgorithmA search = new AlgorithmA(maze, greedy, listener);
                runSearchRef.set(search);
                activeSearch = search;
                if (stopRequested.get()) search.cancel();
                search.Start(searchStartX, searchStartY, searchGoalX, searchGoalY);
                File result = new File("Bludiste-prosle.bmp");
                BufferedImage loaded = ImageIO.read(result);
                Platform.runLater(() -> {
                    if (activeSearch != search) return;
                    resultImage = loaded;
                    running = false;
                    activeSearch = null;
                    runButton.setDisable(false);
                    stopButton.setDisable(true);
                    imageChoice.setDisable(false);
                    algorithmChoice.setDisable(false);
                    drawMaze();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    if (runSearchRef.get() != null && activeSearch != runSearchRef.get()) return;
                    running = false;
                    activeSearch = null;
                    runButton.setDisable(false);
                    stopButton.setDisable(true);
                    imageChoice.setDisable(false);
                    algorithmChoice.setDisable(false);
                    runState.setText("Chyba výpočtu: " + ex.getMessage());
                    drawMaze();
                });
            }
        }, "maze-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void stopSearch() {
        if (!running) return;
        stopRequested.set(true);
        AlgorithmA search = activeSearch;
        stopButton.setDisable(true);
        runState.setText("Zastavuji hledání…");
        if (search != null) search.cancel();
    }

    private static double effectiveBranchingFactor(int generatedStates, int solutionDepth) {
        if (solutionDepth <= 0) return generatedStates <= 1 ? 0.0 : Double.NaN;
        if (generatedStates <= solutionDepth + 1) return 1.0;
        double low = 1.0;
        double high = Math.max(2.0, generatedStates);
        for (int iteration = 0; iteration < 100; iteration++) {
            double middle = (low + high) / 2.0;
            double total = 1.0;
            double term = 1.0;
            for (int level = 1; level <= solutionDepth && total <= generatedStates; level++) {
                term *= middle;
                total += term;
            }
            if (total > generatedStates) high = middle;
            else low = middle;
        }
        return (low + high) / 2.0;
    }

    private void updateOnFxThreadAndWait(Runnable update) {
        CountDownLatch completed = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                update.run();
            } finally {
                completed.countDown();
            }
        });
        try {
            completed.await();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void drawMaze() {
        if (canvas == null || maze.getLogicalMaze() == null) return;
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#101827"));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int columns = maze.getLogicalMaze().length;
        int rows = maze.getLogicalMaze()[0].length;
        if (sourceImage == null) return;
        double scale = Math.min(canvas.getWidth() / sourceImage.getWidth(),
                canvas.getHeight() / sourceImage.getHeight());
        double imageWidth = sourceImage.getWidth() * scale;
        double imageHeight = sourceImage.getHeight() * scale;
        double left = (canvas.getWidth() - imageWidth) / 2;
        double top = (canvas.getHeight() - imageHeight) / 2;
        g.drawImage(sourceImage, left, top, imageWidth, imageHeight);
        boolean colorMaze = maze.getResourcePath().endsWith("/Bludiste_barevne.png");
        Color[][] corridorHighlights = maze.isCorridorCellGrid()
                ? new Color[columns][rows] : null;
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                MazeTitle cell = maze.getLogicalMaze()[x][y];
                Color color = cell.isSolidBlock()
                        ? Color.web("#111827", .48) : Color.web("#f8fafc", .22);
                boolean highlighted = false;
                if (animatedExplored != null && animatedExplored[x][y]) {
                    color = Color.web("#60a5fa", .75);
                    highlighted = true;
                }
                if (animatedPath != null && animatedPath[x][y]) {
                    color = Color.web("#ef4444", .82);
                    highlighted = true;
                }
                if (resultImage != null) {
                    int ix = maze.getGridOriginX() + x * maze.getCellSize() + maze.getCellSize() / 2;
                    int iy = maze.getGridOriginY() + y * maze.getCellSize() + maze.getCellSize() / 2;
                    if (ix < resultImage.getWidth() && iy < resultImage.getHeight()) {
                        int rgb = resultImage.getRGB(ix, iy);
                        if ((rgb & 0x00ffffff) == 0x000000ff) {
                            color = Color.web("#60a5fa", .75);
                            highlighted = true;
                        }
                        if ((rgb & 0x00ffffff) == 0x00ff0000) {
                            color = Color.web("#ef4444", .75);
                            highlighted = true;
                        }
                    }
                }
                if (startX != null && x == startX && y == startY) {
                    color = Color.web("#22c55e");
                    highlighted = true;
                }
                if (goalX != null && x == goalX && y == goalY) {
                    color = Color.web("#facc15");
                    highlighted = true;
                }
                if (corridorHighlights != null && highlighted) {
                    corridorHighlights[x][y] = color;
                }
                g.setFill(color);
                // The logical pitch locates cell centers (103px for the rhino
                // maze); the visible corridor tile can be narrower than that.
                int centerX = maze.getGridOriginX() + x * maze.getCellSize() + maze.getCellSize() / 2;
                int centerY = maze.getGridOriginY() + y * maze.getCellSize() + maze.getCellSize() / 2;
                int cellPixelLeft = centerX - maze.getDisplayCellWidth() / 2;
                int cellPixelTop = centerY - maze.getDisplayCellHeight() / 2;
                int visibleLeft = Math.max(cellPixelLeft, maze.getLeftBorder());
                int visibleTop = Math.max(cellPixelTop, maze.getTopBorder());
                int visibleRight = Math.min(cellPixelLeft + maze.getDisplayCellWidth(), maze.getRightBorder() + 1);
                int visibleBottom = Math.min(cellPixelTop + maze.getDisplayCellHeight(), maze.getBottomBorder() + 1);
                double cellLeft = left + visibleLeft * scale;
                double cellTop = top + visibleTop * scale;
                double cellWidth = (visibleRight - visibleLeft) * scale;
                double cellHeight = (visibleBottom - visibleTop) * scale;
                if (!maze.isCorridorCellGrid() && !colorMaze) {
                    g.fillRect(cellLeft, cellTop, cellWidth, cellHeight);
                } else if (highlighted) {
                    g.fillRect(cellLeft, cellTop, cellWidth, cellHeight);
                }
                g.setStroke(colorMaze ? Color.web("#0f172a", .16) : Color.web("#64748b", .35));
                g.strokeRect(cellLeft, cellTop, cellWidth, cellHeight);
            }
        }
        if (corridorHighlights != null) {
            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {
                    Color color = corridorHighlights[x][y];
                    if (color == null) continue;
                    if (x + 1 < columns && corridorHighlights[x + 1][y] != null
                            && maze.isTransitionOpen(x, y, Orientation.EAST)) {
                        drawCorridorConnector(g, left, top, scale, x, y,
                                Orientation.EAST, color, corridorHighlights[x + 1][y]);
                    }
                    if (y + 1 < rows && corridorHighlights[x][y + 1] != null
                            && maze.isTransitionOpen(x, y, Orientation.SOUTH)) {
                        drawCorridorConnector(g, left, top, scale, x, y,
                                Orientation.SOUTH, color, corridorHighlights[x][y + 1]);
                    }
                }
            }
        }
    }

    private void drawCorridorConnector(GraphicsContext g, double imageLeft, double imageTop,
                                       double scale, int x, int y,
                                       Orientation direction, Color color, Color nextColor) {
        double centerX = maze.getGridOriginX() + x * maze.getCellSize() + maze.getCellSize() / 2.0;
        double centerY = maze.getGridOriginY() + y * maze.getCellSize() + maze.getCellSize() / 2.0;
        double halfWidth = maze.getDisplayCellWidth() / 2.0;
        double halfHeight = maze.getDisplayCellHeight() / 2.0;
        double x1, y1, x2, y2;
        if (direction == Orientation.EAST) {
            double nextCenterX = centerX + maze.getCellSize();
            x1 = centerX + halfWidth - 1;
            x2 = nextCenterX - halfWidth + 1;
            y1 = centerY - halfHeight;
            y2 = centerY + halfHeight;
        } else {
            double nextCenterY = centerY + maze.getCellSize();
            x1 = centerX - halfWidth;
            x2 = centerX + halfWidth;
            y1 = centerY + halfHeight - 1;
            y2 = nextCenterY - halfHeight + 1;
        }
        x1 = Math.max(x1, maze.getLeftBorder());
        y1 = Math.max(y1, maze.getTopBorder());
        x2 = Math.min(x2, maze.getRightBorder() + 1.0);
        y2 = Math.min(y2, maze.getBottomBorder() + 1.0);
        if (x2 > x1 && y2 > y1) {
            if (color.equals(nextColor)) {
                g.setFill(color);
                g.fillRect(imageLeft + x1 * scale, imageTop + y1 * scale,
                        (x2 - x1) * scale, (y2 - y1) * scale);
            } else if (direction == Orientation.EAST) {
                double middleX = (x1 + x2) / 2.0;
                g.setFill(color);
                g.fillRect(imageLeft + x1 * scale, imageTop + y1 * scale,
                        (middleX - x1) * scale, (y2 - y1) * scale);
                g.setFill(nextColor);
                g.fillRect(imageLeft + middleX * scale, imageTop + y1 * scale,
                        (x2 - middleX) * scale, (y2 - y1) * scale);
            } else {
                double middleY = (y1 + y2) / 2.0;
                g.setFill(color);
                g.fillRect(imageLeft + x1 * scale, imageTop + y1 * scale,
                        (x2 - x1) * scale, (middleY - y1) * scale);
                g.setFill(nextColor);
                g.fillRect(imageLeft + x1 * scale, imageTop + middleY * scale,
                        (x2 - x1) * scale, (y2 - middleY) * scale);
            }
        }
    }

    private static Image toFxImage(BufferedImage image) {
        WritableImage fxImage = new WritableImage(image.getWidth(), image.getHeight());
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        fxImage.getPixelWriter().setPixels(0, 0, image.getWidth(), image.getHeight(),
                PixelFormat.getIntArgbInstance(), pixels, 0, image.getWidth());
        return fxImage;
    }

    private static Label section(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 7 0 0 0;");
        return label;
    }
    private static Label info(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");
        return label;
    }
    private static Label hint(String text) {
        Label label = info(text);
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        return label;
    }
    private static Button button(String text, String color) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 9;");
        return button;
    }
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title); alert.setHeaderText(title); alert.showAndWait();
    }
    private record MazeOption(String displayName, String resourcePath) {
        @Override
        public String toString() { return displayName; }
    }
    public static void main(String[] args) { launch(args); }
}
