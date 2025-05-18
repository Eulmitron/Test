import java.awt.*;
import java.awt.image.BufferedImage;

public class Sprites {
    public BufferedImage player;
    public BufferedImage water;
    public BufferedImage ground;
    public BufferedImage campfire;
    public BufferedImage fish;

    public void create(int tileSize) {
        player = createImage(new int[][]{
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0,0,0,0},
            {0,0,0,0xFF000000,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFF000000,0,0,0,0},
            {0,0,0xFF000000,0xFFFFCC99,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFFFFCC99,0xFF000000,0,0,0,0},
            {0,0,0xFF000000,0xFFFFCC99,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFF3366CC,0xFFFFCC99,0xFF000000,0,0,0,0},
            {0,0,0,0xFF000000,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFFFFCC99,0xFF000000,0,0,0,0,0},
            {0,0,0,0,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0xFF000000,0,0,0,0,0,0}
        });
        water = createSolidImage(0xFF0077FF);
        ground = createSolidImage(0xFF229933);
        campfire = createSolidImage(0xFFFF6633);
        fish = createSolidImage(0xFFFFFFFF);
    }

    private BufferedImage createImage(int[][] pixels) {
        int h = pixels.length;
        int w = pixels[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setRGB(x, y, pixels[y][x]);
            }
        }
        return img;
    }

    private BufferedImage createSolidImage(int color) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(color, true));
        g2.fillRect(0, 0, 16, 16);
        g2.dispose();
        return img;
    }
}
