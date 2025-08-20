# The Tower
*The Tower*, well-known among Minecraft players, is a classic minigame set in an empty world with three towers: one for the blue team, one for the red team, and one neutral tower in the middle.
Inside each team’s tower lies a “pool” (a 3x3x2 block pit) that must be defended. The enemy’s objective is to jump into your team’s pool ten times in order to win the match.

To fight, defend, and build bridges between towers, players have access to chests containing blocks and a crafting table. That’s the complete base version of The Tower: a simple concept, yet very popular and requested Minecraft minigame.

# This Tower Plugin - What's new?
As described above, the base version of The Tower is very simple and limited. The goal of this plugin is to make it more dynamic and up to date for 2025.

For this reason, the scattered chests have been removed and replaced with NPCs near each spawn, allowing players to access items quickly. Players can purchase items that stay in their inventory even after death, and these items can be upgraded to become stronger as the match progresses.

Random (or player-triggered) events will occur during the game — for example: “Points count double for the next 2 minutes.” These events ensure that no two matches feel the same and open the door to new strategies.

Many other improvements have been made to enhance the overall gameplay experience, which will be detailed later in this document.

## ⚠️ Usage Restrictions
This code is provided for **exposition and demonstration only**. You are not allowed to:
- Download a compiled release of this project for public use.
- Compile or use **any** of this code, whether for commercial or non-commercial purposes.
### Important:
Any use of this code in violation of these restrictions, especially on publicly accessible servers or for paid services, constitutes a breach of the terms. If you wish to discuss a specific use case, please contact me directly.

