package com.radaeli.ruilib.client.playground;

import com.radaeli.ruilib.neoforge.RUiLib;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RUiLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class UiPlaygroundKeyRegistration {
    static final KeyMapping OPEN_PLAYGROUND = new KeyMapping(
            "key.r_ui_lib.open_ui_playground",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            "key.categories.r_ui_lib"
    );
    static final KeyMapping DISPATCH_TEST_TOAST = new KeyMapping(
            "key.r_ui_lib.dispatch_test_toast",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            "key.categories.r_ui_lib"
    );
    static final KeyMapping OPEN_ITEM_SLOT_EXAMPLE = new KeyMapping(
            "key.r_ui_lib.open_item_slot_example",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.r_ui_lib"
    );

    private UiPlaygroundKeyRegistration() {
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        if (!FMLEnvironment.production) {
            event.register(OPEN_PLAYGROUND);
            event.register(DISPATCH_TEST_TOAST);
            event.register(OPEN_ITEM_SLOT_EXAMPLE);
        }
    }
}
