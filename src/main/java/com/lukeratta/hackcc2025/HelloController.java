package com.lukeratta.hackcc2025;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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

    // Example: call this from color picker or toolbar
    public void setCurrentColor(Color c) { currentColor = c; }
}