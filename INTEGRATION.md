# Soul-Forge 接入约定

## 稳定资源 ID

| 内容 | ID |
|---|---|
| 观测台 / 档案馆 / 钟室 | `time:observatory` / `time:archive` / `time:clockroom` |
| 编年者 / 抄写员 | `time:chronicle_keeper` / `time:archive_scribe` |
| 过去 / 现在 / 未来记录 | `time:past_record` / `time:present_record` / `time:future_record` |
| 过去 / 现在 / 终末结论 | `time:past_conclusion` / `time:present_conclusion` / `time:final_conclusion` |
| 原生时序样本 / 残页 / 核心 | `time:temporal_dust` / `time:chronicle_page` / `time:epoch_core` |
| 原生研究配方类型 | `time:research` |
| 研究台 | `time:research_desk` |

`time:temporal_samples` 物品标签包含原生样本和可选的 `kubejs:time_residue`。`time:chronicle_pages` 包含原生残页和可选的 `kubejs:time_lapse_page`。这些引用使用 `required:false`，独立启动不需要 KubeJS、Anatomy、Beyond Dimensions、TimeClock 或 FTB Quests。

原生残页不是 Anatomy 器官。需要接回已有器官时，可以由 KubeJS 新增转换配方/战利品转换；本模块保留对应物品标签，但不会擅自复制既有器官技能。

## 研究 JSON

```json
{
  "type": "time:research",
  "evidence": {"item": "time:past_record"},
  "catalyst": {"tag": "time:temporal_samples"},
  "reference": {"item": "minecraft:book"},
  "result": {"id": "time:past_conclusion", "count": 1},
  "duration": 400,
  "consume_evidence": false,
  "consume_reference": true
}
```

槽位为「记录、样本、参照、结果」。样本总是消耗一件；其余输入默认保留。相同配方可以连续研究，结果槽满时暂停。输入变化导致配方不匹配时清除进度。配方支持原版 ingredients 和 NeoForge 成分扩展。

## 事件

均发布到 NeoForge 游戏事件总线，可由原生代码或 KubeJS NativeEvents 监听：

- `net.xuwu.time.api.PuzzleSolvedEvent`：`getPlayer()`、`getController()`、`getKind()`。每玩家、每个奖励周期发出一次，玩家稍后回来领取也会触发。
- `net.xuwu.time.api.TemporalStrainEvent`：`getPlayer()`、`getAge()`；可取消。默认施加三秒缓慢/虚弱。整合包可取消默认行为，再调用自己现有的年龄/沙漏逻辑，避免重复累计。

请优先通过事件调用现有器官接口，默认关闭的年龄桥不检查器官槽位。停止、延迟等器官对 Boss 的适用范围仍需由 TimeClock/器官脚本接入；本模组的 `time:time_bosses` 实体标签用于识别本支线敌人。

## 任务衔接

每个谜题有原生进度 `time:puzzles/<kind>`。研究结论有 `time:research/past`、`time:research/present`、`time:research/final` 库存进度，FTB Quests 可以读取原生进度或对应成果物品。

`kind` 顺序：`day_sequence`、`shadow_dials`、`frozen_records`、`archive_order`、`mirror_path`、`delay_bells`、`archive_guardian`、`phase_seals`、`boss_arena`。

建议在过去研究后解锁永恒/逆转/机会器官；现在研究后解锁加速/减速/停止/延迟器官；以时代核心与终末结论接入历史之书。原器官技能、史诗布局判定和原 FTB 任务尚需下一步脚本连接。

## 状态与复位

房间控制器保存前置房间坐标、节点/门坐标、部分谜题进度、领取者 UUID、付费战斗资格和当前实体 UUID。维度 SavedData `time_rooms` 另外保存完成标记，让已卸载的前置房间也可被查询。

Boss 与控制器通过 UUID 互认；孤立的旧实体会自行清理。全员离开约十秒会结束战斗，恢复后沿用已支付资格。胜利后，再用钥匙点击入口核心开始新周期。旧轮次没有领取的玩家应先领取再重开。

机关石材和核心不可在生存中破坏；即使从侧面进入后续房间，成果领取和启动仍检查前置状态。普通陈设和补给箱不是主线进度凭据。
