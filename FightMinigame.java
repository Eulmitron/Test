public class FightMinigame {
    public String fish;
    public int progress;
    public int strength;

    public void start(String fish) {
        this.fish = fish;
        this.progress = 50;
        this.strength = 1 + (int)(Math.random()*3);
    }

    public void reel() {
        progress += 5;
    }

    public void tick() {
        progress -= strength;
    }

    public boolean won() { return progress >= 100; }
    public boolean lost() { return progress <= 0; }
}
