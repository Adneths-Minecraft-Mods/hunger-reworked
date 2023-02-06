package me.adneths.hunger_reworked.core.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.food.FoodData;

@Mixin(FoodData.class)
public class MixinFoodData// implements PlayerSensitive
{
	/*
	Player player;
	
	@Override
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
	@Redirect(method = "eat(IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.PUTFIELD), require = 1)
	private void redirectFoodLevel(FoodData data, int foodLevel)
	{
	}

	@Redirect(method = "eat(IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;saturationLevel:F", opcode = Opcodes.PUTFIELD), require = 1)
	private void redirectSaturationLevel(FoodData data, float saturationLevel)
	{
	}
	*/
}
