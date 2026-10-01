package com.radaeli.easyblockpallets.client.playground;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = "easy_block_pallets", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class UiPlaygroundClientTick {
    private UiPlaygroundClientTick() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (FMLEnvironment.production || !UiPlaygroundKeyRegistration.OPEN_PLAYGROUND.consumeClick()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !(minecraft.screen instanceof UiPlaygroundScreen)) {
            minecraft.setScreen(new UiPlaygroundScreen());
        }
    }
}
