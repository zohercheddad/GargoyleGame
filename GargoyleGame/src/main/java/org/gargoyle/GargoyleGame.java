package org.gargoyle;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;

public class GargoyleGame extends Application {

    // ------ Paramètres écran ------
    private static final int WIDTH = 800;
    private static final int HEIGHT = 450;

    // ------ Joueur ------
    static class Player {
        double x, y, w, h;      // position et taille
        double vx, vy;          // vitesses
        boolean onGround;       // est-il sur le sol ?

        Player(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.vx = 0; this.vy = 0; this.onGround = false;
        }

        void update(double dt, double gravity, double floorY) {
            // gravité + intégration simple
            vy += gravity * dt;
            x  += vx * dt;
            y  += vy * dt;

            // collision sol
            if (y + h > floorY) {
                y = floorY - h;
                vy = 0;
                onGround = true;
            } else {
                onGround = false;
            }

            // petites bornes latérales (hors-écran)
            if (x < 0) x = 0;
            if (x + w > WIDTH) x = WIDTH - w;
        }

        void draw(GraphicsContext gc) {
            gc.setFill(Color.DARKSLATEBLUE);
            gc.fillRect(x, y, w, h);
            // petits "yeux" pour la gargouille :)
            gc.setFill(Color.LIGHTBLUE);
            gc.fillRect(x + 6, y + 6, 6, 6);
            gc.fillRect(x + w - 12, y + 6, 6, 6);
        }
    }

    // ------ Entrées clavier ------
    private boolean left, right, jump;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Sol à 50 px du bas
        final double floorY = HEIGHT - 50;
        final double gravity = 1200.0;
        final double moveSpeed = 300.0;
        final double jumpSpeed = 520.0;

        Player gargoyle = new Player(100, floorY - 32, 32, 32);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle("Gargoyle — base JavaFX (Canvas + AnimationTimer)");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Focus pour capter le clavier
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        // Contrôles: flèches OU ZQSD + Space
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT  || c == KeyCode.Q) left  = true;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = true;
            if (c == KeyCode.UP    || c == KeyCode.Z || c == KeyCode.SPACE) jump = true;
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT  || c == KeyCode.Q) left  = false;
            if (c == KeyCode.RIGHT || c == KeyCode.D) right = false;
            // jump reste "one-shot", on lira/vider dans la boucle
        });

        // Boucle de jeu (60 FPS typique) — voir AnimationTimer. :contentReference[oaicite:4]{index=4}
        final long[] last = { System.nanoTime() };
        new AnimationTimer() {
            @Override public void handle(long now) {
                double dt = (now - last[0]) / 1e9;
                last[0] = now;

                // 1) Entrées -> vitesse horizontale
                if (left && !right) gargoyle.vx = -moveSpeed;
                else if (right && !left) gargoyle.vx = moveSpeed;
                else gargoyle.vx = 0;

                // 2) Saut (one-shot)
                if (jump && gargoyle.onGround) {
                    gargoyle.vy = -jumpSpeed;
                }
                jump = false;

                // 3) Mise à jour physique
                gargoyle.update(dt, gravity, floorY);

                // 4) Rendu
                // ciel
                gc.setFill(Color.SKYBLUE);
                gc.fillRect(0, 0, WIDTH, HEIGHT);
                // sol
                gc.setFill(Color.DARKGREEN);
                gc.fillRect(0, floorY, WIDTH, HEIGHT - floorY);
                // joueur
                gargoyle.draw(gc);
            }
        }.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
