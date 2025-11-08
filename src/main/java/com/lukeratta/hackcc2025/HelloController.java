package com.lukeratta.hackcc2025;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

public class HelloController {

    @FXML
    private Canvas actualDrawingCanvas;

    @FXML
    private Slider sizeSlider;

    private boolean penDown = false;
    private double brushSize = 5;
    private Color currentColor = Color.BLACK;
    private GraphicsContext gc;
    private double lastX, lastY;


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

    // Example: call this from color picker or toolbar
    public void setCurrentColor(Color c) { currentColor = c; }
}