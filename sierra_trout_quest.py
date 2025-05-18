import pygame
import sys
import random

WIN_WIDTH = 640
WIN_HEIGHT = 480
TILE_SIZE = 32

ENERGY_MAX = 100
ENERGY_DECAY = 0.05  # energy lost per frame

# Levels with fish and probabilities
LEVELS = [
    [
        ("Rainbow Trout", 0.6),
        ("Brown Trout", 0.3),
        ("Brook Trout", 0.1),
    ],
    [
        ("Rainbow Trout", 0.4),
        ("Brown Trout", 0.3),
        ("Brook Trout", 0.2),
        ("Golden Trout", 0.1),
    ],
]

pygame.init()
font = pygame.font.SysFont("Arial", 16)
screen = pygame.display.set_mode((WIN_WIDTH, WIN_HEIGHT))
pygame.display.set_caption("Sierra Trout Quest")
clock = pygame.time.Clock()

# Player state
player_pos = [WIN_WIDTH // 2, WIN_HEIGHT // 2]
energy = ENERGY_MAX
inventory = {}
level = 0

def draw_background():
    screen.fill((135, 206, 235))  # sky
    # mountains
    pygame.draw.polygon(screen, (105, 105, 105), [(0, WIN_HEIGHT//2), (160, 150), (320, WIN_HEIGHT//2)])
    pygame.draw.polygon(screen, (128, 128, 128), [(160, WIN_HEIGHT//2), (320, 100), (480, WIN_HEIGHT//2)])
    pygame.draw.polygon(screen, (105, 105, 105), [(320, WIN_HEIGHT//2), (480, 180), (640, WIN_HEIGHT//2)])
    # water
    pygame.draw.rect(screen, (0, 105, 148), (0, WIN_HEIGHT//2, WIN_WIDTH, WIN_HEIGHT//2))


def attempt_fish():
    global energy, level
    species_list = LEVELS[min(level, len(LEVELS)-1)]
    r = random.random()
    for fish, chance in species_list:
        if r <= chance:
            inventory[fish] = inventory.get(fish, 0) + 1
            break
        r -= chance
    energy -= 5
    if inventory.get("Golden Trout", 0) >= 1:
        level = 1  # unlock harsher conditions


def draw_player():
    pygame.draw.rect(screen, (255, 0, 0), (*player_pos, TILE_SIZE, TILE_SIZE))


def draw_hud():
    energy_bar_width = int((energy / ENERGY_MAX) * 200)
    pygame.draw.rect(screen, (255, 255, 255), (10, 10, 200, 20), 2)
    pygame.draw.rect(screen, (0, 255, 0), (10, 10, energy_bar_width, 20))
    y = 40
    for fish, count in inventory.items():
        text = font.render(f"{fish}: {count}", True, (0, 0, 0))
        screen.blit(text, (10, y))
        y += 20


def main():
    global energy
    running = True
    while running:
        dt = clock.tick(60)
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_SPACE and player_pos[1] >= WIN_HEIGHT//2 - TILE_SIZE:
                    attempt_fish()
                if event.key == pygame.K_e:
                    for fish in list(inventory.keys()):
                        if inventory[fish] > 0:
                            inventory[fish] -= 1
                            energy = min(ENERGY_MAX, energy + 15)
                            break
        keys = pygame.key.get_pressed()
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
        energy -= ENERGY_DECAY * dt
        if energy <= 0:
            running = False
        draw_background()
        draw_player()
        draw_hud()
        pygame.display.flip()
    pygame.quit()
    sys.exit()


if __name__ == "__main__":
    main()
