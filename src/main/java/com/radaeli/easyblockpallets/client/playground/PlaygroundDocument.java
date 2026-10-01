package com.radaeli.easyblockpallets.client.playground;

import com.radaeli.easyblockpallets.ui.Alignment;
import net.minecraft.resources.ResourceLocation;

final class PlaygroundDocument {
    static final int CURRENT_VERSION = 1;

    int version = CURRENT_VERSION;
    String name = "untitled";
    PlaygroundNode root = PlaygroundNode.create("absolute", 0, 0);

    static PlaygroundDocument empty(String name) {
        PlaygroundDocument document = new PlaygroundDocument();
        document.name = name;
        document.root = PlaygroundNode.create("absolute", 0, 0);
        document.root.width = 0;
        document.root.height = 0;
        return document;
    }

    void normalize() {
        if (version != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported playground document version: " + version);
        }
        if (name == null || name.isBlank()) name = "untitled";
        if (root == null || !root.isAbsolute()) {
            throw new IllegalArgumentException("Document root must be an absolute layout");
        }
        normalizeNode(root, null, 0, new int[]{0});
    }

    private static void normalizeNode(PlaygroundNode node, PlaygroundNode parent, int depth, int[] count) {
        if (depth > 32) throw new IllegalArgumentException("Layout is nested too deeply");
        if (node == null || node.type == null) throw new IllegalArgumentException("Layout contains an empty node");
        if (++count[0] > 512) throw new IllegalArgumentException("Layout has more than 512 components");
        if (node.children == null) node.children = new java.util.ArrayList<>();
        if (!(node.type.equals("label") || node.type.equals("button")
                || node.type.equals("toggle") || node.type.equals("chip")
                || node.type.equals("textfield") || node.type.equals("searchfield")
                || node.type.equals("slider") || node.type.equals("itemcard")
                || node.type.equals("table")
                || node.type.equals("spacer") || node.isContainer())) {
            throw new IllegalArgumentException("Unsupported component type: " + node.type);
        }
        if (!node.isContainer() && !node.children.isEmpty()) {
            throw new IllegalArgumentException("Only layout containers can have children");
        }
        if (node.text == null) node.text = node.type;
        if (node.horizontalAlignment == null) node.horizontalAlignment = Alignment.START;
        if (node.verticalAlignment == null) node.verticalAlignment = Alignment.STRETCH;
        node.selectedTab = Math.max(0, Math.min(node.selectedTab, Math.max(0, node.children.size() - 1)));
        if (node.width < 0 || node.width > 8192 || node.height < 0 || node.height > 8192
                || node.gap < 0 || node.gap > 4096 || Math.abs((long) node.x) > 100_000
                || Math.abs((long) node.y) > 100_000) {
            throw new IllegalArgumentException("Component dimensions or position are outside supported limits");
        }
        if (node.supportsColumns() && (node.columns < 1 || node.columns > 64)) {
            throw new IllegalArgumentException("Grid columns must be between 1 and 64");
        }
        if (node.usesItemStack()) {
            if (node.itemId == null || ResourceLocation.tryParse(node.itemId) == null) {
                throw new IllegalArgumentException("Item card requires a valid namespaced item ID");
            }
            node.itemCount = Math.max(1, Math.min(64, node.itemCount));
            if (node.text == null || node.text.isBlank()) node.text = node.itemId;
        }
        if (node.type.equals("slider")) {
            if (!Double.isFinite(node.minimum) || !Double.isFinite(node.maximum) || !Double.isFinite(node.value)
                    || !Double.isFinite(node.maximum - node.minimum) || !Double.isFinite(node.step)
                    || node.maximum <= node.minimum || node.step < 0) {
                throw new IllegalArgumentException("Slider requires finite min < max and a non-negative step");
            }
            node.value = Math.max(node.minimum, Math.min(node.maximum, node.value));
            if (node.step > 0) {
                node.value = Math.max(node.minimum, Math.min(node.maximum,
                        node.minimum + Math.round((node.value - node.minimum) / node.step) * node.step));
            }
        }
        node.parent = parent;
        for (PlaygroundNode child : node.children) normalizeNode(child, node, depth + 1, count);
    }
}
