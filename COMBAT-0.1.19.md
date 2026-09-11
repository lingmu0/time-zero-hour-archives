# 0.1.19 · 二阶段攻势调整（仅 NeoForge 1.21.1）

本版建立在 0.1.18 的无尽升华上，只调整二阶段攻势、黑潮、音乐和练习入口。

- 飞弹默认每 40 tick（上升）或 30 tick（停留）发射一轮；每轮基础 4 枚，Boss 低于半血时增加 2 枚。飞弹方向混合覆盖斜上、横向、下方和斜向，保留原有 24 tick 传送环预警、法杖顶端生成和施法动作。
- 黑潮默认每个上升 tick 上升 0.075 格，Boss 上升速度为 0.045 格；停留阶段不抬升。对应配置项是 secondPhaseTideSpeed。
- 配置项 secondPhaseBoltCount 和 secondPhaseBoltInterval 分别调整每轮数量和上升阶段间隔；停留阶段会在该间隔基础上再快 10 tick。
- 二阶段使用“拯救的代价”，注册名为 music.chronicle_keeper_ascension；第一阶段 BGM 和零刻唱片不变。
- time:ascension_challenge_sigil 右键会跳过第一幕进入二阶段练习战，仍保留白光转场和完整平台/黑潮机制；它不推进研究、不发放遗迹奖励。潜行右键可从圣域返回。

本版只完成构建、逻辑测试和资源静态检查，未启动 Minecraft。BGM 的再分发授权仍需在公开发布前由项目维护者向原作者确认。
