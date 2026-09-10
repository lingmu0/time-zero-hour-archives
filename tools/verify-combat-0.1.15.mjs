import fs from 'node:fs';
import assert from 'node:assert/strict';

const root = new URL('../', import.meta.url);
const read = path => fs.readFileSync(new URL(path, root), 'utf8');

const rules = read('src/main/java/net/xuwu/time/logic/EncounterRules.java');
assert(rules.includes('PAST_REWIND_INTERVAL_TICKS = 200'));
assert(rules.includes('PAST_REWIND_RESPONSE_TICKS = 200'));
assert(rules.includes('Math.min(5, phase + 1)'));

const keeper = read('src/main/java/net/xuwu/time/entity/ChronicleKeeperEntity.java');
assert(keeper.includes('fireVolley(this, target.getEyePosition()'));
assert(keeper.includes('EncounterRules.boltCount(phase())'));
assert(keeper.includes('phaseTicks % EncounterRules.PAST_REWIND_INTERVAL_TICKS == 1'));
assert(keeper.includes('message.time.shield_order_hint'));
assert(keeper.indexOf('if (rewindTicks > 0 && --rewindTicks == 0)') < keeper.indexOf('phaseTicks % EncounterRules.PAST_REWIND_INTERVAL_TICKS'));

const echo = read('src/main/java/net/xuwu/time/entity/TemporalEchoEntity.java');
assert(echo.includes('fireVolley(this,target.getEyePosition(),damage,EncounterRules.boltCount(4))'));
assert(echo.includes('mode() != MEMORY || owner == null'));

const bolt = read('src/main/java/net/xuwu/time/entity/ChronalBoltEntity.java');
for (const text of ['fireVolley', 'requestedCount', 'forward.cross(reference)', 'Math.toRadians(2.75 + count * .75)', 'index > 0', 'launch.scale(SPEED)'])
    assert(bolt.includes(text), text);

assert(read('src/main/resources/assets/time/lang/zh_cn.json').includes('message.time.shield_order_hint'));
assert(read('src/main/resources/assets/time/lang/en_us.json').includes('message.time.shield_order_hint'));
console.log('PASS: 0.1.15 memory lifetime, ten-second rewind resolution, shield order hint and staged bolt wiring. Static/offline check only.');
