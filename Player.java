import java.awt.Point;

public class Player {
    public final Point pos;
    public int stamina = 100;
    public int warmth = 100;
    public int wood = 0;
    public boolean campfire = false;

    public Player(int x, int y) {
        this.pos = new Point(x, y);
    }

    public void move(int dx, int dy, int maxW, int maxH, int tile) {
        pos.x = Math.max(0, Math.min(maxW - tile, pos.x + dx));
        pos.y = Math.max(0, Math.min(maxH - tile, pos.y + dy));
    }

    public void decay(String hazard, boolean hourly) {
        if (!hourly) return;
        stamina = Math.max(0, stamina - 1);
        if (!campfire && "Cold".equals(hazard)) {
            warmth = Math.max(0, warmth - 2);
        } else if (!campfire) {
            warmth = Math.max(0, warmth - 1);
        }
    }
}
