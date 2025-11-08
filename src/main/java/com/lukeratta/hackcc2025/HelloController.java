package com.lukeratta.hackcc2025;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

public class HelloController {

    @FXML
    private Pane drawingCanvas;

    @FXML
    private Canvas actualDrawingCanvas;

    private boolean penDown = false;
    private double brushSize = 1;
    private Color currentColor = Color.BLACK;
    private GraphicsContext gc;


    @FXML
    public void initialize() {
//        drawingCanvas.setOnMouseMoved(event -> {
//            double x = event.getX(); // X relative to the Pane
//            double y = event.getY(); // Y relative to the Pane
//            //System.out.println("Mouse at: " + x + ", " + y);
//        });
        gc = actualDrawingCanvas.getGraphicsContext2D();

        // Optional background color
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, actualDrawingCanvas.getWidth(), actualDrawingCanvas.getHeight());

        // Event handlers
        actualDrawingCanvas.setOnMousePressed(this::onMousePressed);
        actualDrawingCanvas.setOnMouseDragged(this::onMouseDragged);
        actualDrawingCanvas.setOnMouseReleased(this::onMouseReleased);
    }
    private void onMousePressed(MouseEvent e) {
        penDown = true;
        drawPoint(e.getX(), e.getY());
    }

    private void onMouseDragged(MouseEvent e) {
        if (penDown) {
            drawPoint(e.getX(), e.getY());
        }
    }

    private void onMouseReleased(MouseEvent e) {
        penDown = false;
    }

    private void drawPoint(double x, double y) {
        gc.setFill(currentColor);
        gc.fillOval(x - brushSize / 2, y - brushSize / 2, brushSize, brushSize);
    }

    // Example: call this from color picker or toolbar
    public void setCurrentColor(Color c) { currentColor = c; }
    public void setBrushSize(double s) { brushSize = s; }
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}