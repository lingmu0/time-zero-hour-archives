import fs from 'node:fs';
import assert from 'node:assert/strict';

const root = new URL('../', import.meta.url);
const read = path => fs.readFileSync(new URL(path, root), 'utf8');

const rules = read('src/main/java/net/xuwu/time/logic/EncounterRules.java');
assert(rules.includes('return new int[]{0, 2, 3, 1}'));

const keeper = read('src/main/java/net/xuwu/time/entity/ChronicleKeeperEntity.java');
assert(keeper.includes('present_phase.time." + q'));
assert(keeper.includes('TimeConfig.ECHO_COUNT.get()'));

const config = read('src/main/java/net/xuwu/time/TimeConfig.java');
for (const key of ['BOSS_HEALTH', 'BOSS_DAMAGE', 'ECHO_COUNT', 'ECHO_HEALTH']) assert(config.includes(key), key);

const echo = read('src/main/java/net/xuwu/time/entity/TemporalEchoEntity.java');
assert(echo.includes('TimeConfig.ECHO_HEALTH.get()'));
console.log('PASS: 0.1.17 counter-clockwise present phase, direction-plus-phase debt prompt and combat config wiring. Static/offline check only.');
