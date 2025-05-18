import java.awt.*;

public class Player {
    public Point pos;
    public int stamina = 100;
    public int warmth = 100;
    public int wood = 0;
    public boolean campfire = false;

    public Player(int x, int y) {
        this.pos = new Point(x, y);
    }

    public void move(int dx, int dy, int maxWidth, int maxHeight, int tile) {
        pos.translate(dx, dy);
        pos.x = Math.max(0, Math.min(maxWidth - tile, pos.x));
        pos.y = Math.max(0, Math.min(maxHeight - tile, pos.y));
    }

    public void decay(String hazard, boolean eachMinute) {
        if (eachMinute) {
            stamina = Math.max(0, stamina - 1);
            if (!campfire && "Cold".equals(hazard)) {
                warmth = Math.max(0, warmth - 2);
            } else if (!campfire) {
                warmth = Math.max(0, warmth - 1);
            }
        }
        if (campfire) {
            warmth = Math.min(100, warmth + 1);
        }
    }
}
