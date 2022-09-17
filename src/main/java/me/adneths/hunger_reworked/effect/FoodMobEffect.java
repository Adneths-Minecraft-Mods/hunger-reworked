package me.adneths.hunger_reworked.effect;

import me.adneths.hunger_reworked.capability.PlayerStomach;
import me.adneths.hunger_reworked.capability.PlayerStomachProvider;
import me.adneths.hunger_reworked.init.Registration;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FoodMobEffect extends MobEffect
{
	public FoodMobEffect(MobEffectCategory pCategory, int pColor)
	{
		super(pCategory, pColor);
	}

	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier)
	{
		if (this == Registration.OVERSTUFFED.get())
		{
			if (!pLivingEntity.level.isClientSide && pLivingEntity.level.random.nextFloat() < 0.02f * pAmplifier)
			{
				pLivingEntity.removeEffect(this);
				pLivingEntity.addEffect(new MobEffectInstance(Registration.VOMITING.get(), 200, pAmplifier));
				pLivingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 6000, 0));
				pLivingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6000, 0));
			}
		}
		else if (this == Registration.STRONG_STOMACH.get())
		{
			if(!pLivingEntity.level.isClientSide && pLivingEntity instanceof Player player && player.hasEffect(Registration.OVERSTUFFED.get()))
			{
				PlayerStomach data = player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).orElse(null);
				if(data.totalFood - (1+pAmplifier)*8 <= 20)
					player.removeEffect(Registration.OVERSTUFFED.get());
			}
		}
		else if (this == Registration.VOMITING.get())
		{
			if (pLivingEntity instanceof Player player)
			{
				if(!player.level.isClientSide)
				{
					PlayerStomach data = player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).orElse(null);
					if (data != null && data.content.size() > 0)
					{
						data.popFood(player);
					}
					else
					{
						FoodData food = player.getFoodData();
						if (food.getFoodLevel() > Math.max(0,10 - 2 * pAmplifier))
							food.setFoodLevel(food.getFoodLevel() - 1);
					}
				}

				Level level = player.level;
				level.playSound(player, player.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1, 1 + level.random.nextFloat()*.2f);
				level.playSound(player, player.blockPosition(), SoundEvents.DROWNED_HURT, SoundSource.PLAYERS, 1, 1 + level.random.nextFloat()*.2f);
				Vec3 pos = player.getEyePosition();
				for (int j = 0; j < 5; ++j)
					level.addParticle(ParticleTypes.ITEM_SLIME, pos.x, pos.y, pos.z, 0,0,0);
			}
		}
	}

	public boolean isDurationEffectTick(int pDuration, int pAmplifier)
	{
		if (this == Registration.OVERSTUFFED.get())
			return pDuration % 20 == 0;
		else if (this == Registration.VOMITING.get())
			return pDuration % 2 == 0;
		else if (this == Registration.STRONG_STOMACH.get())
			return pDuration % 10 == 0;
		return false;
	}

}
