package me.adneths.hunger_reworked.init;

import me.adneths.hunger_reworked.event.FoodEventHandler;
import me.adneths.hunger_reworked.network.Messages;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class CommonSide
{	
	@SubscribeEvent
	public static void init(final FMLCommonSetupEvent event)
	{
		Messages.register();
		event.enqueueWork(() -> {
			BrewingRecipeRegistry.addRecipe(NBTIngredient.of(getPotion(Potions.AWKWARD)), Ingredient.of(Items.CALCITE, Items.NAUTILUS_SHELL), getPotion(Registration.STRONG_STOMACH_POTION.get()));
			BrewingRecipeRegistry.addRecipe(NBTIngredient.of(getPotion(Registration.STRONG_STOMACH_POTION.get())), Ingredient.of(Items.REDSTONE), getPotion(Registration.STRONG_STOMACH_POTION_LONG.get()));
			BrewingRecipeRegistry.addRecipe(NBTIngredient.of(getPotion(Registration.STRONG_STOMACH_POTION.get())), Ingredient.of(Items.GLOWSTONE_DUST), getPotion(Registration.STRONG_STOMACH_POTION_STRONG.get()));
		});
		MinecraftForge.EVENT_BUS.register(FoodEventHandler.class);
	}
	
	private static ItemStack getPotion(Potion potion)
	{
		return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
	}

	@SubscribeEvent
	public static void onFinish(final FMLLoadCompleteEvent event)
	{
		for(Item item : ForgeRegistries.ITEMS.getValues())
		{
			@SuppressWarnings("deprecation")
			FoodProperties food = item.getFoodProperties();
			if(food != null)
				food.canAlwaysEat = true;
		}
	}	
}