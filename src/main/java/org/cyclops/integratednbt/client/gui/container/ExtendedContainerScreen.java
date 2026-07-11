package org.cyclops.integratednbt.client.gui.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.joml.Matrix4f;

public abstract class ExtendedContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public ExtendedContainerScreen(
        T screenContainer,
        Inventory inv,
        Component titleIn
    ) {
        super(screenContainer, inv, titleIn);
    }

    public void drawTexturedModalRectScalable(
        Matrix4f matrix,
        int destX, int destY,
        int destWidth, int destHeight,
        int srcX, int srcY,
        int srcWidth, int srcHeight
    ) {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix, destX, destY + destHeight, 0)
            .setUv((float) (srcX) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F);
        bufferbuilder.addVertex(matrix, destX + destWidth, destY + destHeight, 0)
            .setUv((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F);
        bufferbuilder.addVertex(matrix, destX + destWidth, destY, 0)
            .setUv((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY) * 0.00390625F);
        bufferbuilder.addVertex(matrix, destX, destY, 0)
            .setUv((float) (srcX) * 0.00390625F, (float) (srcY) * 0.00390625F);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
