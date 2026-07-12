package org.cyclops.integratednbt.client.gui.component;

import net.minecraft.resources.Identifier;

public class Texture {
    private Identifier resourceLocation;

    public Texture(Identifier resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public Texture(String namespace, String path) {
        this(Identifier.fromNamespaceAndPath(namespace, path));
    }

    public Identifier getResourceLocation() {
        return this.resourceLocation;
    }

    public TexturePart createPart(int x, int y, int width, int height) {
        return new TexturePart(this, x, y, width, height);
    }
}
