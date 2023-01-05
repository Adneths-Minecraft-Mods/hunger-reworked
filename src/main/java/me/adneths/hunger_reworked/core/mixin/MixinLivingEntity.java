package me.adneths.hunger_reworked.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity
{
	@Inject(method = "addEatEffect", at = @At("HEAD"), cancellable = true, require = 1)
	private void onAddEatEffect(ItemStack pFood, Level pLevel, LivingEntity pLivingEntity, CallbackInfo ci)
	{
		if(pLivingEntity instanceof Player)
			ci.cancel();
	}
}
