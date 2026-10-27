package com.radaeli.ruilib.client.playground;

import com.radaeli.ruilib.forge.RUiLib;
import com.radaeli.ruilib.client.example.ItemSlotExampleScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.Dist;

@EventBusSubscriber(modid = RUiLib.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class UiPlaygroundClientTick {
    private UiPlaygroundClientTick() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (FMLEnvironment.production || event.phase != TickEvent.Phase.END) return;
        UiValidation.clientTick();
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
        if (UiPlaygroundKeyRegistration.OPEN_ITEM_SLOT_EXAMPLE.consumeClick()
                && !(minecraft.screen instanceof ItemSlotExampleScreen)) {
            minecraft.setScreen(new ItemSlotExampleScreen());
        }
    }
}
