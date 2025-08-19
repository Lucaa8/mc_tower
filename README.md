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

# Summary of functionnalities
- Wait Lobby, Ability to choose a team or let the server create random teams (by not picking any team before the start)
- Red and Blue Teams, No Friendly Fire and prevented Spawn-kill/Spawn-lock
- Actions in game (e.g. kills, assists, points, time spent...) reward coins to players
- Players can buy items, blocks, potions and more at the NPCs laying near their spawn
- Armor and food can be upgraded with coins during the game (as well as tools e.g. swords and bows)
- Some custom items (e.g. A Feather which attenuates fall damage when held in hand, why not?)
- A kill assistance system (e.g. which rewards players who knocked someone in the void)
- PvP 1.8
- Scoreboard with points, kills, assists, damage, timers, coins and more
- Players who disconnect during a match have time to rejoin before their team is considered abandoned
- End game (leaderboards with damage, points, kills, assistances and deaths) and map reset
- Everything is customizable in JSON files (Messages, Items shop, Pools and Spawns, etc...)

# Functionalities
## Waiting Lobby
The waiting lobby is where players select their team and wait until the minimum player count is reached to start the match.

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


