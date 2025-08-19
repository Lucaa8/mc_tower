# The Tower
Le Tower, connu de tous, est un mini-jeu pour Minecraft dans un monde vide où se trouvent trois tours. Une pour l'équipe bleue, une pour l'équipe rouge et une tour intérmédiaire, neutre. Dans chacune des deux tours destinées aux équipes rouges et bleues se situe une "piscine" (un trou de 3x3x2 blocs) que l'équipe de cette tour doit proteger. Le but de l'équipe ennemie est de sauter dans la piscine de l'équipe ennemie et cela 10 fois pour gagner la partie. Afin de se combattre et de construire des ponts entre les tours les joueurs ont à disposition des coffres avec des blocs et une table de craft. Et voilà, la version de base du The Tower a été décrite dans son intégralité, c'est un mini-jeu simple et pourtant très connu et demandé. 

# This Tower Plugin, What's new?
Comme décrit ci-dessus, le Tower de base est très simple et limité. Le but de ce plugin est de le rendre plus dynamique, plus au gout du jour en 2025. Pour cette raison les coffres dispersés sur la carte ont été retirés et remplacés par des PNJ proche du spawn pour retrouver tout les items rapidement. Les joueurs peuvent acheter des items qui resteront dans leur inventaire même lors de la mort et peuvent être améliorés pour devenir plus puissant au fur et à mesure de la partie. Des évenements se déclehchent pendant la partie de façon aléatoire (ou déclenchés par les joueurs) par exemple "Les points comptent double pendant les 2 prochaines minutes". De cette façon les parties ne se ressemblent pas toutes et des stratégies peuvent être élaborées autour de ces évenements. Beaucoup d'autres mesures ont été prises pour rendre l'expérience de jeu meilleure et seront décrites plus tard dans ce docuement.

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
- Red and Blue Teams, No FriendlyFire and Removed Spawn-kill/Spawn-lock (anvils falling in front of spawn are removed)
- Actions in game (e.g. kills, assists, points, time spent...) reward coins to players
- Players can buy items, blocks, potions and more at the NPCs laying near their spawn
- Armor and food can be upgraded with coins during the game (as well as tools e.g. swords and bows)
- Some custom items (e.g. A Feather which attenuates fall damage when hold in hand, why not?)
- A kill assistance system (e.g. which rewards players who knocked someone in the void)
- PvP 1.8
- Scoreboard with points, kills, assists, damage, timers, coins and more
- Players crashing during the game have time to rejoin the match before abandoning their team
- End game (leaderboards with damage, points, kills, assistances and deaths) and map reset
- Everything is customizable in JSON files (Messages, Items shop, Pools and Spawns, etc...)

# Functionnalities
## Wait Lobby
The wait lobby is the lobby where players choose their team and wait for the players count to reach the minimum allowed to start the game. In the lobby some actions are prevented;
- Damage to players is prevented
- Food loss is removed
- Breaking and Placing blocks is prevented
- Item drop is prevented.

The players can choose their team by interacting with the coloured wools or leave their team (if any) with the white wool. As soon as a player got a team, they will be teleported in their respective team's waiting room and their messages will be sent with their team's color code in the chat.
| ![wait-blue-team](https://github.com/user-attachments/assets/ab85a353-fa2a-473a-ad54-a61decbb0890) | 
|:--:| 
| *The player chose the Blue Team* |

If a team is not balanced (e.g 3 red vs 1 blue), the server will automatically rearrange teams before the game starts. If some players did not choose any team, they will be scattered between the two teams in a balanced manner.

| ![wait-lobby](https://github.com/user-attachments/assets/d0bf04a3-3ada-4866-8b33-4d5979f99e26) | 
|:--:| 
| *The game starts with two players* |


