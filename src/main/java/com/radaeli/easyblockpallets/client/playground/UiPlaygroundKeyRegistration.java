package com.radaeli.easyblockpallets.client.playground;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "easy_block_pallets", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class UiPlaygroundKeyRegistration {
    static final KeyMapping OPEN_PLAYGROUND = new KeyMapping(
            "key.easy_block_pallets.open_ui_playground",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            "key.categories.easy_block_pallets"
    );

    private UiPlaygroundKeyRegistration() {
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        if (!FMLEnvironment.production) {
            event.register(OPEN_PLAYGROUND);
        }
    }
}
