# AltoClef
*Plays block game.*

*Powered by Baritone.*

A client side bot that tries to beat Minecraft on its own...

**This fork is still under development and is nowhere near perfect, if you have any questions, suggestions, ideas or find a bug don't hesitate to reach out!
You can use the [issues](https://github.com/MiranCZ/altoclef/issues). Or contact me on discord!**

Became [the first bot to beat Minecraft fully autonomously](https://youtu.be/baAa6s8tahA) on May 24, 2021.

**Join the [Discord Server](https://discord.gg/JdFP4Kqdqc)** for discussions/updates/goofs & gaffs

## About this fork
This fork aims to optimize `MarvionBeatMinecraftTask` (I will just refer to it as `BeatMinecraftTask`) from [Marvion's fork](https://github.com/MarvionKirito/altoclef) by trying to fix a lot of cases where the bot gets stuck and improving some of the tasks.

You can look at the [changelog](changelog.md) if you are interested.

Because I rewrote a good portion of the `BeatMinecraftTask` a lot of the config settings don't work. Although I plan to implement configs in the future of course.  

## The preprocessor
I am currently using the [replay mod preprocessor](https://github.com/ReplayMod/preprocessor) to keep the mod updated across multiple versions at the same time.

### Versions
Thanks to that, the mod is currently available on **fabric** for the following versions:
- 1.20.1
- 1.20.2
- 1.20.4
- 1.20.6

*note: All the versions use the "same release" of Alto clef, although some of them use older versions of baritone.* 


## How it works

Take a look at this [Guide from the wiki](https://github.com/MiranCZ/altoclef/wiki/1:-Documentation:-Big-Picture)
or this [Video explanation](https://youtu.be/q5OmcinQ2ck?t=387)


## Download

**Note:** After installing, please move/delete your old baritone configurations if you have any. Preexisting baritone
configurations will interfere with alto clef and introduce bugs. This will be fixed in the future.

[Check releases](https://github.com/MiranCZ/altoclef/releases)

## [Usage Guide](usage.md)

## [TODOs/Future Features](TODO.md)

## [Development Guide](develop.md)
