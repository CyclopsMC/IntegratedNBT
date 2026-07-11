package org.cyclops.integratednbt.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;

public class Texture {
    private ResourceLocation resourceLocation;

    public Texture(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public Texture(String namespace, String path) {
        this(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, this.resourceLocation);
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    public TexturePart createPart(int x, int y, int width, int height) {
        return new TexturePart(this, x, y, width, height);
    }
}
