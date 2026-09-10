# 两套独立 Blockbench 模型

从 0.1.2 起，失序抄写员与终末编年者不再共享模型与贴图。

| 角色 | 当前源文件 | 几何与贴图 | 运行时 |
|---|---|---|---|
| 失序抄写员 | `archive_scribe.bbmodel` | 原模型完整保留：33 方块 / 20 骨骼，256×256 | `ChronalGeometry` / `ChronalModel`，`archive_scribe.png` |
| 终末编年者 | `chronicle_keeper_sovereign.bbmodel`（几何）、`chronicle_keeper_casts.bbmodel`（动作工程） | 267 方块 / 97 骨骼，512×512 主贴图与发光遮罩 | `KeeperGeometry` / `KeeperModel`，`chronicle_keeper.png`、`chronicle_keeper_emissive.png` |
| 普通回声、记忆残响 | 使用抄写员骨架 | 缩小版本 | `TemporalEchoRenderer` |
| 最终阶段假身 | 使用新编年者骨架 | 与 Boss 同贴图、尺寸、阴影和发光，钟环反向转动 | `TemporalEchoRenderer` |

旧 `chronicle_keeper.bbmodel` 保留为改版前原稿，不再作为默认导出入口。`archive_scribe.bbmodel` 是从该原稿完整复制的，本次拆分时两文件 SHA-256 一致。`chronicle_keeper-before-mcp.bbmodel` 是更早期肩甲改动前的备份。

## 新编年者造型

“钟律君王”：象牙面具与五尖冠、沙漏胸腔、金铜包边礼甲、五片后披风与前甲裙、24 分段外钟环、12 分段内钟环、悬浮钟杖、展开的档案书和散页。独立 UV 区域区分象牙、青铜、鎏金、暗甲和孔雀青；额心、眼部、沙漏与刻度等使用透明背景的局部发光遮罩，不把整副护甲照亮。

运行时动画分别驱动内外钟环、长指针、摆锤、头部追踪、双臂、披风、钟杖、书本与散页。每帧重置骨骼姿态，避免多个实体共享渲染器时姿态叠加。血量阶段与护盾影响钟环转速，未改动战斗数值和碰撞箱；外围钟环等属于装饰。

- [新编年者：Blockbench 实际视口](chronicle_keeper_sovereign-preview.png)
- [旧模型：Blockbench 实际视口](chronicle_keeper-preview.png)
- [游戏内 Boss 场景](../run-validation-client/screenshots/06-boss-arena.png)
- [昼间对比](../run-validation-client/screenshots/08-model-comparison-day.png)
- [夜间发光对比](../run-validation-client/screenshots/09-model-comparison-night.png)

对比图由真实客户端拍摄，从左到右为假身、新编年者、抄写员。旧 `chronicle_keeper_preview.png` 则是早期程序化投影，不是当前模型或游戏截图。

## 编辑与导出

1. 在 Blockbench 打开对应角色的当前源文件；两者已隔离，不要误改旧原稿。
2. 保留 Box UV、嵌入式贴图和 `root`、`dial`、`pendulum`、`arms` 基础骨骼。
3. 新 Boss 还需保留 `head`、`inner_halo`、`halo_hand`、`mantle`、`chronostaff`、`held_book`、`staff_arm`、`book_arm`、`staff_hand`、`book_hand`、`ghost_tail`、`orbit_page_0/1/2` 的层级和名称。立方体旋转应放到骨骼上。
4. 修改新 Boss 的 UV 后，同时更新对应发光遮罩 `chronicle_keeper_emissive.png` 并嵌入工程；不需要发光的像素必须透明。不要把遮罩指定给实际模型表面。
5. 在项目根目录运行：

```powershell
node tools/export-bbmodel.mjs          # 导出抄写员、编年者和立体飞弹
node tools/export-bbmodel.mjs scribe   # 只导出抄写员
node tools/export-bbmodel.mjs keeper   # 只导出编年者和发光遮罩
node tools/export-bbmodel.mjs bolt     # 只导出立体钟械飞弹
node tools/verify-resources.mjs
.\gradlew.bat build
```

导出器兼容 Blockbench 4 内联骨骼与 Blockbench 5 分离的 `groups` / UUID 层级，并保留镜像、膨胀与骨骼旋转。常规构建使用已生成的 Java 和 PNG，不依赖 Blockbench 或 Node，也未新增 GeckoLib 等运行时依赖。

`create-keeper-sovereign.mjs` 只用于初始几何与材质构建，拒绝覆盖已有文件；其初稿 255 方块，后经 MCP 在 Blockbench 加入额心刻印，最终 256 方块。请用当前源文件继续美术编辑。旧 `generate-model-assets.mjs` 在模型拆分后会主动停止，防止覆盖新 Boss 的贴图。

## MCP

已连接 [Blockbench MCP 插件](https://github.com/jasonjgardner/blockbench-mcp-plugin) 1.6.1，服务地址 `http://127.0.0.1:3000/bb-mcp`。本次新建独立工程、载入几何、在编辑器中细修额心、导出完整 Blockbench 5 工程并截图；旧工程未关闭或覆盖。

Codex 配置名为 `blockbench`。运行 `node tools/blockbench-status.mjs` 可只读查询当前工程，报告写入 `build/reports/blockbench-mcp.json`；命令不会修改或保存模型。

0.1.12：三套施法动作为飞弹、场地环带、高举下砸。共享数据为 keeper-casts.json，分别执行 tools/export-keeper-casts.mjs 和 tools/export-keeper-socket.mjs 导出客户端动画及服务端杖顶/砸击坐标；两者支持 --check。直接修改 BB 关键帧后需要同步共享数据，不能只保存 BB 而遗漏运行时代码。

用户已授权后续 MCP 未响应时自行检查并启动 Blockbench 后重连。启动前检查是否已有进程，避免重复启动；不得覆盖其他已打开或未保存工程。本轮按要求只运行离线检查，不启动 Minecraft。

0.1.13：下砸源曲线使用 `interpolation: monotone`，由 `cast-clip-utils.mjs` 以半 tick 间隔烘焙，MCP、客户端与服务端插槽共用相同结果。动作不再向外移动手腕，不为零穿模而外撇肩膀。仅下砸法杖与身体的短暂交叠被允许；书本悬浮、飞弹/环带施法的检查保留。旧动画备份为 `chronicle_keeper_casts.before-0.1.13.bbmodel`。

新增 `chronal_bolt.bbmodel`：27 立体方块、15 骨骼、64×32 自制配色图集。游戏使用 `ChronalBoltGeometry` 与 `ChronalBoltRenderer`，不是 `ThrownItemRenderer` 或贴图广告牌；沿速度方向飞行，钟环旋转、能量核心脉动。项目已通过 MCP 在独立 BB 标签中检查并保存。`create-chronal-bolt.mjs` 仅为首次创建器，拒绝覆盖现有模型；`mcp-preview-chronal-bolt.mjs` 仅在确认当前工程已保存后使用。
