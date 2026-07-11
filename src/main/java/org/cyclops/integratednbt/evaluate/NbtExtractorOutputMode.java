package org.cyclops.integratednbt.evaluate;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry.IVariableFacadeFactory;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator.ValueOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString.ValueString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integratednbt.evaluate.operator.NbtExtractionOperator;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.evaluate.nbt.NbtValueConverter;
import org.cyclops.integratednbt.client.gui.component.Texture;
import org.cyclops.integratednbt.client.gui.component.TexturePart;
import org.cyclops.integratednbt.evaluate.variable.NbtExtractedVariableFacade;
import org.cyclops.integratednbt.evaluate.variable.NbtExtractedVariableFacadeHandler;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum NbtExtractorOutputMode {
    REFERENCE(
        "reference",
        ChatFormatting.YELLOW,
        Nest.GUI_TEXTURE.createPart(90, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(90, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            Tag currentNBT,
            SegmentedNbtPath extractionPath,
            byte defaultNBTId,
            Level level,
            BlockState blockState,
            Player player) {
            IVariableFacadeHandlerRegistry registry =
                IntegratedDynamics._instance.getRegistryManager()
                    .getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = sourceVariableFacadeSupplier.get();
            if (variableFacade != null) {
                int sourceNBTId = variableFacade.getId();
                IVariableFacadeFactory<NbtExtractedVariableFacade> factory =
                    new IVariableFacadeFactory<NbtExtractedVariableFacade>() {
                        @Override
                        public NbtExtractedVariableFacade create(boolean generateId) {
                            return new NbtExtractedVariableFacade(
                                generateId,
                                sourceNBTId,
                                extractionPath,
                                defaultNBTId
                            );
                        }

                        @Override
                        public NbtExtractedVariableFacade create(int id) {
                            return new NbtExtractedVariableFacade(
                                id,
                                sourceNBTId,
                                extractionPath,
                                defaultNBTId
                            );
                        }
                    };
                return registry.writeVariableFacadeItem(
                    true,
                    outputVariableItemStack,
                    NbtExtractedVariableFacadeHandler.getInstance(),
                    factory,
                    level,
                    player,
                    blockState
                );
            } else {
                return null;
            }
        }
    },
    OPERATOR(
        "operator",
        ChatFormatting.DARK_GREEN,
        Nest.GUI_TEXTURE.createPart(102, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(102, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            Tag currentNBT,
            SegmentedNbtPath extractionPath,
            byte defaultNBTId,
            Level level,
            BlockState blockState,
            Player player) {
            return getVariableUsingValue(ValueOperator.of(new NbtExtractionOperator(
                extractionPath,
                defaultNBTId
            )), outputVariableItemStack, level, blockState, player);
        }
    },
    VALUE(
        "value",
        ChatFormatting.GOLD,
        Nest.GUI_TEXTURE.createPart(114, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(114, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            Tag currentNBT,
            SegmentedNbtPath extractionPath,
            byte defaultNBTId,
            Level level,
            BlockState blockState,
            Player player) {
            sourceVariableFacadeSupplier.get(); // Refresh variable
            Tag extractedNBT = extractionPath.extract(currentNBT);
            IValue value = extractedNBT == null
                ? NbtValueConverter.getDefaultValue(defaultNBTId)
                : NbtValueConverter.mapNBTToValue(extractedNBT);
            return getVariableUsingValue(value, outputVariableItemStack, level, blockState, player);
        }
    },
    NBT_PATH(
        "nbt_path",
        ChatFormatting.RED,
        Nest.GUI_TEXTURE.createPart(150, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(150, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            Tag currentNBT,
            SegmentedNbtPath extractionPath,
            byte defaultNBTId,
            Level level,
            BlockState blockState,
            Player player) {
            return getVariableUsingValue(
                ValueString.of(extractionPath.getCyclopsNBTPath()),
                outputVariableItemStack,
                level,
                blockState,
                player
            );
        }
    };

    private static class Nest {
        private static final Texture GUI_TEXTURE = new Texture(
            "integratednbt",
            "textures/gui/nbt_extractor.png"
        );
        private static final int BUTTON_SIZE = 12;
    }

    private String translationId;
    private ChatFormatting color;
    private TexturePart buttonTextureNormal;
    private TexturePart buttonTextureHover;

    NbtExtractorOutputMode(
        String translationId,
        ChatFormatting color,
        TexturePart buttonTextureNormal,
        TexturePart buttonTextureHover
    ) {
        this.translationId = translationId;
        this.color = color;
        this.buttonTextureNormal = buttonTextureNormal;
        this.buttonTextureHover = buttonTextureHover;
    }

    @Nullable
    @SuppressWarnings( {"rawtypes", "unchecked"})
    private static ItemStack getVariableUsingValue(
        IValue value,
        ItemStack outputVariableItemStack,
        Level level,
        BlockState blockState,
        Player player
    ) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager()
            .getRegistry(IVariableFacadeHandlerRegistry.class);
        if (value == null) {
            return null;
        }
        return registry.writeVariableFacadeItem(
            true,
            outputVariableItemStack,
            ValueTypes.REGISTRY,
            new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade>() {
                @Override
                public IValueTypeVariableFacade create(boolean generateId) {
                    return new ValueTypeVariableFacade(generateId, value.getType(), value);
                }

                @Override
                public IValueTypeVariableFacade create(int id) {
                    return new ValueTypeVariableFacade(id, value.getType(), value);
                }
            },
            level,
            player,
            blockState
        );
    }

    public TexturePart getButtonTextureNormal() {
        return this.buttonTextureNormal;
    }

    public TexturePart getButtonTextureHover() {
        return this.buttonTextureHover;
    }

    public abstract ItemStack writeItemStack(
        Supplier<IVariableFacade> sourceVariableFacadeSupplier,
        ItemStack outputVariableItemStack,
        Tag currentNBT,
        SegmentedNbtPath extractionPath,
        byte defaultNBTId,
        Level level,
        BlockState blockState,
        Player player);

    public Component getDescription(boolean highlighted) {
        return Component.translatable(
            "integratednbt:nbt_extractor.output_mode." + this.translationId + ".description",
            this.getName()
        ).setStyle(highlighted
            ? Style.EMPTY.withColor(ChatFormatting.GRAY)
            : Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
    }

    public Component getName() {
        return Component.translatable(
            "integratednbt:nbt_extractor.output_mode." + this.translationId + ".name"
        ).setStyle(Style.EMPTY.withColor(this.color));
    }
}
