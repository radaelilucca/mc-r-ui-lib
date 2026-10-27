package com.radaeli.ruilib.client.playground;

import com.radaeli.ruilib.neoforge.RUiLib;
import com.radaeli.ruilib.ui.Bounds;
import com.radaeli.ruilib.ui.ToastHost;
import com.radaeli.ruilib.ui.UiContext;
import com.radaeli.ruilib.ui.UiThemes;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Development-only HUD host for exercising toolkit toasts outside the playground screen. */
@EventBusSubscriber(modid = RUiLib.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class UiPlaygroundToastOverlay {
    private static final ToastHost HOST = new ToastHost();

    private UiPlaygroundToastOverlay() {
    }

    static void dispatchDemoToast() {
        UiPlaygroundToastDemo.dispatch(HOST);
    }

    static void tick() {
        HOST.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (FMLEnvironment.production || HOST.size() == 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.font == null) return;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        HOST.layout(minecraft.font, UiThemes.globalTheme(), new Bounds(0, 0, width, height));
        HOST.render(new UiContext(event.getGuiGraphics(), minecraft.font, UiThemes.globalTheme(),
                minecraft.mouseHandler.xpos(), minecraft.mouseHandler.ypos(), width, height));
    }
}
