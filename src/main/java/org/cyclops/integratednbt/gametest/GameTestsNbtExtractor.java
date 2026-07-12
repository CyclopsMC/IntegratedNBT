package org.cyclops.integratednbt.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.RegistryEntries;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.evaluate.NbtExtractorOutputMode;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.item.ItemNbtExtractorRemote;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.assertValueEqual;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableForOperator;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableFromReader;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getVariableFacade;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.placeVariableInDisplayPanel;

/**
 * Game tests for the NBT Extractor block and the four output modes.
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsNbtExtractor {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final int TIMEOUT = 1000;
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    // NBT extraction path: top-level "Health" field (float) of a sheep entity.
    // A freshly spawned adult sheep has Health = 8.0f (max health).
    // FloatTag (id=5) maps to ValueDouble via NbtValueConverter.mapNBTToValue.
    private static final byte DEFAULT_NBT_ID_FLOAT = 5; // float

    /**
     * Sets up the shared test infrastructure:
     * - Cable at POS with EntityReader part facing WEST
     * - Variable Store at POS.north (connected to same cable network)
     * - NBT Extractor block at POS.east (bridging POS cable and POS.east.east cable)
     * - Cable at POS.east.east with Display Panel part facing EAST
     * - Sheep spawned at POS.west with NoAI enabled
     *
     * @return the spawned sheep and the NBT Extractor block entity
     */
    private static Pair<Sheep, BlockEntityNbtExtractor> setupEntityReaderNetwork(
            GameTestHelper helper,
            NbtExtractorOutputMode outputMode,
            SegmentedNbtPath extractionPath,
            byte defaultNbtId) {
        // Place cable with entity reader
        helper.setBlock(POS, org.cyclops.integrateddynamics.RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(
                helper.getLevel(), helper.absolutePos(POS),
                Direction.WEST, PartTypes.ENTITY_READER,
                new ItemStack(PartTypes.ENTITY_READER.getItem()));

        // Place variable store adjacent to cable (stores entity variable card)
        helper.setBlock(POS.north(), org.cyclops.integrateddynamics.RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Place NBT extractor block (connects to cable at POS and cable at POS.east.east)
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_NBT_EXTRACTOR.value());
        BlockEntityNbtExtractor nbtExtractor = helper.getBlockEntity(POS.east());

        // Place cable with display panel
        helper.setBlock(POS.east().east(), org.cyclops.integrateddynamics.RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(
                helper.getLevel(), helper.absolutePos(POS.east().east()),
                Direction.EAST, PartTypes.DISPLAY_PANEL,
                new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Spawn sheep at the entity reader's target block, disable AI so it stays still
        Sheep sheep = helper.spawn(EntityType.SHEEP, POS.west());
        sheep.setNoAi(true);

        // Create entity variable card from the entity reader (reads the entity at POS.west)
        ItemStack entityVarCard = createVariableFromReader(
                helper.getLevel(),
                PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST),
                Aspects.Read.Entity.ENTITY);

        // Place entity variable card in variable store so its ID is registered in the network
        variableStore.getInventory().setItem(0, entityVarCard);

        // Create operator variable: applies OBJECT_ENTITY_NBT to the entity variable
        int entityVarId = getVariableFacade(helper.getLevel(), entityVarCard).getId();
        ItemStack nbtOperatorVarCard = createVariableForOperator(
                helper.getLevel(),
                Operators.OBJECT_ENTITY_NBT,
                new int[]{entityVarId});

        // Configure the NBT extractor
        nbtExtractor.setExtractionPath(extractionPath);
        nbtExtractor.setOutputMode(outputMode);
        nbtExtractor.setDefaultNBTId(defaultNbtId);

        // Place the entity-NBT operator variable in slot 0 (source NBT slot)
        nbtExtractor.getInventory().setItem(
                BlockEntityNbtExtractor.SRC_NBT_SLOT, nbtOperatorVarCard);
        // Place a blank variable card in slot 1 (output variable slot)
        nbtExtractor.getInventory().setItem(
                BlockEntityNbtExtractor.VAR_OUT_SLOT,
                new ItemStack(org.cyclops.integrateddynamics.RegistryEntries.ITEM_VARIABLE.get()));

        return Pair.of(sheep, nbtExtractor);
    }

    /**
     * Tests the REFERENCE output mode of the NBT Extractor.
     *
     * An entity reader reads a sheep entity. An operator converts the entity to its NBT.
     * The NBT extractor (REFERENCE mode) produces a variable that dynamically extracts
     * "Health" from the entity NBT. The display panel shows the current health value.
     */
    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = TIMEOUT)
    public void testNbtExtractorOutputModeReference(GameTestHelper helper) {
        SegmentedNbtPath path = new SegmentedNbtPath();
        path.pushKey("Health");

        Pair<Sheep, BlockEntityNbtExtractor> setup = setupEntityReaderNetwork(
                helper, NbtExtractorOutputMode.REFERENCE, path, DEFAULT_NBT_ID_FLOAT);
        BlockEntityNbtExtractor nbtExtractor = setup.getRight();

        // Wait for the NBT extractor to process and write to the output slot
        Wrapper<Pair<PartTypePanelDisplay, PartTypePanelDisplay.State>> partAndState = new Wrapper<>(null);
        helper.runAfterDelay(5, () -> {
            ItemStack outputVarCard = nbtExtractor.getInventory()
                    .getItem(BlockEntityNbtExtractor.VAR_OUT_SLOT);
            if (!outputVarCard.isEmpty()) {
                partAndState.set(placeVariableInDisplayPanel(
                        helper.getLevel(),
                        PartPos.of(helper.getLevel(),
                                helper.absolutePos(POS.east().east()), Direction.EAST),
                        outputVarCard.copy()));
            }
        });

        // Sheep at full health = 8.0f, FloatTag maps to ValueDouble.of(8.0)
        helper.succeedWhen(() -> {
            helper.assertTrue(partAndState.get() != null, "Display panel not yet set up");
            assertValueEqual(
                    partAndState.get().getRight().getDisplayValue(),
                    ValueTypeDouble.ValueDouble.of(8.0));
        });
    }

    /**
     * Tests the VALUE output mode of the NBT Extractor.
     *
     * The NBT extractor (VALUE mode) captures a snapshot of the extracted NBT value at
     * the time the output variable is written. We use the shared helper to set up the
     * network, then immediately inject the sheep's current NBT via
     * {@link BlockEntityNbtExtractor#updateLastEvaluatedNBT} before the server ticker fires
     * {@code updateOutVariable()}. Both calls happen in the same tick, so the snapshot is
     * available when the extractor processes the output.
     */
    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = TIMEOUT)
    public void testNbtExtractorOutputModeValue(GameTestHelper helper) {
        SegmentedNbtPath path = new SegmentedNbtPath();
        path.pushKey("Health");

        Pair<Sheep, BlockEntityNbtExtractor> setup = setupEntityReaderNetwork(
                helper, NbtExtractorOutputMode.VALUE, path, DEFAULT_NBT_ID_FLOAT);
        Sheep sheep = setup.getLeft();
        BlockEntityNbtExtractor nbtExtractor = setup.getRight();

        // Inject the sheep's NBT as the last-evaluated NBT.
        // The helper has already set the inventory items (triggering setChanged →
        // shouldUpdateOutVariable = true), but updateOutVariable() does not run until the
        // next server tick. Calling updateLastEvaluatedNBT here — still in the same tick —
        // ensures it is available when the ticker fires.
        // In normal gameplay this is done by ContainerNbtExtractor.broadcastChanges() while
        // the GUI is open.
        CompoundTag entityNbt = new CompoundTag();
        sheep.saveWithoutId(entityNbt);
        nbtExtractor.updateLastEvaluatedNBT(entityNbt);

        Wrapper<Pair<PartTypePanelDisplay, PartTypePanelDisplay.State>> partAndState =
                new Wrapper<>(null);
        helper.runAfterDelay(5, () -> {
            ItemStack outputVarCard =
                    nbtExtractor.getInventory().getItem(BlockEntityNbtExtractor.VAR_OUT_SLOT);
            if (!outputVarCard.isEmpty()) {
                partAndState.set(placeVariableInDisplayPanel(
                        helper.getLevel(),
                        PartPos.of(
                                helper.getLevel(),
                                helper.absolutePos(POS.east().east()),
                                Direction.EAST),
                        outputVarCard.copy()));
            }
        });

        // VALUE mode captures the Health snapshot = 8.0 for a full-health sheep
        helper.succeedWhen(() -> {
            helper.assertTrue(partAndState.get() != null, "Display panel not yet set up");
            assertValueEqual(
                    partAndState.get().getRight().getDisplayValue(),
                    ValueTypeDouble.ValueDouble.of(8.0));
        });
    }

    /**
     * Tests the OPERATOR output mode of the NBT Extractor.
     *
     * The NBT extractor (OPERATOR mode) produces a variable holding a
     * {@code NbtExtractionOperator}. When displayed, the panel shows the operator value.
     * We verify this by applying the operator to the sheep's current NBT and checking
     * that the extracted "Health" equals 8.0.
     */
    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = TIMEOUT)
    public void testNbtExtractorOutputModeOperator(GameTestHelper helper) {
        SegmentedNbtPath path = new SegmentedNbtPath();
        path.pushKey("Health");

        Pair<Sheep, BlockEntityNbtExtractor> setup = setupEntityReaderNetwork(
                helper, NbtExtractorOutputMode.OPERATOR, path, DEFAULT_NBT_ID_FLOAT);
        Sheep sheep = setup.getLeft();
        BlockEntityNbtExtractor nbtExtractor = setup.getRight();

        Wrapper<Pair<PartTypePanelDisplay, PartTypePanelDisplay.State>> partAndState =
                new Wrapper<>(null);
        helper.runAfterDelay(5, () -> {
            ItemStack outputVarCard =
                    nbtExtractor.getInventory().getItem(BlockEntityNbtExtractor.VAR_OUT_SLOT);
            if (!outputVarCard.isEmpty()) {
                partAndState.set(placeVariableInDisplayPanel(
                        helper.getLevel(),
                        PartPos.of(
                                helper.getLevel(),
                                helper.absolutePos(POS.east().east()),
                                Direction.EAST),
                        outputVarCard.copy()));
            }
        });

        // OPERATOR mode: display panel shows a ValueOperator (NbtExtractionOperator).
        // Apply the operator to the sheep's NBT and verify it extracts Health = 8.0.
        helper.succeedWhen(() -> {
            helper.assertTrue(partAndState.get() != null, "Display panel not yet set up");
            IValue displayValue = partAndState.get().getRight().getDisplayValue();
            helper.assertTrue(
                    displayValue instanceof ValueTypeOperator.ValueOperator,
                    "Display value is not a ValueOperator but: " + displayValue);
            ValueTypeOperator.ValueOperator operatorValue =
                    (ValueTypeOperator.ValueOperator) displayValue;

            // Serialize the sheep's current NBT and apply the extraction operator
            CompoundTag currentEntityNbt = new CompoundTag();
            sheep.saveWithoutId(currentEntityNbt);
            ValueTypeNbt.ValueNbt nbtValue = ValueTypeNbt.ValueNbt.of(currentEntityNbt);
            try {
                IValue result = operatorValue.getRawValue().evaluate(new Variable<>(nbtValue));
                assertValueEqual(result, ValueTypeDouble.ValueDouble.of(8.0));
            } catch (EvaluationException e) {
                throw new GameTestAssertException("Operator evaluation failed: " + e.getMessage());
            }
        });
    }

    /**
     * Tests the NBT_PATH output mode of the NBT Extractor.
     *
     * The NBT extractor (NBT_PATH mode) produces a variable holding the Cyclops NBT path
     * string for the configured extraction path. For a single "Health" key segment the
     * expected string is "$.Health" (the dot prefix is added by KeySegment).
     */
    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = TIMEOUT)
    public void testNbtExtractorOutputModeNbtPath(GameTestHelper helper) {
        SegmentedNbtPath path = new SegmentedNbtPath();
        path.pushKey("Health");

        Pair<Sheep, BlockEntityNbtExtractor> setup = setupEntityReaderNetwork(
                helper, NbtExtractorOutputMode.NBT_PATH, path, DEFAULT_NBT_ID_FLOAT);
        BlockEntityNbtExtractor nbtExtractor = setup.getRight();

        Wrapper<Pair<PartTypePanelDisplay, PartTypePanelDisplay.State>> partAndState = new Wrapper<>(null);
        helper.runAfterDelay(5, () -> {
            ItemStack outputVarCard = nbtExtractor.getInventory()
                    .getItem(BlockEntityNbtExtractor.VAR_OUT_SLOT);
            if (!outputVarCard.isEmpty()) {
                partAndState.set(placeVariableInDisplayPanel(
                        helper.getLevel(),
                        PartPos.of(helper.getLevel(),
                                helper.absolutePos(POS.east().east()), Direction.EAST),
                        outputVarCard.copy()));
            }
        });

        // NBT_PATH mode: display panel shows the Cyclops NBT path string.
        // For path ["Health"], getCyclopsNBTPath() builds "$" + ".Health" = "$.Health"
        // because KeySegment.buildCyclopsNBTPath prepends a dot for each key segment.
        helper.succeedWhen(() -> {
            helper.assertTrue(partAndState.get() != null, "Display panel not yet set up");
            assertValueEqual(
                    partAndState.get().getRight().getDisplayValue(),
                    ValueTypeString.ValueString.of("$.Health"));
        });
    }

    /**
     * Tests that an NBT Extractor Remote item can be bound to an NBT Extractor block.
     *
     * After calling {@link ItemNbtExtractorRemote#bindBlock}, the remote's custom data
     * must contain the world dimension key and the block coordinates of the extractor.
     */
    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = TIMEOUT)
    public void testNbtExtractorRemoteBind(GameTestHelper helper) {
        // Place a cable adjacent to the NBT extractor so it forms a valid network
        helper.setBlock(POS, org.cyclops.integrateddynamics.RegistryEntries.BLOCK_CABLE.value());

        // Place the NBT extractor block
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_NBT_EXTRACTOR.value());
        BlockPos extractorAbsPos = helper.absolutePos(POS.east());

        // Create a remote item and bind it to the extractor
        ItemStack remoteItem = new ItemStack(RegistryEntries.ITEM_NBT_EXTRACTOR_REMOTE.get());
        RegistryEntries.ITEM_NBT_EXTRACTOR_REMOTE.get()
                .bindBlock(remoteItem, helper.getLevel(), extractorAbsPos);

        // Verify the binding data stored in the remote item
        CompoundTag modNbt = RegistryEntries.ITEM_NBT_EXTRACTOR_REMOTE.get().getModNBT(remoteItem);

        helper.assertTrue(modNbt.contains("world"),
                "Remote item does not contain 'world' key after binding");
        helper.assertTrue(modNbt.contains("x"),
                "Remote item does not contain 'x' coordinate after binding");
        helper.assertTrue(modNbt.contains("y"),
                "Remote item does not contain 'y' coordinate after binding");
        helper.assertTrue(modNbt.contains("z"),
                "Remote item does not contain 'z' coordinate after binding");

        helper.assertValueEqual(modNbt.getInt("x"), extractorAbsPos.getX(),
                "Remote item X coordinate mismatch");
        helper.assertValueEqual(modNbt.getInt("y"), extractorAbsPos.getY(),
                "Remote item Y coordinate mismatch");
        helper.assertValueEqual(modNbt.getInt("z"), extractorAbsPos.getZ(),
                "Remote item Z coordinate mismatch");
        helper.assertValueEqual(
                modNbt.getString("world"),
                helper.getLevel().dimension().location().toString(),
                "Remote item dimension key mismatch");

        helper.succeed();
    }
}
