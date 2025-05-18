import pygame
import sys
import random

WIN_WIDTH = 640
WIN_HEIGHT = 480
TILE_SIZE = 32

# Game states
START, MAP, FISHING, FIGHT = range(4)

# Level definitions with weather and fish tables
LEVELS = [
    {
        "name": "River", 
        "weather": "Clear", 
        "fish": [("Rainbow Trout", 0.5), ("Brown Trout", 0.3), ("Brook Trout", 0.2)]
    },
    {
        "name": "Mountain Stream", 
        "weather": "Cold", 
        "fish": [("Rainbow Trout", 0.4), ("Brown Trout", 0.4), ("Golden Trout", 0.2)]
    },
    {
        "name": "High Lake", 
        "weather": "Stormy", 
        "fish": [("Cutthroat Trout", 0.5), ("Rainbow Trout", 0.3), ("Brown Trout", 0.2)]
    },
]

# Equipment modifiers
RODS = {
    "Basic Rod": 1.0,
    "Pro Rod": 1.2,
}

FLIES = {
    "Dry Fly": {"Rainbow Trout": 0.1},
    "Nymph": {"Brown Trout": 0.1},
}

pygame.init()
font = pygame.font.SysFont("Arial", 16)
screen = pygame.display.set_mode((WIN_WIDTH, WIN_HEIGHT))
pygame.display.set_caption("Sierra Trout Quest")
clock = pygame.time.Clock()

# Game state variables
state = START
selected_level = 0
current_level = None

player_pos = [WIN_WIDTH // 2, WIN_HEIGHT // 2]
fish_inventory = {}

# equipment
rod = "Basic Rod"
fly = "Dry Fly"

# fight state variables
fight_fish = None
fight_progress = 0
fight_strength = 0


def draw_start_screen():
    screen.fill((0, 0, 0))
    title = font.render("Sierra Trout Quest", True, (255, 255, 255))
    prompt = font.render("Press Enter to start", True, (255, 255, 255))
    screen.blit(title, (WIN_WIDTH // 2 - title.get_width() // 2, WIN_HEIGHT // 3))
    screen.blit(prompt, (WIN_WIDTH // 2 - prompt.get_width() // 2, WIN_HEIGHT // 2))


def draw_map():
    screen.fill((0, 100, 0))
    y = 150
    for i, lvl in enumerate(LEVELS):
        color = (255, 255, 0) if i == selected_level else (255, 255, 255)
        text = font.render(f"{lvl['name']} - {lvl['weather']}", True, color)
        screen.blit(text, (WIN_WIDTH // 2 - text.get_width() // 2, y))
        y += 40
    prompt = font.render("Arrow keys to select, Enter to fish", True, (255, 255, 255))
    screen.blit(prompt, (WIN_WIDTH // 2 - prompt.get_width() // 2, WIN_HEIGHT - 60))


def draw_fishing():
    screen.fill((135, 206, 235))  # sky
    pygame.draw.rect(screen, (0, 105, 148), (0, WIN_HEIGHT//2, WIN_WIDTH, WIN_HEIGHT//2))
    pygame.draw.rect(screen, (255, 0, 0), (*player_pos, TILE_SIZE, TILE_SIZE))
    y = 10
    inv_text = font.render(f"Rod: {rod}  Fly: {fly}", True, (0, 0, 0))
    screen.blit(inv_text, (10, y))
    y += 20
    for fish, count in fish_inventory.items():
        text = font.render(f"{fish}: {count}", True, (0, 0, 0))
        screen.blit(text, (10, y))
        y += 20


def attempt_fish():
    global state, fight_fish, fight_progress, fight_strength
    species = LEVELS[selected_level]['fish']
    # base probabilities
    probs = {fish: chance for fish, chance in species}
    # apply rod modifier
    rod_mod = RODS.get(rod, 1.0)
    # apply fly modifier
    for fish, bonus in FLIES.get(fly, {}).items():
        if fish in probs:
            probs[fish] += bonus
    # normalize
    total = sum(probs.values())
    r = random.random() * total * rod_mod
    cum = 0
    for fish, chance in probs.items():
        cum += chance
        if r <= cum:
            fight_fish = fish
            break
    fight_progress = 50
    fight_strength = random.randint(1, 3)
    state = FIGHT


def draw_fight():
    screen.fill((50, 50, 50))
    fish_text = font.render(f"Fighting {fight_fish}!", True, (255, 255, 255))
    screen.blit(fish_text, (WIN_WIDTH // 2 - fish_text.get_width() // 2, 100))
    pygame.draw.rect(screen, (255, 255, 255), (100, 200, 440, 20), 2)
    fill_width = int(440 * fight_progress / 100)
    pygame.draw.rect(screen, (0, 255, 0), (100, 200, fill_width, 20))
    prompt = font.render("Press SPACE repeatedly to reel in!", True, (255, 255, 255))
    screen.blit(prompt, (WIN_WIDTH // 2 - prompt.get_width() // 2, 240))


def handle_fight(events):
    global state, fight_progress
    for e in events:
        if e.type == pygame.KEYDOWN and e.key == pygame.K_SPACE:
            fight_progress += 5
    fight_progress -= fight_strength
    if fight_progress >= 100:
        fish_inventory[fight_fish] = fish_inventory.get(fight_fish, 0) + 1
        state = FISHING
    elif fight_progress <= 0:
        state = FISHING


def main():
    global state, selected_level, current_level
    running = True
    while running:
        dt = clock.tick(60)
        events = pygame.event.get()
        for event in events:
            if event.type == pygame.QUIT:
                running = False
            elif state == START and event.type == pygame.KEYDOWN and event.key == pygame.K_RETURN:
                state = MAP
            elif state == MAP:
                if event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_LEFT:
                        selected_level = (selected_level - 1) % len(LEVELS)
                    if event.key == pygame.K_RIGHT:
                        selected_level = (selected_level + 1) % len(LEVELS)
                    if event.key == pygame.K_RETURN:
                        current_level = LEVELS[selected_level]
                        state = FISHING
        keys = pygame.key.get_pressed()
        if state == FISHING:
            if keys[pygame.K_LEFT]:
                player_pos[0] -= 3
            if keys[pygame.K_RIGHT]:
                player_pos[0] += 3
            if keys[pygame.K_UP]:
                player_pos[1] -= 3
            if keys[pygame.K_DOWN]:
                player_pos[1] += 3
            player_pos[0] = max(0, min(WIN_WIDTH - TILE_SIZE, player_pos[0]))
            player_pos[1] = max(0, min(WIN_HEIGHT - TILE_SIZE, player_pos[1]))
            for event in events:
                if event.type == pygame.KEYDOWN and event.key == pygame.K_SPACE and player_pos[1] >= WIN_HEIGHT//2 - TILE_SIZE:
                    attempt_fish()
        elif state == FIGHT:
            handle_fight(events)

        if state == START:
            draw_start_screen()
        elif state == MAP:
            draw_map()
        elif state == FISHING:
            draw_fishing()
        elif state == FIGHT:
            draw_fight()

        pygame.display.flip()

    pygame.quit()
    sys.exit()


if __name__ == "__main__":
    main()
