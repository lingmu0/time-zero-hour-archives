package net.xuwu.time.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xuwu.time.TimeMod;
import net.xuwu.time.block.PuzzleControllerBlock;
import net.xuwu.time.block.PuzzleControllerBlockEntity;
import net.xuwu.time.block.PuzzleNodeBlock;
import net.xuwu.time.block.PuzzleNodeBlockEntity;
import net.xuwu.time.block.SanctumPlatformBlock;
import net.xuwu.time.entity.ArchiveScribeEntity;
import net.xuwu.time.entity.ChronalBoltEntity;
import net.xuwu.time.entity.ChronicleKeeperEntity;
import net.xuwu.time.entity.TemporalEchoEntity;
import net.xuwu.time.entity.TemporalStasisEffect;
import net.xuwu.time.item.ArchiveItem;
import net.xuwu.time.item.AscensionChallengeItem;
import net.xuwu.time.item.ChallengeSigilItem;
import net.xuwu.time.item.LocatorItem;
import net.xuwu.time.research.ResearchDeskBlock;
import net.xuwu.time.research.ResearchDeskBlockEntity;
import net.xuwu.time.research.ResearchMenu;
import net.xuwu.time.research.ResearchRecipe;
import net.xuwu.time.world.TimeRuinPiece;
import net.xuwu.time.world.TimeRuinStructure;

