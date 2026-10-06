# 移植进度

## 阶段

1. [x] 导入上游源码、搭建 Fabric Loom 多项目构建、AT → AW
2. [ ] Zeta 核心去 Forge 化（进行中）
3. [ ] `zetaimplfabric`：FabricZeta、事件总线、事件实现与触发点、注册、网络、配置、能力、方块/物品扩展钩子
4. [ ] Quark 本体中直接使用 Forge API 的代码（约 98 个文件）
5. [ ] 资源/数据转换：`forge:conditions` → `fabric:load_conditions`，`forge` 标签 → `c` 标签，Forge 生物群系修改器、战利品修改器等
6. [ ] 开发环境实际运行，逐模块对照 Forge 版验证
7. [ ] 第三方联动（EMI/JEI、Trinkets 代替 Curios、TerraBlender、Lootr、Flan）

## 事件触发点（Fabric 侧需要实现）

Quark + Zeta 共监听 112 种 Zeta 事件，每种都需要在 `zetaimplfabric` 中有实现类和触发点（Fabric API 回调或 Mixin）。

## 需要在原版调用点挂接的 Forge 钩子

由 `IZetaBlockExtensions` / `IZetaItemExtensions` 承载，需在 Fabric 侧用 Mixin 让原版代码调用：

- 方块：光照（位置相关）、梯子、开启活板门可攀爬、植物支撑、潮涌核心框架、附魔加成、位置相关音效、信标颜色、黏性（活塞，Zeta 自带解析器）、可燃性/火焰蔓延、碰撞高度、流体覆盖贴图、工具修改方块状态（斧/锹/锄）、脚手架
- 物品：右键优先使用、可修复、物品实体更新、潜行绕过、可装备、附魔书可附魔、盔甲贴图、最大耐久、可剪、附魔值、附魔台可附魔、附魔等级、全部附魔、重新装备动画、燃烧时间、耐久损耗、末影人面具、鞘翅飞行、默认隐藏提示
- `SimpleFluidloggedBlock#getPickupSound(BlockState)`：需在 `BucketItem` 中调用

## 已知差异

- `FlagIngredient` 改为 Fabric `CustomIngredient`，`getItems()` 结果由 Fabric 缓存，配置热重载后 JEI 显示可能不刷新（匹配逻辑本身是实时的）。
