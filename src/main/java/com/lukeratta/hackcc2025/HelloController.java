package com.lukeratta.hackcc2025;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HelloController {

    @FXML
    private Pane drawingCanvas;

    @FXML
    private Canvas actualDrawingCanvas;

    @FXML
    private Slider sizeSlider;

    @FXML
    private ImageView mirror; // fx:id="mirror" in your FXML

    @FXML
    private ImageView rainbow;

    @FXML
    private HBox mainHBox;

    private boolean penDown = false;
    private double brushSize = 12;
    private Color currentColor = Color.BLACK;
    private GraphicsContext gc;
    private double lastX, lastY;
    private Timeline hueCycle;
    private ColorAdjust colorAdjust;
    private int symmetry = 2;
    private boolean rainbowMode = false;
    private long rainbowT0Nanos = 0L;
    private double oldWidth = 0;
    private double oldHeight = 0;

    private Color nextRainbowColor() {
        long now = System.nanoTime();
        double seconds = (now - rainbowT0Nanos) / 1_000_000_000.0;
        // 180°/s = full cycle in ~2 seconds
        double rainbowSpeedDegPerSec = 180.0;
        double hue = (seconds * rainbowSpeedDegPerSec) % 360.0;
        if (hue < 0) hue += 360.0;
        return Color.hsb(hue, 1.0, 1.0);
    }

    private static void drawSymmetricSegment(GraphicsContext g,
                                             double centerX, double centerY,
                                             int symmetry,
                                             double x0, double y0,
                                             double x1, double y1) {
        if (symmetry <= 0) return;

        // Work in coordinates relative to the center
        double ax = x0 - centerX, ay = y0 - centerY;
        double bx = x1 - centerX, by = y1 - centerY;

        for (int k = 0; k < symmetry; k++) {
            double angle = 2.0 * Math.PI * k / symmetry;
            double cos = Math.cos(angle), sin = Math.sin(angle);

            double rax =  ax * cos - ay * sin;
            double ray =  ax * sin + ay * cos;
            double rbx =  bx * cos - by * sin;
            double rby =  bx * sin + by * cos;

            g.strokeLine(centerX + rax, centerY + ray,
                    centerX + rbx, centerY + rby);
        }
    }

    private void applyMirror(double value) {
        symmetry = (int) value;
        System.out.println("Mirror value = " + value);
    }

    public void attachResizeListener(Stage stage) {
        // Fire once now so sizes are correct on first show
        resizeToStage(stage);

        // Resize continuously as the window changes
        stage.widthProperty().addListener((obs, ov, nv) -> resizeToStage(stage));
        stage.heightProperty().addListener((obs, ov, nv) -> resizeToStage(stage));
    }

    private void resizeToStage(Stage stage) {
        if (stage.getScene() == null) return;

        double w = Math.max(0, stage.getScene().getWidth()) - 96;
        double h = Math.max(0, stage.getScene().getHeight());

        double prevW = oldWidth;
        double prevH = oldHeight;

        actualDrawingCanvas.setWidth(w);
        actualDrawingCanvas.setHeight(h);

        // Fill only the new areas
        gc.setFill(Color.WHITE);

        // Right extension
        if (w > prevW) {
            gc.fillRect(prevW, 0, w - prevW, h);
        }

        // Bottom extension
        if (h > prevH) {
            gc.fillRect(0, prevH, w, h - prevH);
        }

        oldWidth = w;
        oldHeight = h;
    }

    public void saveCanvasAsPNG(File file) {
        try {
            // Take a snapshot of the canvas
            javafx.scene.image.WritableImage image =
                    new javafx.scene.image.WritableImage(
                            (int) actualDrawingCanvas.getWidth(), (int) actualDrawingCanvas.getHeight());
            actualDrawingCanvas.snapshot(null, image);

            // Convert to BufferedImage for ImageIO
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            // Write the file
            ImageIO.write(bufferedImage, "png", file);
            System.out.println("✅ Saved to: " + file.getAbsolutePath());

        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        gc = actualDrawingCanvas.getGraphicsContext2D();
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        // Optional background color
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, actualDrawingCanvas.getWidth(), actualDrawingCanvas.getHeight());

        // Event handlers
        actualDrawingCanvas.setOnMousePressed(this::onMousePressed);
        actualDrawingCanvas.setOnMouseDragged(this::onMouseDragged);
        actualDrawingCanvas.setOnMouseReleased(this::onMouseReleased);

        brushSize = sizeSlider.getValue(); // initial
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brushSize = newVal.doubleValue();
            System.out.println("Brush size: " + brushSize);
        });

        // --- Mirror popup spinner setup ---
        ContextMenu mirrorMenu = new ContextMenu();
        Spinner<Integer> mirrorSpinner = new Spinner<>(2, 100, 2, 1);
        mirrorSpinner.setEditable(true);
        mirrorSpinner.setPrefWidth(100);

        // Put spinner in a menu item that doesn’t auto-hide
        CustomMenuItem spinnerItem = new CustomMenuItem(mirrorSpinner, false);
        mirrorMenu.getItems().setAll(spinnerItem);

        // Show the popup when the mirror icon is clicked
        mirror.setOnMouseClicked(e ->
                mirrorMenu.show(mirror, e.getScreenX(), e.getScreenY())
        );

        // When the user presses Enter or moves focus away, apply and close
        mirrorSpinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                applyMirror(mirrorSpinner.getValue());
                mirrorMenu.hide();
            }
        });

        mirrorSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Optional: live update while they scroll the spinner
            applyMirror(newVal);
        });

        colorAdjust = new ColorAdjust();
        rainbow.setEffect(colorAdjust);

        // Animate hue from -1 to +1 (full sweep), then back
        hueCycle = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(colorAdjust.hueProperty(), -1)),
                new KeyFrame(Duration.seconds(3),  new KeyValue(colorAdjust.hueProperty(),  1))
        );
        hueCycle.setAutoReverse(false);
        hueCycle.setCycleCount(Animation.INDEFINITE);

        colorAdjust = new ColorAdjust();
        rainbow.setEffect(colorAdjust);

