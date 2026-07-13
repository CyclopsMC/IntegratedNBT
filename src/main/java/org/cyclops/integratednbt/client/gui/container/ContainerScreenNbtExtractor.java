package org.cyclops.integratednbt.client.gui.container;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.cyclops.integratednbt.IntegratedNbt;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.client.gui.component.HoverTextImageButton;
import org.cyclops.integratednbt.client.gui.component.NbtTreeViewer;
import org.cyclops.integratednbt.client.gui.component.Texture;
import org.cyclops.integratednbt.client.gui.component.TexturePart;
import org.cyclops.integratednbt.evaluate.NbtExtractorOutputMode;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.inventory.container.ContainerNbtExtractor;
import org.cyclops.integratednbt.network.packet.NbtExtractorSetExtractionPathPacket;
import org.cyclops.integratednbt.network.packet.NbtExtractorSetOutputModePacket;
import org.cyclops.integratednbt.network.packet.NbtExtractorUpdateAutoRefreshPacket;
import org.cyclops.integratednbt.network.packet.UpdateClientNbtExtractorPacket;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Arrays;


public class ContainerScreenNbtExtractor extends AbstractContainerScreen<ContainerNbtExtractor> {
    public static final int SCREEN_EDGE = 4;
    public static final Texture GUI_TEXTURE = new Texture(
        "integratednbt",
        "textures/gui/nbt_extractor.png"
    );
    // Different parts of the texture; See texture file for definitions
    private static final TexturePart PART0 = GUI_TEXTURE.createPart(0, 0, 8, 24);
    private static final TexturePart PART1 = GUI_TEXTURE.createPart(12, 0, 4, 24);
    private static final TexturePart PART2 = GUI_TEXTURE.createPart(20, 0, 8, 24);
    private static final TexturePart PART3 = GUI_TEXTURE.createPart(0, 28, 8, 4);
    private static final TexturePart PART4 = GUI_TEXTURE.createPart(12, 28, 4, 4);
    private static final TexturePart PART5 = GUI_TEXTURE.createPart(20, 28, 8, 4);
    private static final TexturePart PART6 = GUI_TEXTURE.createPart(0, 36, 8, 8);
    private static final TexturePart PART7 = GUI_TEXTURE.createPart(12, 36, 4, 8);
    private static final TexturePart PART8 = GUI_TEXTURE.createPart(20, 36, 178, 110);
    private static final TexturePart PART9 = GUI_TEXTURE.createPart(202, 36, 8, 8);
    private static final int BUTTON_SIZE = 12;
    private static final TexturePart BUTTON_UNKNOWN = GUI_TEXTURE.createPart(
        78,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_UNKNOWN_HOVER = GUI_TEXTURE.createPart(
        78,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_ON = GUI_TEXTURE.createPart(
        126,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_ON_HOVER = GUI_TEXTURE.createPart(
        126,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_OFF = GUI_TEXTURE.createPart(
        138,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_OFF_HOVER = GUI_TEXTURE.createPart(
        138,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final int BASE_PADDING = 200;
    private static final int INVENTORY_WIDTH = 178;
    private static final int INVENTORY_HEIGHT = 110;
    private static final int TOP_BORDER_SIZE = 24;
    private static final int SIDE_BORDER_SIZE = 8;
    private static final double CENTERED_TEXT_MAX_RATIO = 0.8;
    private static final int BUTTON_SPACING = 2;

    // These are static because GUI sometimes after receiving the update packets.
    private static ContainerScreenNbtExtractor lastInstance = null;
    // Null signify that the first update packet has not arrived yet.
    private static UpdateClientNbtExtractorPacket.ErrorCode errorCode = null;
    private static Tag nbt;
    private static SegmentedNbtPath extractionPath = null;
    private static NbtExtractorOutputMode outputMode = null;
    private static Component errorMessage = null;
    private static Boolean autoRefresh = null;

    private NbtTreeViewer treeViewer;
    private ContainerNbtExtractor nbtExtractorContainer;
    private Font fontRenderer = Minecraft.getInstance().font;
    /**
     * Padding outside the GUI; Responsive; Updated by updateCalculations
     */
    private int padding;
    /**
     * The width of NBT screen; Responsive; Updated by updateCalculations
     */
    private int screenWidth;
    /**
     * The height of NBT screen; Responsive; Updated by updateCalculations
     */
    private int screenHeight;
    /**
     * The scale factor of Minecraft; Updated by updateCalculations
     */
    private double scaleFactor;
    private HoverTextImageButton outputModeButton;
    private HoverTextImageButton autoRefreshButton;

    public ContainerScreenNbtExtractor(
        ContainerNbtExtractor screenContainer,
        Inventory inventory,
        Component title
    ) {
        super(screenContainer, inventory, title);
        ContainerScreenNbtExtractor.lastInstance = this;
        this.nbtExtractorContainer = screenContainer;
        BlockEntityNbtExtractor tileEntity = this.nbtExtractorContainer.getNbtExtractorEntity();
        this.treeViewer = new NbtTreeViewer(
            this,
            tileEntity.getExpandedPaths(),
            tileEntity.getScrollTop()
        ) {
            @Override
            public void onUpdateSelectedPath(SegmentedNbtPath newPath, Tag nbt) {
                IntegratedNbt._instance.getPacketHandler().sendToServer(new NbtExtractorSetExtractionPathPacket(
                        ContainerScreenNbtExtractor.this.nbtExtractorContainer.getNbtExtractorEntity()
                                .getBlockPos(),
                        newPath,
                        nbt.getId()
                ));
            }

            @Override
            public SegmentedNbtPath getSelectedPath() {
                return extractionPath;
            }
        };
    }

    public static void updateError(UpdateClientNbtExtractorPacket.ErrorCode errorCode) {
        ContainerScreenNbtExtractor.errorCode = errorCode;
    }

    public static void updateNBT(Tag nbt) {
        ContainerScreenNbtExtractor.nbt = nbt;
    }

    public static void updateExtractionPath(SegmentedNbtPath extractionPath) {
        ContainerScreenNbtExtractor.extractionPath = extractionPath;
    }

    public static void updateOutputMode(NbtExtractorOutputMode outputMode) {
        ContainerScreenNbtExtractor.outputMode = outputMode;
        if (lastInstance != null) {
            lastInstance.updateOutputModeButton();
        }
    }

    private void updateOutputModeButton() {
        if (this.outputModeButton == null) {
            return;
        }
        ArrayList<Component> messages = new ArrayList<>();
        if (outputMode == null) {
            this.outputModeButton.setTexture(
                BUTTON_UNKNOWN,
                BUTTON_UNKNOWN_HOVER
            );
            messages.add(Component.translatable(
                "integratednbt:nbt_extractor.output_mode",
                Component.translatable("integratednbt:nbt_extractor.loading")
            ));
        } else {
            this.outputModeButton.setTexture(
                outputMode.getButtonTextureNormal(),
                outputMode.getButtonTextureHover()
            );
            messages.add(Component.translatable(
                "integratednbt:nbt_extractor.output_mode",
                outputMode.getName()
            ));
        }
        messages.add(Component.translatable(
            "integratednbt:nbt_extractor.output_mode.description.begin").setStyle(Style.EMPTY.withColor(
            ChatFormatting.GRAY)));
        messages.add(Component.literal(" "));
        Arrays.stream(NbtExtractorOutputMode.values())
            .forEach(describingOutputMode -> messages.add(describingOutputMode.getDescription(
                describingOutputMode.equals(outputMode))));
        messages.add(Component.literal(" "));
        messages.add(Component.translatable(
            "integratednbt:nbt_extractor.output_mode.description.end",
            NbtExtractorOutputMode.REFERENCE.getName()
        ).setStyle(Style.EMPTY.withColor(
            ChatFormatting.GRAY)));
        this.outputModeButton.setHoverText(messages);
    }

    public static void updateErrorMessage(Component errorMessage) {
        ContainerScreenNbtExtractor.errorMessage = errorMessage;
    }

    public static void updateAutoRefresh(Boolean autoRefresh) {
        ContainerScreenNbtExtractor.autoRefresh = autoRefresh;
        if (lastInstance != null) {
            lastInstance.updateAutoRefreshButton();
        }
    }

    private void updateAutoRefreshButton() {
        if (this.autoRefreshButton == null) {
            return;
        }
        ArrayList<String> messages = new ArrayList<>();
        if (autoRefresh == null) {
            this.autoRefreshButton.setTexture(
                BUTTON_UNKNOWN,
                BUTTON_UNKNOWN_HOVER
            );
            messages.add(I18n.get(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.get("integratednbt:nbt_extractor.loading")
            ));
        } else if (autoRefresh) {
            this.autoRefreshButton.setTexture(
                BUTTON_REFRESH_ON,
                BUTTON_REFRESH_ON_HOVER
            );
            messages.add(I18n.get(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.get("integratednbt:nbt_extractor.auto_refresh.on")
            ));
        } else {
            this.autoRefreshButton.setTexture(
                BUTTON_REFRESH_OFF,
                BUTTON_REFRESH_OFF_HOVER
            );
            messages.add(I18n.get(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.get("integratednbt:nbt_extractor.auto_refresh.off")
            ));
        }
        messages.addAll(Arrays.asList(I18n.get(
            "integratednbt:nbt_extractor.auto_refresh.description").split("\\\\n")));
        this.autoRefreshButton.setHoverTextRaw(messages);
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubleClick
    ) {
        super.mouseClicked(event, doubleClick);
        this.treeViewer.mouseClicked(event.button());
        return true;
    }

    @Override
    protected void init() {
        this.updateCalculations();
        super.init();
        // Override leftPos and topPos for full-screen responsive layout
        this.leftPos = this.padding;
        this.topPos = this.padding;
        int containerWidth = this.width - 2 * this.padding;
        int containerHeight = this.height - 2 * this.padding;
        this.nbtExtractorContainer.setSlotOffset(
            (containerWidth - INVENTORY_WIDTH) / 2,
            containerHeight - INVENTORY_HEIGHT
        );
        this.treeViewer.updateBounds(
            this.padding + SIDE_BORDER_SIZE,
            this.padding + TOP_BORDER_SIZE,
            this.screenWidth,
            this.screenHeight
        );
        this.outputModeButton = new HoverTextImageButton(
            this,
            this.width - this.padding - 7 - BUTTON_SIZE,
            this.padding + 7,
            this::onOutputModeButtonClick
        );
        this.updateOutputModeButton();
        this.addWidget(this.outputModeButton);
        this.autoRefreshButton = new HoverTextImageButton(
            this,
            this.width - this.padding - 7 - BUTTON_SIZE * 2 - BUTTON_SPACING,
            this.padding + 7,
            this::onAutoRefreshButtonClick
        );
        this.updateAutoRefreshButton();
        this.addWidget(this.autoRefreshButton);
    }

    /**
     * Update
     */
    private void updateCalculations() {
        this.scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
        this.padding = (int) Math.min(
            Math.max(BASE_PADDING / Math.pow(this.scaleFactor, 3), 4),
            Math.min(this.width, this.height) / 10.
        );
        this.screenWidth = this.width - 2 * this.padding - 2 * SIDE_BORDER_SIZE;
        this.screenHeight = this.height - 2 * this.padding - TOP_BORDER_SIZE - INVENTORY_HEIGHT;
    }

    public void onOutputModeButtonClick(Button ignored) {
        if (outputMode == null) {
            return;
        }
        IntegratedNbt._instance.getPacketHandler().sendToServer(new NbtExtractorSetOutputModePacket(
                this.nbtExtractorContainer.getNbtExtractorEntity().getBlockPos(),
                NbtExtractorOutputMode.values()[(outputMode.ordinal() + 1) %
                        NbtExtractorOutputMode.values().length]
        ));
    }

    public void onAutoRefreshButtonClick(Button ignored) {
        if (autoRefresh == null) {
            return;
        }
        IntegratedNbt._instance.getPacketHandler().sendToServer(new NbtExtractorUpdateAutoRefreshPacket(
                this.nbtExtractorContainer.getNbtExtractorEntity().getBlockPos(),
                !autoRefresh
        ));
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double scrollX,
        double scrollY
    ) {
        super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (errorCode == UpdateClientNbtExtractorPacket.ErrorCode.NO_ERROR && nbt != null) {
            this.treeViewer.mouseScrolled(scrollY);
        }
        return true;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        this.outputModeButton.drawHover(guiGraphics, mouseX, mouseY);
        this.autoRefreshButton.drawHover(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw the GUI frame background BEFORE super, so items/slots are rendered on top of it.
        this.renderGuiParts(guiGraphics);
        guiGraphics.text(
            this.fontRenderer,
            I18n.get("block.integratednbt.nbt_extractor"),
            this.padding + 8,
            this.padding + 9,
            ARGB.opaque(4210752),
            false
        );
        // Scissor test allows restricting rendering to a rectangular portion of the screen.
        // In this case, we only want to render in the screen area of the NBT Extractor.
        guiGraphics.enableScissor(
            this.padding + SIDE_BORDER_SIZE,
            this.padding + TOP_BORDER_SIZE,
            this.padding + SIDE_BORDER_SIZE + this.screenWidth,
            this.padding + TOP_BORDER_SIZE + this.screenHeight
        );
        Slot srcNBTSlot = this.nbtExtractorContainer.getSrcNBTSlot();
        if (!srcNBTSlot.hasItem()) {
            errorCode = null;
            this.renderWelcome(guiGraphics);
        } else if (errorCode == null) {
            this.renderLoading(guiGraphics);
        } else if (!errorCode.equals(UpdateClientNbtExtractorPacket.ErrorCode.NO_ERROR)) {
            this.renderError(guiGraphics);
        } else {
            this.treeViewer.render(guiGraphics, nbt, mouseX, mouseY);
        }
        guiGraphics.disableScissor();
        // Draw widgets (buttons) + labels + slot highlights + item icons on top of the GUI frame.
        super.extractContents(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        this.treeViewer.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void removed() {
        if (lastInstance == this) {
            lastInstance = null;
            errorCode = null;
            nbt = null;
            extractionPath = null;
            outputMode = null;
            errorMessage = null;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderGuiParts(GuiGraphicsExtractor guiGraphics) {
        int padding = this.padding;
        int screenWidth = this.screenWidth;
        int screenHeight = this.screenHeight;
        PART0.renderTo(guiGraphics, padding, padding);
        PART1.renderToScaled(guiGraphics, padding + SIDE_BORDER_SIZE, padding, screenWidth, -1);
        PART2.renderTo(guiGraphics, this.width - padding - SIDE_BORDER_SIZE, padding);
        PART3.renderToScaled(guiGraphics, padding, padding + TOP_BORDER_SIZE, -1, screenHeight);
        PART4.renderToScaled(
            guiGraphics,
            padding + SIDE_BORDER_SIZE,
            padding + TOP_BORDER_SIZE,
            screenWidth,
            screenHeight
        );
        PART5.renderToScaled(
            guiGraphics,
            this.width - padding - SIDE_BORDER_SIZE,
            padding + TOP_BORDER_SIZE,
            -1,
            screenHeight
        );
        int topOfPart6789 = this.height - padding - INVENTORY_HEIGHT;
        PART6.renderTo(guiGraphics, padding, topOfPart6789);
        int part7Width2x = this.width - 2 * padding - 2 * SIDE_BORDER_SIZE - INVENTORY_WIDTH;
        int part7WidthFloor = (int) Math.floor(part7Width2x / 2.0);
        int part7WidthCeil = (int) Math.ceil(part7Width2x / 2.0);
        PART7.renderToScaled(
            guiGraphics,
            padding + SIDE_BORDER_SIZE,
            topOfPart6789,
            part7WidthFloor,
            -1
        );
        PART8.renderTo(
            guiGraphics,
            padding + SIDE_BORDER_SIZE + part7WidthFloor,
            topOfPart6789
        );
        PART7.renderToScaled(
            guiGraphics,
            padding + SIDE_BORDER_SIZE + part7WidthFloor + INVENTORY_WIDTH,
            topOfPart6789,
            part7WidthCeil,
            -1
        );
        PART9.renderTo(
            guiGraphics,
            this.width - padding - SIDE_BORDER_SIZE,
            topOfPart6789
        );
    }

    private void renderWelcome(GuiGraphicsExtractor guiGraphics) {
        this.renderCenteredTextGroup(
            guiGraphics,
            I18n.get("integratednbt:nbt_extractor.welcome"),
            0xFF00FFFF,
            I18n.get("integratednbt:nbt_extractor.welcome.description")
        );
    }

    private void renderLoading(GuiGraphicsExtractor guiGraphics) {
        this.renderCenteredTextGroup(
            guiGraphics,
            I18n.get("integratednbt:nbt_extractor.loading"),
            0xFFFFFF00,
            I18n.get("integratednbt:nbt_extractor.loading.description")
        );
    }

    private void renderError(GuiGraphicsExtractor guiGraphics) {
        String message = "";
        if (errorMessage != null) {
            message = errorMessage.getString();
        } else {
            switch (errorCode) {
                case EVAL_ERROR:
                    message = I18n.get("integratednbt:nbt_extractor.error.eval");
                    break;
                case TYPE_ERROR:
                    message = I18n.get("integratednbt:nbt_extractor.error.type");
                    break;
                case UNEXPECTED_ERROR:
                    message = I18n.get("integratednbt:nbt_extractor.error.unexpected");
                    break;
            }
        }
        this.renderCenteredTextGroup(
            guiGraphics,
            I18n.get("integratednbt:nbt_extractor.error"),
            0xFFFF5555,
            message
        );
    }

    private void renderCenteredTextGroup(GuiGraphicsExtractor guiGraphics, String title, int titleColor, String description) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        try {
            int x = this.screenCenterX();
            int y = this.screenCenterY();
            int titleWidth = this.fontRenderer.width(title);
            poseStack.pushMatrix();
            try {
                this.scaleAt(poseStack, x, y, 2);
                guiGraphics.text(
                    this.fontRenderer,
                    title,
                    (int) (-titleWidth / 2f),
                    -this.fontRenderer.lineHeight - 1,
                    titleColor,
                    false
                );
            } finally {
                poseStack.popMatrix();
            }
            this.scaleAt(poseStack, x, y, 1);
            int wrappingWidth = (int) (this.screenWidth * CENTERED_TEXT_MAX_RATIO);
            int descriptionWidth = this.fontRenderer.width(description);
            if (descriptionWidth > wrappingWidth) {
                guiGraphics.textWithWordWrap(
                    this.fontRenderer,
                    Component.literal(description),
                    -wrappingWidth / 2,
                    4,
                    wrappingWidth,
                    0xFFFFFFFF
                );
            } else {
                guiGraphics.text(
                    this.fontRenderer,
                    description,
                    (int) (-descriptionWidth / 2f),
                    4,
                    0xFFFFFFFF,
                    false
                );
            }
        } finally {
            poseStack.popMatrix();
        }
    }

    private int screenCenterX() {
        return this.width / 2;
    }

    private int screenCenterY() {
        return this.padding + TOP_BORDER_SIZE + this.screenHeight / 2;
    }

    private void scaleAt(Matrix3x2fStack poseStack, int x, int y, float scale) {
        poseStack.scale(scale, scale);
        poseStack.translate(x / scale, y / scale);
    }
}
