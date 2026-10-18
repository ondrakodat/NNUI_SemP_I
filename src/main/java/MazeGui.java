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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class MazeGui extends Application {
    private final Maze maze = new Maze();
    private Canvas canvas;
    private ChoiceBox<MazeOption> imageChoice;
    private ChoiceBox<String> clickMode;
    private ChoiceBox<Orientation> orientationChoice;
    private Slider speed;
    private Label startLabel, goalLabel, runState, details;
    private Integer startX, startY, goalX, goalY;
    private BufferedImage resultImage;
    private boolean running;
    private String selectedMazePath = "/Bludiste.bmp";

    @Override
    public void start(Stage stage) {
        try {
            maze.GetMazeFromFile(selectedMazePath);
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

        ChoiceBox<String> algorithmChoice = new ChoiceBox<>();
        algorithmChoice.getItems().add("A*");
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
        speed.setMajorTickUnit(330);
        speed.setDisable(true);
        Label speedHint = hint("Animace není dostupná: A* nepředává průběžné stavy.");

        Button run = button("Spustit hledání", "#2563eb");
        Button pause = button("Pozastavit / pokračovat", "#475569");
        Button stop = button("Vymazat vizualizaci", "#475569");
        Button newSearch = button("Nové hledání", "#0f766e");
        pause.setDisable(true);
        stop.setDisable(true);
        run.setOnAction(e -> runSearch());
        newSearch.setOnAction(e -> resetSearch());
        stop.setOnAction(e -> { resultImage = null; runState.setText("Výsledek vizualizace vymazán"); drawMaze(); });
        pause.setOnAction(e -> {});

        runState = info("Připraveno · klikněte na start a cíl");
        details = info("Cena: N/A   Prohledané: N/A   Vygenerované: N/A\nČas: N/A   Faktor větvení: N/A");
        Label limitation = hint("Přerušení, pauza a statistiky nejsou dostupné bez změny algoritmu. Běh probíhá na pozadí, aby GUI zůstalo responzivní.");

        VBox box = new VBox(9,
                title, subtitle, section("BLUDIŠTĚ"), imageChoice,
                section("ALGORITMUS"), algorithmChoice,
                section("VSTUPNÍ BODY"), clickMode, startLabel, goalLabel,
                section("ORIENTACE"), orientationChoice, orientationHint,
                section("RYCHLOST ANIMACE"), speed, speedHint,
                run, pause, stop, newSearch,
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
        double scale = Math.min(canvas.getWidth() / maze.getLogicalMaze().length,
                canvas.getHeight() / maze.getLogicalMaze()[0].length);
        double width = maze.getLogicalMaze().length * scale;
        double height = maze.getLogicalMaze()[0].length * scale;
        double left = (canvas.getWidth() - width) / 2;
        double top = (canvas.getHeight() - height) / 2;
        if (px < left || py < top || px >= left + width || py >= top + height) return;
        int x = (int) ((px - left) / scale);
        int y = (int) ((py - top) / scale);
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
        imageChoice.setDisable(true);
        resultImage = null;
        drawMaze();
        runState.setText("Běží A*…");
        details.setText("Cena: N/A   Prohledané: N/A   Vygenerované: N/A\nČas: N/A   Faktor větvení: N/A");
        Thread worker = new Thread(() -> {
            try {
                new AlgorithmA(maze).Start(startX, startY, goalX, goalY);
                File result = new File("Bludiste-prosle.bmp");
                BufferedImage loaded = ImageIO.read(result);
                Platform.runLater(() -> {
                    resultImage = loaded;
                    running = false;
                    imageChoice.setDisable(false);
                    runState.setText("Výpočet dokončen. Algoritmus nevrací informaci o nalezení cesty do GUI.");
                    drawMaze();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    running = false;
                    imageChoice.setDisable(false);
                    runState.setText("Chyba výpočtu: " + ex.getMessage());
                    resultImage = null;
                    drawMaze();
                });
            }
        }, "maze-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void drawMaze() {
        if (canvas == null || maze.getLogicalMaze() == null) return;
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#101827"));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int columns = maze.getLogicalMaze().length;
        int rows = maze.getLogicalMaze()[0].length;
        double scale = Math.min(canvas.getWidth() / columns, canvas.getHeight() / rows);
        double left = (canvas.getWidth() - columns * scale) / 2;
        double top = (canvas.getHeight() - rows * scale) / 2;
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                MazeTitle cell = maze.getLogicalMaze()[x][y];
                Color color = cell.isSolidBlock() ? Color.web("#111827") : Color.web("#f8fafc");
                if (resultImage != null) {
                    int ix = maze.getLeftBorder() + x * maze.getCellSize() + maze.getCellSize() / 2;
                    int iy = maze.getTopBorder() + y * maze.getCellSize() + maze.getCellSize() / 2;
                    if (ix < resultImage.getWidth() && iy < resultImage.getHeight()) {
                        int rgb = resultImage.getRGB(ix, iy);
                        if ((rgb & 0x00ffffff) == 0x000000ff) color = Color.web("#60a5fa");
                        if ((rgb & 0x00ffffff) == 0x00ff0000) color = Color.web("#ef4444");
                    }
                }
                if (startX != null && x == startX && y == startY) color = Color.web("#22c55e");
                if (goalX != null && x == goalX && y == goalY) color = Color.web("#facc15");
                g.setFill(color);
                g.fillRect(left + x * scale, top + y * scale, Math.ceil(scale), Math.ceil(scale));
                g.setStroke(Color.web("#64748b", .22));
                g.strokeRect(left + x * scale, top + y * scale, scale, scale);
            }
        }
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
