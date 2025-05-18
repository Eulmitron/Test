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
        String hazard;
        Map<String, Double> fish;
        Level(String name, String weather, String hazard, Map<String, Double> fish) {
            this.name = name;
            this.weather = weather;
            this.hazard = hazard;
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

    // Predefined fishing areas with weather and hazards
    static final Level[] LEVELS = {
        new Level("River Bend", "Sun", "None",
                fishMap("Rainbow Trout", 0.5, "Brown Trout", 0.3, "Brook Trout", 0.2)),
        new Level("Mountain Stream", "Snow", "Cold",
                fishMap("Rainbow Trout", 0.4, "Brown Trout", 0.3, "Golden Trout", 0.3)),
        new Level("High Lake", "Thunder", "Storm",
                fishMap("Cutthroat Trout", 0.5, "Rainbow Trout", 0.3, "Brown Trout", 0.2)),
        new Level("Foothill Pond", "Rain", "Muddy",
                fishMap("Brown Trout", 0.5, "Rainbow Trout", 0.4, "Brook Trout", 0.1)),
        new Level("Alpine Lake", "Sun", "Wind",
                fishMap("Golden Trout", 0.5, "Cutthroat Trout", 0.3, "Rainbow Trout", 0.2))
    };

    final Map<String, Double> RODS = new HashMap<>();
    final Map<String, Map<String, Double>> FLIES = new HashMap<>();

    int state = START;
    int selectedLevel = 0;
    Level currentLevel = null;
    Point playerPos = new Point(WIN_WIDTH/2, WIN_HEIGHT/2);
    Map<String, Integer> fishInventory = new HashMap<>();
    String rod = "Basic Rod";
    String fly = "Dry Fly";

    int stamina = 100;
    int warmth = 100;
    int wood = 0;
    int timeOfDay = 720; // minutes, start at noon
    boolean campfire = false;

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
        timeOfDay = (timeOfDay + 1) % 1440;
        if (state == FISHING) {
            if (left) playerPos.x -= 3;
            if (right) playerPos.x += 3;
            if (up) playerPos.y -= 3;
            if (down) playerPos.y += 3;
            playerPos.x = Math.max(0, Math.min(WIN_WIDTH - TILE_SIZE, playerPos.x));
            playerPos.y = Math.max(0, Math.min(WIN_HEIGHT - TILE_SIZE, playerPos.y));
            if ((timeOfDay % 60) == 0) {
                stamina = Math.max(0, stamina - 1);
                if (!campfire && "Cold".equals(currentLevel.hazard)) {
                    warmth = Math.max(0, warmth - 2);
                } else if (!campfire) {
                    warmth = Math.max(0, warmth - 1);
                }
            }
        } else if (state == FIGHT) {
            fightProgress -= fightStrength;
            if (fightProgress >= 100) {
                fishInventory.merge(fightFish, 1, Integer::sum);
                stamina = Math.max(0, stamina - 2);
                state = FISHING;
            } else if (fightProgress <= 0) {
                state = FISHING;
            }
        }
        if (campfire) {
            warmth = Math.min(100, warmth + 1);
        }
        repaint();
    }

    private void attemptFish() {
        if (stamina <= 0 || warmth <= 0) return;
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
        stamina = Math.max(0, stamina - 5);
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
            String text = LEVELS[i].name + " - " + LEVELS[i].weather + " (" + LEVELS[i].hazard + ")";
            drawCentered(g, text, y);
            y += 40;
        }
        g.setColor(Color.WHITE);
        drawCentered(g, "Arrow keys to select, Enter to fish", WIN_HEIGHT-60);
    }

    private void drawFishing(Graphics g) {
        int sky = (int)(100 + 155 * Math.cos(Math.PI * timeOfDay / 720.0));
        g.setColor(new Color(sky, sky, 235));
        g.fillRect(0,0,WIN_WIDTH,WIN_HEIGHT);
        g.setColor(new Color(0,105,148));
        g.fillRect(0, WIN_HEIGHT/2, WIN_WIDTH, WIN_HEIGHT/2);
        g.setColor(Color.RED);
        g.fillRect(playerPos.x, playerPos.y, TILE_SIZE, TILE_SIZE);
        if (campfire) {
            g.setColor(Color.ORANGE);
            g.fillOval(WIN_WIDTH - 50, WIN_HEIGHT - 50, 30, 30);
        }
        g.setColor(Color.BLACK);
        int y = 10;
        g.drawString("Rod: " + rod + "  Fly: " + fly, 10, y);
        y += 20;
        g.drawString("Stamina: " + stamina + "  Warmth: " + warmth + "  Wood: " + wood, 10, y);
        y += 20;
        g.drawString("Time: " + (timeOfDay/60) + ":" + String.format("%02d", timeOfDay%60) +
                " Weather: " + currentLevel.weather, 10, y);
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
            if (key == KeyEvent.VK_B) {
                wood++;
            }
            if (key == KeyEvent.VK_C) {
                craft();
            }
            if (key == KeyEvent.VK_F5) {
                saveGame();
            }
            if (key == KeyEvent.VK_F9) {
                loadGame();
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

    private void craft() {
        if (wood >= 5 && !"Pro Rod".equals(rod)) {
            wood -= 5;
            rod = "Pro Rod";
        } else if (wood >= 3 && !campfire) {
            wood -= 3;
            campfire = true;
        }
    }

    private void saveGame() {
        try {
            Properties p = new Properties();
            p.setProperty("rod", rod);
            p.setProperty("wood", Integer.toString(wood));
            p.setProperty("stamina", Integer.toString(stamina));
            p.setProperty("warmth", Integer.toString(warmth));
            p.setProperty("level", Integer.toString(selectedLevel));
            p.setProperty("time", Integer.toString(timeOfDay));
            for (Map.Entry<String, Integer> e : fishInventory.entrySet()) {
                p.setProperty("fish." + e.getKey(), e.getValue().toString());
            }
            p.store(new java.io.FileOutputStream("savegame.properties"), "game");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadGame() {
        try {
            Properties p = new Properties();
            p.load(new java.io.FileInputStream("savegame.properties"));
            rod = p.getProperty("rod", rod);
            wood = Integer.parseInt(p.getProperty("wood", "0"));
            stamina = Integer.parseInt(p.getProperty("stamina", "100"));
            warmth = Integer.parseInt(p.getProperty("warmth", "100"));
            selectedLevel = Integer.parseInt(p.getProperty("level", "0"));
            timeOfDay = Integer.parseInt(p.getProperty("time", "720"));
            fishInventory.clear();
            for (String name : p.stringPropertyNames()) {
                if (name.startsWith("fish.")) {
                    fishInventory.put(name.substring(5), Integer.parseInt(p.getProperty(name)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
