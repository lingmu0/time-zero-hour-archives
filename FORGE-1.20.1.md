# Forge 1.20.1 移植说明

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
```

本分支只执行编译、资源处理和 JAR 产物检查，没有启动客户端或服务端。模型源仍位于 `model_source/`，音频授权说明见 `music_source/README.md`。
