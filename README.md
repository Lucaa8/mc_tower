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

| <video src="https://github.com/user-attachments/assets/d0bf04a3-3ada-4866-8b33-4d5979f99e26"/> | 
|:--:| 
| *Game starts with two players — the second player was auto-assigned to the Red Team* |

## Prevented Spawn-kill and Spawn-lock
To ensure fair gameplay, spawn-killing is prevented by giving respawned players a short period of invulnerability, allowing them time to leave their spawn safely.
| <video src="https://github.com/user-attachments/assets/5161af39-0bd2-4e0a-84b0-bf9d7b515ff2"/> | ![spawnkilled-2](https://github.com/user-attachments/assets/0bb8833a-8ff0-4582-a47f-f72907369e16) |
|:--:|:--:| 
| *A player attempting to spawn-kill another player* | *The respawned player has 2.5 seconds of immunity* |

To prevent players from being trapped in their spawn, block placement and breaking are disabled near the spawn area for both enemy and friendly teams. Any anvils placed at the top of the spawn area are detected and removed upon landing.
| <video src="https://github.com/user-attachments/assets/b80f23bf-6761-48be-8d49-4bf131cb447d"/> | <video src="https://github.com/user-attachments/assets/315097c8-ea55-4691-89a1-6fe2364d323f"/> |
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

### Sword (PvP 1.8)
Every player starts with a default sword (level 0) and can upgrade it to a maximum of 5 levels. Swords are unbreakable!
| Level | Material | Enchantments                            | Damage (1/2 ❤️) | Price (Upgrade) | Price (Temp) |
|-------|----------|-----------------------------------------|---------------------|----------------|--------------|
| 0     | Wood     | None                                    | 4                   | 0$             | Unobtainable |
| 1     | Stone    | None                                    | 5                   | 30$            | 3$           |
| 2     | Stone    | Knockback I                             | 5                   | 80$            | 15$          |
| 3     | Stone    | Knockback I, Sharpness I                | 6                   | 60$            | 12$          |
| 4     | Iron     | Knockback I, Sharpness I                | 7                   | 80$            | 15$          |
| 5     | Iron     | Knockback I, Sharpness I, Fire Aspect I | 7                   | 100$           | 15$          | 

### Pickaxe
Every player starts with a default pickaxe (level 0) and can upgrade it to a maximum of 4 levels.
| Level | Material | Durability    | Enchantments                | Mining Speed | Damage (1/2 ❤️) | Price (Upgrade) | Price (Temp) |
|-------|----------|---------------|-----------------------------|--------------|-----------------|-----------------|--------------|
| 0     | Wood     | 59/59         | None                        | 2            | 2               | 0$              | Unobtainable |
| 1     | Stone    | 80/131        | None                        | 4            | 3               | 60$             | 8$           |
| 2     | Stone    | 80/131 (162)  | Efficiency I, Unbreaking I  | 6            | 3               | 60$             | 10$          |
| 3     | Stone    | 131/131 (264) | Efficiency II, Unbreaking I | 9            | 3               | 80$             | 15$          |
| 4     | Diamond  | 264/1561      | Efficiency I                | 10           | 4               | 90$             | 16$          |

For **Durability**, the number in parentheses indicates the *effective* durability, which takes into account the Unbreaking enchantment.

### Axe (PvP 1.8)
Players do not start with a default axe. For this reason, axe upgrades start at Level 1 and reach their maximum at Level 3.

Axes are relatively strong both for PvP and for breaking wood, so their upgrade progression is more limited compared to other items.
| Level | Material | Durability   | Enchantments                             | Mining Speed | Damage (1/2 ❤️) | Price (Upgrade) | Price (Temp) |
|-------|----------|--------------|------------------------------------------|--------------|-----------------|-----------------|--------------|
| 1     | Wood     | 30/59        | None                                     | 2            | 3               | 45$             | 5$           |
| 2     | Stone    | 80/131       | None                                     | 4            | 4               | 70$             | 15$          |
| 3     | Stone    | 80/131 (162) | Efficiency I, Unbreaking I, Sharpness II | 6            | 5.5             | 300$            | 30$          |

For **Durability**, the number in parentheses indicates the *effective* durability, which takes into account the Unbreaking enchantment.

### Bow
Bows have been heavily nerfed compared to other tools. Players who spam bows and camp near the enemy pool can be extremely frustrating to face, and there are very few strategies to counter this effectively.

For this reason:
- Players **do not** start with a default bow.
- Bow upgrades start at Level 1 and can reach a maximum of Level 2.
- Level 2 is not a permanent upgrade: players **do not respawn** with the Level 2 bow and must re-purchase it after death. (They respawn with Level 1 tho)

Despite these limitations, bows were included to allow for tactical options, but in a more controlled and balanced way.
| Level | Durability | Enchantments | Price (Upgrade) | Price (Temp) |
|-------|------------|--------------|-----------------|--------------|
| 1     | 64/384     | None         | 150$            | 25$          |
| 2     | 64/384     | Power I      | Unobtainable    | 35$          |

Arrows must be purchased separately (**32 arrows cost $4**), and players do not respawn with any arrows, even if they have the Level 1 bow upgrade. To compensate for this nerf, players can craft chests, buy large quantities of arrows, and store them in these chests next to their spawn cage for easy access upon respawning.

**Note**: Bow durability has been set to **64**, meaning a bow can fire at most one full stack of arrows before breaking.  
Since one stack of arrows costs **8$**, players will typically spend a total of:  
- **32\$** for a Level 1 Bow (25\$ bow + 8$ arrows)  
- **43\$** for a Level 2 Bow (35\$ bow + 8$ arrows)

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
| Protection | Projectile Protection  | Fall Protection | Fire Protection |
|------------|------------------------|-----------------|-----------------|
| 16%        | 16%                    | 16%             | 16%             |

#### Armor Level 1
Price: 65$
| Piece      | Color     | Enchantments            |
|------------|-----------|-------------------------|
| Helmet     | White     | Protection II           |
| Chestplate |           | Projectile Protection I |
| Leggings   |           | Projectile Protection I |
| Boots      |           | Protection II           |

Damage reductions;
| Protection | Projectile Protection | Fall Protection | Fire Protection |
|------------|-----------------------|-----------------|-----------------|
| 16%        | 32%                   | 16%             | 16%             |

#### Armor Level 2
Price: 100$
| Piece      | Color     | Enchantments                      |
|------------|-----------|-----------------------------------|
| Helmet     | Green     | Protection III                    |
| Chestplate |           | Projectile Protection II          |
| Leggings   |           | Projectile Protection I           |
| Boots      |           | Protection III, Feather Falling I |

Damage reductions;
| Protection | Projectile Protection | Fall Protection | Fire Protection |
|------------|-----------------------|-----------------|-----------------|
| 24%        | 48%                   | 36%             | 24%             |

#### Armor Level 3
Price: 180$
| Piece      | Color     | Enchantments                      |
|------------|-----------|-----------------------------------|
| Helmet     | Black     | Protection IV                     |
| Chestplate |           | Projectile Protection III         |
| Leggings   |           | Fire Protection II                |
| Boots      |           | Protection IV, Feather Falling II |

Damage reductions;
| Protection | Projectile Protection | Fall Protection | Fire Protection |
|------------|-----------------------|-----------------|-----------------|
| 32%        | 56%                   | 44%             | 48%             |

| <video src="https://github.com/user-attachments/assets/38115ad9-3eb4-45a9-821a-fd172da26691"/> |
|:--:| 
| *The Armor System — With the changing helmet color* |
*(This video was recorded before the balance changes. Protection values and prices shown do not reflect the current state. However, the overall upgrade system remains unchanged.)*

### Food
Last but not least, the food system also works as an upgrade. Players start with no food, but don’t worry—the first food upgrade is cheap and can be purchased with starting money.

As the game progresses, players can buy better food types. Just like with armor and other tools, players cannot instantly buy the best food, even if they have enough money, ensuring a gradual progression.
| Level | Food Type      | Count | Food Restored | Saturation Restored | Price |
|-------|----------------|-------|---------------|---------------------|-------|
| 1     | Carrot         | 32    | +1.5          | +3.6                | 5$    |   
| 2     | Bread          | 32    | +2.5          | +6.0                | 15$   |
| 3     | Cooked Chicken | 16    | +3.0          | +7.2                | 25$   |
| 4     | Cooked Salmon  | 16    | +3.0          | +9.6                | 40$   |
| 5     | Golden Carrot  | 12    | +3.0          | +14.4               | 70$   |

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
5) Server is shut and map reset

