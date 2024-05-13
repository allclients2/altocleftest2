# Usage Guide

Alto Clef has a variety of commands, settings and modes. This will give users an overview on how to use the bot.

Keep in mind this project is still in rapid development. A lot of features are placeholders and a work in progress.

## Commands

Commands are prefixed with `@`. Here's a list along with their functions:

| command                | description                                                                                                            | examples      |
|------------------------|------------------------------------------------------------------------------------------------------------------------|---------------|
| `help`                 | Lists all commands                                                                                                     |               |
| `gamer`                | Beats the game epic style                                                                                              | `@gamer`      |
| `reload_settings`      | Reloads the local settings file. Run this every time you want your settings to be updated.                             |               |
| `gamma {brightness=1}` | Sets the game's gamma. Useful for testing. 0 is "Moody" and 1 is "Bright", and you can go beyond to enable fullbright. | `@gamma 1000` |
| `scan`                 | Logs the nearest given block currently found be `BlockScanner`.                                                        | `@scan dirt`  |
| `status`               | Prints the status of the currently executing command. Mostly useful when running through `/msg`.                       |               |
| `stop`                 | Forcefully stops the currently running command. The shortcut `CTRL+K` also achieves this.                              |               |
| `test {testname}`      | Runs a "test" command. These vary, and will be described below.                                                        |               |


### Notable test commands

Test commands are temporary/only exist as an experiment, but some of these might be interesting.
For example, `@test terminate` runs the terminator.
Here's a list of some highlights.

**note** *this list doesn't include all the test tasks, but it is not my priority to update it...*

| test name   | what it does                                                                                                                                                                                                                                                                                         |
|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `terminate` | Runs the terminator task. When without diamond gear, the bot flees players and obtains diamond gear + food. When diamond gear is equipped, the bot hunts any nearby players down and searches for any chunks that were last seen to have players in them.                                            |
| `deadmeme`  | Expects a file to exist in your `.minecraft` directory called `test.txt`. Reads from this file, then displays the text contents of the file by placing signs in a line. Dubbed the "Bee Movie Task" for stupid reasons. Will automatically collect signs and building materials if the bot runs out. |
| `173`       | Attacks any player that doesn't have direct line of sight to the bot and stands still otherwise. Like a weeping angel.                                                                                                                                                                               | 
| `replace`   | Replace grass block within around 100 blocks with crafting tables. Frequently fails when replacing grass blocks next to water.                                                                                                                                                                       |
| `piglin`    | Collects 12 ender pearls via piglin bartering.                                                                                                                                                                                                                                                       |
| `stacked`   | Collects diamond armor, a sword and a crafting table.                                                                                                                                                                                                                                                |
| `netherite` | Same as `stacked` but for netherite gear.                                                                                                                                                                                                                                                            |
| `sign`      | Place a sign nearby that says "Hello there!"                                                                                                                                                                                                                                                         |
| `bed`       | Right clicks a nearby bed to set the bot's spawnpoint, placing one if it does not exist.                                                                                                                                                                                                             |

## Bot Settings/Configuration

After running the game with the mod once, a folder called `altoclef` should appear in your `.minecraft` directory. This
contains `altoclef` related settings and configurations.

Regarding the `altoclef_settings.json` file
Check [Settings.java](https://github.com/MiranCZ/altoclef/blob/main/src/main/java/adris/altoclef/Settings.java)

Other configs can be found in the `configs` subdirectory. Some of the `beat_minecraft.json` settings may not work, but I plan to reimplement proper settings in the future.

After modifying your local settings, run `@reload_settings` to apply your changes in-game.