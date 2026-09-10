# 0.1.17 相位提示与战斗配置

- 现在阶段安全区改为真正的逆时针顺序：西北 → 西南 → 东南 → 东北。
- 未来债务提示复用现在阶段的方位与相位文本，不再显示颜色区域。
- `config/time-common.toml` 新增并支持热加载：`bossHealth`、`bossDamage`、`echoCount`、`echoHealth`。
- `echoCount` 控制过去残响每轮生成数量，`echoHealth` 控制残响生命值；默认值分别为 3 和 18。
- 主版本仍按 NeoForge 1.21.1 构建；Forge 1.20.1 移植位于 `forge-1.20.1` 分支。
