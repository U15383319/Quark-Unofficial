# 项目简介

Quark-Unofficial 仓库是 Quark 模组的 Fabric 非官方移植版，发布名为 **Quark Woven**（Zeta 的移植版为 **Zeta Woven**，单独发布），目标 Minecraft 版本为 `1.20.1`，内容与 1.20.1 Forge 版保持一致。只有显示名称、描述和 jar 文件名用 Woven；模组 ID 与包名保持 `quark` / `zeta` / `org.violetmoon.quark` 不变，以保证方块物品 ID、数据包、资源包和存档兼容。

# 移植来源

- Quark：原项目位于 `https://github.com/VazkiiMods/Quark`，使用 `1.20` 分支（Forge 1.20.1，提交 `c1bb06230edad49552296444a7b5706463e82252`）。
- Zeta：Quark 依赖的模块化框架，原项目位于 `https://github.com/VazkiiMods/Zeta`。本项目内置的是 `org.violetmoon.zeta:Zeta:1.0-29.135` 的源码（来自 maven.blamejared.com 的 sources/pure jar，与 Quark 1.20 实际依赖版本一致），另合入了上游 1.0-30 唯一的改动（`RecipeCrawlHandler` 捕获其他模组 `getRecipeRemainder` 抛出的异常）。
- 本机上游克隆的位置等本地信息记在 `CLAUDE.local.md` 中（已被 `.gitignore` 忽略，不提交）。
- 未修改的上游源码保存在本仓库第一个提交 `060cda2` 中，可用 `git show 060cda2:<路径>` 对照原始 Forge 代码（包括已删除的 `zetaimplforge`）。

# 构建

- Gradle 多项目：根项目为 Quark（`src/`），子项目 `:zeta` 为内置的 Zeta（`zeta/`），作为独立模组 Zeta Woven 单独发布（不打包进 Quark）。子项目 `:oddities`（`oddities/`）是 Quark Woven Oddities，模组 ID `quarkoddities`，没有代码：正式环境里 Quark 只有在这个 ID 的模组加载时才启用 oddities 类别（`Quark.ODDITIES_ID`，对应 Forge 的 Quark Oddities 附加模组；开发环境始终启用）。Zeta Woven 的版本号是上游版本加 `.woven.N`（`gradle.properties` 中的 `zeta_version`），Quark 依赖的范围由 `zeta_version_range` 填入 `fabric.mod.json`；改动 Zeta 后发布时要递增 N，并按需调整范围。
- Fabric Loom（`net.fabricmc.fabric-loom-remap`），映射为 Mojang 官方映射 + Parchment `2023.09.03`（与上游 Forge 构建相同）。
- 配置系统依赖 Forge Config API Port（从 Modrinth maven 获取），使 Zeta 基于 `ForgeConfigSpec` 的 TOML 配置与 Forge 版完全一致。
- 访问转换器（AT）已转换为访问加宽器（`quark.accesswidener`、`zeta.accesswidener`）。Loom 只在子项目自己的编译中应用其 AW，所以 Zeta 的 AW 条目也复制到了 Quark 的 AW 中。

# Java 源码目录

- `zeta/src/main/java/org/violetmoon/zeta/`：Zeta 平台无关核心（模块系统、事件总线、配置、注册、网络等）。仅做了去 Forge 化的最小改动。
- `zeta/src/main/java/org/violetmoon/zetaimplfabric/`：Zeta 的 Fabric 实现（替代原来的 `zetaimplforge`），包括 `FabricZeta`、事件总线与事件实现、注册、网络、配置、能力（capability）以及挂接原版行为的 Mixin。
- `src/main/java/org/violetmoon/quark/`：Quark 本体。
  - `base`：入口、代理、配置、网络、通用处理器。
  - `content`：按类别（automation、building、client、management、mobs、tools、tweaks、world、experimental）划分的功能模块。
  - `addons/oddities`：原 Quark Oddities 内容。
  - `mixin`：Quark 自身的 Mixin。
  - `integration`：第三方模组联动（第一阶段暂不启用）。

# 移植约定

- Forge 给原版类打补丁新增的方法（如 `IForgeBlock`、`IForgeItem` 中的钩子）统一改走 Zeta 的 `IZetaBlockExtensions` / `IZetaItemExtensions`，再由 `zetaimplfabric` 中的 Mixin 在原版调用点调用。
- Forge 事件统一由 Zeta 事件接口表示；Fabric 侧由 Fabric API 回调或 Mixin 构造 `zetaimplfabric` 中的事件实现并触发。所有 Zeta 实例共享一条 play 总线（对应 Forge 的 `MinecraftForge.EVENT_BUS`），load 总线每个模组一条。
- Forge 的 `IItemHandler` / Capability 改用 Fabric Transfer API；Zeta 的 `ZetaCapabilityManager` 在首次查询对象时触发附加事件并缓存。
- 数据文件中的 Forge 条件（`forge:conditions`）需转换为 Fabric 资源条件（`fabric:load_conditions`），自定义配料使用 `fabric:type`。
- 移植进度与待办见 `PORTING.md`。
