package org.cyclops.integratednbt.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integratednbt.evaluate.nbt.NbtValueConverter;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.helpers.Wrapper;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.cyclops.integratednbt.client.gui.container.ContainerScreenNbtExtractor.GUI_TEXTURE;
import static org.cyclops.integratednbt.client.gui.container.ContainerScreenNbtExtractor.SCREEN_EDGE;

public abstract class NbtTreeViewer {
    private static final long SMOOTH_SCROLLING_TRANSITION_TIME_MS = 75;
    private static final double SCROLL_SPEED = 30;
    private static final int LINE_SPACE = 1;
    private static final int EXPAND_BUTTON_RIGHT_MARGIN = 3;
    private static final int INDENTATION = 10;
    private static final int HIGHLIGHT_COLOR = 0xFF505050; // Dark gray
    private static final int SELECTED_COLOR = 0xFF506850; // Lighter Dark gray
    private static final int LABEL_COLOR = 0xFFFF55; // Yellow
    private static final int EXPAND_COLOR = 0xFFBB11; // Orange
    private static final int NUMBER_COLOR = 0x66FFFF; // Cyan
    private static final int STRING_COLOR = 0x55FF55; // Green
    private static final int COMPLEX_COLOR = 0xAAAAAA; // Gray
    private static final int EMPTY_COLOR = 0x777777; // Gray
    private static final int SCREEN_BACKGROUND_COLOR = 0xFF303030;
    private static final int SCROLL_BAR_COLOR = 0xFFCCCCCC;
    private static final int SCROLL_BAR_PADDING = 2;
    private static final int SCROLL_BAR_WIDTH = 3;
    private static final int EXPAND_BUTTON_SIZE = 7;
    private static final TexturePart PLUS_BUTTON = GUI_TEXTURE.createPart(
        48,
        0,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart PLUS_BUTTON_HOVER = GUI_TEXTURE.createPart(
        48,
        9,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart MINUS_BUTTON = GUI_TEXTURE.createPart(
        57,
        0,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart MINUS_BUTTON_HOVER = GUI_TEXTURE.createPart(
        57,
        9,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );

    private final Set<SegmentedNbtPath> expandedPaths;
    private final Wrapper<Integer> scrollTop;
    private final Font fontRenderer = Minecraft.getInstance().font;
    private AbstractContainerScreen<?> gui;
    private int left;
    private int top;
    private int width;
    private int height;
    private double renderScroll;
    private long renderScrollTransitionStartTime;
    private double renderScrollTransitionStartLocation;
    private int maxScroll = 0;
    private int currentY;
    private int currentX;
    private SegmentedNbtPath currentPath;
    private SegmentedNbtPath hoveringPath;
    private Tag hoveringNBTNode;
    private SegmentedNbtPath selecting;
    /**
     * X coordinate of mouse in the screen
     */
    private int mouseX;
    /**
     * Y coordinate of mouse in the screen
     */
    private int mouseY;
    private SegmentedNbtPath hoveringExpandableButton;

    public NbtTreeViewer(
        AbstractContainerScreen<?> gui,
        Set<SegmentedNbtPath> expandedPaths,
        Wrapper<Integer> scrollTop
    ) {
        this.gui = gui;
        this.expandedPaths = expandedPaths;
        this.scrollTop = scrollTop;
        this.renderScroll = scrollTop.get();
    }

    public void updateBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void mouseScrolled(double dWheel) {
        int newValue = (int) (this.scrollTop.get() - dWheel * SCROLL_SPEED);
        if (newValue < 0) {
            newValue = 0;
        } else if (newValue > this.maxScroll) {
            newValue = this.maxScroll;
        }
        if (this.scrollTop.get() != newValue) {
            this.scrollTop.set(newValue);
            this.startScrollTransition();
        }
    }

    private void startScrollTransition() {
        this.renderScrollTransitionStartLocation = this.renderScroll;
        this.renderScrollTransitionStartTime = System.currentTimeMillis();
    }

    private boolean renderKVPair(
        GuiGraphicsExtractor guiGraphics,
        String label,
        String value,
        int valueColor
    ) {
        if (label.isEmpty()) {
            return false;
        }
        boolean isSelected = this.getSelectedPath().equals(this.currentPath);
        boolean isHovering = false;
        int bottomBoundary = this.currentY + this.fontRenderer.lineHeight;
        boolean isThreeSideInBounds = ((this.mouseX >= 0) && (this.mouseX < this.width) &&
            (this.mouseY >= this.renderScroll) && (this.mouseY < this.renderScroll + this.height) &&
            (this.mouseY >= this.currentY) && (this.mouseY < bottomBoundary + LINE_SPACE) &&
            (this.mouseX >= this.currentX));
        if (isThreeSideInBounds || isSelected) {
            int rightBoundary =
                this.currentX + this.fontRenderer.width(label + (
                    value.isEmpty()
                        ? ""
                        : (": " + value)
                ));
            if (isSelected || this.mouseX < rightBoundary) {
                if (isThreeSideInBounds && this.mouseX < rightBoundary) {
                    isHovering = true;
                }
                guiGraphics.fill(
                    this.currentX - 1,
                    this.currentY - 1,
                    rightBoundary + 1,
                    bottomBoundary + 1,
                    isSelected ? SELECTED_COLOR : HIGHLIGHT_COLOR
                );
            }
        }
        if (value.isEmpty()) {
            guiGraphics.text(this.fontRenderer, label, this.currentX, this.currentY, LABEL_COLOR, false);
        } else {
            guiGraphics.text(this.fontRenderer, label + ": ", this.currentX, this.currentY, LABEL_COLOR, false);
            int valueX = this.currentX + this.fontRenderer.width(label + ": ");
            guiGraphics.text(this.fontRenderer, value, valueX, this.currentY, valueColor, false);
        }
        return isHovering;
    }

    private void renderExpandableButton(GuiGraphicsExtractor guiGraphics, boolean expanded) {
        boolean hovering = (
            this.mouseX >= this.currentX &&
                this.mouseX < (this.currentX + EXPAND_BUTTON_SIZE) &&
                this.mouseY >= this.currentY &&
                this.mouseY < (this.currentY + PLUS_BUTTON.getHeight())
        );
        TexturePart part = expanded
            ? (hovering ? MINUS_BUTTON_HOVER : MINUS_BUTTON)
            : (hovering ? PLUS_BUTTON_HOVER : PLUS_BUTTON);
        if (hovering) {
            this.hoveringExpandableButton = this.currentPath.copy();
        }
        part.renderTo(guiGraphics, this.currentX, this.currentY, EXPAND_COLOR);
        this.currentX += EXPAND_BUTTON_SIZE + EXPAND_BUTTON_RIGHT_MARGIN;
    }

    public void render(GuiGraphicsExtractor guiGraphics, Tag nbt, int absMouseX, int absMouseY) {
        this.hoveringPath = null;
        this.hoveringNBTNode = null;
        this.hoveringExpandableButton = null;
        this.currentPath = new SegmentedNbtPath();
        this.currentY = SCREEN_EDGE;
        this.updateScroll();
        this.mouseX = absMouseX - this.left;
        this.mouseY = (int) (absMouseY - this.top + this.renderScroll);
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        try {
            poseStack.translate(this.left, this.top - (float) this.renderScroll);
            if (nbt == null) {
                guiGraphics.text(
                    this.fontRenderer,
                    I18n.get("integratednbt:nbt_extractor.empty"),
                    SCREEN_EDGE,
                    this.currentY,
                    EMPTY_COLOR,
                    false
                );
            } else {
                this.renderNode(guiGraphics, I18n.get("integratednbt:nbt_extractor.root"), nbt);
            }
            int totalHeight = this.currentY + SCREEN_EDGE;
            poseStack.translate(0, (float) this.renderScroll);
            this.maxScroll = Math.max(totalHeight - this.height, 0);
            if (this.scrollTop.get() > this.maxScroll) {
                this.scrollTop.set(this.maxScroll);
                this.startScrollTransition();
            }
            if (this.maxScroll != 0) {
                guiGraphics.fill(
                    this.width - SCROLL_BAR_PADDING * 2 - SCROLL_BAR_WIDTH - 1,
                    -1,
                    this.width + 1,
                    this.height,
                    SCREEN_BACKGROUND_COLOR
                );
                guiGraphics.fill(
                    this.width - SCROLL_BAR_PADDING - SCROLL_BAR_WIDTH,
                    SCROLL_BAR_PADDING + (int) (
                        this.renderScroll / totalHeight * (
                            this.height - SCROLL_BAR_PADDING * 2)),
                    this.width - SCROLL_BAR_PADDING,
                    SCROLL_BAR_PADDING + (int) (
                        this.renderScroll / totalHeight * (
                            this.height - SCROLL_BAR_PADDING * 2)) +
                        (int) (Math.ceil(
                            (double) this.height / totalHeight * (
                                this.height - SCROLL_BAR_PADDING * 2))),
                    SCROLL_BAR_COLOR
                );
            }
        } finally {
            poseStack.popMatrix();
        }
    }

    public void renderTooltip(GuiGraphicsExtractor guiGraphics, int absMouseX, int absMouseY) {
        if (this.hoveringPath != null) {
            IValueType<? extends IValue> hoveringValueType =
                    NbtValueConverter.mapNBTToValueType(this.hoveringNBTNode);
            ArrayList<String> list = new ArrayList<>(5);
            list.add(this.hoveringPath.getDisplayText());
            list.add(I18n.get(
                    "integratednbt:nbt_extractor.tooltip.nbt_type",
                    // Get NBT tag type from tag type id
                    TagTypes.getType(this.hoveringNBTNode.getId()).getPrettyName()
            ));
            list.add(I18n.get(
                    "integratednbt:nbt_extractor.tooltip.converted_type",
                    hoveringValueType.getDisplayColorFormat()
                            + I18n.get(hoveringValueType.getTranslationKey())
            ));
            list.add(I18n.get(
                    "integratednbt:nbt_extractor.tooltip.default_value",
                    NbtValueConverter.getDefaultValueDisplayText(this.hoveringNBTNode.getId())
            ));
            if (Objects.equals(this.getSelectedPath(), this.hoveringPath)) {
                list.add(I18n.get("integratednbt:nbt_extractor.tooltip.selected"));
            } else if (Objects.equals(this.selecting, this.hoveringPath)) {
                list.add(I18n.get("integratednbt:nbt_extractor.tooltip.selecting"));
            } else {
                list.add(I18n.get("integratednbt:nbt_extractor.tooltip.left_click"));
            }
            if (isNodeExpandable(this.hoveringNBTNode)) {
                if (this.expandedPaths.contains(this.hoveringPath)) {
                    list.add(I18n.get("integratednbt:nbt_extractor.tooltip"
                            + ".right_click_collapse"));
                } else {
                    list.add(I18n.get(
                            "integratednbt:nbt_extractor.tooltip.right_click_expand"));
                }
            }
            List<Component> tooltipComponents = new ArrayList<>();
            for (String s : list) {
                tooltipComponents.add(Component.literal(s));
            }
            guiGraphics.setTooltipForNextFrame(
                    this.fontRenderer,
                    tooltipComponents,
                    java.util.Optional.empty(),
                    absMouseX,
                    absMouseY
            );
        }
    }

    private void updateScroll() {
        long transitionTime = System.currentTimeMillis() - this.renderScrollTransitionStartTime;
        if (transitionTime > SMOOTH_SCROLLING_TRANSITION_TIME_MS) {
            // Transition ended
            this.renderScroll = this.scrollTop.get();
            return;
        }
        double ratio = (double) transitionTime / SMOOTH_SCROLLING_TRANSITION_TIME_MS;
        this.renderScroll = this.renderScrollTransitionStartLocation
            + (this.scrollTop.get() - this.renderScrollTransitionStartLocation) * ratio;
    }

    public void mouseClicked(int mouseButton) {
        if (mouseButton == 0) { // Left click
            if (this.hoveringExpandableButton != null) {
                this.toggleExpanded(this.hoveringExpandableButton);
            }
            if (this.hoveringPath != null) {
                this.selecting = this.hoveringPath;
                this.onUpdateSelectedPath(this.hoveringPath, this.hoveringNBTNode);
            }
        } else if (mouseButton == 1) { // Right click
            if (this.hoveringPath != null && isNodeExpandable(this.hoveringNBTNode)) {
                this.toggleExpanded(this.hoveringPath);
            }
        }
    }

    private void toggleExpanded(SegmentedNbtPath path) {
        if (this.expandedPaths.contains(path)) {
            this.expandedPaths.remove(path);
        } else {
            this.expandedPaths.add(path.copy());
        }
    }

    public abstract void onUpdateSelectedPath(SegmentedNbtPath newPath, Tag nbt);

    private static boolean isNodeExpandable(Tag nbt) {
        int nbtId = nbt.getId();
        return nbtId == 9 || nbtId == 10;
    }

    private void renderEmpty(GuiGraphicsExtractor guiGraphics) {
        this.currentX = (this.currentPath.getDepth() + 1) * INDENTATION + SCREEN_EDGE
            + EXPAND_BUTTON_RIGHT_MARGIN + EXPAND_BUTTON_SIZE;
        guiGraphics.text(
            this.fontRenderer,
            I18n.get("integratednbt:nbt_extractor.empty"),
            this.currentX,
            this.currentY,
            EMPTY_COLOR,
            false
        );
        this.currentY += this.fontRenderer.lineHeight + LINE_SPACE;
    }

    private void renderNode(GuiGraphicsExtractor guiGraphics, String label, Tag node) {
        this.currentX = this.currentPath.getDepth() * INDENTATION + SCREEN_EDGE;
        boolean isExpandedIfExpandable = false;
        if (isNodeExpandable(node)) {
            isExpandedIfExpandable = this.expandedPaths.contains(this.currentPath);
        } else if (this.currentPath.getDepth() != 0) {
            this.currentX += EXPAND_BUTTON_RIGHT_MARGIN + EXPAND_BUTTON_SIZE;
        }

        boolean isHoveringText;

        // Render Value
        switch (node.getId()) {
            case 1: // Byte
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    String.valueOf(((ByteTag) node).byteValue()),
                    NUMBER_COLOR
                );
                break;
            case 2: // Short
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    String.valueOf(((ShortTag) node).shortValue()),
                    NUMBER_COLOR
                );
                break;
            case 3: // Int
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    String.valueOf(((IntTag) node).intValue()),
                    NUMBER_COLOR
                );
                break;
            case 4: // Long
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    String.valueOf(((LongTag) node).longValue()),
                    NUMBER_COLOR
                );
                break;
            case 5: // Float
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    String.valueOf(((FloatTag) node).floatValue()),
                    NUMBER_COLOR
                );
                break;
            case 6: // Double
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    String.valueOf(((DoubleTag) node).doubleValue()),
                    NUMBER_COLOR
                );
                break;
            case 7: // Byte Array
            case 11: // Int Array
            case 12: // Long Array
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    "[]",
                    NUMBER_COLOR
                );
                break;
            case 8: // String
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    // Yes, I understand technically we should escape double quotes in the string.
                    // However, I think that will just make it confusing.
                    '"' + node.asString().orElse("") + '"',
                    STRING_COLOR
                );
                break;
            case 9: // List
            case 10: { // Compound
                this.renderExpandableButton(guiGraphics, isExpandedIfExpandable);
                isHoveringText = this.renderKVPair(
                    guiGraphics,
                    label,
                    (isExpandedIfExpandable ? "" : node.toString()),
                    COMPLEX_COLOR
                );
                break;
            }
            default:
                throw new RuntimeException("Unexpected NBT id:" + node.getId());
        }
        if (isHoveringText) {
            this.hoveringPath = this.currentPath.copy();
            this.hoveringNBTNode = node;
        }
        this.currentY += this.fontRenderer.lineHeight + LINE_SPACE;
        if (isExpandedIfExpandable) {
            // Recursive calls
            switch (node.getId()) {
                case 9: { // List
                    if (((ListTag) node).size() == 0) {
                        this.renderEmpty(guiGraphics);
                        break;
                    }
                    int i = 0;
                    for (Tag item : (ListTag) node) {
                        this.currentPath.pushIndex(i);
                        this.renderNode(guiGraphics, "#" + i, item);
                        this.currentPath.pop();
                        i++;
                    }
                    break;
                }
                case 10: { // Compound
                    if (((CompoundTag) node).size() == 0) {
                        this.renderEmpty(guiGraphics);
                        break;
                    }
                    CompoundTag compound = (CompoundTag) node;
                    for (String key : compound.keySet()) {
                        this.currentPath.pushKey(key);
                        this.renderNode(guiGraphics, key, compound.get(key));
                        this.currentPath.pop();
                    }
                    break;
                }
            }
        }
    }

    public abstract SegmentedNbtPath getSelectedPath();
}
