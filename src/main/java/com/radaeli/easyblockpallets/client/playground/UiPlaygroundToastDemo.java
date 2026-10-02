package com.radaeli.easyblockpallets.client.playground;

import com.radaeli.easyblockpallets.ui.Toast;
import com.radaeli.easyblockpallets.ui.ToastHost;
import net.minecraft.network.chat.Component;

/** Shared toast showcase sequence used by the playground button and the F6 HUD shortcut. */
final class UiPlaygroundToastDemo {
    private static int nextToast;

    private UiPlaygroundToastDemo() {
    }

    static void dispatch(ToastHost host) {
        switch (nextToast++ % 4) {
            case 0 -> host.show(Toast.info(Component.literal("Info toast preview")));
            case 1 -> host.show(Toast.success(Component.literal("Success toast preview")));
            case 2 -> host.show(Toast.warning(Component.literal("Warning toast preview")));
            default -> host.show(Toast.error(Component.literal("Error toast preview")));
        }
    }
}
