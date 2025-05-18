import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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

    static final Level[] LEVELS = new Level[] {
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

    final Map<String, Double> rods = new HashMap<>();
    final Map<String, Map<String, Double>> flies = new HashMap<>();

    int state = START;
    int selectedLevel = 0;
    Level currentLevel;

    Player player = new Player(WIN_WIDTH/2, WIN_HEIGHT/2);
    FightMinigame fight = new FightMinigame();
    Sprites sprites = new Sprites();

    Map<String, Integer> fishInventory = new HashMap<>();
    String rod = "Basic Rod";
    String fly = "Dry Fly";
    int timeOfDay = 720; // minutes

    boolean left, right, up, down;

    javax.swing.Timer timer;

    char[][] map = new char[WIN_HEIGHT / TILE_SIZE][WIN_WIDTH / TILE_SIZE];

    public SierraTroutQuest() {
        setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        rods.put("Basic Rod", 1.0);
        rods.put("Pro Rod", 1.2);
        Map<String, Double> dry = new HashMap<>();
        dry.put("Rainbow Trout", 0.1);
        flies.put("Dry Fly", dry);
        Map<String, Double> nymph = new HashMap<>();
        nymph.put("Brown Trout", 0.1);
        flies.put("Nymph", nymph);

        sprites.create(TILE_SIZE);
        initMap();

        timer = new javax.swing.Timer(16, this);
        timer.start();
    }

    private void initMap() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                map[y][x] = (y >= map.length / 2) ? 'W' : 'G';
            }
        }
    }

    private static Map<String, Double> fishMap(Object... args) {
        Map<String, Double> m = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            m.put((String) args[i], (Double) args[i + 1]);
        }
        return m;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timeOfDay = (timeOfDay + 1) % 1440;
        if (state == FISHING) {
            int dx = (left? -3:0) + (right?3:0);
            int dy = (up? -3:0) + (down?3:0);
            player.move(dx, dy, WIN_WIDTH, WIN_HEIGHT, TILE_SIZE);
            if ((timeOfDay % 60)==0) {
                player.decay(currentLevel.hazard, true);
            } else {
                player.decay(currentLevel.hazard, false);
            }
        } else if (state == FIGHT) {
            fight.tick();
            if (fight.won()) {
                fishInventory.merge(fight.fish, 1, Integer::sum);
                player.stamina = Math.max(0, player.stamina - 2);
                state = FISHING;
            } else if (fight.lost()) {
                state = FISHING;
            }
        }
        repaint();
    }

    private void attemptFish() {
        if (player.stamina <= 0 || player.warmth <= 0) return;
        Map<String, Double> probs = new LinkedHashMap<>(currentLevel.fish);
        for (Map.Entry<String, Double> e : flies.getOrDefault(fly, Collections.emptyMap()).entrySet()) {
            probs.merge(e.getKey(), e.getValue(), Double::sum);
        }
        double rodMod = rods.getOrDefault(rod, 1.0);
        double total = 0; for (double v:probs.values()) total += v;
        double r = Math.random() * total * rodMod;
        double cum = 0; String chosen = null;
        for (Map.Entry<String, Double> e : probs.entrySet()) {
            cum += e.getValue();
            if (r <= cum) { chosen = e.getKey(); break; }
        }
        if (chosen!=null) {
            fight.start(chosen);
            player.stamina = Math.max(0, player.stamina - 5);
            state = FIGHT;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == START) drawStart(g);
        else if (state == MAP) drawMap(g);
        else if (state == FISHING) drawFishing(g);
        else if (state == FIGHT) drawFight(g);
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
        int y=150;
        for (int i=0;i<LEVELS.length;i++) {
            g.setColor(i==selectedLevel?Color.YELLOW:Color.WHITE);
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
        for (int y=0;y<WIN_HEIGHT/TILE_SIZE;y++) {
            for (int x=0;x<WIN_WIDTH/TILE_SIZE;x++) {
                BufferedImage tile = (y>=WIN_HEIGHT/(2*TILE_SIZE))?sprites.water:sprites.ground;
                g.drawImage(tile, x*TILE_SIZE, y*TILE_SIZE, null);
            }
        }
        g.drawImage(sprites.player, player.pos.x, player.pos.y, null);
        if (player.campfire) {
            g.drawImage(sprites.campfire, WIN_WIDTH - TILE_SIZE*2, WIN_HEIGHT - TILE_SIZE*2, null);
        }
        g.setColor(Color.BLACK);
        int y = 10;
        g.drawString("Rod: " + rod + "  Fly: " + fly, 10, y); y+=20;
        g.drawString("Stamina: " + player.stamina + "  Warmth: " + player.warmth + "  Wood: " + player.wood, 10, y); y+=20;
        g.drawString("Time: " + (timeOfDay/60) + ":" + String.format("%02d", timeOfDay%60) + " Weather: " + currentLevel.weather, 10, y); y+=20;
        for (Map.Entry<String,Integer> e : fishInventory.entrySet()) {
            g.drawString(e.getKey()+": "+e.getValue(), 10, y); y+=20;
        }
    }

    private void drawFight(Graphics g) {
        g.setColor(new Color(50,50,50));
        g.fillRect(0,0,WIN_WIDTH,WIN_HEIGHT);
        g.setColor(Color.WHITE);
        drawCentered(g, "Fighting " + fight.fish + "!", 100);
        g.drawRect(100,200,440,20);
        g.setColor(Color.GREEN);
        int fill = (int)(440 * fight.progress / 100.0);
        g.fillRect(100,200,fill,20);
        g.drawImage(sprites.fish, WIN_WIDTH/2 - TILE_SIZE/2, 140, null);
        g.setColor(Color.WHITE);
        drawCentered(g, "Press SPACE repeatedly to reel in!", 240);
    }

    private void drawCentered(Graphics g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (WIN_WIDTH - fm.stringWidth(text))/2;
        g.drawString(text, x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (state == START && key == KeyEvent.VK_ENTER) {
            state = MAP;
        } else if (state == MAP) {
            if (key==KeyEvent.VK_LEFT) selectedLevel = (selectedLevel-1+LEVELS.length)%LEVELS.length;
            else if (key==KeyEvent.VK_RIGHT) selectedLevel = (selectedLevel+1)%LEVELS.length;
            else if (key==KeyEvent.VK_ENTER) { currentLevel = LEVELS[selectedLevel]; state = FISHING; }
        } else if (state == FISHING) {
            if (key==KeyEvent.VK_LEFT) left=true;
            if (key==KeyEvent.VK_RIGHT) right=true;
            if (key==KeyEvent.VK_UP) up=true;
            if (key==KeyEvent.VK_DOWN) down=true;
            if (key==KeyEvent.VK_SPACE && player.pos.y >= WIN_HEIGHT/2 - TILE_SIZE) attemptFish();
            if (key==KeyEvent.VK_B) player.wood++;
            if (key==KeyEvent.VK_C) craft();
            if (key==KeyEvent.VK_F5) saveGame();
            if (key==KeyEvent.VK_F9) loadGame();
        } else if (state == FIGHT) {
            if (key==KeyEvent.VK_SPACE) fight.reel();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key==KeyEvent.VK_LEFT) left=false;
        if (key==KeyEvent.VK_RIGHT) right=false;
        if (key==KeyEvent.VK_UP) up=false;
        if (key==KeyEvent.VK_DOWN) down=false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void craft() {
        if (player.wood >= 5 && !"Pro Rod".equals(rod)) {
            player.wood -= 5; rod = "Pro Rod";
        } else if (player.wood >= 3 && !player.campfire) {
            player.wood -= 3; player.campfire = true;
        }
    }

    private void saveGame() {
        try {
            Properties p = new Properties();
            p.setProperty("rod", rod);
            p.setProperty("wood", Integer.toString(player.wood));
            p.setProperty("stamina", Integer.toString(player.stamina));
            p.setProperty("warmth", Integer.toString(player.warmth));
            p.setProperty("level", Integer.toString(selectedLevel));
            p.setProperty("time", Integer.toString(timeOfDay));
            for (Map.Entry<String,Integer> e : fishInventory.entrySet()) {
                p.setProperty("fish."+e.getKey(), e.getValue().toString());
            }
            p.store(new java.io.FileOutputStream("savegame.properties"), "game");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadGame() {
        try {
            Properties p = new Properties();
            p.load(new java.io.FileInputStream("savegame.properties"));
            rod = p.getProperty("rod", rod);
            player.wood = Integer.parseInt(p.getProperty("wood", "0"));
            player.stamina = Integer.parseInt(p.getProperty("stamina", "100"));
            player.warmth = Integer.parseInt(p.getProperty("warmth", "100"));
            selectedLevel = Integer.parseInt(p.getProperty("level", "0"));
            timeOfDay = Integer.parseInt(p.getProperty("time", "720"));
            fishInventory.clear();
            for (String name : p.stringPropertyNames()) {
                if (name.startsWith("fish.")) {
                    fishInventory.put(name.substring(5), Integer.parseInt(p.getProperty(name)));
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
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
