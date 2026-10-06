# 移植进度

## 阶段

1. [x] 导入上游源码、搭建 Fabric Loom 多项目构建、AT → AW
2. [x] Zeta 核心去 Forge 化
3. [x] `zetaimplfabric`：FabricZeta、事件总线、事件实现与触发点、注册、网络、配置、能力、方块/物品扩展钩子
4. [x] Quark 本体中直接使用 Forge API 的代码（编译通过）
5. [x] 资源/数据转换：`forge:conditions` → `fabric:load_conditions`，`forge` 标签 → `c` 标签（Quark 的数据里没有 Forge 生物群系修改器/战利品修改器文件）
6. [ ] 开发环境实际运行，逐模块对照 Forge 版验证（进行中：客户端能进入标题界面，数据加载与部分钩子已有自动测试）
7. [ ] 第三方联动（EMI/JEI、Trinkets 代替 Curios、TerraBlender、Lootr、Flan）

## 开发命令

- `./gradlew build`：构建（含 AW 校验），产物在 `build/libs`，Zeta 以 jar-in-jar 打包。
- `./gradlew runClient`：开发客户端。只有根项目有运行配置，`:zeta` 的运行任务已禁用。
- `./gradlew runMixinAudit`：启动客户端，在客户端初始化时应用全部 Mixin（`MixinEnvironment.audit()`）后退出。想一次看到所有失败的 Mixin 时，先把各 `*.mixins.json` 的 `required` 临时改为 `false`。
- `./gradlew runGametest`：无头游戏测试服务端（不需要 EULA），运行 `src/gametest` 中的测试后退出。目前覆盖：配方/条件/标签/战利品表加载、熔炼配方的 Forge 式物品堆结果、书架附魔加成、树苗种在树篱上、绳子可攀爬。

## 数据转换

- 配方与进度的 `conditions` 改为 `fabric:load_conditions`：`forge:and/or/not` → `fabric:and/or/not`，`forge:mod_loaded` → `fabric:all_mods_loaded`，Quark 自己的条件（`quark:flag` 等）字段不变，只把 `type` 换成 `condition`。
- `forge:` 标签改为 `c:` 标签，命名规则：`ingots/iron` → `iron_ingots`、`storage_blocks/apple` → `apple_blocks`、`gems/diamond` → `diamonds`、`tools/bows` → `bows`、`glass` → `glass_blocks`、生物群系 `is_x` → `x`（`is_overworld` → `in_overworld`）。每个被引用的标签都按 Forge 1.20.1 自带的内容（加上 Quark 原有的追加）完整提供，因此配方匹配的物品与 Forge 版一致。`ZetaTags` 中使用的名字与此一致。
- 熔炼类配方的结果允许写成 `{"item": ..., "count": ...}`（Forge 扩展，`SimpleCookingSerializerExtMixin`）。
- `attribute_tooltips.json` 为 Reach Entity Attributes 的 `reach` / `attack_range` 增加了与 Forge `block_reach` / `entity_reach` 相同的条目。

## 原版调用点挂接的 Forge 钩子

`zetaimplfabric/mixin/hook`（客户端为 `mixin/client/hook`）让原版在 Forge 调用 IForgeBlock/IForgeItem 的位置调用 Zeta 扩展。只有方块/物品类覆写了对应钩子时才生效（`ZetaHooks`），其余保持原版行为——Zeta 的部分默认实现（如 `canEquipZeta` 返回 false）与原版不同。

- 已挂接：可燃性/火焰蔓延/能否着火、梯子、植物支撑（`BushBlock`）、信标颜色、附魔加成、碰撞高度（`Entity#getOnPos`）、流体覆盖贴图（Fabric `FluidRenderHandlerRegistry#setBlockTransparency`）、工具修改方块状态（斧/锹）、推拉反应、物品实体更新、潜行绕过、盔甲槽可装备、附魔书可附魔（铁砧）、最大耐久/可损耗/耐久损耗/耐久条、附魔值、默认隐藏提示、重新装备动画、鞘翅飞行（Fabric `EntityElytraEvents.ALLOW`）、附魔等级/全部附魔（`ForgeItemEnchantmentsMixin`）。
- 生物破坏（mobGriefing）事件：所有对该游戏规则的读取都会触发，归属于正在 tick 的实体；投射物/火球归属于发射者、凋灵玫瑰归属于凶手，与 Forge 一致。
- 未挂接（Quark 中没有覆写或只在暂不启用的联动中使用）：位置相关光照、开启活板门可攀爬、潮涌核心框架、脚手架、`onItemUseFirst`（仅 Lootr 箱子）、`isRepairable`（仅回旋镐，且总返回 true，与原版对可损耗物品的判断一致）、熔岩点燃（`LavaFluid`）。

## 待办

- `SimpleFluidloggedBlock#getPickupSound(BlockState)`：需在 `BucketItem` 中调用。
- 配方书放置（`PlaceRecipe`）对 `IZetaShapedRecipe`（`ExclusionRecipe`、`MixedExclusionRecipe`）的宽高支持。
- 锄头耕地（`hoe_till`）的 `getToolModifiedState` 挂接。
- 在开发客户端中进入世界，逐模块对照 Forge 版验证（世界生成、GUI、网络、配置界面等）。
- 第三方联动。

## 已知差异

- `FlagIngredient` 改为 Fabric `CustomIngredient`，`getItems()` 结果由 Fabric 缓存，配置热重载后 JEI 显示可能不刷新（匹配逻辑本身是实时的）。
- 可燃性钩子拿不到 Forge 传入的朝向（原版不记录），传 null；Quark 的实现都不使用它。
- 生物破坏事件在实体 tick 之外（如玩家操作）读取规则时，事件中的实体为 null。
- 原版没有为模组实体注册数据修复器，启动时会打印 “No data fixer registered for …”（不影响功能）。
