package com.radaeli.ruilib.client.playground;

import com.radaeli.ruilib.neoforge.RUiLib;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = RUiLib.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class UiPlaygroundClientTick {
    private UiPlaygroundClientTick() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (FMLEnvironment.production) return;
        Minecraft minecraft = Minecraft.getInstance();
        UiPlaygroundToastOverlay.tick();
        if (minecraft.player == null) return;
        if (UiPlaygroundKeyRegistration.DISPATCH_TEST_TOAST.consumeClick()) {
            UiPlaygroundToastOverlay.dispatchDemoToast();
        }
        if (UiPlaygroundKeyRegistration.OPEN_PLAYGROUND.consumeClick()
                && !(minecraft.screen instanceof UiPlaygroundScreen)) {
            minecraft.setScreen(new UiPlaygroundScreen());
        }
    }
}
