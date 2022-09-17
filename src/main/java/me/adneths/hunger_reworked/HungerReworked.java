package me.adneths.hunger_reworked;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import me.adneths.hunger_reworked.init.ClientSide;
import me.adneths.hunger_reworked.init.CommonSide;
import me.adneths.hunger_reworked.init.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HungerReworked.MODID)
public class HungerReworked
{
    public static final String MODID = "hunger_reworked";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HungerReworked()
    {
    	LOGGER.info("Loading Hunger Reworked");
		Registration.init();
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        modbus.register(CommonSide.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.register(ClientSide.class));
    }
}
