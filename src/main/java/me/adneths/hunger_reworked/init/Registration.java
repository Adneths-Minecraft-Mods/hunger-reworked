package me.adneths.hunger_reworked.init;

import me.adneths.hunger_reworked.HungerReworked;
import me.adneths.hunger_reworked.effect.FoodMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration
{
	private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HungerReworked.MODID);
	private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, HungerReworked.MODID);

	public static void init()
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		MOB_EFFECTS.register(bus);
		POTIONS.register(bus);
	}
	
	public static final RegistryObject<MobEffect> OVERSTUFFED = MOB_EFFECTS.register("overstuffed", 
			() -> new FoodMobEffect(MobEffectCategory.HARMFUL, 0xb57b18).addAttributeModifier(Attributes.MOVEMENT_SPEED, "B9DEBEC9-EE6F-4DFD-A4C1-507FB6D700F6", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<MobEffect> VOMITING = MOB_EFFECTS.register("vomiting", 
			() -> new FoodMobEffect(MobEffectCategory.HARMFUL, 0x719665).addAttributeModifier(Attributes.MOVEMENT_SPEED, "BF8881B2-62C1-45F9-A06F-8FFD9ADE6B01", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL)
			.addAttributeModifier(Attributes.ATTACK_SPEED, "EE38244B-B251-4988-A8EF-800F0CFAB727", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<MobEffect> STRONG_STOMACH = MOB_EFFECTS.register("strong_stomach", 
			() -> new FoodMobEffect(MobEffectCategory.BENEFICIAL, 0xd9d9d9));

	public static final RegistryObject<Potion> STRONG_STOMACH_POTION = POTIONS.register("strong_stomach_potion", () -> new Potion("strong_stomach_potion", new MobEffectInstance(STRONG_STOMACH.get(),3600,0)));
	public static final RegistryObject<Potion> STRONG_STOMACH_POTION_LONG = POTIONS.register("strong_stomach_potion_long", () -> new Potion("strong_stomach_potion", new MobEffectInstance(STRONG_STOMACH.get(),9600,0)));
	public static final RegistryObject<Potion> STRONG_STOMACH_POTION_STRONG = POTIONS.register("strong_stomach_potion_strong", () -> new Potion("strong_stomach_potion", new MobEffectInstance(STRONG_STOMACH.get(),3600,1)));
}
