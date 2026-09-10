# 0.1.2 实测记录

日期：2026-09-09（Asia/Shanghai）。Minecraft 1.21.1 / NeoForge 21.1.233 / Java 21；所有游戏测试均使用隔离的开发模块和新建世界，未安装到整合包，未操作现有存档。以下是自动化实际运行与截图检查，不是人工完整通关或多人压力测试。

## 已修复与完善

- 0.1.2 拆分角色模型：抄写员保留 33 方块 / 20 骨骼旧模型；编年者更换 256 方块 / 84 骨骼独立模型、512×512 贴图、局部发光及多部件动画。最终假身同步新模型，普通残响保持旧模型。战斗数值与碰撞箱未变。

- 修复自然生成阶段告示牌因尚未关联 Level 而崩溃的问题；直接通过 NBT 加载文本和蜡封状态，兼容 ProtoChunk。
- 探针解析真实地表入口，不再把原版结构定位的 Y=0 当作入口；已有 Y=0 缓存也会修正。
- 地下遗迹入口增加安全落脚平台和门前清理，室内增加顶灯与地灯；观测台增加铜饰边、角塔和屋顶钟盘。
- Boss 环形攻击先预警再结算，并以水平距离判定环带；保留阶段边界伤害限制。
- 早期 Blockbench MCP 编辑的肩甲、腕甲、表冠与双指针模型现在由抄写员使用；新编年者在独立工程中完成。导出器同时校验两套 Blockbench 5 模型。
- 研究台改为原版 176×166 灰色容器、原版凹槽、大输出槽与熔炉进度箭头；九列背包与实际槽位坐标对齐。详细消耗说明放在提示文字的悬停说明中。
- 研究配方不再触发未知配方书分类；菜单支持不带方块坐标的通用打开方式。

## 编译与静态检查

`gradlew.bat build`：Java 编译、资源处理、发布 jar 与 31 项独立状态断言。

`node tools/verify-resources.mjs`：86 个 JSON 文档、117 个引用、24 个物品模型、九组谜题线索，以及两套共 289 个方块的 UV、Java、主贴图和发光遮罩一致性。

产物：[time-0.1.2-1.21.1.jar](build/libs/time-0.1.2-1.21.1.jar)。测试源码位于 `src/validation`，不进入发布 jar。

## 服务端 GameTest：8 组

在真实 ServerLevel 中运行注册的方块、方块实体、实体、配方和伤害事件，使用 FakePlayer 交互；并非仅测试纯数学函数。

| 用例 | 覆盖范围 |
|---|---|
| observatory_chain_and_persistence | 观测台三谜题、告示牌、考古材料、开门、部分状态重载、个人成果去重 |
| archive_rhythm_and_guardian | 排序、镜像、真实 tick 钟声、守卫生成与死亡奖励、入口平台 |
| research_real_ticks_and_output_backpressure | 实际研究 tick、三配方、原件保留、输出堵塞不吞材料 |
| boss_shields_damage_gates_and_rewards | 三印记、收费启动、四锚破盾、超大伤害阶段边界、胜利奖励 |
| wipe_retry_does_not_charge_second_key | 全员离场重置、冷却、免费重新生成 Boss |
| boss_memory_echoes_control_rewind | 保留残响导致回溯回血、摧毁三残响阻止回血 |
| boss_quadrant_and_paradox_defenses | 安全象限、错误攻击惩罚、假身无敌与碰撞尺寸 |
| boss_and_menu_reload_contracts | Boss UUID/NBT 恢复、未完成破盾续接、菜单空载荷、原版槽位边界和 Shift 点击转移 |

机器报告：[runtime-gametests.json](build/reports/runtime-gametests.json)。重复运行使用唯一新世界名，避免上次 SavedData 的已解谜状态污染结果。

