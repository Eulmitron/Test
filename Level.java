public class Level {
    public final String name;
    public final String weather;
    public final String hazard;
    public final java.util.Map<String, Double> fish;

    public Level(String name, String weather, String hazard, java.util.Map<String, Double> fish) {
        this.name = name;
        this.weather = weather;
        this.hazard = hazard;
        this.fish = fish;
    }
}
