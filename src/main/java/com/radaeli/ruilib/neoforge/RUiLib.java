package com.radaeli.ruilib.neoforge;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(RUiLib.MOD_ID)
public final class RUiLib {
    public static final String MOD_ID = "r_ui_lib";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public RUiLib() {
        LOGGER.info("{} initialized on NeoForge", MOD_ID);
    }
}