回归中发现并修正测试启动时序问题：强制加载的区块并非立即开始实体追踪和 tick，立即按 UUID 查询新 Boss 或在第 5 tick 检查残响都可能过早。现先确认控制器成功分配战斗 UUID，再用有限超时的 GameTest 序列等待该 Boss 可查询、首次真实残响出现，随后相对计时验证回血与阻止回血。原版实体管理器只把可追踪区块内实体加入可查询列表；日志也记录到世界经过 5 tick 时 Boss 只执行了 2 tick。未通过延长 Boss 技能时间、修改伤害或跳过断言掩盖问题。修正后本轮 8 组测试均通过。

## 客户端自然生成与画面

实际启动带图形渲染的客户端，在正常主世界、种子 `20260909` 中查找并生成三座遗迹；通过真实探针使用逻辑核对入口坐标。原版 `/locate` 返回 Y=0，因此测试特意验证旧探针缓存的修复。

| 遗迹 | 地表入口 X / Y / Z | 自然生成与探针 |
|---|---|---|
| 残刻观测台 | -280 / 73 / 65 | 通过 |
| 逆时档案馆 | 168 / 71 / 209 | 通过 |
| 零点钟室 | 40 / 65 / 353 | 通过 |

机器报告：[visual-runtime.json](build/reports/visual-runtime.json)，包含生成时间和测试存档目录。客户端原生截图：

- [观测台外观](run-validation-client/screenshots/01-observatory-exterior.png)
- [观测台谜题](run-validation-client/screenshots/02-observatory-puzzle.png)
- [档案馆入口](run-validation-client/screenshots/03-archive-entrance.png)
- [镜像房间](run-validation-client/screenshots/04-archive-mirror.png)
- [钟室入口](run-validation-client/screenshots/05-clockroom-entrance.png)
- [Boss 场地与模型](run-validation-client/screenshots/06-boss-arena.png)
- [原版风格研究界面](run-validation-client/screenshots/07-research-interface.png)
- [假身／新编年者／抄写员昼间对比](run-validation-client/screenshots/08-model-comparison-day.png)
- [夜间局部发光对比](run-validation-client/screenshots/09-model-comparison-night.png)

检查了实际界面、中文显示、物品槽对齐、进度箭头、模型和场地照明。研究 UI 通过真实方块交互打开。截图不是离线合成或 Blockbench 预览。地下入口周边仍保留自然树木，森林中可能部分遮挡远观视线。

0.1.2 的 9 个客户端场景已实际运行；`model_bindings_verified=true` 表示真实客户端中抄写员、新编年者和最终假身的贴图绑定已逐一核对。人工查看了 Boss 场景与昼夜截图，确认几何、材质和局部自发光正常。展示用实体设为无 AI，完整技能动画仍以遗迹战斗场景及运行时模型代码为准，不把展示台当成战斗平衡测试。

## Blockbench MCP

连接 `http://127.0.0.1:3000/bb-mcp`，插件 1.6.1，94 个工具；当前新项目 `chronicle_keeper_sovereign`，256 方块 / 84 骨骼 / 2 张贴图。实际执行查询、细修、完整导出与视口截图；旧模型保留，新的抄写员副本与旧原稿 SHA-256 相同。Codex 已保存名为 `blockbench` 的连接。复核命令：`node tools/blockbench-status.mjs`，报告：[blockbench-mcp.json](build/reports/blockbench-mcp.json)。

## 重复运行与边界

```powershell
$env:JAVA_HOME='C:\Users\123\.jdks\jbr-21.0.10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\tools\run-validation.ps1 -Offline -Client
```

脚本检查本轮生成的报告时间，防止把旧成功报告或单纯进程退出误判为成功。不加 `-Client` 时不启动图形客户端。Node 不在 PATH 时可提供 `-NodePath`。

尚未验收：两个以上真实联网玩家、整合包高属性装备下的战斗数值、完整人工生存通关、所有种子和生物群系地形衔接、全部资源包/GUI 缩放组合、长时间区块卸载及进程重启后的多人恢复。Boss 的未来债务完整战斗体验和全部攻击时机仍需人工体验测试；现有用例不能替代这些验证。没有修改 FTB Quests、KubeJS 器官或整合包配方。
