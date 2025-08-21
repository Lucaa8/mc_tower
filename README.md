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
The upgrade system is simple: players can spend a significant amount of money on certain items, but unlike other gear, these items are kept after death.

This allows players to focus on their preferred playstyle:
- A player who pushes aggressively and aims to score points may invest in upgrading an axe and pickaxe to break into the enemy pool faster.
- A player who prefers defending the pool might focus on upgrading a bow to keep opponents at bay.

Upgrades also ensure that respawning players always have a minimum set of useful equipment (e.g., sword, axe, etc.) to rejoin the fight immediately.

### Disclaimer ⚠️
Upgraded items **cannot** be traded between players. Otherwise, one player could max out a sword, drop it to a teammate, then die and respawn with the same upgraded item, effectively duplicating this item for free.
For the same reason, upgraded items are not dropped on death. This system is designed to prevent duplication exploits.
### End Of Disclaimer ⚠️

### Temporary Tools
All upgrade levels (Armor, Food and level 2 bow are excluded) also come with a “one-time use” option. For example, if you currently have a Level 1 sword, you can either:
- **Permanently upgrade** (right-click) to Level 2 (you respawn with the Level 2 sword every time), or
- **Purchase a temporary** (left-click) Level 2 sword (cheaper than the permanent upgrade), which is dropped on death and can be given to other players.

This allows players to buy a higher-tier item for specific situations—like a full-team push or defending the pool when the enemy team is nearby—without spending a lot of money.

| <video src="https://github.com/user-attachments/assets/55cfdb9f-de5a-4738-91f0-b812ab40fdd4"/> | <video src="https://github.com/user-attachments/assets/4a26c597-cf25-418a-ba0d-79bacf8db53f"/> |
|:--:|:--:| 
| *Player left-clicked the sword — Granting them a temporary level 1 sword* | *Player right-clicked the sword — Granting them a permanent level 1 sword* |

Here is a list of every item that can be upgraded during the game;
(Note: Those upgrades may change for balancing purposes)

### Sword
Every player starts with a default sword (level 0) and can upgrade it to a maximum of 5 levels. Swords are unbreakable!
| Level | Material | Enchantments                            | Price (Upgrade) | Price (Temp) |
|-------|----------|-----------------------------------------|-----------------|--------------|
| 0     | Wood     | None                                    | 0$              | Unobtainable |
| 1     | Stone    | None                                    | 100$            | 10$          |
| 2     | Stone    | Knockback I                             | 200$            | 20$          |
| 3     | Stone    | Knockback I, Sharpness I                | 300$            | 30$          |
| 4     | Iron     | Knockback I, Sharpness I                | 400$            | 40$          |
| 5     | Iron     | Knockback I, Sharpness I, Fire Aspect I | 500$            | 50$          | 

### Pickaxe
Every player starts with a default pickaxe (level 0) and can upgrade it to a maximum of 4 levels.
| Level | Material | Durability | Enchantments                | Price (Upgrade) | Price (Temp) |
|-------|----------|------------|-----------------------------|-----------------|--------------|
| 0     | Wood     | 59/59      | None                        | 0$              | Unobtainable |
| 1     | Stone    | 80/131     | None                        | 100$            | 10$          |
| 2     | Stone    | 80/131     | Efficiency I, Unbreaking I  | 200$            | 20$          |
| 3     | Stone    | 131/131    | Efficiency II, Unbreaking I | 300$            | 30$          |
| 4     | Diamond  | 264/1561   | Efficiency I                | 400$            | 40$          |

### Axe
Players do not start with a default axe. For this reason, axe upgrades start at Level 1 and reach their maximum at Level 3.

Axes are relatively strong both for PvP and for breaking wood, so their upgrade progression is more limited compared to other items.
| Level | Material | Durability | Enchantments               | Price (Upgrade) | Price (Temp) |
|-------|----------|------------|----------------------------|-----------------|--------------|
| 1     | Wood     | 30/59      | None                       | 100$            | 10$          |
| 2     | Stone    | 80/131     | None                       | 200$            | 20$          |
| 3     | Stone    | 80/131     | Efficiency I, Unbreaking I | 300$            | 30$          |

### Bow
Bows have been heavily nerfed compared to other tools. Players who spam bows and camp near the enemy pool can be extremely frustrating to face, and there are very few strategies to counter this effectively.

