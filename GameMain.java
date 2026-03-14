package ballcatcher;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class GameMain extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BALL_DIAMETER = 35;
    private static final int CATCHER_WIDTH = 130;
    private static final int CATCHER_HEIGHT = 20;

    private JPanel gamePanel;
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private Timer gameTimer;
    private Random random = new Random();

    private boolean gameOver = false;
    private boolean gameWon = false;

    private double currentBallSpeedY = 6.0;
    private int currentCatcherWidth = CATCHER_WIDTH;
    private int currentCatcherHeight = CATCHER_HEIGHT;
    private int catcherX;

    private ArrayList<Ball> balls = new ArrayList<>();
    private int framesSinceLastBall = 0;

    private int levelDisplayTimer = 0;
    private String levelMessage = "";

    private JButton restartButton;
    private JButton quitButton;

    // Star background
    private int[] starX = new int[50];
    private int[] starY = new int[50];
    private int[] starSpeed = new int[50];

    // Modern color palette
    private Color[] colors = {
            new Color(243, 139, 168), // Pink
            new Color(250, 179, 135), // Peach
            new Color(249, 226, 175), // Yellow
            new Color(166, 227, 161), // Green
            new Color(137, 180, 250), // Blue
            new Color(203, 166, 247), // Mauve
            new Color(245, 194, 231) // Flamingo
    };

    private class Ball {
        double x, y;
        int colorIndex;

        public Ball(double x, double y, int colorIndex) {
            this.x = x;
            this.y = y;
            this.colorIndex = colorIndex;
        }
    }

    public GameMain() {
        setTitle("Ball Catcher - Premium Edition");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Initialize stars
        for (int i = 0; i < starX.length; i++) {
            starX[i] = random.nextInt(WIDTH);
            starY[i] = random.nextInt(HEIGHT);
            starSpeed[i] = 1 + random.nextInt(3);
        }

        restartButton = createStyledButton("Restart", new Color(166, 227, 161));
        quitButton = createStyledButton("Quit", new Color(243, 139, 168));

        restartButton.setBounds(WIDTH / 2 - 130, HEIGHT / 2 + 30, 120, 45);
        quitButton.setBounds(WIDTH / 2 + 10, HEIGHT / 2 + 30, 120, 45);

        restartButton.addActionListener(e -> startGame());
        quitButton.addActionListener(e -> System.exit(0));

        restartButton.setVisible(false);
        quitButton.setVisible(false);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Deep space background
                GradientPaint bg = new GradientPaint(
                        0, 0, new Color(20, 20, 35),
                        0, getHeight(), new Color(9, 9, 15));
                g2d.setPaint(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw streaming stars
                g2d.setColor(new Color(255, 255, 255, 100));
                for (int i = 0; i < starX.length; i++) {
                    int size = starSpeed[i] == 3 ? 3 : 2;
                    g2d.fillOval(starX[i], starY[i], size, size);
                }

                // Inner glow / Play area border
                g2d.setColor(new Color(137, 180, 250, 150));
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 15, 15);
                g2d.setStroke(new BasicStroke(1));

                int catcherY = getHeight() - currentCatcherHeight - 30;

                // Draw sleek rounded Catcher
                GradientPaint catcherPaint = new GradientPaint(
                        catcherX, catcherY, new Color(137, 180, 250),
                        catcherX, catcherY + currentCatcherHeight, new Color(80, 110, 200));
                g2d.setPaint(catcherPaint);
                g2d.fill(new RoundRectangle2D.Double(catcherX, catcherY, currentCatcherWidth, currentCatcherHeight, 15,
                        15));
                // Catcher Highlight
                g2d.setColor(new Color(255, 255, 255, 120));
                g2d.draw(new RoundRectangle2D.Double(catcherX + 1, catcherY + 1, currentCatcherWidth - 2,
                        currentCatcherHeight - 2, 15, 15));

                if (!gameOver && !gameWon) {
                    // Draw 3D Balls
                    for (Ball b : balls) {
                        Color baseColor = colors[b.colorIndex % colors.length];
                        Color highlight = baseColor.brighter().brighter();

                        Point2D center = new Point2D.Double(b.x + BALL_DIAMETER / 3.0, b.y + BALL_DIAMETER / 3.0);
                        float radius = BALL_DIAMETER / 1.5f;
                        float[] dist = { 0.0f, 1.0f };
                        Color[] gradColors = { highlight, baseColor.darker() };

                        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, gradColors);
                        g2d.setPaint(rgp);
                        g2d.fillOval((int) b.x, (int) b.y, BALL_DIAMETER, BALL_DIAMETER);

                        // Outer rim
                        g2d.setColor(baseColor.darker().darker());
                        g2d.drawOval((int) b.x, (int) b.y, BALL_DIAMETER, BALL_DIAMETER);
                    }
                }

                // UI Overlay - Score & Level
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 26));
                drawTextWithShadow(g2d, "Score: " + score, 20, 35, Color.WHITE);
                drawTextWithShadow(g2d, "Level: " + level, 20, 70, new Color(249, 226, 175));

                // UI Overlay - Hearts
                int heartX = getWidth() - 150;
                int heartY = 15;
                for (int i = 0; i < lives; i++) {
                    drawHeart(g2d, heartX + (i * 35), heartY, 24, 24);
                }

                // Game Over & Win states
                if (gameOver || gameWon) {
                    // Darken screen
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 55));
                    String msg = gameWon ? "🏆 YOU WIN! 🏆" : "👾 GAME OVER 👾";
                    Color msgColor = gameWon ? new Color(249, 226, 175) : new Color(243, 139, 168);

                    FontMetrics fm = g2d.getFontMetrics();
                    int msgWidth = fm.stringWidth(msg);
                    drawTextWithShadow(g2d, msg, getWidth() / 2 - msgWidth / 2, getHeight() / 2 - 40, msgColor);
                } else if (levelDisplayTimer > 0) {
                    int alpha = Math.min(255, levelDisplayTimer * 4); // Smoother fade
                    Color overlay = new Color(255, 255, 255, alpha);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 55));
                    FontMetrics fm = g2d.getFontMetrics();
                    int msgWidth = fm.stringWidth(levelMessage);

                    g2d.setColor(new Color(0, 0, 0, alpha / 2));
                    g2d.drawString(levelMessage, getWidth() / 2 - msgWidth / 2 + 3, getHeight() / 2 + 3);
                    g2d.setColor(overlay);
                    g2d.drawString(levelMessage, getWidth() / 2 - msgWidth / 2, getHeight() / 2);
                }
            }
        };

        gamePanel.setLayout(null);
        gamePanel.add(restartButton);
        gamePanel.add(quitButton);

        gamePanel.setFocusable(true);
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    catcherX -= 40;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    catcherX += 40;
                }

                int w = gamePanel.getWidth();
                if (w == 0)
                    w = WIDTH;

                // Enforce borders strictly
                if (catcherX < 8) {
                    catcherX = 8;
                } else if (catcherX + currentCatcherWidth > w - 8) {
                    catcherX = w - currentCatcherWidth - 8;
                }

                gamePanel.repaint();
            }
        });

        gameTimer = new Timer(16, e -> updateGame());
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(new Color(30, 30, 46));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 46), 2, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(text, x + 2, y + 2);
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private void drawHeart(Graphics2D g2d, int x, int y, int width, int height) {
        int[] triangleX = { x - 2 * width / 18, x + width + 2 * width / 18, x + (width / 2) };
        int[] triangleY = { y + height - 2 * height / 3, y + height - 2 * height / 3, y + height };
        g2d.setColor(new Color(243, 139, 168)); // Premium red/pink
        g2d.fillOval(x - width / 12, y, width / 2 + width / 6, height / 2);
        g2d.fillOval(x + width / 2 - width / 12, y, width / 2 + width / 6, height / 2);
        g2d.fillPolygon(triangleX, triangleY, triangleX.length);
    }

    public void startGame() {
        score = 0;
        lives = 3;
        level = 1;
        gameOver = false;
        gameWon = false;

        currentCatcherWidth = CATCHER_WIDTH;
        currentCatcherHeight = CATCHER_HEIGHT;
        currentBallSpeedY = 6.0;

        catcherX = WIDTH / 2 - currentCatcherWidth / 2;

        levelMessage = "Level 1";
        levelDisplayTimer = 120;

        restartButton.setVisible(false);
        quitButton.setVisible(false);

        balls.clear();
        balls.add(createNewBall());
        framesSinceLastBall = 0;

        gameTimer.start();
        gamePanel.requestFocusInWindow();
        gamePanel.repaint();
    }

    private Ball createNewBall() {
        int w = gamePanel.getWidth();
        if (w <= 0)
            w = WIDTH;
        return new Ball(10 + random.nextInt(w - BALL_DIAMETER - 20), -BALL_DIAMETER, random.nextInt(colors.length));
    }

    private void endGame(boolean win) {
        gameOver = !win;
        gameWon = win;
        gameTimer.stop();
        restartButton.setVisible(true);
        quitButton.setVisible(true);
        gamePanel.repaint();
    }

    private void checkLevelUp() {
        if (score == 10 && level == 1) {
            level = 2;
            currentBallSpeedY = 9.0;
            int oldWidth = currentCatcherWidth;
            currentCatcherWidth = 90;
            currentCatcherHeight = 15;
            catcherX += (oldWidth - currentCatcherWidth) / 2;
            levelMessage = "Level 2!";
            levelDisplayTimer = 120;
        } else if (score == 20 && level == 2) {
            level = 3;
            currentBallSpeedY = 11.0;
            framesSinceLastBall = 0;
            levelMessage = "Level 3 - Multiple Balls!";
            levelDisplayTimer = 120;
        } else if (score == 30 && level == 3) {
            endGame(true);
        }
    }

    private void updateGame() {
        if (gameOver || gameWon)
            return;

        // Update star positions for dynamic background
        for (int i = 0; i < starY.length; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > gamePanel.getHeight()) {
                starY[i] = 0;
                starX[i] = random.nextInt(gamePanel.getWidth());
            }
        }

        if (levelDisplayTimer > 0) {
            levelDisplayTimer--;
            gamePanel.repaint();
            return;
        }

        if (level == 3) {
            framesSinceLastBall++;
            if (framesSinceLastBall > 45) { // spawn a new ball every ~0.72 seconds
                balls.add(createNewBall());
                framesSinceLastBall = 0;
            }
        }

        Iterator<Ball> iter = balls.iterator();
        boolean missed = false;
        int catcherY = gamePanel.getHeight() - currentCatcherHeight - 30;

        while (iter.hasNext()) {
            Ball b = iter.next();
            b.y += currentBallSpeedY;

            if (b.y > gamePanel.getHeight()) {
                iter.remove();
                missed = true;
            } else if (b.y + BALL_DIAMETER >= catcherY && b.y <= catcherY + currentCatcherHeight) {
                if (b.x + BALL_DIAMETER >= catcherX && b.x <= catcherX + currentCatcherWidth) {
                    score++;
                    iter.remove();
                    checkLevelUp();
                    if (gameWon)
                        return;
                }
            }
        }

        if (missed) {
            lives--;
            if (lives <= 0) {
                endGame(false);
                return;
            }
        }

        if (balls.isEmpty() && level != 3) {
            balls.add(createNewBall());
        }

        gamePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Set sleek system look & feel if possible
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            GameMain g = new GameMain();
            g.setVisible(true);
            g.startGame();
        });
    }
}