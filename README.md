# Quark Woven (Fabric 1.20.1)

An **unofficial** Fabric port of [Quark](https://quarkmod.net) for Minecraft 1.20.1. The content is the same as Quark 1.20.1 on Forge.

The mod ids are still `quark` and `zeta`, so worlds, data packs and resource packs made for the Forge version work with it.

> This project is not affiliated with, endorsed by or supported by the Quark team or Violet Moon. Please report problems with this port here, not to Quark.

## Installation

Like Quark on Forge, Quark Woven needs the Zeta library. Put these in your `mods` folder:

- **Quark Woven** (`QuarkWoven-1.20.1-*.jar`)
- **Zeta Woven** (`ZetaWoven-1.20.1-*.jar`): the Fabric port of Zeta. Its version is the upstream Zeta version plus `.woven.N`; each Quark Woven release accepts a matching range of Zeta Woven versions. The Forge version of Zeta does not work on Fabric.
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port), which keeps the config files in the same format as on Forge

Optional:

- **Quark Woven Oddities** (`QuarkWovenOddities-1.20.1-*.jar`, mod id `quarkoddities`) turns on the Oddities content: backpacks, crates, pipes, magnets, matrix enchanting, the totem of holding and the tiny potato. Like Quark Oddities on Forge, it contains no content itself; it only tells Quark Woven to enable it. Without it the Oddities content is disabled, as on Forge without Quark Oddities.
- [Mod Menu](https://modrinth.com/mod/modmenu) adds a mod list from which Quark's config can also be opened. Without it, the config is opened with the "q" button on the title screen and the pause menu, as on Forge.

## Differences from Quark on Forge

- No integrations with other mods yet: Quark's JEI, Lootr, TerraBlender and land-claim support are not ported.
- Wall hanging signs made from Quark wood drop hanging signs. On Forge they drop regular signs, which is an upstream bug.
- After a config reload, recipe viewers may keep showing the old ingredients for config-dependent recipes; the recipes themselves follow the new config.
- At startup the log says "No data fixer registered for ..." for each Quark entity. Vanilla prints this for every modded entity and it is harmless.

## Credits

- **Quark** by Vazkii, WireSegal, MCVinnyq, Sully and other contributors. Website <https://quarkmod.net>, source <https://github.com/VazkiiMods/Quark>. This port is based on its `1.20` branch (commit `c1bb062`).
- **Zeta** by Vazkii, quat, IThundxr, siuol, wiresegal, MehVahdJukaar and other contributors. Source <https://github.com/VazkiiMods/Zeta>. This repository contains the source of its `1.0-29.135` release, ported and released as Zeta Woven.
- **Java Universal Tween Engine** by Aurelien Ribon. Source <https://github.com/AurelienRibon/universal-tween-engine>, Apache License 2.0; included with Quark in `src/main/java/aurelienribon`.

## Changes

This project is an adaptation of the works above. The main changes:

- Zeta's Forge implementation is replaced by a Fabric one (`org.violetmoon.zetaimplfabric`), and the code in Quark and Zeta that used Forge APIs is adapted.
- Forge conditions and `forge:` tags in the data packs are converted to their Fabric equivalents (`fabric:load_conditions`, `c:` tags).
- Third-party integrations are not included yet.

The unmodified upstream source is the first commit of this repository; diff against it with git to see every change. Notes on the port (in Chinese) are in [PORTING.md](PORTING.md).

## Building

```
./gradlew build
```

Gradle needs Java 21; the mods themselves run on Java 17 or newer. Quark Woven is built to `build/libs`, Zeta Woven to `zeta/build/libs` and Quark Woven Oddities to `oddities/build/libs`. `./gradlew runGametest` runs the game tests; PORTING.md lists the other tasks, including the checks against the released jars.

## License

Quark and Zeta are released under [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) (Attribution-NonCommercial-ShareAlike 3.0). As an adaptation, this port is released under the same license; the full text is in [LICENSE.md](LICENSE.md) (for Zeta, [zeta/LICENSE.md](zeta/LICENSE.md)):

- no commercial use;
- if you redistribute or adapt this project, keep the credits and license notice above, say what you changed, and release it under the same license.

The Java Universal Tween Engine is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0); its license and notice are in [src/main/java/aurelienribon/LICENSE](src/main/java/aurelienribon/LICENSE) and [NOTICE](src/main/java/aurelienribon/NOTICE), and are also included in the jar.

The icons of Quark Woven, Zeta Woven and Quark Woven Oddities were made for this port and are not Quark's logo.
