import java.awt.*;
import java.awt.image.BufferedImage;

public class Sprites {
    public BufferedImage player;
    public BufferedImage campfire;
    public BufferedImage fish;
    public BufferedImage water;
    public BufferedImage ground;

    public void create(int tile) {
        player = new BufferedImage(tile, tile, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = player.createGraphics();
        g.setColor(new Color(200,50,50));
        g.fillRect(0,0,tile,tile);
        g.setColor(Color.WHITE);
        g.fillRect(tile/4, tile/4, tile/2, tile/2);
        g.dispose();

        campfire = new BufferedImage(tile, tile, BufferedImage.TYPE_INT_ARGB);
        g = campfire.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(tile/4, tile/2, tile/2, tile/4);
        g.setColor(Color.ORANGE);
        g.fillOval(tile/4, tile/4, tile/2, tile/2);
        g.dispose();

        fish = new BufferedImage(tile, tile, BufferedImage.TYPE_INT_ARGB);
        g = fish.createGraphics();
        g.setColor(Color.GREEN);
        g.fillOval(0, tile/4, tile, tile/2);
        g.dispose();

        water = new BufferedImage(tile, tile, BufferedImage.TYPE_INT_ARGB);
        g = water.createGraphics();
        g.setColor(new Color(0,105,148));
        g.fillRect(0,0,tile,tile);
        g.dispose();

        ground = new BufferedImage(tile, tile, BufferedImage.TYPE_INT_ARGB);
        g = ground.createGraphics();
        g.setColor(new Color(34,139,34));
        g.fillRect(0,0,tile,tile);
        g.dispose();
    }
}
