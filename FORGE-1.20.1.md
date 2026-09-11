# Forge 1.20.1 移植说明

- 本分支已同步第二幕“无尽升华”：白光转场进入 time:sanctum，使用透明玻璃边框平台进行间歇式向上追逐；Boss、平台层和黑潮共用可配置的上升速度，休息期间暂停，飞弹仍继续。第二幕默认独立生命值 500，无专属跳跃增幅；直达挑战物品可从创造栏或命令获取。
- 二阶段 BGM 只在客户端实际进入 time:sanctum 且 Boss 已进入升华状态后播放，白光转场期间保留第一幕音乐。用户提供的音频当前仍是未核验再分发授权，公开发布前需要确认原作者许可。
- 两个版本的三座遗迹使用同一组较低自然生成密度：observatory 32/12、archive 40/16、clockroom 48/20（spacing/separation）。

本分支将零刻档案移植到 Minecraft 1.20.1 / Forge 47.4.23；NeoForge 1.21.1 版本在 `main` 分支。移植保持同一套内容 ID 和战斗数值：Boss 默认生命值 900、攻击伤害 10；过去阶段每轮默认生成 3 个残响，每个默认生命值 18。

已适配的版本差异包括：

- Forge 注册表和 `RegistryObject` 注册方式；
- Forge 事件总线、客户端渲染注册、生成蛋和配置 API；
- 1.20.1 的实体同步、插值、模型渲染、方块交互和 BlockEntity NBT；
- 1.20.1 自定义研究配方的 JSON/网络序列化；
- Forge 复数资源目录（`advancements`、`recipes`、`loot_tables`、`tags/entity_types`）；
- `RecordItem` 唱片实现，替代 1.21 的 jukebox 数据组件。

构建与验证：

```powershell
$env:JAVA_HOME='C:\Users\123\.jdks\jbr-17.0.14'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assemble --offline
node tools/verify-resources.mjs
node tools/verify-ascension.mjs
```

本分支只执行编译、资源处理和 JAR 产物检查，没有启动客户端或服务端。模型源仍位于 `model_source/`，音频授权说明见 `music_source/README.md`。