// Animate hue from 0 → 1 and wrap back to 0 for smooth rollover
        hueCycle = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(colorAdjust.hueProperty(), -1)),
                new KeyFrame(Duration.seconds(3), new KeyValue(colorAdjust.hueProperty(),  1))
        );
        hueCycle.setAutoReverse(false);
        hueCycle.setCycleCount(Animation.INDEFINITE);

// When hovering, fade in from hue 0 to the current animation smoothly
        rainbow.setOnMouseEntered(e -> {
            if (hueCycle.getStatus() == Animation.Status.STOPPED) {
                // Map current hue (-1..1) to timeline fraction (0..1) and jump there
                double h = colorAdjust.getHue();          // likely 0 on first run
                double frac = (h + 1.0) / 2.0;            // -1..1 -> 0..1
                hueCycle.jumpTo(Duration.seconds(3 * frac));
            }
            if (hueCycle.getStatus() != Animation.Status.RUNNING) {
                hueCycle.play();                          // resume without resetting
            }
        });

        rainbow.setOnMouseExited(e -> hueCycle.pause());




    }
    private void onMousePressed(MouseEvent e) {
        penDown = true;
        lastX = e.getX();
        lastY = e.getY();
        boolean hasLast = true;

        Color brush = rainbowMode ? nextRainbowColor() : currentColor;
        gc.setStroke(brush);
        gc.setLineWidth(brushSize);
        gc.setStroke(brush);
        gc.setFill(brush);
        // Start stroke with a dot
        gc.strokeLine(lastX, lastY, lastX, lastY);
    }

    private void onMouseDragged(MouseEvent e) {
        if (penDown) {
            double x = e.getX();
            double y = e.getY();

            Color brush = rainbowMode ? nextRainbowColor() : currentColor;
            gc.setStroke(brush);
            gc.setLineWidth(brushSize);
            gc.strokeLine(lastX, lastY, x, y);   // draw a segment between points

            GraphicsContext g = gc; // assuming you already have gc = actualDrawingCanvas.getGraphicsContext2D()
            // keep your existing stroke settings (color, line width, caps) before drawing:
            // e.g., g.setStroke(currentColor); g.setLineWidth(sizeSlider.getValue());

            double cx = actualDrawingCanvas.getWidth()  * 0.5;
            double cy = actualDrawingCanvas.getHeight() * 0.5;

            drawSymmetricSegment(g, cx, cy, Math.max(1, symmetry), lastX, lastY, x, y);


            lastX = x;
            lastY = y;
        }
    }

    private void onMouseReleased(MouseEvent e) {
        penDown = false;
    }

    @FXML
    private void onSaveClicked() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Drawing");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = chooser.showSaveDialog(drawingCanvas.getScene().getWindow());
        if (file != null) saveCanvasAsPNG(file);
    }

    @FXML
    private void setColorToRed() { setCurrentColor(new Color(0.878, 0.227, 0.243, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToOrange() { setCurrentColor(new Color(0.961, 0.510, 0.122, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToYellow() { setCurrentColor(new Color(0.992, 0.722, 0.153, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToGreen() { setCurrentColor(new Color(0.38, 0.733, 0.275, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToBlue() { setCurrentColor(new Color(0.000, 0.616, 0.863, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToPurple() { setCurrentColor(new Color(0.588, 0.239, 0.592, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToWhite() { setCurrentColor(new Color(1.0, 1.0, 1.0, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToGrey() { setCurrentColor(new Color(0.3, 0.3, 0.3, 1.0)); rainbowMode = false; }
    @FXML
    private void setColorToBlack() { setCurrentColor(new Color(0.0, 0.0, 0.0, 1.0)); rainbowMode = false; }

    @FXML
    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, actualDrawingCanvas.getWidth(), actualDrawingCanvas.getHeight());
    }
    @FXML
    private void disableSymmetry() {
        symmetry = 1;
    }
    @FXML
    private void enableRainbowMode() {
        rainbowMode = true;
        rainbowT0Nanos = System.nanoTime();
    }

    // Example: call this from color picker or toolbar
    public void setCurrentColor(Color c) { currentColor = c; }
}