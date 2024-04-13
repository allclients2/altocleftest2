# Change Log
*(this list may not contain all the changes, but I tried to list most of them)*

### Misc
- Fixed set gamma not working
- Blacklist wool in ancient cities
- Removed wandering and looking through chunks when looking for stronghold causing massive lag spikes
- Removed render distance manipulation
- Improved and fixed a lot of cases, where the bot gets stuck in an infinite loop/dies
- Removed usage of twisting vines
- Improved one cycle

### Removed usage of blast furnace
The bot currently gets **11** iron, (**6** for two buckets, **3** for pickaxe, **1** for shield, **1** for flint and steel) and **5** gold for golden helmet.

Blast furnace makes your smelting twice as fast, but it comes at a cost of crafting it.

To craft a blast furnace you need **5** iron and **3** smooth stone (done by smelting **3** normal stone).

So you need to smelt **11** items to craft a blast furnace (**5** iron, **3** cobblestone to make stone and **3** stone to make smooth stone).

Without a blast furnace you just need to smelt **11** items, with a blast furnace you need to smelt **11** items for even making it and additional **5.5** items for tools (**11** items two times faster).

**So it is currently not worth making a blast furnace even when we don't consider additional resources we use such as coal etc.**

### Rewrote most of `BeatMinecraftTask`

I changed the way tasks are selected to be more dynamic.

Each task gets some priority and is executed according to it,
if you want to understand this more in depth you can look at the [wiki](https://github.com/MiranCZ/altoclef/wiki/2:-Documentation:-The-task-priority-system).

### Useless items
The bot has a long handwritten list (hopefully a config in the future as well) of useless items that it throws out as soon as it picks them up.

### Chest looting
It now loots almost any chest, not a just ones from ruined portals and dessert temples.

### Rewrote BlockTracker
The new block tracker now keeps track of all blocks in loaded chunks instead of only scanning the ones the bot needs atm.

I plan on adding a config for this (amongst other things in the future, but its currently hardcoded).

It is not super optimized and I might edit it in the future.


### Iron pickaxe saving
Sometimes the bot would break its iron pickaxe before getting a diamond one. This caused it to mine additional 3 iron and craft another iron pickaxe.

Instead, I made it so that the bot doesn't use the iron pickaxe, when it doesn't need to once it gets to a low durability.

### Improved status overlay
The new status overlay is a bit smaller and has some color coding.

It also includes name of the class that is executing the tasks as well as the current task chain and its priority.

**OLD:**
![old_overlay.png](https://github.com/MiranCZ/altoclef/assets/98049269/16f29a8c-1e1f-4a3d-a26b-d8bd053602ca)
**NEW:**
![new_overlay.png](https://github.com/MiranCZ/altoclef/assets/98049269/6708a091-88a8-4be2-b4bc-eade779b7bc5)

