# Quark Unofficial（Fabric 1.20.1）

[Quark](https://quarkmod.net) 的**非官方** Fabric 移植版，目标 Minecraft 1.20.1，内容与 Quark 1.20.1 Forge 版保持一致。

> 本项目与 Quark 原作者及 Violet Moon 没有关联，也未经其认可或支持。使用本移植版遇到的问题请在本仓库反馈，不要报告给原版 Quark。

## 署名

- **Quark**：Vazkii、WireSegal、MCVinnyq、Sully 及其他贡献者。官网 <https://quarkmod.net>，源码 <https://github.com/VazkiiMods/Quark>。本项目基于其 `1.20` 分支（提交 `c1bb062`）。
- **Zeta**：Vazkii、quat、IThundxr、siuol、wiresegal、MehVahdJukaar 及其他贡献者。源码 <https://github.com/VazkiiMods/Zeta>。本项目内置其 `1.0-29.135` 版的源码。
- **Java Universal Tween Engine**：Aurelien Ribon。源码 <https://github.com/AurelienRibon/universal-tween-engine>，以 Apache License 2.0 发布，随 Quark 内置于 `src/main/java/aurelienribon`。

## 修改说明

本项目是上述作品的改编作品，主要改动：

- 用 Fabric 实现（新增 `org.violetmoon.zetaimplfabric`）替换 Zeta 的 Forge 实现，并修改 Quark 与 Zeta 中依赖 Forge API 的代码；
- 把数据包中的 Forge 条件和 `forge:` 标签转换为 Fabric 格式（`fabric:load_conditions`、`c:` 标签）；
- 暂不包含第三方模组联动。

未修改的上游源码保存在本仓库的第一个提交中，可用 git 与之对照。具体改动见提交历史和 [PORTING.md](PORTING.md)。

## 许可证

Quark 与 Zeta 以 [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/)（署名-非商业性使用-相同方式共享 3.0）发布。本移植版作为改编作品，同样以 CC BY-NC-SA 3.0 发布，协议全文见 [LICENSE.md](LICENSE.md)（Zeta 部分见 [zeta/LICENSE.md](zeta/LICENSE.md)）：

- 不得用于商业目的；
- 再发布或改编本项目时，须保留上面的署名与许可证说明，标明所做的修改，并以相同协议发布。

Java Universal Tween Engine 部分适用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)。

## 上游 README（原文）

以下是 Quark 原版的 README。其中的发布流程和签名说明只适用于原版 Quark：本项目的发布物不带 Violet Moon 的签名。

---

Small things. Learn more at [the Mod's website](https://quarkmod.net).

This mod makes use of the [Java Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by Aurelien Ribon, licensed under the Apache 2.0 License.  

### Zeta?

*Project Zeta* is a planned rearchitecture of Quark to make it less tightly-integrated with Forge. Read the roadmap [in Vazkii's post on the Violet Moon forums](https://forum.violetmoon.org/d/78-project-zeta-aka-quark-on-fabric-real).

There is still lots to do! If you'd like to contribute, see `zeta-todo/README.md`

### Release Process
Quark's release process is mostly automated. Here's the steps:

1. Pull master so you're up to date, make sure everything is committed
2. Run `git tag -a release-<mc_version>-<build_number>`. If you don't know or remember what those are, look at `build.properties`
3. In the editor that pops up, write the changelog
4. In `build.properties`, increment the build_number by one for the next version. Commit this.
5. Push master and the release tag: `git push origin master release-<mc_version>-<build_number>`
6. Shortly after, the mod should be automatically uploaded to GitHub's release tab, Modrinth, and CurseForge.

### Signing
Releases are signed with the Violet Moon signing key, see [this
page](https://github.com/VazkiiMods/.github/blob/main/security/README.md) for information
about how to verify the artifacts.
