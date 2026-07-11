package org.cyclops.integratednbt.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * Represents a part in a texture; Offers help method for quick rendering
 */
public class TexturePart {
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

    public void renderTo(GuiGraphics gui, int x, int y) {
        gui.blit(this.texture.getResourceLocation(), x, y, this.x, this.y, this.width, this.height);
    }

    private void setColorInt(int color) {
        RenderSystem.setShaderColor(
            (float) (color >> 16 & 255) / 255.0f,
            (float) (color >> 8 & 255) / 255.0f,
            (float) (color & 255) / 255.0F,
            1
        );
    }

    public void renderTo(GuiGraphics gui, int x, int y, int color) {
        this.setColorInt(color);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        this.renderTo(gui, x, y);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public void renderToScaled(GuiGraphics gui, int x, int y, int width, int height) {
        int destWidth = width == -1 ? this.width : width;
        int destHeight = height == -1 ? this.height : height;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        this.texture.bind();
        Matrix4f matrix = gui.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, x, y + destHeight, 0)
            .uv((float) (this.x) * 0.00390625F, (float) (this.y + this.height) * 0.00390625F)
            .endVertex();
        bufferbuilder.vertex(matrix, x + destWidth, y + destHeight, 0)
            .uv((float) (this.x + this.width) * 0.00390625F, (float) (this.y + this.height) * 0.00390625F)
            .endVertex();
        bufferbuilder.vertex(matrix, x + destWidth, y, 0)
            .uv((float) (this.x + this.width) * 0.00390625F, (float) (this.y) * 0.00390625F)
            .endVertex();
        bufferbuilder.vertex(matrix, x, y, 0)
            .uv((float) (this.x) * 0.00390625F, (float) (this.y) * 0.00390625F)
            .endVertex();
        tesselator.end();
    }

    public void renderToScaled(GuiGraphics gui, int x, int y, int width, int height, int color) {
        this.setColorInt(color);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        this.renderToScaled(gui, x, y, width, height);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