| <video src="https://github.com/user-attachments/assets/bf7bad96-ff41-4fd9-8490-5e3787b055c9"/> |
|:--:| 
| *The game ended by reaching the point goal* |

## Configurations
Yes — almost everything can be customized!

### Configuration files
- **actions.json** : Defines how much money players earn for in-game actions (kills, points, etc.)
- **armor.json** : Manages default armor and armor upgrade settings (color, enchants, ...)
- **config.json** : Contains general Tower settings (e.g. max players, points to win, timers)
- **default_items.json** : Specifies the default starter items granted to every player (Sword and Pickaxe)
- **npc.json** : Sets up NPCs (location, name, skin)
- **plugin_messages.json** : Customizes all plugin messages (from lobby to end game)
- **pools.json** : Defines scoring zones where players earn points by entering
- **spawns.json** : Configures spawn locations for teams, lobby, and spectators
- **scoreboards.json** : Controls every scoreboard a player may encounter, including placeholders
- **shop.json** : Defines shop contents (Tools, Blocks, Utilities, etc.)

Here’s a list of everything you can tweak to tailor the Tower experience:

### actions.json
This file lets you configure how much money players receive for specific in-game actions:
- **Start Money** → the amount of money granted at the beginning of the game to every player (excluding spectators)
- **Play Interval** → recurring reward based on time played (e.g. $50 every 300 seconds)
- **Kill / Assistance / Participation** → money given for combat contributions
- **Point and Point Participation**
- **Damage** → money per amount of damage inflicted (based on heart)
- ⚠️ **Note** → This action list may be subject to additions or removals in future updates

