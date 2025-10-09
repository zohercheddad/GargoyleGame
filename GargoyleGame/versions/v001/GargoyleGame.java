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

import java.util.ArrayList;
import java.util.List;

public class GargoyleGame extends Application {

    // -------- Paramètres d'écran --------
    private static final int WIDTH = 800;
    private static final int HEIGHT = 450;

    // -------- Entité Joueur --------
    static class Player {
        double x, y, w, h;   // position + taille
        double vx, vy;       // vitesses
        boolean onGround;    // sur le sol ?

        Player(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.vx = 0; this.vy = 0; this.onGround = false;
        }

        void draw(GraphicsContext gc) {
            gc.setFill(Color.DARKSLATEBLUE);
            gc.fillRect(x, y, w, h);
            gc.setFill(Color.LIGHTBLUE);
            gc.fillRect(x + 6, y + 6, 6, 6);
            gc.fillRect(x + w - 12, y + 6, 6, 6);
        }
    }

    // -------- Obstacle (plateforme rectangulaire) --------
    static class Obstacle {
        double x, y, w, h;
        Obstacle(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
        void draw(GraphicsContext gc) {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(x, y, w, h);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, w, h);
        }
    }

    // -------- Entrées clavier --------
    private boolean left, right, jump;

    // -------- AABB utilitaire --------
    static boolean aabb(double ax, double ay, double aw, double ah,
                        double bx, double by, double bw, double bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    // -------- Résolution verticale --------
    static void resolveVertical(Player p, List<Obstacle> obs, double floorY) {
        // d’abord le "sol" global
        if (p.y + p.h > floorY) {
            p.y = floorY - p.h;
            p.vy = 0;
            p.onGround = true;
        } else {
            p.onGround = false;
        }
        // puis les plateformes
        for (Obstacle o : obs) {
            if (aabb(p.x, p.y, p.w, p.h, o.x, o.y, o.w, o.h)) {
                if (p.vy > 0) { // on descend : poser sur le dessus
                    p.y = o.y - p.h;
                    p.vy = 0;
                    p.onGround = true;
                } else if (p.vy < 0) { // on monte : heurter le dessous
                    p.y = o.y + o.h;
                    p.vy = 0;
                }
            }
        }
    }

    // -------- Résolution horizontale --------
    static void resolveHorizontal(Player p, List<Obstacle> obs) {
        for (Obstacle o : obs) {
            if (aabb(p.x, p.y, p.w, p.h, o.x, o.y, o.w, o.h)) {
                if (p.vx > 0) { // on allait à droite
                    p.x = o.x - p.w;
                } else if (p.vx < 0) { // on allait à gauche
                    p.x = o.x + o.w;
                }
                p.vx = 0;
            }
        }
        // garder à l’écran
        if (p.x < 0) p.x = 0;
        if (p.x + p.w > WIDTH) p.x = WIDTH - p.w;
    }

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Monde
        final double floorY    = HEIGHT - 50; // sol
        final double gravity   = 1200.0;
        final double moveSpeed = 300.0;
        final double jumpSpeed = 520.0;

        Player gargoyle = new Player(100, floorY - 32, 32, 32);

        // Plateformes
        List<Obstacle> obstacles = new ArrayList<>();
        obstacles.add(new Obstacle(260, floorY - 40, 120, 20));
        obstacles.add(new Obstacle(450, floorY - 100, 140, 20));
        obstacles.add(new Obstacle(650, floorY - 160, 100, 20));

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle("Gargoyle — obstacles + collisions (AABB)");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

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
        });

        final long[] last = { System.nanoTime() };
        new AnimationTimer() {
            @Override public void handle(long now) {
                double dt = (now - last[0]) / 1e9;
                last[0] = now;

                // Entrées → vitesse horizontale
                if (left && !right) gargoyle.vx = -moveSpeed;
                else if (right && !left) gargoyle.vx = moveSpeed;
                else gargoyle.vx = 0;

                // Saut (seulement si sur une surface)
                if (jump && gargoyle.onGround) {
                    gargoyle.vy = -jumpSpeed;
                }
                jump = false;

                // --- Physique axe par axe ---
                // 1) Vertical
                gargoyle.vy += gravity * dt;
                gargoyle.y  += gargoyle.vy * dt;
                resolveVertical(gargoyle, obstacles, floorY);

                // 2) Horizontal
                gargoyle.x  += gargoyle.vx * dt;
                resolveHorizontal(gargoyle, obstacles);

                // --- Rendu ---
                // ciel
                gc.setFill(Color.SKYBLUE);
                gc.fillRect(0, 0, WIDTH, HEIGHT);
                // sol
                gc.setFill(Color.DARKGREEN);
                gc.fillRect(0, floorY, WIDTH, HEIGHT - floorY);
                // plateformes
                for (Obstacle o : obstacles) o.draw(gc);
                // joueur
                gargoyle.draw(gc);
            }
        }.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