## Dependencies
- Spigot/Paper 1.20.x
- [SpigotApi](https://github.com/Lucaa8/SpigotApi) (A lot of the work is done indirectly by myself in my SpigotApi plugin and then used in the tower.)

# Summary of functionalities
- Wait Lobby, Ability to choose a team or let the server create random teams (by not picking any team before the start)
- Red and Blue Teams, No Friendly Fire and prevented Spawn-kill/Spawn-lock
- Actions in game (e.g. kills, assists, points, time spent...) reward money to players
- Players can buy items, blocks, potions and more at the NPCs laying near their spawn
- Armor and food can be upgraded with coins during the game (as well as tools e.g. swords and bows)
- PvP 1.8
- Some custom items (e.g. A Feather which attenuates fall damage when held in hand, why not?)
- A kill assistance system (e.g. which rewards players who knocked someone in the void)
- Scoreboard with points, kills, assists, damage, timers, money and more
- Players who disconnect during a match have time to rejoin before their team is considered abandoned
- End game (leaderboards with damage, points, kills, assistances and deaths) and map reset
- Everything is customizable in JSON files (Messages, Items shop, Pools and Spawns, etc...)

# Functionalities
## Waiting Lobby
The waiting lobby is where players select their team and wait until the minimum player count is reached to start the match. Once the minimum is reached, a countdown timer begins, and the game starts when it hits 0.

If a player leaves the lobby before the timer reaches 0 and the minimum player count is no longer met, the timer resets, and players must wait again.

In the lobby, certain actions are restricted:
- Players cannot take damage
- Hunger does not decrease
- Blocks cannot be broken or placed
- Items cannot be dropped

Players choose their team by interacting with colored wool blocks, or leave their current team (if any) with the white wool. Once a player has joined a team, they are teleported to their team’s waiting room, and their chat messages will appear in their team’s color.
| ![wait-blue-team](https://github.com/user-attachments/assets/ab85a353-fa2a-473a-ad54-a61decbb0890) | 
|:--:| 
| *Player has joined the Blue Team* |

If teams are unbalanced (e.g., 3 Red vs. 1 Blue), the server will automatically redistribute players before the match begins. Players who haven’t chosen a team will be assigned automatically to ensure balanced teams.

| ![wait-lobby](https://github.com/user-attachments/assets/d0bf04a3-3ada-4866-8b33-4d5979f99e26) | 
|:--:| 
| *Game starts with two players — the second player was auto-assigned to the Red Team* |

## Prevented Spawn-kill and Spawn-lock
To ensure fair gameplay, spawn-killing is prevented by giving respawned players a short period of invulnerability, allowing them time to leave their spawn safely.
| <video src="https://github.com/user-attachments/assets/5161af39-0bd2-4e0a-84b0-bf9d7b515ff2"/> | ![spawnkilled-2](https://github.com/user-attachments/assets/0bb8833a-8ff0-4582-a47f-f72907369e16) |
|:--:|:--:| 
| *A player attempting to spawn-kill another player* | *The respawned player has 2.5 seconds of immunity* |

To prevent players from being trapped in their spawn, block placement and breaking are disabled near the spawn area for both enemy and friendly teams. Any anvils placed at the top of the spawn area are detected and removed upon landing.
| ![spawnlocked-1](https://github.com/user-attachments/assets/b80f23bf-6761-48be-8d49-4bf131cb447d) | ![spawnlocked-2](https://github.com/user-attachments/assets/315097c8-ea55-4691-89a1-6fe2364d323f) |
|:--:|:--:| 
| *Blocks cannot be placed or broken* | *Anvils are removed if they land near the spawn area* |

## Actions rewarding Money
Players can earn in-game currency by performing various actions. Here is a list of these actions:
- Game start
- Time played (e.g., every 5 minutes grants $50)
- Kill
- Kill assist (any player who dealt damage to the killed player in the last 10 seconds)
- Team participation in a kill (all other team members who are not the killer or assist contributors)
- Point
- Team participation in a point (all other team members)
- Dealing damage

Players can earn in-game currency by performing various actions. The money earned from each action is displayed above the hotbar in the following format: `<ACTION> (+$AMOUNT)` e.g. `Killed Player1 (5.0$)`. Check below for some examples;

| <video src="https://github.com/user-attachments/assets/7412fa4b-c36b-4072-9286-6df6e6617780"/> | <video src="https://github.com/user-attachments/assets/d236da3d-68c2-44da-b54f-b041e0d41d19"/> |
|:--:|:--:| 
| *Player 1 scores a point for their team* | *Player 2 — on the same team — also receives money* |


| <video src="https://github.com/user-attachments/assets/8c2dc2b4-e26a-4664-861d-ea646e2c8f26"/> | <video src="https://github.com/user-attachments/assets/b388d3c6-9b86-4530-aea2-761835497073"/> |
|:--:|:--:| 
| *Player 1 kills an enemy player — earning money for the kill* | *Player 2 — on the same team, who previously damaged the enemy — earns money for a kill assist* |

## NPCs Shop
The money earned from the actions above can be spent on items by interacting with NPCs located near each team’s spawn.

For clarity, the upgrade system is not explained here and will be detailed in the next section.

| ![npcs-shop](https://github.com/user-attachments/assets/472dc3c2-8d6a-4e21-8a27-e7caa33eee7b) | 
|:--:| 
| *The NPCs near the spawn* |

There are 4 NPCs, each one has a specific utility:
- Tools : Sword, Axe, Pickaxe, Bow, Arrows and Armor
- Blocks : Nether Bricks Blocks, Cobblestone, Wood, Glass, etc..
- Resources : Slime Blocks, Fences, etc...
- Utilities : Custom items like the Fire Aspect Blaze Rod, Potions, Rails/Minecarts, Food, etc...

Players can access NPCs both in their own spawn **and** in the enemy’s spawn. Since the items sold by NPCs are unlimited and not tied to a specific team, accessing enemy NPCs is possible — though risky — and can sometimes be a useful strategy.

Money is personal and not shared between teammates, and it cannot be traded between players. However, temporary items (those that drop on death) can be traded. For example, if a player runs out of money, a teammate can purchase blocks and give them instead.

| <video src="https://github.com/user-attachments/assets/4c06b39d-b1a3-4fca-a10e-6f217704b710"/> | 
|:--:| 
| *A player buying items at their spawn — and yes, empty potion bottles are automatically removed!* |

## The Upgrade System
The upgrade system is simple: players can spend a significant amount of money on certain items, but unlike temporary gear, these items are kept after death.

This allows players to focus on their preferred playstyle:
- A player who pushes aggressively and aims to score points may invest in upgrading an axe and pickaxe to break into the enemy pool faster.
- A player who prefers defending the pool might focus on upgrading a bow to keep opponents at bay.

Upgrades also ensure that respawning players always have a minimum set of useful equipment (e.g., sword, axe, etc.) to rejoin the fight immediately.

### Disclaimer ⚠️
Upgraded items **cannot** be traded between players. Otherwise, one player could max out a sword, drop it to a teammate, then die and respawn with the same upgraded item, effectively duplicating this item for free.
For the same reason, upgraded items are not dropped on death. This system is designed to prevent duplication exploits.

Here is a list of every item that can be upgraded during the game;
- Sword: From level 0 (default) to level 5 (max)
- Pickaxe: From level 0 (default) to level 4 (max)
- Axe: From level 1 to level 3 (max)
- Bow: From level 1 to level 2 (max), note that level 2 bow needs to be rebought every time and is NOT a definitve upgrade.
