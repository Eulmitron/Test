import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class SierraTroutQuest extends JPanel implements ActionListener, KeyListener {
    static final int WIN_WIDTH = 640;
    static final int WIN_HEIGHT = 480;
    static final int TILE_SIZE = 32;

    static final int START = 0;
    static final int MAP = 1;
    static final int FISHING = 2;
    static final int FIGHT = 3;

    static class Level {
        String name;
        String weather;
        Map<String, Double> fish;
        Level(String name, String weather, Map<String, Double> fish) {
            this.name = name;
            this.weather = weather;
            this.fish = fish;
        }
    }

    private static Map<String, Double> fishMap(Object... args) {
        Map<String, Double> m = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            m.put((String) args[i], (Double) args[i + 1]);
        }
        return m;
    }

    Level[] LEVELS = new Level[] {
        new Level("River", "Clear", fishMap("Rainbow Trout", 0.5, "Brown Trout", 0.3, "Brook Trout", 0.2)),
        new Level("Mountain Stream", "Cold", fishMap("Rainbow Trout", 0.4, "Brown Trout", 0.4, "Golden Trout", 0.2)),
        new Level("High Lake", "Stormy", fishMap("Cutthroat Trout", 0.5, "Rainbow Trout", 0.3, "Brown Trout", 0.2))
    };

    Map<String, Double> RODS = new HashMap<>();
    Map<String, Map<String, Double>> FLIES = new HashMap<>();

    int state = START;
    int selectedLevel = 0;
    Level currentLevel = null;
    Point playerPos = new Point(WIN_WIDTH/2, WIN_HEIGHT/2);
    Map<String, Integer> fishInventory = new HashMap<>();
    String rod = "Basic Rod";
    String fly = "Dry Fly";

    String fightFish = null;
    int fightProgress = 0;
    int fightStrength = 0;

    boolean left, right, up, down;

    javax.swing.Timer timer;

    public SierraTroutQuest() {
        setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        RODS.put("Basic Rod", 1.0);
        RODS.put("Pro Rod", 1.2);
        Map<String, Double> dry = new HashMap<>();
        dry.put("Rainbow Trout", 0.1);
        FLIES.put("Dry Fly", dry);
        Map<String, Double> nymph = new HashMap<>();
        nymph.put("Brown Trout", 0.1);
        FLIES.put("Nymph", nymph);

        timer = new javax.swing.Timer(16, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == FISHING) {
            if (left) playerPos.x -= 3;
            if (right) playerPos.x += 3;
            if (up) playerPos.y -= 3;
            if (down) playerPos.y += 3;
            playerPos.x = Math.max(0, Math.min(WIN_WIDTH - TILE_SIZE, playerPos.x));
            playerPos.y = Math.max(0, Math.min(WIN_HEIGHT - TILE_SIZE, playerPos.y));
        } else if (state == FIGHT) {
            fightProgress -= fightStrength;
            if (fightProgress >= 100) {
                fishInventory.merge(fightFish, 1, Integer::sum);
                state = FISHING;
            } else if (fightProgress <= 0) {
                state = FISHING;
            }
        }
        repaint();
    }

    private void attemptFish() {
        Map<String, Double> probs = new LinkedHashMap<>(currentLevel.fish);
        for (Map.Entry<String, Double> entry : FLIES.getOrDefault(fly, Collections.emptyMap()).entrySet()) {
            probs.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
        double rodMod = RODS.getOrDefault(rod, 1.0);
        double total = 0;
        for (double v : probs.values()) total += v;
        double r = Math.random() * total * rodMod;
        double cum = 0;
        for (Map.Entry<String, Double> entry : probs.entrySet()) {
            cum += entry.getValue();
            if (r <= cum) {
                fightFish = entry.getKey();
                break;
            }
        }
        fightProgress = 50;
        fightStrength = 1 + (int)(Math.random()*3);
        state = FIGHT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == START) {
            drawStart(g);
        } else if (state == MAP) {
            drawMap(g);
        } else if (state == FISHING) {
            drawFishing(g);
        } else if (state == FIGHT) {
            drawFight(g);
        }
    }

    private void drawStart(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,WIN_WIDTH,WIN_HEIGHT);
        g.setColor(Color.WHITE);
        drawCentered(g, "Sierra Trout Quest", WIN_HEIGHT/3);
        drawCentered(g, "Press Enter to start", WIN_HEIGHT/2);
    }

    private void drawMap(Graphics g) {
        g.setColor(new Color(0,100,0));
        g.fillRect(0,0,WIN_WIDTH,WIN_HEIGHT);
        int y = 150;
        for (int i=0;i<LEVELS.length;i++) {
            if (i==selectedLevel) g.setColor(Color.YELLOW); else g.setColor(Color.WHITE);
            String text = LEVELS[i].name + " - " + LEVELS[i].weather;
            drawCentered(g, text, y);
            y += 40;
        }
        g.setColor(Color.WHITE);
        drawCentered(g, "Arrow keys to select, Enter to fish", WIN_HEIGHT-60);
    }

    private void drawFishing(Graphics g) {
        g.setColor(new Color(135,206,235));
        g.fillRect(0,0,WIN_WIDTH,WIN_HEIGHT);
        g.setColor(new Color(0,105,148));
        g.fillRect(0, WIN_HEIGHT/2, WIN_WIDTH, WIN_HEIGHT/2);
        g.setColor(Color.RED);
        g.fillRect(playerPos.x, playerPos.y, TILE_SIZE, TILE_SIZE);
        g.setColor(Color.BLACK);
        int y = 10;
        g.drawString("Rod: " + rod + "  Fly: " + fly, 10, y);
        y += 20;
        for (Map.Entry<String, Integer> entry : fishInventory.entrySet()) {
            g.drawString(entry.getKey()+": "+entry.getValue(), 10, y);
            y += 20;
        }
    }

    private void drawFight(Graphics g) {
        g.setColor(new Color(50,50,50));
        g.fillRect(0,0,WIN_WIDTH,WIN_HEIGHT);
        g.setColor(Color.WHITE);
        drawCentered(g, "Fighting "+fightFish+"!", 100);
        g.drawRect(100,200,440,20);
        g.setColor(Color.GREEN);
        int fill = (int)(440 * fightProgress / 100.0);
        g.fillRect(100,200,fill,20);
        g.setColor(Color.WHITE);
        drawCentered(g, "Press SPACE repeatedly to reel in!", 240);
    }

    private void drawCentered(Graphics g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (WIN_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (state == START && key == KeyEvent.VK_ENTER) {
            state = MAP;
        } else if (state == MAP) {
            if (key == KeyEvent.VK_LEFT) {
                selectedLevel = (selectedLevel - 1 + LEVELS.length) % LEVELS.length;
            } else if (key == KeyEvent.VK_RIGHT) {
                selectedLevel = (selectedLevel + 1) % LEVELS.length;
            } else if (key == KeyEvent.VK_ENTER) {
                currentLevel = LEVELS[selectedLevel];
                state = FISHING;
            }
        } else if (state == FISHING) {
            if (key == KeyEvent.VK_LEFT) left = true;
            if (key == KeyEvent.VK_RIGHT) right = true;
            if (key == KeyEvent.VK_UP) up = true;
            if (key == KeyEvent.VK_DOWN) down = true;
            if (key == KeyEvent.VK_SPACE && playerPos.y >= WIN_HEIGHT/2 - TILE_SIZE) {
                attemptFish();
            }
        } else if (state == FIGHT) {
            if (key == KeyEvent.VK_SPACE) {
                fightProgress += 5;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_RIGHT) right = false;
        if (key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_DOWN) down = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sierra Trout Quest");
        SierraTroutQuest game = new SierraTroutQuest();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
