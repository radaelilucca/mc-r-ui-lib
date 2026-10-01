package com.radaeli.easyblockpallets.neoforge;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(EasyBlockPallets.MOD_ID)
public final class EasyBlockPallets {
    public static final String MOD_ID = "easy_block_pallets";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public EasyBlockPallets() {
        LOGGER.info("{} initialized on NeoForge", MOD_ID);
    }
}
