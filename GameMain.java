package ballcatcher;

import java.awt.*;
import java.awt.event.*;
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
    private Timer gameTimer;
    private Random random = new Random();

    private double ballX, ballY;
    private int catcherX;
    private boolean gameOver = false;
    private double ballSpeedY = 4.0;
    private double ballSpeedX = 0.0;

    private Rectangle restartBtnBounds;
    private Rectangle quitBtnBounds;

    private Color[] colors = {
        new Color(243, 139, 168),
        new Color(250, 179, 135),
        new Color(249, 226, 175),
        new Color(166, 227, 161),
        new Color(137, 180, 250),
        new Color(203, 166, 247),
        new Color(245, 194, 231)
    };

    private int colorIndex = 0;

    public GameMain() {
        setTitle("Ball Catcher - Premium Edition");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        gamePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bg = new GradientPaint(
                        0, 0, new Color(30, 30, 46),
                        0, getHeight(), new Color(17, 17, 27)
                );

                g2d.setPaint(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int catcherY = getHeight() - CATCHER_HEIGHT - 30;

                g2d.setColor(Color.BLUE);
                g2d.fillRect(catcherX, catcherY, CATCHER_WIDTH, CATCHER_HEIGHT);

                if (!gameOver) {
                    g2d.setColor(colors[colorIndex % colors.length]);
                    g2d.fillOval((int) ballX, (int) ballY, BALL_DIAMETER, BALL_DIAMETER);
                }

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.drawString("Score: " + score, 20, 30);

                g2d.drawString("Lives: " + lives, getWidth() - 120, 30);

                if (gameOver) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 40));
                    g2d.drawString("GAME OVER", getWidth() / 2 - 120, getHeight() / 2);
                }
            }
        };

        gamePanel.setFocusable(true);
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    catcherX -= 40;
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    catcherX += 40;
                }

                gamePanel.repaint();
            }
        });

        gameTimer = new Timer(16, e -> updateGame());
    }

    public void startGame() {
        score = 0;
        lives = 3;
        gameOver = false;

        catcherX = WIDTH / 2 - CATCHER_WIDTH / 2;

        resetBall();

        gameTimer.start();
    }

    private void resetBall() {

        ballX = random.nextInt(WIDTH - BALL_DIAMETER);
        ballY = 0;

        colorIndex = random.nextInt(colors.length);
    }

    private void updateGame() {

        if (gameOver) return;

        ballY += ballSpeedY;

        int catcherY = gamePanel.getHeight() - CATCHER_HEIGHT - 30;

        if (ballY + BALL_DIAMETER >= catcherY) {

            if (ballX >= catcherX &&
                    ballX <= catcherX + CATCHER_WIDTH) {

                score++;
                resetBall();
            } else {

                lives--;

                if (lives <= 0) {
                    gameOver = true;
                    gameTimer.stop();
                } else {
                    resetBall();
                }
            }
        }

        gamePanel.repaint();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            GameMain g = new GameMain();
            g.setVisible(true);
            g.startGame();

        });
    }
}