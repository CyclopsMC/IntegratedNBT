package org.cyclops.integratednbt.client.gui.component;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Basically net.minecraft.client.gui.ImageButton, but more dynamic
 */
public class ImageButton extends Button {
    private TexturePart textureNormal;
    private TexturePart textureHover;

    public ImageButton(
        TexturePart textureNormal,
        TexturePart textureHover,
        int x,
        int y,
        Button.OnPress onPress
    ) {
        super(Button.builder(Component.literal(""), onPress).pos(x, y).size(textureNormal.getWidth(), textureNormal.getHeight()));
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
    }

    /**
     * For lazy initialization of textures.
     */
    public ImageButton(int x, int y, Button.OnPress onPress) {
        super(Button.builder(Component.literal(""), onPress).pos(x, y).size(1, 1));
    }

    public void setTexture(TexturePart textureNormal, TexturePart textureHover) {
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
        this.width = textureNormal.getWidth();
        this.height = textureNormal.getHeight();
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width &&
                mouseY < this.getY() + this.height;
            TexturePart texturePart = this.isHovered
                ? this.textureHover
                : this.textureNormal;
            texturePart.renderTo(guiGraphics, this.getX(), this.getY(), 0xffffff);
        }
    }
}
