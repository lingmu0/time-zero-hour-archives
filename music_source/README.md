# 配乐与唱片（0.1.8）

## Boss 战：漆黒の代償 / 漆黑的代价

- 来源：用户提供的本地 MP3，原文件未修改。
- 用户提供的参考视频：https://www.youtube.com/watch?v=ZrgrrObNZFo （网页无法读取，未独立核实对应关系或作者）。
- 授权状态：unverified-user-supplied。文件名中的“免费 BGM”不代表已核实模组再分发许可。公开发布前需确认原作者使用条款和署名要求。
- jar 的 META-INF/THIRD-PARTY-MUSIC.txt 如实记录上述状态，不沿用旧曲 CC0，不声称拥有该音乐版权。
- 完整 204.89288 秒、44.1 kHz 双声道 Ogg Vorbis；保留结构、速度及自然收尾。
- MP3 解码峰值约 +2.09 dBFS；整体衰减约 5.59 dB，编码前峰值目标 -3.5 dBFS，首尾各 10 ms 防爆音淡化。不采用动态压缩。
- 整曲循环，不声称节拍/乐句无缝；原曲结尾的自然回落仍然保留。
- 资源：src/main/resources/assets/time/sounds/music/chronicler_battle.ogg
- 来源与输出 SHA256、精确帧数及处理参数：music_source/boss-track.json。
- 20 秒试听：build/audio/chronicler_battle_preview.wav（曲目第 30–50 秒）。

上一版的 MintoDog《Heavy Boss Battle 1》已退出当前 jar，保存在 build/audio-source/chronicler_battle-before-0.1.8.ogg；原始下载另在 build/audio-source/heavy_boss_battle_1_bpm200.ogg。其 CC0 信息仅适用于该历史音频：来源 https://opengameart.org/content/heavy-boss-battle-1 ，许可 https://creativecommons.org/publicdomain/zero/1.0/ 。

## 唱片：零点钟决 / The Zero-Hour Reckoning

0.1.6 的原始程序合成配乐，完整保留音频字节。96 BPM、D 小调、32 小节、80 秒、44.1 kHz 双声道。钟琴、拨弦、合成低弦、管风琴和钟摆节拍，作为偏氛围的收藏唱片。

旋律、和声、编配和音色由本项目脚本生成；没有第三方音乐、采样、音色库或声字体，遵循项目分发许可。

- 乐谱与生成器：tools/compose-boss-music.py（历史文件名，现仅生成唱片，不生成新 Boss BGM）。
- 原始资源：src/main/resources/assets/time/sounds/music/zero_hour.ogg
- SHA256：5eb9734f39103ba47b4ccb19a88dc6ef9d6ac570a5269aa04d311611af6423b7
- 物品：time:music_disc_zero_hour，最大堆叠 1，稀有品质，原版 Relic 图标。
- 无序合成：黑曜石、紫水晶碎片、编年残页、时序尘各 1；取得编年残页解锁配方书。
- 原版 JUKEBOX_PLAYABLE 组件 + data/time/jukebox_song/zero_hour.json，原版唱片机播放、弹出和红石行为，比对器输出 12。
- 唱片音效事件：time:music_disc.zero_hour，原版“唱片 / 音符盒”音量。

## 自动战斗播放

Boss 音效事件仍为 time:music.chronicle_keeper，但资源指向新曲。只在玩家进入已启动的正式战斗区域时播放，开场护盾阶段开始；刷怪蛋或命令直接召唤的未绑定 Boss 不触发。阶段切换、真假换位不重播，假身不创建第二个实例，音乐不受 Boss 距离或位置影响。

2 秒淡入；离场、玩家死亡、Boss 死亡或移除后约 1.5 秒淡出。断线或切换世界立即清理。暂停随原版暂停，静音不强制改设置，声音设备重载可恢复单个实例。

使用原版“音乐”和“主音量”；播放期间暂停原版背景音乐选择。没有停止或调整唱片、攻击与预警音效。

## 非启动验证与复现

玩家仅需模组 jar。下列工具仅供开发：

```powershell
python -m pip install --no-deps --target build/audio-tools soundfile==0.13.1
# 仅需重新转换时：python -X utf8 tools/import-boss-music.py "<本地 MP3 路径>"
python tools/analyze-music.py
node tools/verify-boss-music.mjs
node tools/verify-resources.mjs
./gradlew.bat assemble compileValidationJava --offline
```

Python 另需 NumPy。analyze-music.py 不修改两个 OGG，只解码检查并生成试听和电平/循环接缝报告。旧曲报告为 build/audio/zero-hour-analysis.json，新曲报告为 build/audio/boss-music-analysis.json。脚本验证 SHA256，防止混曲或旧报告误通过。

本版不启动 Minecraft、服务端或 GameTest；游戏内唱片/配方/起停行为未实测，音频数值检查不等于实际听感保证。
