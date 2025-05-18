# Sierra Trout Quest

Equip different rods and flies to influence which species you might hook.

The Java version now draws simple SNES-style pixel art sprites instead of plain rectangles.

## Requirements
- Python 3.8+
- Pygame (see `requirements.txt`)

## Running the Game
1. Install the Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```
2. Run the game:
   ```bash
   python sierra_trout_quest.py
   ```

### Java Version
Compile and run the Java port (requires a graphical environment):
```bash
javac SierraTroutQuest.java
java SierraTroutQuest
```

## Controls
- **Arrow Keys**: Move the player and navigate menus
- **Space**: Cast your line and reel during a fight
- **Enter**: Confirm selections
- **B**: Gather wood
- **C**: Craft upgrade or campfire
- **F5**: Save game
- **F9**: Load game
- **Close Window**: Quit the game

The demo now features five fishing spots with different weather, hazards and day/night cycles.
Stamina and warmth decrease over time, so gather wood (`B`) and craft (`C`) campfires or upgrade rods.
Press `F5` to save and `F9` to load your progress.

Version 2 adds placeholder pixel art for the player, fish, tiles, and campfire to better emulate SNES-style graphics.