For this reason:
- Players **do not** start with a default bow.
- Bow upgrades start at Level 1 and can reach a maximum of Level 2.
- Level 2 is not a permanent upgrade: players **do not respawn** with the Level 2 bow and must re-purchase it after death. (They respawn with Level 1 tho)

Despite these limitations, bows were included to allow for tactical options, but in a more controlled and balanced way.
| Level | Durability | Enchantments | Price (Upgrade) | Price (Temp) |
|-------|------------|--------------|-----------------|--------------|
| 1     | 40/384     | None         | 1'000$          | 100$         |
| 2     | 50/384     | Power I      | Unobtainable    | 200$         |

Arrows must be purchased separately (**32 arrows cost $8**), and players do not respawn with any arrows, even if they have the Level 1 bow upgrade. To compensate for this nerf, players can craft chests, buy large quantities of arrows, and store them in these chests next to their spawn cage for easy access upon respawning.

### Armors
Armor upgrades apply to the entire set—helmet, chestplate, leggings, and boots—simultaneously. Players cannot mix different levels, for example keeping Level 1 boots with a Level 2 chestplate. This ensures that the overall protection level remains balanced. Also, note that all four armor pieces are unbreakable!

For instance, a Level 2 chestplate loses its Protection I enchantment, which is replaced by Projectile Protection. To compensate, the Level 2 helmet gains Protection II instead of Protection I. This way, the total protection of a Level 2 armor set is equivalent to Level 1, with the added benefit of projectile protection.

To help players gauge their opponent’s armor level before engaging in combat (maybe you’ll think twice before fighting someone with max-level armor!), the helmet changes color according to the armor’s level. This way, the overall armor color remains consistent—so you won’t confuse teammates with enemies—while still giving you a quick visual clue about how strong your opponent is (based on gear, not skill… or their better gaming chair!).

#### Default Armor (Level 0)
Price: 0$, every player starts with this armor!
| Piece      | Color     | Enchantments |
|------------|-----------|--------------|
| Helmet     | Red/Blue  | Protection I |
| Chestplate |           | Protection I |
| Leggings   |           | Protection I |
| Boots      |           | Protection I |

Damage reductions;
| Protection | Projectile Protection | Fall Protection |
|------------|-----------------------|-----------------|
| 16%        | 0%                    | 0%              |

#### Armor Level 1
Price: 700$
| Piece      | Color     | Enchantments                    |
|------------|-----------|---------------------------------|
| Helmet     | White     | Protection II                   |
| Chestplate |           | Projectile Protection II        |
| Leggings   |           | Protection I                    |
| Boots      |           | Protection I, Feather Falling I |

Damage reductions;
| Protection | Projectile Protection | Fall Protection |
|------------|-----------------------|-----------------|
| 16%        | 16%                   | 12%             |

#### Armor Level 2
Price: 1'200$
| Piece      | Color     | Enchantments                     |
|------------|-----------|----------------------------------|
| Helmet     | Green     | Protection II                    |
| Chestplate |           | Projectile Protection III        |
| Leggings   |           | Protection II                    |
| Boots      |           | Protection I, Feather Falling II |

Damage reductions;
| Protection | Projectile Protection | Fall Protection |
|------------|-----------------------|-----------------|
| 20%        | 24%                   | 24%             |

| <video src="https://github.com/user-attachments/assets/38115ad9-3eb4-45a9-821a-fd172da26691"/> |
|:--:| 
| *The Armor System — With the changing helmet color* |

### Food
Last but not least, the food system also works as an upgrade. Players start with no food, but don’t worry—the first food upgrade is cheap and can be purchased with starting money.

As the game progresses, players can buy better food types. Just like with armor and other tools, players cannot instantly buy the best food, even if they have enough money, ensuring a gradual progression.
| Level | Food Type      | Count | Food Restored | Saturation Restored | Price |
|-------|----------------|-------|---------------|---------------------|-------|
| 1     | Carrot         | 32    | +1.5          | +3.6                | 5$    |   
| 2     | Bread          | 32    | +2.5          | +6.0                | 12$   |
| 3     | Cooked Chicken | 16    | +3.0          | +7.2                | 20$   |
| 4     | Cooked Salmon  | 16    | +3.0          | +9.6                | 50$   |
| 5     | Golden Carrot  | 12    | +3.0          | +14.4               | 100$  |

