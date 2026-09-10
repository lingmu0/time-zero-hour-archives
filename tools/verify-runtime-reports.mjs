import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const args = process.argv.slice(2);
assert(args.every(arg => arg === '--client' || arg.startsWith('--since=')), 'Usage: node tools/verify-runtime-reports.mjs [--client] [--since=ISO-date]');
const sinceArgument = args.find(arg => arg.startsWith('--since='));
const since = sinceArgument ? Date.parse(sinceArgument.slice(8)) : 0;
assert(Number.isFinite(since), 'Invalid --since timestamp');
function readReport(name) {
  const report = JSON.parse(fs.readFileSync(path.join(root, 'build/reports', name), 'utf8'));
  assert(Date.parse(report.generated_at) >= since, `${name} is stale or missing a valid timestamp`);
  return report;
}
const server = readReport('runtime-gametests.json');
const required = ['observatory_chain_and_persistence', 'archive_rhythm_and_guardian',
  'research_real_ticks_and_output_backpressure', 'boss_shields_damage_gates_and_rewards',
  'wipe_retry_does_not_charge_second_key', 'boss_memory_echoes_control_rewind',
  'boss_quadrant_and_paradox_defenses', 'boss_and_menu_reload_contracts', 'boss_facing_cast_and_ring_sync', 'boss_active_paradox_and_swap'];
for (const name of required) {
  const matches = server.tests.filter(test => test.name === name);
  assert(matches.length === 1 && matches[0].passed === true, `Missing or failed GameTest: ${name}`);
}
assert(server.tests.every(test => test.passed === true), 'Additional GameTests failed');
console.log(`PASS: ${server.tests.length} required runtime GameTests (${server.generated_at}).`);
if (args.includes('--client')) {
  const client = readReport('visual-runtime.json');
  assert.equal(client.status, 'passed', JSON.stringify(client));
  assert.equal(client.model_bindings_verified,true,'Scribe, keeper and false-body bindings must be checked in the real client');
  for (const name of ['observatory', 'archive', 'clockroom']) {
    const test = client.natural_structures.find(test => test.structure === name);
    assert(test?.natural_generation && test.probe_resolves_surface_entrance, `Natural generation/probe check failed: ${name}`);
  }
  const screenshots = ['01-observatory-exterior', '02-observatory-puzzle', '03-archive-entrance',
    '04-archive-mirror', '05-clockroom-entrance', '06-boss-arena', '07-research-interface', '08-model-comparison-day', '09-model-comparison-night'];
  for (const name of screenshots) {
    const file = path.join(root, 'run-validation-client/screenshots', name + '.png');
    assert(fs.statSync(file).mtimeMs >= since, `Stale screenshot: ${name}`);
    assert.equal(fs.readFileSync(file).subarray(0, 8).toString('hex'), '89504e470d0a1a0a', `Invalid PNG: ${name}`);
  }
  console.log(`PASS: 3 naturally generated structures/probes and ${screenshots.length} client screenshots (${client.generated_at}).`);
}
