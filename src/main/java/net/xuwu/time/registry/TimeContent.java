package net.xuwu.time.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
import net.xuwu.time.TimeMod;
import net.xuwu.time.block.*;
import net.xuwu.time.entity.*;
import net.xuwu.time.item.*;
import net.xuwu.time.research.*;
import net.xuwu.time.world.*;

public final class TimeContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TimeMod.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TimeMod.ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TimeMod.ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_MUSIC = SOUNDS.register("music.chronicle_keeper",
        () -> SoundEvent.createVariableRangeEvent(TimeMod.id("music.chronicle_keeper")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ZERO_HOUR_MUSIC = SOUNDS.register("music_disc.zero_hour",
        () -> SoundEvent.createVariableRangeEvent(TimeMod.id("music_disc.zero_hour")));
    public static final ResourceKey<JukeboxSong> ZERO_HOUR_SONG =
        ResourceKey.create(Registries.JUKEBOX_SONG, TimeMod.id("zero_hour"));
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TimeMod.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TimeMod.ID);
    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, TimeMod.ID);
    public static final DeferredHolder<net.minecraft.world.effect.MobEffect, TemporalStasisEffect> STASIS = EFFECTS.register("temporal_stasis", TemporalStasisEffect::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ChronalBoltEntity>> CHRONAL_BOLT =
        ENTITIES.register("chronal_bolt", () -> EntityType.Builder.<ChronalBoltEntity>of(ChronalBoltEntity::new, MobCategory.MISC).sized(.25f, .25f).clientTrackingRange(8).updateInterval(1).build("time:chronal_bolt"));
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TimeMod.ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TimeMod.ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TimeMod.ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, TimeMod.ID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, TimeMod.ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TimeMod.ID);

    private static BlockBehaviour.Properties mechanism() {
        return BlockBehaviour.Properties.of().strength(-1, 3600000).noLootTable().sound(SoundType.COPPER);
    }
    public static final DeferredBlock<Block> CHRONAL_STONE = BLOCKS.register("chronal_stone",
        () -> new Block(mechanism()));
    public static final DeferredBlock<Block> TEMPORAL_BARRIER = BLOCKS.register("temporal_barrier",
        () -> new Block(mechanism().lightLevel(s -> 8)));
    public static final DeferredBlock<PuzzleControllerBlock> CONTROLLER = BLOCKS.register("puzzle_controller",
        () -> new PuzzleControllerBlock(mechanism()));
    public static final DeferredBlock<PuzzleNodeBlock> NODE = BLOCKS.register("chronal_pedestal",
        () -> new PuzzleNodeBlock(mechanism().lightLevel(s -> s.getValue(PuzzleNodeBlock.LIT) ? 12 : 3)));
    public static final DeferredBlock<ResearchDeskBlock> RESEARCH_DESK = BLOCKS.register("research_desk",
        () -> new ResearchDeskBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.WOOD)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PuzzleControllerBlockEntity>> CONTROLLER_BE =
        BLOCK_ENTITIES.register("puzzle_controller", () -> BlockEntityType.Builder.of(PuzzleControllerBlockEntity::new, CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PuzzleNodeBlockEntity>> NODE_BE =
        BLOCK_ENTITIES.register("chronal_pedestal", () -> BlockEntityType.Builder.of(PuzzleNodeBlockEntity::new, NODE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchDeskBlockEntity>> RESEARCH_BE =
        BLOCK_ENTITIES.register("research_desk", () -> BlockEntityType.Builder.of(ResearchDeskBlockEntity::new, RESEARCH_DESK.get()).build(null));

    public static final DeferredHolder<EntityType<?>, EntityType<ChronicleKeeperEntity>> CHRONICLE_KEEPER =
        ENTITIES.register("chronicle_keeper", () -> EntityType.Builder.of(ChronicleKeeperEntity::new, MobCategory.MONSTER)
            .sized(1.2f, 3.2f).clientTrackingRange(12).updateInterval(2).fireImmune().build("time:chronicle_keeper"));
    public static final DeferredHolder<EntityType<?>, EntityType<ArchiveScribeEntity>> ARCHIVE_SCRIBE =
        ENTITIES.register("archive_scribe", () -> EntityType.Builder.of(ArchiveScribeEntity::new, MobCategory.MONSTER)
            .sized(.8f, 2.3f).clientTrackingRange(10).fireImmune().build("time:archive_scribe"));
    public static final DeferredHolder<EntityType<?>, EntityType<TemporalEchoEntity>> TEMPORAL_ECHO =
        ENTITIES.register("temporal_echo", () -> EntityType.Builder.of(TemporalEchoEntity::new, MobCategory.MONSTER)
            .sized(.65f, 1.7f).clientTrackingRange(10).updateInterval(2).fireImmune().build("time:temporal_echo"));

    public static final DeferredItem<Item> TEMPORAL_DUST = item("temporal_dust");
    public static final DeferredItem<ChallengeSigilItem> CHALLENGE_SIGIL=ITEMS.register("challenge_sigil",
        ()->new ChallengeSigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<Item> MUSIC_DISC_ZERO_HOUR = ITEMS.register("music_disc_zero_hour",
        () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ZERO_HOUR_SONG)));
    public static final DeferredItem<Item> PAST_RECORD = item("past_record");
    public static final DeferredItem<Item> PRESENT_RECORD = item("present_record");
    public static final DeferredItem<Item> FUTURE_RECORD = item("future_record");
    public static final DeferredItem<Item> PAST_SEAL = item("past_seal");
    public static final DeferredItem<Item> PRESENT_SEAL = item("present_seal");
    public static final DeferredItem<Item> BLANK_SEAL = item("blank_seal");
    public static final DeferredItem<Item> PAST_CONCLUSION = item("past_conclusion");
    public static final DeferredItem<Item> PRESENT_CONCLUSION = item("present_conclusion");
    public static final DeferredItem<Item> FINAL_CONCLUSION = item("final_conclusion");
    public static final DeferredItem<Item> CHRONICLE_PAGE = item("chronicle_page");
    public static final DeferredItem<Item> EPOCH_CORE = item("epoch_core");
    public static final DeferredItem<Item> ZERO_KEY = item("zero_key");
    public static final DeferredItem<Item> FIELD_JOURNAL = ITEMS.register("field_journal",
        () -> new ArchiveItem(new Item.Properties().stacksTo(1), "field_journal"));
    public static final DeferredItem<LocatorItem> TIME_PROBE = ITEMS.register("time_probe",
        () -> new LocatorItem(new Item.Properties().stacksTo(1), "observatory"));
    public static final DeferredItem<LocatorItem> ARCHIVE_PROBE = ITEMS.register("archive_probe",
        () -> new LocatorItem(new Item.Properties().stacksTo(1), "archive"));
    public static final DeferredItem<LocatorItem> ZERO_PROBE = ITEMS.register("zero_probe",
        () -> new LocatorItem(new Item.Properties().stacksTo(1), "clockroom"));

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchMenu>> RESEARCH_MENU =
        MENUS.register("research", () -> IMenuTypeExtension.create(ResearchMenu::new));
    public static final DeferredHolder<RecipeType<?>, RecipeType<ResearchRecipe>> RESEARCH_TYPE =
        RECIPE_TYPES.register("research", () -> new RecipeType<>() { public String toString() { return "time:research"; } });
    public static final DeferredHolder<RecipeSerializer<?>, ResearchRecipe.Serializer> RESEARCH_SERIALIZER =
        SERIALIZERS.register("research", ResearchRecipe.Serializer::new);
    public static final DeferredHolder<StructureType<?>, StructureType<TimeRuinStructure>> RUIN_TYPE =
        STRUCTURE_TYPES.register("time_ruin", () -> () -> TimeRuinStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> RUIN_PIECE =
        PIECE_TYPES.register("time_ruin", () -> TimeRuinPiece::new);

    static {
        for (var block : new DeferredBlock<?>[]{CHRONAL_STONE, TEMPORAL_BARRIER, CONTROLLER, NODE, RESEARCH_DESK}) {
            ITEMS.registerSimpleBlockItem(block);
        }
        ITEMS.register("chronicle_keeper_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(CHRONICLE_KEEPER, 0x152E3C, 0xE5B85B, new Item.Properties()));
        ITEMS.register("archive_scribe_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ARCHIVE_SCRIBE, 0x28283E, 0xA48EDC, new Item.Properties()));
        TABS.register("archives", () -> CreativeModeTab.builder().title(Component.translatable("tab.time"))
            .icon(() -> EPOCH_CORE.toStack()).displayItems((parameters, output) -> ITEMS.getEntries().forEach(i -> output.accept(i.get()))).build());
    }
    private static DeferredItem<Item> item(String id) {
        return ITEMS.register(id, () -> new ArchiveItem(new Item.Properties(), id));
    }
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); SOUNDS.register(bus); BLOCK_ENTITIES.register(bus); ENTITIES.register(bus); EFFECTS.register(bus);
        MENUS.register(bus); RECIPE_TYPES.register(bus); SERIALIZERS.register(bus);
        STRUCTURE_TYPES.register(bus); PIECE_TYPES.register(bus); TABS.register(bus);
        bus.addListener(TimeContent::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(CHRONICLE_KEEPER.get(), ChronicleKeeperEntity.attributes().build());
        event.put(ARCHIVE_SCRIBE.get(), ArchiveScribeEntity.attributes().build());
        event.put(TEMPORAL_ECHO.get(), TemporalEchoEntity.attributes().build());
    }
    private TimeContent() {}
}
