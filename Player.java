import java.awt.*;

public class Player {
    public Point pos = new Point();
    public int stamina = 100;
    public int warmth = 100;
    public int wood = 0;
    public boolean campfire = false;

    public Player(int x, int y) {
        pos.setLocation(x, y);
    }

    public void move(int dx, int dy, int maxW, int maxH, int tile) {
        pos.x = Math.max(0, Math.min(maxW - tile, pos.x + dx));
        pos.y = Math.max(0, Math.min(maxH - tile, pos.y + dy));
    }

    public void decay(String hazard, boolean hourly) {
        if (hourly) {
            stamina = Math.max(0, stamina - 1);
            if (!campfire && "Cold".equals(hazard)) {
                warmth = Math.max(0, warmth - 2);
            } else if (!campfire) {
                warmth = Math.max(0, warmth - 1);
            }
        }
        if (campfire) warmth = Math.min(100, warmth + 1);
    }
}
