package me.adneths.hunger_reworked.init;

import me.adneths.hunger_reworked.event.FoodEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSide
{
	public static Minecraft minecraft;
	public static boolean isClient = false;
	
	@SubscribeEvent
	public static void init(final FMLClientSetupEvent event)
	{
		minecraft = Minecraft.getInstance();
		isClient = true;
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FOOD_LEVEL_ELEMENT, "stomach_hud", FoodEventHandler.STOMACH_OVERLAY);
	}
}