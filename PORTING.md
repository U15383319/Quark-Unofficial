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

- `./gradlew build`：构建（含 AW 校验），Quark Woven 的产物在 `build/libs`，Zeta Woven 的在 `zeta/build/libs`，两者分开发布。
- `./gradlew runClient`：开发客户端。只有根项目有运行配置，`:zeta` 的运行任务已禁用。
- `./gradlew runMixinAudit`：启动客户端，在客户端初始化时应用全部 Mixin（`MixinEnvironment.audit()`）后退出。想一次看到所有失败的 Mixin 时，先把各 `*.mixins.json` 的 `required` 临时改为 `false`。
- `./gradlew runProductionClient` / `./gradlew runProductionMixinAudit`：用打包好的 Quark Woven 和 Zeta Woven 的 jar，加上从 Maven 下载的 Fabric API 和 Forge Config API Port，在与玩家相同的中间名（intermediary）环境中启动客户端；后者应用全部 Mixin 后退出。**发布前必须跑一遍**：开发环境用的是 Mojang 名，Mixin 目标写错时可能仍按方法名匹配成功，只有这里能发现。Mixin 的名字靠注解处理器生成的 refmap（`quark.refmap.json`、`zeta.refmap.json`）映射；`@At` 的 `args` 里的类名（如 `classValue=`）不会被映射，不要用。
- `./gradlew runProductionGametest`：在生产服务端上用发布的 jar（加上改写成中间名的测试 jar 和 Fabric API 的 gametest 模块）运行同一套 GameTest，结果在 `build/production-junit.xml`。注意 oddities 类别在正式环境默认关闭（见下），所以磁铁测试在这里会失败。测试代码里不要按名字反射访问原版成员，正式环境里名字不同。
- `./gradlew runGametest`：无头游戏测试服务端（不需要 EULA），运行 `src/gametest` 中的测试后退出，每个测试的结果写在 `build/junit.xml`。目前 27 个测试，覆盖：
  - 数据：配方/条件/标签/战利品表加载，熔炼配方的 Forge 式物品堆结果，碎玻璃掉落，两个半砖合成整块。
  - 自动化：漏斗向溜槽、板条箱、管道送物品（Fabric Transfer API），活塞推动装有物品的箱子，铁杆破坏方块，发射器放置方块，磁铁推动铁块。
  - 钩子：书架附魔加成、树苗种在树篱上、绳子可攀爬、刚玉染色信标光柱、纸墙可燃、磁石旁的混凝土粉末不凝固、简易收获（右键事件）。
  - 物品：背包掉在地上撒出物品、背包不能在附魔台附魔、铁砧不给背包上附魔书、泥刀耐久跟随配置。
  - 冒烟：所有 Quark 方块都有战利品表，各种告示牌掉落正确的物品；所有带方块实体的方块能放置并保存/读取；所有 Quark 实体能生成、tick 并保存/读取。

## 数据转换

- 配方与进度的 `conditions` 改为 `fabric:load_conditions`：`forge:and/or/not` → `fabric:and/or/not`，`forge:mod_loaded` → `fabric:all_mods_loaded`，Quark 自己的条件（`quark:flag` 等）字段不变，只把 `type` 换成 `condition`。
- `forge:` 标签改为 `c:` 标签，命名规则：`ingots/iron` → `iron_ingots`、`storage_blocks/apple` → `apple_blocks`、`gems/diamond` → `diamonds`、`tools/bows` → `bows`、`glass` → `glass_blocks`、生物群系 `is_x` → `x`（`is_overworld` → `in_overworld`）。每个被引用的标签都按 Forge 1.20.1 自带的内容（加上 Quark 原有的追加）完整提供，因此配方匹配的物品与 Forge 版一致。`ZetaTags` 中使用的名字与此一致。
- 熔炼类配方的结果允许写成 `{"item": ..., "count": ...}`（Forge 扩展，`SimpleCookingSerializerExtMixin`）。
- `attribute_tooltips.json` 为 Reach Entity Attributes 的 `reach` / `attack_range` 增加了与 Forge `block_reach` / `entity_reach` 相同的条目。

## 原版调用点挂接的 Forge 钩子

`zetaimplfabric/mixin/hook`（客户端为 `mixin/client/hook`）让原版在 Forge 调用 IForgeBlock/IForgeItem 的位置调用 Zeta 扩展。只有方块/物品类覆写了对应钩子时才生效（`ZetaHooks`），其余保持原版行为——Zeta 的部分默认实现（如 `canEquipZeta` 返回 false）与原版不同。

