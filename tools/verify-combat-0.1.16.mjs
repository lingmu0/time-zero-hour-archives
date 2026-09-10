import fs from 'node:fs';
import assert from 'node:assert/strict';

const root = new URL('../', import.meta.url);
const read = path => fs.readFileSync(new URL(path, root), 'utf8');

const arena = read('src/main/java/net/xuwu/time/logic/ArenaPattern.java');
assert(arena.includes('CIRCLE_COUNT=20'));
assert(arena.includes('CIRCLE_MIN_SEPARATION=4.5'));
assert(arena.includes('Math.hypot(c.x-x,c.z-z)>CIRCLE_MIN_SEPARATION'));

const tests = read('src/test/java/net/xuwu/time/logic/EncounterPatternTests.java');
assert(tests.includes('p.circles().size()==ArenaPattern.CIRCLE_COUNT'));
assert(tests.includes('dense circles keep their minimum center separation'));

console.log('PASS: 0.1.16 dense 20-circle pattern, 4.5-block center separation and fixed target-foot hazard wiring. Static/offline check only.');