### armor.json
This file configures **all armor upgrades available** to players. It includes helmets, chestplates, leggings, and boots, along with their **enchantments, lore, attributes, and metadata**. When players purchase an upgrade, the specified armor set will be automatically equipped.

You can define:
- **Default Armor** → equipment granted when the game starts
- **Level 1 Armor** → first upgrade tier
- **Level 2 Armor** → second upgrade tier
- **New Levels** → can be added freely (must also exist in *shop.json* to be purchasable)

**Notes**:
- Armor color is determined dynamically at runtime based on a player's team
- Exception on helmet which can be configured with a color for level 1 and 2 (and more if added)
- Each armor piece can be fully customized (name, lore, enchantments, attributes, etc.)
- For a complete reference, check the [SpigotApi Items documentation](https://github.com/Lucaa8/SpigotApi/blob/master/examples/Items.md).

### config.json
All the "general" settings of the tower;
| Name                            | Config Name           | Unit | Description                                                                                  | Default Value |
|---------------------------------|-----------------------|------|----------------------------------------------------------------------------------------------|---------------|
| Max Players                     | MAX_PLAYERS           | None | Maximum number of players allowed (must be an even number)                                   | 8             |
| Min Players                     | MIN_PLAYERS           | None | Minimum number of players required to start the game                                         | 4             |
| Lobby Timer Countdown           | TIMER_DURATION_WAIT   | s    | Countdown duration before game start once the minimum number of players has been reached     | 10            |
| Game Timer Countdown            | TIMER_DURATION_GAME   | s    | Duration of the match before it automatically ends                                           | 1800          |
| End Timer Countdown             | TIMER_DURATION_END    | s    | Time before the server shuts down and the map resets after the game ends                     | 30            |
| Death Invulnerability Countdown | TIMER_IMMUNE_ON_DEATH | s    | Duration of invulnerability granted to a player upon respawn                                 | 2.5           |
| Goal Points                     | GOAL_POINTS           | None | Number of points required for a team to win                                                  | 10            |
| Assistance Validity Timer       | LAST_ATTACKER_TIMER   | s    | Time window in which a player’s damage still counts as an assist after hitting an opponent   | 10            |
| Friendly Fire                   | FRIENDLY_FIRE         | None | Determines whether players can damage their teammates                                        | False         |
| Friendly Fire Money Earnings    | FRIENDLY_FIRE_MONEY   | None | Determines whether players of the same team earn money when teamkilling, assisting, etc..    | False         |
| Abandon After                   | ABANDON_AFTER         | s    | Time before a disconnected player is permanently removed from their team                     | 180           |

### default_items.json
This file defines the default tools given to players at the start of the game (excluding armors, which are configured separately in `armor.json`). Those items are also received upon respawn until player upgrades it in the Tools shop.
Currently, only two tools have default versions:
- Sword  
- Pickaxe 

**Note 1**:  
For the upgrade system to work properly, the **type (id)** of a default item must match the one used for its upgrades.  
For example:  
- The default sword in *default_items.json* has the type `"sword"` (which becomes `-1_sword` at runtime).  
- In *shop.json*, the level 1 sword is `"0_sword"`, level 2 is `"1_sword"`, etc.  

This allows the plugin to track a player’s current upgrade level with a simple integer and correctly replace tools when upgrading or respawning.

**Note 2**:  
In the same way, a default bow could be added by defining a default item with the type `"bow"`.  

### npc.json
This file configures all NPCs.  
Since **facing direction and location differ depending on the team**, each NPC type (Utilities, Tools, Blocks, etc.) must be configured **twice**:  
- Once for the red spawn  
- Once for the blue spawn

To differentiate them, NPC names cannot be exactly the same (otherwise the client would see them as duplicates and showing only one of them).  
For example:  
- Utilities NPC for the blue team → `Utilities§b§f`  
- Utilities NPC for the red team → `Utilities§c§f`  

#### Textures
To apply a skin to an NPC, retrieve the **signature** and **value** of the desired skin directly from Mojang servers.  

Steps:  
1. Get the player’s UUID from their username:  
   `https://api.mojang.com/users/profiles/minecraft/{username}`  
   *(copy the `"id"` value)*
   
2. Retrieve the full skin data using that UUID:  
   `https://sessionserver.mojang.com/session/minecraft/profile/{copied id}?unsigned=false`  

3. Copy the `"value"` and `"signature"` fields and paste them into the `npc.json` configuration.

### plugin_messages.json
This file contains all the messages used by the plugin (from the lobby to the end of the game).  
It allows you to **customize texts** shown to players, such as hot bar alerts, titles, or chat messages.  

#### Notes
- Currently, only **one language file** is supported.  
- In future versions, multiple `plugin_messages.json` files may be added, allowing players to select their preferred language.  

### pools.json
This file defines the **pool areas** for each team (red and blue).  
Each pool is represented by a cube, delimited by the coordinates `X1`, `X2`, `Z1`, `Z2`, `Y1`, and `Y2`.  
When a player (excluding spectators) enters the enemy's pool region, the plugin detects it and adds a point to their team.  

#### Notes
- This configuration is especially useful when using a **custom map**, as pool locations may vary.  

### spawns.json
This file defines the **spawn locations** for different phases of the game:  
- Initial lobby spawn  
- Lobby red team spawn  
- Lobby blue team spawn  
- In-game red team spawn cage  
- In-game blue team spawn cage  
- In-game spectator spawn  

It also includes the configuration of the **map folder name**, which is used to reset the map between games.  

### scoreboards.json
This file configures the **scoreboards** displayed during the different game phases: lobby, in-game, spectator, and end game.  
As spectators do not have statistics like kills or deaths, those placeholders will not be replaced for them if used.  

#### Empty lines ⚠️
To insert empty lines in a scoreboard, use a color code as the line text.  
If you need multiple empty lines, each one must use a **different color code**.  
Example:  
- Text on line 1  
- §1 *this line will appear as empty in game*
- Text on line 2  
- §2 *this line will appear as empty in game*

---

Here is the list of scoreboards available:

#### Lobby
**Scoreboard Name:** `WAIT`  
**Placeholders:**  
- `{PLAYER_COUNT}` : Current number of online players in the lobby  
- `{MAX_PLAYER_COUNT}` : Maximum allowed players in the lobby, defined by `MAX_PLAYERS` in *config.json*  
- `{TEAM}` : Player team status → None, Blue, or Red (already includes the color code; "None" is green)  
- `{TIMER}` : Countdown before the game starts, defined by `TIMER_DURATION_WAIT` in *config.json*  

---

#### Game (Player)
**Scoreboard Name:** `GAME`  
**Placeholders:**  
- `{POINTS_RED}` and `{POINTS_BLUE}` : Current points of each team (**does not include color codes**)  
- `{MAX_POINTS}` : Point goal required to win, defined by `GOAL_POINTS` in *config.json*  
- `{TIMER}` : Current game timer, defined by `TIMER_DURATION_GAME` in *config.json*  
- `{TEAM}` : Player’s team → Blue or Red (already includes the color code)  
- `{DAMAGE}` : Player’s total damage dealt (displayed in hearts, includes the heart emoji)  
- `{POINTS}` : Player’s personal points  
- `{KILLS}` : Player’s kills  
- `{ASSISTS}` : Player’s assists  
- `{DEATHS}` : Player’s deaths  
- `{MONEY}` : Player’s money (displayed with `$`)  

---

#### Game (Spectator)
**Scoreboard Name:** `SPECTATOR`  
**Placeholders:**  
- `{POINTS_RED}`, `{POINTS_BLUE}`, `{MAX_POINTS}`, `{TIMER}`  

---

#### Game Ended (Player)
**Scoreboard Name:** `END`  
**Placeholders:**  
- `{TEAM}` : The winning team (**not the player’s team**, includes the color code)  
- `{POINTS_RED}` and `{POINTS_BLUE}` : Final points of each team (**does not include color codes**)  
- `{TIMER}` : Countdown before the server shuts down, defined by `TIMER_DURATION_END` in *config.json*  
- `{DAMAGE}` : Player’s total final damage (displayed in hearts, includes the heart emoji) 
- `{POINTS}` : Player’s final personal points  
- `{KILLS}` : Player’s total kills  
- `{ASSISTS}` : Player’s total assists  
- `{DEATHS}` : Player’s total deaths  

---

#### Game Ended (Spectator)
**Scoreboard Name:** `SPECTATOR_END`  
**Placeholders:**  
- `{TEAM}`, `{POINTS_RED}`, `{POINTS_BLUE}`, `{TIMER}`  

### shop.json
This file controls **all NPC shops** visible to players in the game.  
Almost everything is fully customizable:  
- Item icons  
- Names  
- Upgrade items  
- Prices  

> ⚠️ Note: This file can be complex to edit manually.   
> If you have any specific questions or plan to modify it, please contact me on Discord at `lucaa_8`.

# What's up for the Future ?
The following features and ideas are **still in development** or planned for future release:

## Tower Lobby (Bungee)
Do not confuse this lobby with the **waiting lobby** before a game.  
The Tower Lobby would be a separate Spigot server where players can:
- See their overall Tower statistics  
- Browse and join new games  
- Check other players’ profiles and leaderboards  

### Signs
To keep a nostalgic touch, **signs** could be used to enter games.  
These signs could display information about active servers, map types, or game modes.  
They could also serve as portals to different maps, events, or shorter, streamlined Tower game modes.  

### Leaderboards
Leaderboards can display:  
- Overall statistics (player level, kills/deaths, etc.)  
- Monthly statistics, with the **top 3 players rewarded** with badges or other prizes. The statistic tested could change every month;

Example:  
- January → Top 3 killers rewarded  
- February → Top 3 damage dealers rewarded 
- March → Top 3 most games played rewarded 
- April → Top 3 most money spent rewarded 

This system makes leaderboard progression **fun and unique**, encouraging players to aim for different objectives each month.  

## Player Profile
Clicking on a player in the lobby opens their profile, displaying:  
- Overall stats: kills, deaths, kills with sword, kills with bow, etc.  
- Achievements/badges: e.g.  
  - Killer I → Kill 1,000 players  
  - Killer II → Kill 5,000 players  
- Leaderboard badges, best rankings, personal records (e.g., most kills in one game)  

### Player Progression System
A level system could be implemented as a **reward for playing**.  
- Players gain experience points at the end of each game based on performance (kills, points, etc.)  
- Levels are displayed in the player profile, on leaderboards, and in the tab list  
- Provides a simple summary of a player’s overall progress
- Can create fair matchmaking (if matchmaking games are added, not planned yet tho)

### Player Challenges System
Challenges provide additional objectives and rewards (badges, exp for level).  
Examples:  
- Killer I, II, III → Kill 1,000 / 5,000 / 10,000 players  
- Traveler → Travel 10,000 blocks in a single game  
- Fully Kitted → Upgrade two tools to max level in one game  
- Harry Potter I, II, III → Kill 10 / 50 / 100 players with potions  

This encourages players to focus on diverse objectives beyond simply scoring points.  

## Game
Some ideas that could improve and add uniqueness to each Tower Game.

### Custom Modifiers
Custom Modifiers add **unique dynamics** to each game. They can trigger randomly or be activated strategically by players.  
Examples of modifiers:  
- Points count double for the next 2 minutes  
- Each kill rewards a point  
- Next point becomes a winning point (rare event)  
- Creepers spawn in the enemy base and attack players 

### TNTFly
Adjusting TNT velocity can allow players to **propel themselves**, opening new strategies to attack pools or destroy enemy defenses. 

### Enchanting System
T.B.D.  

In the old version of Tower, an enchanting table was available and lapis lazuli was spawning at the center of the map.  
Currently, tools and armor come **pre-enchanted**, so the legacy enchanting table system has been removed, and the center of the map mostly serves as a gateway to the enemy base, which is a bit sad and empty.  

Ideally, a **streamlined and more controlled enchanting system** could be added:  
- An **elixir** spawns at the center of the map (Lapis Lazuli but replaced by a more fun Harry Potter texture with a resource pack)
- Players use this elixir to purchase controlled enchantments on tools (maybe at a new NPC or the Utilities one)
- This avoids overpowered combinations (e.g., Sharpness IV swords) by the classic enchantment table while keeping the center of the map strategically important

### Shorters games or Team size changes
The plugin is fully customizable:  
- Reduce shop prices, timers, or point goals for shorter matches  
- Support alternative formats: 2v2, 4v4, 8v8  
- Combine with unique custom modifiers, maps, challenges, and shops 

### Events
Thanks to the highly customizable nature of the plugin, **temporary events** can be created very easily.
A *temporary event* refers to a dedicated server running a Tower instance with unique and higly edited configurations.  
These special game modes would be accessible through **different signs in the Tower Lobby**, allowing players to quickly join and experience new variations of the game.

Examples of what we could do:  
- Custom shop upgrades (e.g., a sword with Knockback VI)  
- Custom modifiers that alter gameplay, such as clearing the enemy’s pool from any block or disabling all tools (melee only PvP)
- Shops have limited resources (i.e. max 2048 wood blocks can be bought during the game)
- Limited-time game modes (or recurent ones) with unique challenges (adding a rarity on some badges/profile achievements)

> ⚠️ Note: These event games **do not count** toward overall statistics or ranking systems.  
> They are designed to **diversify gameplay** and add new functionalities for fun and variety.

### Saving the game data
Optional, but useful for analytics:  
- Record each game’s duration, winning team, participants, kills, points, deaths, and timestamps  
- Track shop statistics (most bought tools, to make balance adjustments)  
- Data could be displayed on a website for overall stats and analytics  
