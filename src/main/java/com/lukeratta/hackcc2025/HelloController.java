package com.lukeratta.hackcc2025;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;

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

    private boolean penDown = false;
    private double brushSize = 5;
    private Color currentColor = Color.BLACK;
    private GraphicsContext gc;
    private double lastX, lastY;
    private boolean rainbowEnabled = false;

    private double[] cartesianToPolar(double[] cartesian) {
        double[] polar = new double[2];
        if (cartesian[0] < 0) {
            polar[1] = Math.PI;
        }
        else {
            polar[1] = 0;
        }
        polar[0] = Math.sqrt(Math.pow(cartesian[1], 2) + Math.pow(cartesian[0], 2));
        if (cartesian[0] == 0) {
            cartesian[0] = 0.00001;
        }
        polar[1] += Math.atan(cartesian[1] / cartesian[0]);
        return polar;

    }
    private double[] polarToCartesian(double[] polar) {
        double[] cartesian = new double[2];
        cartesian[0] = polar[0] * Math.cos(polar[1]);
        cartesian[1] = polar[0] * Math.sin(polar[1]);
        return cartesian;
    }

    private void applyMirror(double value) {
        // TODO: hook into your mirror logic
        System.out.println("Mirror value = " + value);
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


    }
    private void onMousePressed(MouseEvent e) {
        penDown = true;
        lastX = e.getX();
        lastY = e.getY();

        gc.setStroke(currentColor);
        gc.setLineWidth(brushSize);

        // Start stroke with a dot
        gc.strokeLine(lastX, lastY, lastX, lastY);
    }

    private void onMouseDragged(MouseEvent e) {
        if (penDown) {
            double x = e.getX();
            double y = e.getY();

            gc.setStroke(currentColor);
            gc.setLineWidth(brushSize);
            gc.strokeLine(lastX, lastY, x, y);   // draw a segment between points

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
    private void setColorToRed() { setCurrentColor(new Color(0.878, 0.227, 0.243, 1.0)); }
    @FXML
    private void setColorToOrange() { setCurrentColor(new Color(0.961, 0.510, 0.122, 1.0)); }
    @FXML
    private void setColorToYellow() { setCurrentColor(new Color(0.992, 0.722, 0.153, 1.0)); }
    @FXML
    private void setColorToGreen() { setCurrentColor(new Color(0.38, 0.733, 0.275, 1.0)); }
    @FXML
    private void setColorToBlue() { setCurrentColor(new Color(0.000, 0.616, 0.863, 1.0)); }
    @FXML
    private void setColorToPurple() { setCurrentColor(new Color(0.588, 0.239, 0.592, 1.0)); }
    @FXML
    private void setColorToWhite() { setCurrentColor(new Color(1.0, 1.0, 1.0, 1.0)); }
    @FXML
    private void setColorToBlack() { setCurrentColor(new Color(0.0, 0.0, 0.0, 1.0)); }

    // Example: call this from color picker or toolbar
    public void setCurrentColor(Color c) { currentColor = c; }
}