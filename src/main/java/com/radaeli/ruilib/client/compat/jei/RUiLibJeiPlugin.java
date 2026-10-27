package com.radaeli.ruilib.client.compat.jei;

import com.radaeli.ruilib.ui.ItemSlot;
import com.radaeli.ruilib.ui.UiComponent;
import com.radaeli.ruilib.ui.UiScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import com.radaeli.ruilib.platform.UiPlatform;
import com.radaeli.ruilib.platform.UiJeiProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Optional JEI bridge that exposes every visible R-Ui Lib ItemSlot as a ghost target. */
@JeiPlugin
public final class RUiLibJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = UiPlatform.id(
            "r_ui_lib", "item_slot_ghost_ingredients");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(UiScreen.class, UiJeiProperties::new);
        registration.addGhostIngredientHandler(UiScreen.class, new IGhostIngredientHandler<>() {
            @Override
            public <I> List<Target<I>> getTargetsTyped(UiScreen screen,
                                                        ITypedIngredient<I> ingredient,
                                                        boolean doStart) {
                if (!(ingredient.getIngredient() instanceof ItemStack)) return List.of();

                List<ItemSlot> slots = new ArrayList<>();
                UiComponent activeRoot = screen.modal() == null ? screen.root() : screen.modal();
                activeRoot.visitComponents(component -> {
                    if (component instanceof ItemSlot slot && slot.isEnabled()
                            && slot.bounds().width() > 0 && slot.bounds().height() > 0
                            && activeRoot.hitTest(slot.bounds().centerX(), slot.bounds().centerY()) == slot) {
                        slots.add(slot);
                    }
                });

                List<Target<I>> targets = new ArrayList<>(slots.size());
                for (ItemSlot slot : slots) {
                    targets.add(new Target<>() {
                        @Override
                        public Rect2i getArea() {
                            var bounds = slot.bounds();
                            return new Rect2i(bounds.x(), bounds.y(), bounds.width(), bounds.height());
                        }

                        @Override
                        public void accept(I acceptedIngredient) {
                            if (acceptedIngredient instanceof ItemStack stack) {
                                slot.setItemStack(stack);
                            }
                        }
                    });
                }
                return targets;
            }

            @Override
            public void onComplete() {
            }
        });
    }
}
