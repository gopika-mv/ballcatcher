package ballcatcher;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
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
    private int lives = 3; // 3 Lifelines
    private Timer gameTimer;
    private Random random = new Random();
    
    // Game state
    private double ballX, ballY;
    private int catcherX;
    private boolean gameOver = false;
    private double ballSpeedY = 4.0;
    private double ballSpeedX = 0.0;
    
    // Buttons state
    private Rectangle restartBtnBounds;
    private Rectangle quitBtnBounds;
    
    // Animations & Color Palette
    private Color[] colors = {
        new Color(243, 139, 168), // Red
        new Color(250, 179, 135), // Peach
        new Color(249, 226, 175), // Yellow
        new Color(166, 227, 161), // Green
        new Color(137, 180, 250), // Blue
        new Color(203, 166, 247), // Mauve
        new Color(245, 194, 231)  // Pink
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
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // 1. Premium Anti-aliasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // 2. Draw modern gradient background (Dark Mode)
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(30, 30, 46), 
                    0, getHeight(), new Color(17, 17, 27)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int catcherY = getHeight() - CATCHER_HEIGHT - 30;

                // 3. Draw Catcher (Shadow, Gradient, and Highlight)
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRoundRect(catcherX + 5, catcherY + 5, CATCHER_WIDTH, CATCHER_HEIGHT, 20, 20); // Shadow
                
                GradientPaint catcherGradient = new GradientPaint(
                    catcherX, catcherY, new Color(137, 180, 250), 
                    catcherX, catcherY + CATCHER_HEIGHT, new Color(116, 199, 236)
                );
                g2d.setPaint(catcherGradient);
                g2d.fillRoundRect(catcherX, catcherY, CATCHER_WIDTH, CATCHER_HEIGHT, 20, 20); // Main Body
                
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.drawRoundRect(catcherX, catcherY, CATCHER_WIDTH, CATCHER_HEIGHT, 20, 20); // Highlight Border

                // 4. Draw Ball
                if (!gameOver) {
                    // Ball shadow
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.fillOval((int)ballX + 7, (int)ballY + 7, BALL_DIAMETER, BALL_DIAMETER);
                    
                    // Ball Pseudo-3D Gradient
                    Point2D center = new Point2D.Float((float)ballX + BALL_DIAMETER * 0.35f, (float)ballY + BALL_DIAMETER * 0.35f);
                    float radius = BALL_DIAMETER * 0.7f;
                    float[] dist = {0.0f, 1.0f};
                    
                    Color mainColor = colors[colorIndex % colors.length];
                    Color lighterColor = new Color(
                        Math.min(255, mainColor.getRed() + 80),
                        Math.min(255, mainColor.getGreen() + 80),
                        Math.min(255, mainColor.getBlue() + 80)
                    );
                    
                    Color[] gradientColors = {lighterColor, mainColor};
                    RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, gradientColors);
                    
                    g2d.setPaint(rgp);
                    g2d.fillOval((int)ballX, (int)ballY, BALL_DIAMETER, BALL_DIAMETER);
                }

                // 5. HD Score Text
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 34));
                String scoreText = "Score: " + score;
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(scoreText, 24, 44);
                // Text
                g2d.setColor(new Color(166, 227, 161)); // Pastel Green
                g2d.drawString(scoreText, 20, 40);

                // 6. Draw Lifelines (Hearts)
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
                for (int i = 0; i < lives; i++) {
                    // Heart Shadow
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.drawString("❤", getWidth() - 46 - (i * 40), 44);
                    // Heart Icon
                    g2d.setColor(new Color(243, 139, 168)); // Red/Pink
                    g2d.drawString("❤", getWidth() - 50 - (i * 40), 40);
                }

                // 7. Game Over Screen
                if (gameOver) {
                    drawGameOverScreen(g2d, getWidth(), getHeight());
                }
            }
        };
        
        gamePanel.setBackground(Color.BLACK);
        gamePanel.setFocusable(true);
        add(gamePanel, BorderLayout.CENTER);

        // Keyboard Controls
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        catcherX -= 40;
                        if (catcherX < 0) catcherX = 0;
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        catcherX += 40;
                        if (catcherX > gamePanel.getWidth() - CATCHER_WIDTH) {
                            catcherX = gamePanel.getWidth() - CATCHER_WIDTH;
                        }
                    }
                } else {
                    if (e.getKeyCode() == KeyEvent.VK_Q) {
                        System.exit(0);
                    }
                }
                gamePanel.repaint();
            }
        });

        // Smooth Mouse Tracking Control
        gamePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!gameOver) {
                    catcherX = e.getX() - CATCHER_WIDTH / 2;
                    if (catcherX < 0) catcherX = 0;
                    if (catcherX > gamePanel.getWidth() - CATCHER_WIDTH) {
                        catcherX = gamePanel.getWidth() - CATCHER_WIDTH;
                    }
                } else if (gameOver) {
                    // If mouse moves over buttons, we can trigger repaints for hovers, but let's keep it simple
                    gamePanel.repaint();
                }
            }
        });
        
        // Clicks mapping for simulated buttons
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) {
                    Point p = e.getPoint();
                    if (restartBtnBounds != null && restartBtnBounds.contains(p)) {
                        startGame();
                    } else if (quitBtnBounds != null && quitBtnBounds.contains(p)) {
                        System.exit(0);
                    }
                } else {
                    gamePanel.requestFocusInWindow();
                }
            }
        });
        
        // 60 FPS Game Loop Update
        gameTimer = new Timer(16, e -> updateGame());
    }
    
    private void drawGameOverScreen(Graphics2D g2d, int width, int height) {
        // Dark Overlay
        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRect(0, 0, width, height);

        // Title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 72));
        String msg = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int msgWidth = fm.stringWidth(msg);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(msg, (width - msgWidth) / 2 + 3, height / 2 - 87);
        g2d.setColor(new Color(243, 139, 168)); // Red/Pink
        g2d.drawString(msg, (width - msgWidth) / 2, height / 2 - 90);
        
        // Final Score
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
        String scoreMsg = "Final Score: " + score;
        int scoreWidth = g2d.getFontMetrics().stringWidth(scoreMsg);
        g2d.setColor(Color.WHITE);
        g2d.drawString(scoreMsg, (width - scoreWidth) / 2, height / 2 - 20);
        
        // Dimensions for simulated buttons
        int btnWidth = 220;
        int btnHeight = 60;
        int btnY = height / 2 + 30;
        
        int gap = 30;
        int restartX = (width - (btnWidth * 2 + gap)) / 2;
        int quitX = restartX + btnWidth + gap;
        
        restartBtnBounds = new Rectangle(restartX, btnY, btnWidth, btnHeight);
        quitBtnBounds = new Rectangle(quitX, btnY, btnWidth, btnHeight);
        
        // Helper block to draw the simulated buttons
        Point mousePos = gamePanel.getMousePosition();

        // 1. RESTART BUTTON
        boolean restartHover = mousePos != null && restartBtnBounds.contains(mousePos);
        drawButton(g2d, "Restart", restartX, btnY, btnWidth, btnHeight, 
                   new Color(166, 227, 161), restartHover);
        
        // 2. QUIT BUTTON
        boolean quitHover = mousePos != null && quitBtnBounds.contains(mousePos);
        drawButton(g2d, "Quit", quitX, btnY, btnWidth, btnHeight, 
                   new Color(243, 139, 168), quitHover);
    }
    
    private void drawButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color baseColor, boolean isHover) {
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(x + 5, y + 5, w, h, 20, 20);
        
        if (isHover) {
            // Brighter hover state
            g2d.setColor(baseColor.brighter());
            gamePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            g2d.setColor(baseColor);
            gamePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        
        // Button Text
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2d.setColor(new Color(30, 30, 46));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textY = y + ((h - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x + (w - textWidth) / 2, textY);
    }

    public void startGame() {
        score = 0;
        lives = 3; // Reset lives parameter!
        gameOver = false;
        ballSpeedY = 4.0;
        ballSpeedX = 0;
        
        if (gamePanel.getWidth() > 0) {
            catcherX = gamePanel.getWidth() / 2 - CATCHER_WIDTH / 2;
        } else {
            catcherX = WIDTH / 2 - CATCHER_WIDTH / 2;
        }
        
        resetBall();
        gameTimer.start();
        gamePanel.requestFocusInWindow();
    }

    private void resetBall() {
        int panelWidth = gamePanel.getWidth() > 0 ? gamePanel.getWidth() : WIDTH;
        ballX = random.nextInt(panelWidth - BALL_DIAMETER);
        ballY = -BALL_DIAMETER;
        
        // Add horizontal drift coefficient for a fun unpredictable ball movement
        ballSpeedX = (random.nextDouble() - 0.5) * 4.0;
        
        // Select a new random color for aesthetic visuals!
        colorIndex = random.nextInt(colors.length);
    }

    private void updateGame() {
        if (gameOver) return;

        ballY += ballSpeedY;
        ballX += ballSpeedX;
        
        int panelWidth = gamePanel.getWidth();
        // Dynamic horizontal bouncing logic
        if (ballX < 0) {
            ballX = 0;
            ballSpeedX = Math.abs(ballSpeedX); // bounce right
        } else if (ballX > panelWidth - BALL_DIAMETER) {
            ballX = panelWidth - BALL_DIAMETER;
            ballSpeedX = -Math.abs(ballSpeedX); // bounce left
        }

        int catcherY = gamePanel.getHeight() - CATCHER_HEIGHT - 30;

        // Collision logic
        if (ballY + BALL_DIAMETER >= catcherY && ballY <= catcherY + CATCHER_HEIGHT) {
            if (ballX + BALL_DIAMETER >= catcherX && ballX <= catcherX + CATCHER_WIDTH) {
                // Ball Caught!
                score++;
                
                // Increase difficulty progressively (Speed up falling rate slightly)
                if (score % 3 == 0) {
                    ballSpeedY += 0.8;
                }
                resetBall();
            }
        } 
        
        // Ball missed!
        if (ballY > gamePanel.getHeight()) {
            lives--; // Decrement life
            if (lives <= 0) {
                gameOver = true;
                gameTimer.stop();
            } else {
                resetBall(); // Bring a new ball down
            }
        }

        gamePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameMain gm = new GameMain();
            gm.setVisible(true);
            gm.startGame();
        });
    }
}
