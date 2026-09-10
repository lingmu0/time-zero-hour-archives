# 0.1.3 战斗可读性与攻击动画

本次只修改 time 原生模块；没有安装到整合包、修改现有存档或更换 Blockbench 模型文件。

## 行为

- 护盾：使用原版闪电苦力怕的能量材质与 energySwirl 渲染，略放大的流动蓝白外壳随服务端 Shield 状态出现/消失。开场和最终护盾共用；破盾立即移除。
- 时序环带：只在攻击预警到爆发后短暂显示。中心固定在钟室中心，不跟着 Boss 悬浮移动；危险半径严格为 4 < r < 8 格。红橙色填充、内外轮廓与扫过的亮边表示 30 tick 蓄力，结算后能量波消退，10 tick 后不再绘制。
- 现在阶段：按用户后续要求，不常驻铺色或标出安全/危险象限；保留文字提示，移除安全象限常驻粒子。受到停滞攻击时，玩家周围短暂出现逆向粒子。
- 朝向：身体以每 tick 最多 12 度转向当前攻击目标；侧向悬浮不再把身体转到移动方向。创造模式玩家也可检查朝向，但不会成为伤害目标。
- 飞弹：10 tick 抬杖蓄力，发射时前推，随后收势；书本和身体联动。环带使用举臂、抬杖、悬浮书页和释放动作。动画使用服务端同步的起始时间，不使用客户端随机触发。
- 阶段限伤：事件最终伤害上限加上 hurt 事务内实际生命写入保护。常规攻击和绕过无敌标签的伤害只能打到 80% / 60% / 35% / 15% 的下一阶段边界；同一 tick 连续攻击不能越过等待转换的边界。最终阶段破盾后可击杀。管理员 /kill 明确保留；不宣称能拦截其他模组直接 discard/remove 实体或绕开伤害流程的任意生命改写。

## 可重复验证

```powershell
$env:JAVA_HOME='C:\Users\123\.jdks\jbr-21.0.10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build runValidationServer runCombatClient --offline
node tools/verify-resources.mjs
node tools/verify-runtime-reports.mjs
node tools/verify-combat-report.mjs
```

客户端使用新建的 TimeCombat 世界和独立 run-validation-combat 目录，正常加载模组，使用真实服务端 Boss 与实际客户端渲染。测试角色免伤以便固定观察；没有修改发布版战斗伤害。首个新目录运行曾停在原版无障碍引导页，已为这个显式启用的测试入口添加引导处理。

- 构建包含 35 项独立状态检查。
- 服务端 9 组 GameTest；新增转向、施法、环带同步验证。原有阶段测试加入百万级伤害、绕过无敌标签与同 tick 连续命中。
- [服务端报告](build/reports/runtime-gametests.json)。
- [战斗视觉报告](build/reports/combat-visual.json)：记录每张图实际阶段、护盾、施法时间、环带倒计时及朝向误差。
- [北侧护盾](run-validation-combat/screenshots/01-shield-north.png)、[东侧转向](run-validation-combat/screenshots/02-shield-east.png)。
- [飞弹蓄力](run-validation-combat/screenshots/03-bolt-windup.png)、[飞弹释放](run-validation-combat/screenshots/04-bolt-release.png)。
- [环带预警](run-validation-combat/screenshots/05-ring-warning.png)、[环带爆发](run-validation-combat/screenshots/06-ring-impact.png)、[环带消退](run-validation-combat/screenshots/07-ring-cleared.png)。
- [现在阶段无区域铺色](run-validation-combat/screenshots/08-present-no-zone-overlay.png)。

时间新鲜度可用两个报告检查脚本的 --since=ISO时间 参数校验。旧 VALIDATION.md 的自然生成和研究台图像是 0.1.2 记录，不应当作本次新特效证据。仍未进行真实多人延迟、整合包自定义伤害系统和完整生存平衡测试。