public final class TimeContent {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TimeMod.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TimeMod.ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TimeMod.ID);
    public static final RegistryObject<SoundEvent> BOSS_MUSIC = SOUNDS.register("music.chronicle_keeper",
        () -> SoundEvent.createVariableRangeEvent(TimeMod.id("music.chronicle_keeper")));
    public static final RegistryObject<SoundEvent> ASCENSION_BOSS_MUSIC = SOUNDS.register("music.chronicle_keeper_ascension",
        () -> SoundEvent.createVariableRangeEvent(TimeMod.id("music.chronicle_keeper_ascension")));
    public static final RegistryObject<SoundEvent> ZERO_HOUR_MUSIC = SOUNDS.register("music_disc.zero_hour",
        () -> SoundEvent.createVariableRangeEvent(TimeMod.id("music_disc.zero_hour")));
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TimeMod.ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TimeMod.ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TimeMod.ID);
    public static final RegistryObject<TemporalStasisEffect> STASIS = EFFECTS.register("temporal_stasis", TemporalStasisEffect::new);
    public static final RegistryObject<EntityType<ChronalBoltEntity>> CHRONAL_BOLT = ENTITIES.register("chronal_bolt",
        () -> EntityType.Builder.<ChronalBoltEntity>of(ChronalBoltEntity::new, MobCategory.MISC).sized(.25f, .25f).clientTrackingRange(8).updateInterval(1).build("time:chronal_bolt"));
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TimeMod.ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, TimeMod.ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TimeMod.ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, TimeMod.ID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, TimeMod.ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TimeMod.ID);

    private static BlockBehaviour.Properties mechanism() {
        return BlockBehaviour.Properties.of().strength(-1, 3600000).noLootTable().sound(SoundType.COPPER);
    }
    public static final RegistryObject<Block> CHRONAL_STONE = BLOCKS.register("chronal_stone", () -> new Block(mechanism()));
    public static final RegistryObject<SanctumPlatformBlock> SANCTUM_PLATFORM = BLOCKS.register("sanctum_platform",
        () -> new SanctumPlatformBlock(mechanism().sound(SoundType.GLASS).dynamicShape().noOcclusion().lightLevel(s -> 12)));
    public static final RegistryObject<Block> TEMPORAL_BARRIER = BLOCKS.register("temporal_barrier", () -> new Block(mechanism().lightLevel(s -> 8)));
    public static final RegistryObject<PuzzleControllerBlock> CONTROLLER = BLOCKS.register("puzzle_controller", () -> new PuzzleControllerBlock(mechanism()));
    public static final RegistryObject<PuzzleNodeBlock> NODE = BLOCKS.register("chronal_pedestal",
        () -> new PuzzleNodeBlock(mechanism().lightLevel(s -> s.getValue(PuzzleNodeBlock.LIT) ? 12 : 3)));
    public static final RegistryObject<ResearchDeskBlock> RESEARCH_DESK = BLOCKS.register("research_desk",
        () -> new ResearchDeskBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.WOOD)));

    public static final RegistryObject<BlockEntityType<PuzzleControllerBlockEntity>> CONTROLLER_BE = BLOCK_ENTITIES.register("puzzle_controller",
        () -> BlockEntityType.Builder.of(PuzzleControllerBlockEntity::new, CONTROLLER.get()).build(null));
    public static final RegistryObject<BlockEntityType<PuzzleNodeBlockEntity>> NODE_BE = BLOCK_ENTITIES.register("chronal_pedestal",
        () -> BlockEntityType.Builder.of(PuzzleNodeBlockEntity::new, NODE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ResearchDeskBlockEntity>> RESEARCH_BE = BLOCK_ENTITIES.register("research_desk",
        () -> BlockEntityType.Builder.of(ResearchDeskBlockEntity::new, RESEARCH_DESK.get()).build(null));

    public static final RegistryObject<EntityType<ChronicleKeeperEntity>> CHRONICLE_KEEPER = ENTITIES.register("chronicle_keeper",
        () -> EntityType.Builder.of(ChronicleKeeperEntity::new, MobCategory.MONSTER).sized(1.2f, 3.2f).clientTrackingRange(12).updateInterval(2).fireImmune().build("time:chronicle_keeper"));
    public static final RegistryObject<EntityType<ArchiveScribeEntity>> ARCHIVE_SCRIBE = ENTITIES.register("archive_scribe",
        () -> EntityType.Builder.of(ArchiveScribeEntity::new, MobCategory.MONSTER).sized(.8f, 2.3f).clientTrackingRange(10).fireImmune().build("time:archive_scribe"));
    public static final RegistryObject<EntityType<TemporalEchoEntity>> TEMPORAL_ECHO = ENTITIES.register("temporal_echo",
        () -> EntityType.Builder.of(TemporalEchoEntity::new, MobCategory.MONSTER).sized(.65f, 1.7f).clientTrackingRange(10).updateInterval(2).fireImmune().build("time:temporal_echo"));

    public static final RegistryObject<Item> TEMPORAL_DUST = item("temporal_dust");
    public static final RegistryObject<ChallengeSigilItem> CHALLENGE_SIGIL = ITEMS.register("challenge_sigil",
        () -> new ChallengeSigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<AscensionChallengeItem> ASCENSION_CHALLENGE_SIGIL = ITEMS.register("ascension_challenge_sigil",
        () -> new AscensionChallengeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> MUSIC_DISC_ZERO_HOUR = ITEMS.register("music_disc_zero_hour",
        () -> new RecordItem(12, ZERO_HOUR_MUSIC.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 1600));
    public static final RegistryObject<Item> PAST_RECORD = item("past_record");
    public static final RegistryObject<Item> PRESENT_RECORD = item("present_record");
    public static final RegistryObject<Item> FUTURE_RECORD = item("future_record");
    public static final RegistryObject<Item> PAST_SEAL = item("past_seal");
    public static final RegistryObject<Item> PRESENT_SEAL = item("present_seal");
    public static final RegistryObject<Item> BLANK_SEAL = item("blank_seal");
    public static final RegistryObject<Item> PAST_CONCLUSION = item("past_conclusion");
    public static final RegistryObject<Item> PRESENT_CONCLUSION = item("present_conclusion");
    public static final RegistryObject<Item> FINAL_CONCLUSION = item("final_conclusion");
    public static final RegistryObject<Item> CHRONICLE_PAGE = item("chronicle_page");
    public static final RegistryObject<Item> EPOCH_CORE = item("epoch_core");
    public static final RegistryObject<Item> ZERO_KEY = item("zero_key");
    public static final RegistryObject<Item> FIELD_JOURNAL = ITEMS.register("field_journal",
        () -> new ArchiveItem(new Item.Properties().stacksTo(1), "field_journal"));
    public static final RegistryObject<LocatorItem> TIME_PROBE = ITEMS.register("time_probe", () -> new LocatorItem(new Item.Properties().stacksTo(1), "observatory"));
    public static final RegistryObject<LocatorItem> ARCHIVE_PROBE = ITEMS.register("archive_probe", () -> new LocatorItem(new Item.Properties().stacksTo(1), "archive"));
    public static final RegistryObject<LocatorItem> ZERO_PROBE = ITEMS.register("zero_probe", () -> new LocatorItem(new Item.Properties().stacksTo(1), "clockroom"));

    public static final RegistryObject<MenuType<ResearchMenu>> RESEARCH_MENU = MENUS.register("research", () -> IForgeMenuType.create(ResearchMenu::new));
    public static final RegistryObject<RecipeType<ResearchRecipe>> RESEARCH_TYPE = RECIPE_TYPES.register("research", () -> new RecipeType<>() {
        public String toString() { return "time:research"; }
    });
    public static final RegistryObject<ResearchRecipe.Serializer> RESEARCH_SERIALIZER = SERIALIZERS.register("research", ResearchRecipe.Serializer::new);
    public static final RegistryObject<StructureType<TimeRuinStructure>> RUIN_TYPE = STRUCTURE_TYPES.register("time_ruin", () -> () -> TimeRuinStructure.CODEC);
    public static final RegistryObject<StructurePieceType> RUIN_PIECE = PIECE_TYPES.register("time_ruin", () -> TimeRuinPiece::new);

    static {
        ITEMS.register("chronal_stone", () -> new BlockItem(CHRONAL_STONE.get(), new Item.Properties()));
        ITEMS.register("sanctum_platform", () -> new BlockItem(SANCTUM_PLATFORM.get(), new Item.Properties()));
        ITEMS.register("temporal_barrier", () -> new BlockItem(TEMPORAL_BARRIER.get(), new Item.Properties()));
        ITEMS.register("puzzle_controller", () -> new BlockItem(CONTROLLER.get(), new Item.Properties()));
        ITEMS.register("chronal_pedestal", () -> new BlockItem(NODE.get(), new Item.Properties()));
        ITEMS.register("research_desk", () -> new BlockItem(RESEARCH_DESK.get(), new Item.Properties()));
        ITEMS.register("chronicle_keeper_spawn_egg", () -> new ForgeSpawnEggItem(CHRONICLE_KEEPER, 0x152E3C, 0xE5B85B, new Item.Properties()));
        ITEMS.register("archive_scribe_spawn_egg", () -> new ForgeSpawnEggItem(ARCHIVE_SCRIBE, 0x28283E, 0xA48EDC, new Item.Properties()));
        TABS.register("archives", () -> CreativeModeTab.builder().title(Component.translatable("tab.time"))
            .icon(() -> EPOCH_CORE.get().getDefaultInstance()).displayItems((parameters, output) -> ITEMS.getEntries().forEach(i -> output.accept(i.get()))).build());
    }
    private static RegistryObject<Item> item(String id) {
        return ITEMS.register(id, () -> new ArchiveItem(new Item.Properties(), id));
    }
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); SOUNDS.register(bus); BLOCK_ENTITIES.register(bus); ENTITIES.register(bus); EFFECTS.register(bus);
        MENUS.register(bus); RECIPE_TYPES.register(bus); SERIALIZERS.register(bus); STRUCTURE_TYPES.register(bus); PIECE_TYPES.register(bus); TABS.register(bus);
        bus.addListener(TimeContent::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(CHRONICLE_KEEPER.get(), ChronicleKeeperEntity.attributes().build());
        event.put(ARCHIVE_SCRIBE.get(), ArchiveScribeEntity.attributes().build());
        event.put(TEMPORAL_ECHO.get(), TemporalEchoEntity.attributes().build());
    }
    private TimeContent() {}
}
