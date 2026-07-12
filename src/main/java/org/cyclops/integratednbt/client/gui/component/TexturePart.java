package org.cyclops.integratednbt.client.gui.component;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

/**
 * Represents a part in a texture; Offers help method for quick rendering
 */
public class TexturePart {
    private static final int TEXTURE_SIZE = 256;

    private Texture texture;
    private int x;
    private int y;
    private int width;
    private int height;

    public TexturePart(Texture texture, int x, int y, int width, int height) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void renderTo(GuiGraphicsExtractor gui, int x, int y) {
        gui.blit(RenderPipelines.GUI_TEXTURED, this.texture.getResourceLocation(),
            x, y, (float) this.x, (float) this.y,
            this.width, this.height, this.width, this.height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public void renderTo(GuiGraphicsExtractor gui, int x, int y, int color) {
        gui.blit(RenderPipelines.GUI_TEXTURED, this.texture.getResourceLocation(),
            x, y, (float) this.x, (float) this.y,
            this.width, this.height, this.width, this.height, TEXTURE_SIZE, TEXTURE_SIZE, ARGB.opaque(color));
    }

    public void renderToScaled(GuiGraphicsExtractor gui, int x, int y, int width, int height) {
        int destWidth = width == -1 ? this.width : width;
        int destHeight = height == -1 ? this.height : height;
        gui.blit(RenderPipelines.GUI_TEXTURED, this.texture.getResourceLocation(),
            x, y, (float) this.x, (float) this.y,
            destWidth, destHeight, this.width, this.height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public void renderToScaled(GuiGraphicsExtractor gui, int x, int y, int width, int height, int color) {
        int destWidth = width == -1 ? this.width : width;
        int destHeight = height == -1 ? this.height : height;
        gui.blit(RenderPipelines.GUI_TEXTURED, this.texture.getResourceLocation(),
            x, y, (float) this.x, (float) this.y,
            destWidth, destHeight, this.width, this.height, TEXTURE_SIZE, TEXTURE_SIZE, ARGB.opaque(color));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