| <video src="https://github.com/user-attachments/assets/5d92e244-a043-44d2-938f-f835b3cc9ed3"/> |
|:--:| 
| *The Food System* |

## PvP 1.8
Weapons are “**1.8 PvP enabled**”, meaning **the hit cooldown has been removed** and **the damage has been slightly reduced** to balance it.

Every item that is 1.8 PvP enabled will show a green line: “1.8 PvP enabled” in its description inside the Tools NPC shop.

If you don’t see this line, you cannot spam-click the item. For example, pickaxes are not 1.8 PvP enabled, so they retain the normal cooldown between hits.

| ![leg_pvp_enabled](https://github.com/user-attachments/assets/6152e9c1-7ea8-4d2c-8142-e7b68e2f4a59) | ![leg_pvp_disabled](https://github.com/user-attachments/assets/ac8b2986-a4f5-4585-a586-c7b5e5fceb6c) |
|:--:|:--:| 
| *The sword shows the green text, cooldown is removed* | *The pickaxe does not show the green text, cooldown is enabled* |

## Custom Items (Utilities NPC)

### Feather (Harmless Feather)
In the default Tower version, this feather had one purpose: pushing players into the void with its Knockback I enchantment.

Now that some swords already come with this enchantment, the feather needed a new purpose. It's why now **the feather reduces fall damage by 15%** for any player taking fall damage **while holding the feather in their main or off-hand**. Simply having it in the inventory does not grant the effect—the feather must be held when hitting the ground. 

Note: The feather’s damage reduction is multiplicative (and not additive), applied after all other sources of fall damage reduction (e.g., Feather Falling boots). For example, if boots provide 24% fall damage reduction, the total reduction is not simply 39% (24% + 15%). The calculation is: `Final Damage = Initial Fall Damage × (1 − 0.24) × (1 − 0.15)` and not `Final Damage = Initial Fall Damage × (1 − 0.39)`. This ensures that the feather’s reduction always scales fairly with other protections and prevents it from becoming overpowered during extreme falls.

| <video src="https://github.com/user-attachments/assets/d6a393fa-21b7-4116-a011-b76fb606b218"/> | <video src="https://github.com/user-attachments/assets/a77e409e-8aff-4175-8cff-4bc2e7201600"/> |
|:--:|:--:| 
| *Player does not hold the Harmless Feather — Resulting in death* | *Player does hold the Harmless Feather — Resulting in survival* |

### Carved Pumpkin (Spiky Pumpkin)
<img width="381" height="250" alt="Spiky Pumpkin" src="https://github.com/user-attachments/assets/84b41686-bb15-419a-a795-8a9cd8006999" />

By wearing a pumpkin (which replaces the helmet), players hide their armor level from others. This issue may need to be addressed if too many players start abusing this mechanic.

### Blaze Rod (Rod of Fire)
<img width="348" height="250" alt="Rod of Fire" src="https://github.com/user-attachments/assets/bead24f9-4005-4d30-b633-e9d164674442" />

### Beer
<img width="392" height="250" alt="Beer" src="https://github.com/user-attachments/assets/3674448d-6010-44f3-83d8-9a0b01dd790f" />

In the Potion section (Harry Potter), you can find the old Tower potion "Beer" which gives buff and debuffs

## The Kill Assistance System
Ever pushed someone into the void but didn’t get the kill? Or dealt 9.5 hearts of damage only to have someone else land the last hit and steal the kill? That’s frustrating — but not anymore, thanks to the Kill Assistance System.

### How does it work?
The system works in a simple way: it tracks every player who deals damage to another player and keeps this damage record active for a short time (e.g., 10 seconds).
- If a player dies, the last damager (the most recent registered hit) is credited with the kill.
- Every other player who dealt damage within that short time frame is awarded an assist.

This also applies to void deaths: if a player is knocked into the void, the pusher is still registered as the last damager. The death is counted as “by void,” but the kill credit go to the players who provoked the fall.

**Note**: Currently, assists are not displayed in the death message (to avoid too long or messy messages). However, they are still properly counted on the scoreboard, in the final leaderboard, and they award money to all assisting players.

| <video src="https://github.com/user-attachments/assets/755ae030-1c59-4d2f-95f6-b5de19d3d8bd"/> |
|:--:| 
| *Player 1 pushes an enemy into the void — the enemy places a block and dies from fall — kill is credited to Player 1* |

| <video src="https://github.com/user-attachments/assets/8c2dc2b4-e26a-4664-861d-ea646e2c8f26"/> | <video src="https://github.com/user-attachments/assets/b388d3c6-9b86-4530-aea2-761835497073"/> |
|:--:|:--:| 
| *Player 1 kills an enemy player — kill is credited to Player 1* | *Player 2 — on the same team, who previously damaged the enemy — gets a kill assistance* |

This is not shown here, but the system also detects deaths caused by Fire Aspect items, such as the Level 5 Sword or the Rod of Fire.

## Scoreboard
The scoreboard displays real-time information about the game and the player's personal statistics.  
Yes, it updates live — for example, every time your Fire Aspect weapon proc a fire status effect, your damage and money stats will refresh live!

### Global Information Shown During the Game
- Current points of your team and the enemy team  
- The points goal required to win the match (because the goal can change)  
- Time left before the game ends  

### Personal Information Shown During the Game
- Your team  
- Your total damage dealt (expressed in full hearts)  
- Your personal points (i.e. how many times you reached the enemy team's pool)  
- Your kills and kill assists  
- Your money balance  

### Example of live scoreboard
| <video src="https://github.com/user-attachments/assets/3a9037e3-3d44-4498-8e52-212f45204c07"/> |
|:--:| 
| *Damage, Money and Kill are updated* |

## Game Abandons
The game is forgiving when a player leaves the match. After all, who never had their game crash or forgot to pay their internet bills for a good connection?  

In this Tower, if a player disconnects, they have a short period of time to rejoin the match before it is considered an **abandon**.  

⚠️ **Important rules:**  
- If *all players of a team* leave the match, the game instantly ends with a **defeat for that team** and a **victory for the enemy team**.
- If a player rejoins *before* the abandon timer runs out, their **inventory is wiped** and their **location reset to their spawn**. (This prevents abuse such as disconnecting near the enemy pool and reconnecting later when no one is defending!)
- If a player rejoins *after* the abandon timer runs out, they automatically become a **spectator** and cannot interact in any way with their previous team. (Their statistics, such as kills, do not count towards their total kills.)

### Player left but rejoins before the abandon timer runs out
(This footage shows that the inventory of the rejoining player is not wiped. This is a bug and has been fixed)

| <video src="https://github.com/user-attachments/assets/04a305f5-240c-4091-a2de-f246b7924923"/> | <video src="https://github.com/user-attachments/assets/d5bbc965-3216-4986-8bd1-15508a5da873"/> |
|:--:|:--:| 
| *Player 1 — Sees in the chat that their teammate disconnected* | *Player 2 — disconnecting and reconnecting — in the spawn cage of their team* |

### Player left but rejoins after the abandon timer runs out
| <video src="https://github.com/user-attachments/assets/684b2e46-e9eb-4e7a-a2cf-7162b7d9c749"/> | <video src="https://github.com/user-attachments/assets/d48c656c-76b4-4b5b-84fe-57def32fad4a"/> |
|:--:|:--:| 
| *Player 1 — Sees in the chat that their teammate abandonned the game* | *Player 2 — reconnecting after 3 minutes or more — is now a spectator* |

### All players of the red team disconnected, giving victory to blue team
<img width="658" height="112" alt="image" src="https://github.com/user-attachments/assets/e9b9722b-4c7c-465a-859d-eee69dfa9e89" />

## End of the game
A match can end in three different ways:
- One team reaches the point goal
- The timer runs out (0 seconds left)
- All players of a team leave the game

Regardless of how the game ends, the closing sequence is always the same:
1) Remaining players are switched to Creative mode and can now disconnect safely
2) A message is broadcast announcing the winning team
3) Leaderboards are displayed for kills, damage, deaths, and points
4) A final message warns that the server will shut down shortly (e.g. in 30 seconds)

| <video src="https://github.com/user-attachments/assets/bf7bad96-ff41-4fd9-8490-5e3787b055c9"/> |
|:--:| 
| *The game ended by reaching the point goal* |
