package com.radaeli.ruilib.client.playground;

import com.radaeli.ruilib.client.compat.jei.RUiLibJeiPlugin;
import com.radaeli.ruilib.ui.ItemSlot;
import com.radaeli.ruilib.ui.UiScreen;
import com.radaeli.ruilib.platform.UiJeiProperties;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.lang.reflect.Proxy;

/** Loaded only by the opt-in validation run with JEI on the runtime classpath. */
final class UiJeiValidation {
    @SuppressWarnings("unchecked")
    static void verify(UiScreen screen, ItemSlot slot) {
        IGhostIngredientHandler<UiScreen>[] captured = new IGhostIngredientHandler[1];
        IGuiHandlerRegistration registration = (IGuiHandlerRegistration) Proxy.newProxyInstance(
                IGuiHandlerRegistration.class.getClassLoader(), new Class<?>[]{IGuiHandlerRegistration.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("addGhostIngredientHandler")) captured[0] = (IGhostIngredientHandler<UiScreen>) arguments[1];
                    return null;
                });
        new RUiLibJeiPlugin().registerGuiHandlers(registration);
        if (captured[0] == null) throw new AssertionError("JEI ghost handler not registered");
        ITypedIngredient<ItemStack> ingredient = (ITypedIngredient<ItemStack>) Proxy.newProxyInstance(
                ITypedIngredient.class.getClassLoader(), new Class<?>[]{ITypedIngredient.class},
                (proxy, method, arguments) -> method.getName().equals("getIngredient") ? new ItemStack(Items.EMERALD, 64) : null);
        var targets = captured[0].getTargetsTyped(screen, ingredient, true);
        var target = targets.stream().filter(t -> t.getArea().getX() == slot.bounds().x()
                && t.getArea().getY() == slot.bounds().y()).findFirst().orElseThrow();
        target.accept(new ItemStack(Items.EMERALD, 64));
        if (!slot.itemStack().is(Items.EMERALD) || slot.itemStack().getCount() != 1) throw new AssertionError("JEI ghost accept");
        if (!(new UiJeiProperties(screen) instanceof mezz.jei.api.gui.handlers.IGuiProperties)) throw new AssertionError("JEI properties");
    }
}
