import fs from 'node:fs';
import assert from 'node:assert/strict';

const root = new URL('../', import.meta.url);
const read = path => fs.readFileSync(new URL(path, root), 'utf8');
const json = path => JSON.parse(read('src/main/resources/' + path));

const dimension = json('data/time/dimension/sanctum.json');
const type = json('data/time/dimension_type/sanctum.json');
assert.equal(dimension.type, 'time:sanctum');
assert.equal(dimension.generator.type, 'minecraft:flat');
assert.deepEqual(dimension.generator.settings.layers, [{ height: 1, block: 'minecraft:air' }]);
assert.equal(type.effects, 'time:sanctum');
assert.equal(type.height % 16, 0);
assert(type.height <= 4064 && type.min_y + type.height <= 2032 && !type.has_ceiling);

const glass = json('assets/time/models/block/sanctum_platform.json');
assert.equal(glass.textures.all, 'minecraft:block/glass');
assert(['cutout', 'minecraft:cutout'].includes(glass.render_type), 'platform must use a cutout render type');
assert.equal(json('assets/time/blockstates/sanctum_platform.json').variants[''].model, 'time:block/sanctum_platform');
assert.equal(json('assets/time/models/item/ascension_challenge_sigil.json').textures.layer0, 'time:item/challenge_sigil');

const sounds = json('assets/time/sounds.json');
assert.equal(sounds['music.chronicle_keeper_ascension'].sounds[0].name, 'time:music/ascension_battle');
assert(fs.existsSync(new URL('../src/main/resources/assets/time/sounds/music/ascension_battle.ogg', import.meta.url)),
  'Missing ascension BGM');

for (const lang of ['en_us', 'zh_cn']) {
  const strings = json('assets/time/lang/' + lang + '.json');
  for (const key of [
    'phase.time.ascension',
    'block.time.sanctum_platform',
    'message.time.sanctum_enter',
    'message.time.sanctum_missing'
  ]) assert(strings[key], lang + ' is missing ' + key);
}

const fight = read('src/main/java/net/xuwu/time/entity/AscensionFight.java');
assert(!/MobEffects|JUMP_BOOST|MOVEMENT_SPEED|addEffect/.test(fight), 'no extra jump or movement buffs');
assert(fight.includes('player.getMaxHealth() * .6f'));
assert(fight.includes('Math.max(0, tier - 3); i <= tier + 4'));
assert(fight.includes('rise += ascentSpeed'));
assert(fight.includes('tideRise += ascentSpeed'));
assert(fight.includes('platformSyncInterval'));
assert(fight.includes('changeDimension(destination, new ITeleporter'));
assert(fight.includes('new PortalInfo(new Vec3(siteX + .5, 72, siteZ + .5)'));

const musicManager = read('src/main/java/net/xuwu/time/client/BossMusicManager.java');
assert(musicManager.includes('mc.level.dimension().equals(AscensionFight.DIMENSION)'));
assert(musicManager.includes('boolean ascension = inSanctum && encounter != null && encounter.ascended()'));
assert(!musicManager.includes('encounter.transitioning()'), 'sanctum BGM must not start during the transition');

const keeper = read('src/main/java/net/xuwu/time/entity/ChronicleKeeperEntity.java');
assert(keeper.includes('ascension.finish(true)'));
assert(keeper.includes('TimeConfig.BOSS_SECOND_HEALTH.get()'));
assert(keeper.includes('tag.put("Ascension", ascension.save())'));
assert(read('src/main/java/net/xuwu/time/TimeMod.java').includes('EventPriority.LOWEST'));

const config = read('src/main/java/net/xuwu/time/TimeConfig.java');
assert(config.includes('defineInRange("bossSecondPhaseHealth", 500.0'));
assert(config.includes('defineInRange("secondPhaseBoltCount", 4'));
assert(config.includes('defineInRange("secondPhaseBoltInterval", 40'));
assert(config.includes('defineInRange("secondPhaseTideSpeed", .075'));
assert(read('src/main/java/net/xuwu/time/client/BossMusicManager.java').includes('ASCENSION_BOSS_MUSIC'));
assert(read('src/main/java/net/xuwu/time/item/AscensionChallengeItem.java').includes('bindDirect'));

const expectedSpacing = {
  observatory: [32, 12],
  archive: [40, 16],
  clockroom: [48, 20]
};
for (const [name, [spacing, separation]] of Object.entries(expectedSpacing)) {
  const placement = json('data/time/worldgen/structure_set/' + name + '.json').placement;
  assert.equal(placement.spacing, spacing, name + ' spacing');
  assert.equal(placement.separation, separation, name + ' separation');
  assert(spacing > separation, name + ' spacing must exceed separation');
}

console.log('PASS: Forge 1.20.1 sanctum, glass resources, arrival-gated BGM, ascent sync, config and reduced structure-frequency checks. No game launched.');
