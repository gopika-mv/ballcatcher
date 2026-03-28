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

    public enum GameState {
        MENU, PLAYING, GAME_OVER, WIN
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private GameState currentState = GameState.MENU;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    private JPanel gamePanel;
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private Timer gameTimer;
    private Random random = new Random();

    private double currentBallSpeedY = 6.0;
    private int currentCatcherWidth = 130;
    private int currentCatcherHeight = 20;
    private int catcherX;

    private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<FloatingText> floatingTexts = new ArrayList<>();
    
    private int framesSinceLastBall = 0;
    private int shakeFrames = 0;

    private int levelDisplayTimer = 0;
    private String levelMessage = "";

    // Menu Buttons
    private JButton easyButton;
    private JButton mediumButton;
    private JButton hardButton;
    private JButton quitMenuButton;

    // Game Over Buttons
    private JButton restartButton;
    private JButton mainMenuButton;

    // Star background
    private int[] starX = new int[100];
    private int[] starY = new int[100];
    private int[] starSpeed = new int[100];
    private int[] starSize = new int[100];

    // Modern color palette
    private Color[] colors = {
            new Color(243, 139, 168), // Pink
            new Color(250, 179, 135), // Peach
            new Color(249, 226, 175), // Yellow
            new Color(166, 227, 161), // Green
            new Color(137, 180, 250), // Blue
            new Color(203, 166, 247), // Mauve
            new Color(245, 194, 231)  // Flamingo
    };

    private class Ball {
        double x, y;
        int colorIndex;
        ArrayList<Point2D.Double> trail = new ArrayList<>();

        public Ball(double x, double y, int colorIndex) {
            this.x = x;
            this.y = y;
            this.colorIndex = colorIndex;
        }
    }

    private class Particle {
        double x, y, vx, vy;
        int life, maxLife;
        Color color;

        public Particle(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.vx = (random.nextDouble() - 0.5) * 15;
            this.vy = (random.nextDouble() - 0.5) * 15;
            this.maxLife = 20 + random.nextInt(25);
            this.life = this.maxLife;
        }
    }

    private class FloatingText {
        double x, y;
        String text;
        int life, maxLife;
        Color color;

        public FloatingText(String text, double x, double y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.maxLife = 45;
            this.life = this.maxLife;
        }
    }

    public GameMain() {
        setTitle("Ball Catcher - Premium Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Initialize stars
        for (int i = 0; i < starX.length; i++) {

            starX[i] = random.nextInt(WIDTH);
            starY[i] = random.nextInt(HEIGHT);
            starSpeed[i] = 1 + random.nextInt(5);
            starSize[i] = random.nextInt(3) + 1;
        }

        // Initialize Buttons
        easyButton = createStyledButton("EASY", new Color(166, 227, 161));
        mediumButton = createStyledButton("MEDIUM", new Color(249, 226, 175));
        hardButton = createStyledButton("HARD", new Color(243, 139, 168));
        quitMenuButton = createStyledButton("QUIT", new Color(200, 200, 200));

        restartButton = createStyledButton("RESTART", new Color(166, 227, 161));
        mainMenuButton = createStyledButton("MAIN MENU", new Color(137, 180, 250));

        // Layout them in the center
        int btnWidth = 200;
        int btnHeight = 55;
        int centerX = WIDTH / 2 - btnWidth / 2;

        easyButton.setBounds(centerX, 250, btnWidth, btnHeight);
        mediumButton.setBounds(centerX, 320, btnWidth, btnHeight);
        hardButton.setBounds(centerX, 390, btnWidth, btnHeight);
        quitMenuButton.setBounds(centerX, 460, btnWidth, btnHeight);

        restartButton.setBounds(centerX - 120, 400, btnWidth, btnHeight);
        mainMenuButton.setBounds(centerX + 120, 400, btnWidth, btnHeight);

        // Action Listeners
        easyButton.addActionListener(e -> startGame(Difficulty.EASY));
        mediumButton.addActionListener(e -> startGame(Difficulty.MEDIUM));
        hardButton.addActionListener(e -> startGame(Difficulty.HARD));
        quitMenuButton.addActionListener(e -> System.exit(0));

        restartButton.addActionListener(e -> startGame(currentDifficulty));
        mainMenuButton.addActionListener(e -> goToMenu());

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Deep space background
                GradientPaint bg = new GradientPaint(
                        0, 0, new Color(15, 15, 30),
                        0, getHeight(), new Color(5, 5, 12));
                g2d.setPaint(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Screen shake effect setup
                int shakeOffsetX = 0;
                int shakeOffsetY = 0;
                if (shakeFrames > 0 && currentState == GameState.PLAYING) {
                    shakeOffsetX = (random.nextInt(3) - 1) * 8;
                    shakeOffsetY = (random.nextInt(3) - 1) * 8;
                }
                g2d.translate(shakeOffsetX, shakeOffsetY);

                // Draw streaming stars
                for (int i = 0; i < starX.length; i++) {
                    int size = starSize[i];
                    int alpha = 100 + random.nextInt(155); // twinkling effect
                    g2d.setColor(new Color(255, 255, 255, alpha));
                    g2d.fillOval(starX[i], starY[i], size, size);
                }

                if (currentState == GameState.MENU) {
                    // Draw Main Menu Overlay
                    g2d.translate(-shakeOffsetX, -shakeOffsetY); // undo shake for UI
                    
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 75));
                    String title = "BALL CATCHER";
                    FontMetrics fm = g2d.getFontMetrics();
                    int tw = fm.stringWidth(title);
                    
                    // Title shadow
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.drawString(title, getWidth() / 2 - tw / 2 + 5, 155);
                    
                    // Title gradient
                    GradientPaint tp = new GradientPaint(
                            0, 100, new Color(137, 180, 250),
                            0, 180, new Color(245, 194, 231));
                    g2d.setPaint(tp);
                    g2d.drawString(title, getWidth() / 2 - tw / 2, 150);
                    
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 22));
                    String subtitle = "Select your difficulty to start";
                    g2d.setColor(Color.WHITE);
                    int stw = g2d.getFontMetrics().stringWidth(subtitle);
                    g2d.drawString(subtitle, getWidth() / 2 - stw / 2, 210);

                    g2d.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                    String hint = "Hint: Move your MOUSE or use ARROW KEYS to catch the balls!";
                    g2d.setColor(new Color(200, 200, 200));
                    int hintW = g2d.getFontMetrics().stringWidth(hint);
                    g2d.drawString(hint, getWidth() / 2 - hintW / 2, getHeight() - 50);

                    return;
                }

                // --- PLAYING / GAME OVER / WIN STATES ---

                // Inner glow / Play area border
                g2d.setColor(new Color(137, 180, 250, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(6, 6, getWidth() - 12 - shakeOffsetX, getHeight() - 12 - shakeOffsetY, 15, 15);
                g2d.setStroke(new BasicStroke(1));

                int catcherY = getHeight() - currentCatcherHeight - 30;

                // Draw sleek rounded Catcher
                GradientPaint catcherPaint = new GradientPaint(
                        catcherX, catcherY, new Color(137, 180, 250),
                        catcherX, catcherY + currentCatcherHeight, new Color(60, 90, 180));
                g2d.setPaint(catcherPaint);
                
                // Catcher Glow
                g2d.setColor(new Color(137, 180, 250, 60));
                g2d.fill(new RoundRectangle2D.Double(catcherX - 5, catcherY - 5, currentCatcherWidth + 10, currentCatcherHeight + 10, 25, 25));

                g2d.setPaint(catcherPaint);
                g2d.fill(new RoundRectangle2D.Double(catcherX, catcherY, currentCatcherWidth, currentCatcherHeight, 15, 15));
                
                // Catcher Highlight
                g2d.setColor(new Color(255, 255, 255, 140));
                g2d.draw(new RoundRectangle2D.Double(catcherX + 1, catcherY + 1, currentCatcherWidth - 2, currentCatcherHeight - 2, 15, 15));

                if (currentState == GameState.PLAYING || currentState == GameState.GAME_OVER) {
                    // Draw 3D Balls with Trails
                    for (Ball b : balls) {
                        Color baseColor = colors[b.colorIndex % colors.length];
                        
                        // Draw trail
                        for (int i = 0; i < b.trail.size(); i++) {
                            Point2D.Double p = b.trail.get(i);
                            float alpha = (float)(i + 1) / (b.trail.size() + 1);
                            int trailSize = (int)(BALL_DIAMETER * 0.75 * alpha);
                            g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(alpha * 120)));
                            g2d.fillOval((int)p.x + (BALL_DIAMETER - trailSize)/2, (int)p.y + (BALL_DIAMETER - trailSize)/2, trailSize, trailSize);
                        }

                        // Draw Ball
                        Color highlight = baseColor.brighter().brighter();
                        Point2D center = new Point2D.Double(b.x + BALL_DIAMETER / 3.0, b.y + BALL_DIAMETER / 3.0);
                        float radius = BALL_DIAMETER / 1.5f;
                        float[] dist = { 0.0f, 1.0f };
                        Color[] gradColors = { highlight, baseColor.darker() };

                        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, gradColors);
                        g2d.setPaint(rgp);
                        g2d.fillOval((int) b.x, (int) b.y, BALL_DIAMETER, BALL_DIAMETER);

                        // Ball Outer rim
                        g2d.setColor(baseColor.darker().darker());
                        g2d.drawOval((int) b.x, (int) b.y, BALL_DIAMETER, BALL_DIAMETER);
                    }
                }

                // Draw Particles
                for (Particle p : particles) {
                    float alpha = Math.max(0, (float) p.life / p.maxLife);
                    g2d.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (alpha * 255)));
                    int size = (int) (14 * alpha);
                    g2d.fillOval((int) p.x, (int) p.y, size, size);
                }

                // Restore translation before drawing UI to keep UI stable
                g2d.translate(-shakeOffsetX, -shakeOffsetY);

                // Draw Floating Texts
                for (FloatingText ft : floatingTexts) {
                    float alpha = Math.max(0, (float) ft.life / ft.maxLife);
                    g2d.setColor(new Color(ft.color.getRed(), ft.color.getGreen(), ft.color.getBlue(), (int) (alpha * 255)));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 26));
                    g2d.drawString(ft.text, (int) ft.x, (int) ft.y);
                }

                // Top UI Overlay - Score & Level
                g2d.setFont(new Font("Consolas", Font.BOLD, 28));
                drawTextWithShadow(g2d, "SCORE: " + score, 25, 40, Color.WHITE);
                drawTextWithShadow(g2d, "LEVEL: " + level, 25, 75, new Color(249, 226, 175));
                drawTextWithShadow(g2d, currentDifficulty.name() + " MODE", 25, 110, new Color(137, 180, 250));

                // Top UI Overlay - Hearts
                int maxLives = (currentDifficulty == Difficulty.EASY) ? 5 : ((currentDifficulty == Difficulty.MEDIUM) ? 3 : 2);
                int heartX = getWidth() - (maxLives * 40) - 20;
                int heartY = 20;
                for (int i = 0; i < maxLives; i++) {
                    if (i < lives) {
                        drawHeart(g2d, heartX + (i * 40), heartY, 28, 28, true);
                    } else {
                        drawHeart(g2d, heartX + (i * 40), heartY, 28, 28, false);
                    }
                }

                // Game Over & Win Screen Dimming and Texts
                if (currentState == GameState.GAME_OVER || currentState == GameState.WIN) {
                    // Darken screen
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 70));
                    String msg = currentState == GameState.WIN ? "Y O U   W I N" : "G A M E   O V E R";
                    Color msgColor = currentState == GameState.WIN ? new Color(249, 226, 175) : new Color(243, 139, 168);

                    FontMetrics fm = g2d.getFontMetrics();
                    int msgWidth = fm.stringWidth(msg);
                    drawTextWithShadow(g2d, msg, getWidth() / 2 - msgWidth / 2, getHeight() / 2 - 80, msgColor);
                    
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 32));
                    String subMsg = "Final Score: " + score;
                    int subMsgWidth = g2d.getFontMetrics().stringWidth(subMsg);
                    drawTextWithShadow(g2d, subMsg, getWidth() / 2 - subMsgWidth / 2, getHeight() / 2 - 10, Color.WHITE);
                } else if (levelDisplayTimer > 0 && currentState == GameState.PLAYING) {
                    // Level transition text
                    int alpha = Math.min(255, levelDisplayTimer * 4); // Smoother fade
                    float scale = 1.0f + (120 - levelDisplayTimer) * 0.005f; // Slight zoom effect
                    
                    Color overlay = new Color(255, 255, 255, alpha);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, (int)(60 * scale)));
                    FontMetrics fm = g2d.getFontMetrics();
                    int msgWidth = fm.stringWidth(levelMessage);

                    g2d.setColor(new Color(0, 0, 0, alpha / 2));
                    g2d.drawString(levelMessage, getWidth() / 2 - msgWidth / 2 + 5, getHeight() / 2 + 5);
                    g2d.setColor(overlay);
                    g2d.drawString(levelMessage, getWidth() / 2 - msgWidth / 2, getHeight() / 2);
                }
            }
        };

        gamePanel.setLayout(null);
        gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        gamePanel.add(easyButton);
        gamePanel.add(mediumButton);
        gamePanel.add(hardButton);
        gamePanel.add(quitMenuButton);
        gamePanel.add(restartButton);
        gamePanel.add(mainMenuButton);

        gamePanel.setFocusable(true);
        add(gamePanel, BorderLayout.CENTER);
        
        pack(); // Packs window exactly to the canvas size
        setLocationRelativeTo(null); // Centers window on screen

        // Controls
        gamePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentState == GameState.PLAYING) {
                    catcherX = e.getX() - currentCatcherWidth / 2;
                    int w = gamePanel.getWidth();
                    if (w == 0) w = WIDTH;
                    if (catcherX < 8) catcherX = 8;
                    else if (catcherX + currentCatcherWidth > w - 8) catcherX = w - currentCatcherWidth - 8;
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }
        });

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentState != GameState.PLAYING) return;
                
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

        // Initialize Menu State
        goToMenu();

        gameTimer = new Timer(16, e -> updateGame());
        gameTimer.start(); // Run timer continuously for particles and stars
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setBackground(baseColor);
        btn.setForeground(new Color(20, 20, 30)); // Dark text for visibility
        btn.setFocusPainted(false);
        // Force opaque drawing
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(baseColor.brighter());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
            }
        });
        return btn;
    }

    private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(text, x + 3, y + 3);
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private void drawHeart(Graphics2D g2d, int x, int y, int width, int height, boolean active) {
        int[] triangleX = { x - 2 * width / 18, x + width + 2 * width / 18, x + (width / 2) };
        int[] triangleY = { y + height - 2 * height / 3, y + height - 2 * height / 3, y + height };
        
        if (active) {
            g2d.setColor(new Color(243, 139, 168)); // Premium red/pink
        } else {
            g2d.setColor(new Color(80, 80, 100)); // Dark offline heart
        }
        
        g2d.fillOval(x - width / 12, y, width / 2 + width / 6, height / 2);
        g2d.fillOval(x + width / 2 - width / 12, y, width / 2 + width / 6, height / 2);
        g2d.fillPolygon(triangleX, triangleY, triangleX.length);
        
        if (active) {
            // Little highlight
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillOval(x + 2, y + 3, width / 4, height / 4);
        }
    }

    private void hideAllButtons() {
        easyButton.setVisible(false);
        mediumButton.setVisible(false);
        hardButton.setVisible(false);
        quitMenuButton.setVisible(false);
        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);
    }

    private void showMenuButtons() {
        easyButton.setVisible(true);
        mediumButton.setVisible(true);
        hardButton.setVisible(true);
        quitMenuButton.setVisible(true);
    }

    private void showGameOverButtons() {
        restartButton.setVisible(true);
        mainMenuButton.setVisible(true);
    }

    public void goToMenu() {
        currentState = GameState.MENU;
        hideAllButtons();
        showMenuButtons();
        setCursor(Cursor.getDefaultCursor());
        balls.clear();
        particles.clear();
        floatingTexts.clear();
        gamePanel.repaint();
    }

    public void startGame(Difficulty diff) {
        currentDifficulty = diff;
        currentState = GameState.PLAYING;
        score = 0;
        level = 1;
        shakeFrames = 0;

        switch (diff) {
            case EASY:
                lives = 5;
                currentCatcherWidth = 160;
                currentBallSpeedY = 3.0; // Slowed down from 4.0
                break;
            case MEDIUM:
                lives = 3;
                currentCatcherWidth = 130;
                currentBallSpeedY = 4.5; // Slowed down from 6.0
                break;
            case HARD:
                lives = 2;
                currentCatcherWidth = 90;
                currentBallSpeedY = 7.0; // Slowed down from 8.5
                break;
        }

        catcherX = WIDTH / 2 - currentCatcherWidth / 2;

        levelMessage = "L E V E L   1";
        levelDisplayTimer = 120;

        hideAllButtons();

        balls.clear();
        particles.clear();
        floatingTexts.clear();
        balls.add(createNewBall());
        framesSinceLastBall = 0;

        gamePanel.requestFocusInWindow();
        
        // Hide standard cursor during gameplay
        setCursor(getToolkit().createCustomCursor(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB), new Point(), null));
        gamePanel.repaint();
    }

    private Ball createNewBall() {
        int w = gamePanel.getWidth();
        if (w <= 0)
            w = WIDTH;
        return new Ball(10 + random.nextInt(w - BALL_DIAMETER - 20), -BALL_DIAMETER * 2, random.nextInt(colors.length));
    }

    private void endGame(boolean win) {
        currentState = win ? GameState.WIN : GameState.GAME_OVER;
        showGameOverButtons();
        setCursor(Cursor.getDefaultCursor());
        gamePanel.repaint();
    }

    private void createExplosion(double x, double y, Color color) {
        for (int i = 0; i < 25; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    private void checkLevelUp() {
        if (score == 10 && level == 1) {
            level = 2;
            currentBallSpeedY += 2.0;
            int oldWidth = currentCatcherWidth;
            currentCatcherWidth = Math.max(70, currentCatcherWidth - 20); // Make catcher smaller
            catcherX += (oldWidth - currentCatcherWidth) / 2;
            levelMessage = "L E V E L   2 !";
            levelDisplayTimer = 120;
        } else if (score == 20 && level == 2) {
            level = 3;
            currentBallSpeedY += 2.0;
            framesSinceLastBall = 0;
            levelMessage = "L E V E L   3 !";
            levelDisplayTimer = 120;
        } else if (score == 30 && level == 3) {
            endGame(true);
        }
    }

    private void updateGame() {
        // Always update stars
        for (int i = 0; i < starY.length; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > gamePanel.getHeight()) {
                starY[i] = 0;
                starX[i] = random.nextInt(gamePanel.getWidth());
            }
        }

        // Always update particles and floating text lengths
        updateParticlesAndTexts();
        
        if (currentState != GameState.PLAYING) {
            gamePanel.repaint();
            return;
        }

        if (shakeFrames > 0) shakeFrames--;

        if (levelDisplayTimer > 0) {
            levelDisplayTimer--;
            gamePanel.repaint();
            return;
        }

        if (level == 3) {
            framesSinceLastBall++;
            int spawnRate = (currentDifficulty == Difficulty.HARD) ? 35 : ((currentDifficulty == Difficulty.EASY) ? 55 : 45);
            if (framesSinceLastBall > spawnRate) { 
                balls.add(createNewBall());
                framesSinceLastBall = 0;
            }
        }

        Iterator<Ball> iter = balls.iterator();
        boolean missed = false;
        int catcherY = gamePanel.getHeight() - currentCatcherHeight - 30;

        while (iter.hasNext()) {
            Ball b = iter.next();
            
            // update trail
            b.trail.add(new Point2D.Double(b.x, b.y));
            if (b.trail.size() > 8) {
                b.trail.remove(0);
            }

            b.y += currentBallSpeedY;

            if (b.y > gamePanel.getHeight()) {
                iter.remove();
                missed = true;
            } else if (b.y + BALL_DIAMETER >= catcherY && b.y <= catcherY + currentCatcherHeight) {
                if (b.x + BALL_DIAMETER >= catcherX && b.x <= catcherX + currentCatcherWidth) {
                    score++;
                    Color ballColor = colors[b.colorIndex % colors.length];
                    createExplosion(b.x + BALL_DIAMETER/2.0, b.y + BALL_DIAMETER/2.0, ballColor);
                    floatingTexts.add(new FloatingText("+1", b.x, b.y, ballColor));
                    iter.remove();
                    checkLevelUp();
                    if (currentState == GameState.WIN)
                        return;
                }
            }
        }

        if (missed) {
            lives--;
            shakeFrames = 25; // Screen shake on miss
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

    private void updateParticlesAndTexts() {
        Iterator<Particle> pIter = particles.iterator();
        while (pIter.hasNext()) {
            Particle p = pIter.next();
            p.x += p.vx;
            p.y += p.vy;
            p.life--;
            if (p.life <= 0) pIter.remove();
        }

        Iterator<FloatingText> ftIter = floatingTexts.iterator();
        while(ftIter.hasNext()) {
            FloatingText ft = ftIter.next();
            ft.y -= 2.0; // float up faster
            ft.life--;
            if (ft.life <= 0) ftIter.remove();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use CrossPlatform L&F so Button background colors work explicitly
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            GameMain g = new GameMain();
            g.setVisible(true);
        });
    }
}