- 已挂接：可燃性/火焰蔓延/能否着火、梯子、植物支撑（`BushBlock`）、信标颜色、附魔加成、碰撞高度（`Entity#getOnPos`）、流体覆盖贴图（Fabric `FluidRenderHandlerRegistry#setBlockTransparency`）、工具修改方块状态（斧/锹）、推拉反应、物品实体更新、潜行绕过、盔甲槽可装备、附魔书可附魔（铁砧）、最大耐久/可损耗/耐久损耗/耐久条、附魔值、默认隐藏提示、重新装备动画、鞘翅飞行（Fabric `EntityElytraEvents.ALLOW`）、方块属性 `lootFrom`（延迟读取另一方块的战利品表；原版 `dropsLike` 在方块注册前调用会读到并缓存 `blocks/air`）、附魔等级/全部附魔（`ForgeItemEnchantmentsMixin`）、含水方块的桶装取音效（`BucketItem`）、配方书按 `IZetaShapedRecipe` 的宽高放置（`PlaceRecipe`）。
- 生物群系修改（Forge 的 `zeta:biome_modifier`，`FabricZetaBiomeModifier`）：用 Fabric `BiomeModifications` 给所有生物群系加上 Zeta 的 11 个延迟世界生成特征（Quark 的全部世界生成都挂在上面；对应的 configured/placed feature 文件在 Zeta 的 `data/zeta/worldgen`），以及各 Zeta 模组登记的生物生成和生成成本。
- 自定义氧化铜（Forge 的 `WeatheringCopperMixin`，`WeatheringCopperExtMixin`）：原版 `WeatheringCopper#getPrevious/getFirst` 对 Quark 的铜竖半砖也返回正确结果（斧头刮锈；闪电除锈由 Quark 自己的 `LightningBoltMixin` 处理）。
- 金工具附魔（`IZetaItemExtensionsMixin`）：上游用 Zeta 的 `@DelegateInterfaceMixin`，它按描述符字符串匹配目标，正式环境里不会被映射而静默失效，已改为普通接口 Mixin。
- 生物破坏（mobGriefing）事件：所有对该游戏规则的读取都会触发，归属于正在 tick 的实体；投射物/火球归属于发射者、凋灵玫瑰归属于凶手，与 Forge 一致。
- 未挂接（Quark 中没有覆写或只在暂不启用的联动中使用）：位置相关光照、开启活板门可攀爬、潮涌核心框架、脚手架、`onItemUseFirst`（仅 Lootr 箱子）、`isRepairable`（仅回旋镐，且总返回 true，与原版对可损耗物品的判断一致）、熔岩点燃（`LavaFluid`）、锄头耕地（`hoe_till`，没有 Quark 方块覆写）。

## 与上游 Forge 实现的对照审计（2026-11-11）

逐项核对了上游 `zetaimplforge` 的全部类、移植中删除的文件、Forge 专用数据文件：

- 事件：Quark 和 Zeta 监听的 114 种 Zeta 事件都有触发点（`ZGatherHints` 只在 JEI 联动中触发）。
- 入口类（`ZetaModCommonProxy`/`ClientProxy`/`ForgeZeta`/`ForgeZetaClient`）注册的处理器、配置事件、首次客户端 tick、颜色查询、盆栽注册：都有对应实现。
- 漏掉并已补上：生物群系修改器（生物生成 + 全部世界生成）、`WeatheringCopperMixin`。
- 原版方块/物品的默认扩展行为与 Forge 的 `IForgeBlock`/`IForgeItem` 默认值逐个对比：附魔加成改为按 `enchantment_power_provider` 标签判断，其余一致。
- 模块：正式环境加载 164 个模块，与源码声明一致（排除暂不启用的联动）。

## 待办

- 在开发客户端中进入世界，逐模块对照 Forge 版验证（世界生成、GUI、网络、配置界面等）。
- 第三方联动。

- oddities 类别（背包、板条箱、管道、磁铁、矩阵附魔、不死图腾、小土豆）：上游 `Quark.ODDITIES_ID` 在正式环境为 `quarkoddities`，即只有安装了 Quark Oddities 附加模组才启用，开发环境始终启用。Fabric 上还没有这个附加模组，需要决定怎么提供。

## 已知差异

- 有意修正的上游 bug：Forge 版的墙上悬挂告示牌掉落的是普通告示牌；本移植版改为掉落悬挂告示牌（`WoodSetHandler`，与原版的写法一致）。
- `FlagIngredient` 改为 Fabric `CustomIngredient`，`getItems()` 结果由 Fabric 缓存，配置热重载后 JEI 显示可能不刷新（匹配逻辑本身是实时的）。
- 可燃性钩子拿不到 Forge 传入的朝向（原版不记录），传 null；Quark 的实现都不使用它。
- 生物破坏事件在实体 tick 之外（如玩家操作）读取规则时，事件中的实体为 null。
- 原版没有为模组实体注册数据修复器，启动时会打印 “No data fixer registered for …”（不影响功能）。
- 上游 `QuarkRemapHandler`（Forge 的缺失映射事件）把 1.19 时代 Quark 的方块 ID（竹子系列、合成器、磨制凝灰岩等，后被原版收录）映射到原版 ID；Fabric 1.20.1 没有对应接口，未移植。只影响从 1.19 Forge 版升级来的存档。
