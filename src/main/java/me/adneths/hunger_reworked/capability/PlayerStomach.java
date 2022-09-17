package me.adneths.hunger_reworked.capability;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import me.adneths.hunger_reworked.init.Registration;
import me.adneths.hunger_reworked.network.Messages;
import me.adneths.hunger_reworked.network.StomachPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;

public class PlayerStomach
{
	public List<Food> content;
	public int totalFood;

	public PlayerStomach()
	{
		content = new ArrayList<Food>();
		totalFood = 0;
	}

	public void addFood(Player player, Food food)
	{
		content.add(food);
		if(!player.level.isClientSide)
		{
		totalFood += food.food;
		sendUpdatePacket(player);
		}
	}

	public void popFood(Player player)
	{
		content.remove(content.size()-1);
		if(!player.level.isClientSide)
		{
			totalFood = 0;
			content.forEach((food) -> {totalFood += food.foodRemaining();});
			sendUpdatePacket(player);
		}
	}
	
	public void digest(Player player, float amount)
	{		
		FoodData data = player.getFoodData();
		double part = amount;
		double spillover = 0;
		int maxFood = 20-data.getFoodLevel();
		float satAmount = 0;
		for (int i = 0; i < content.size() && maxFood > 0; i++)
		{
			if (i != content.size() - 1) part /= 2;
			Food food = content.get(i);

			double newProg = food.progress + (part+spillover)/food.getDigest();
			int maxGain = food.foodGained(Math.min(1, newProg));
			if(maxGain > maxFood)
			{
				food.progress += food.digestNeeded(maxFood);
				maxFood = 0;
				break;
			}
			else
			{
				maxFood -= maxGain;
				satAmount += food.satGained(newProg);
				if(newProg < 1)
					food.progress = newProg;
				else
				{
					spillover = (newProg-1)*food.getDigest();
					content.remove(i--);
					if(!player.level.isClientSide)
						for (Pair<MobEffectInstance, Float> pair : food.getEffects())
							if (!player.level.isClientSide && pair.getFirst() != null && player.level.random.nextFloat() < pair.getSecond())
								player.addEffect(new MobEffectInstance(pair.getFirst()));
				}
			}
		}

		if(!player.level.isClientSide)
		{
			data.setFoodLevel(20-maxFood);
			data.setSaturation(Math.min(data.getFoodLevel(), satAmount));
			
			totalFood = 0;
			content.forEach((food) -> {totalFood += food.foodRemaining();});
			
			MobEffectInstance ss = player.getEffect(Registration.STRONG_STOMACH.get());
			int ext;
			if((ext = totalFood - 20 - (ss==null?0:(ss.getAmplifier()+1)*8)) > 0 && !player.hasEffect(Registration.VOMITING.get()))
			{
				player.addEffect(new MobEffectInstance(Registration.OVERSTUFFED.get(), 80, ext/8 - (ss==null?0:ss.getAmplifier())));
			}
			
			sendUpdatePacket(player);
		}
	}

	public static void sendUpdatePacket(Player player)
	{
		Messages.sendToPlayer(new StomachPacket(player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).orElse(new PlayerStomach())), (ServerPlayer)player);
	}
	
	public void copyFrom(PlayerStomach source)
	{
		this.content.clear();
		for (Food food : source.content)
			this.content.add(food);
		this.totalFood = source.totalFood;
	}

	public void saveNBTData(CompoundTag compound)
	{
		ListTag list = new ListTag();
		for (Food food : content)
			list.add(food.getNBT());
		compound.put("content", list);
		compound.putInt("total", this.totalFood);
	}

	public void loadNBTData(CompoundTag compound)
	{
		content.clear();
		compound.getList("content", Tag.TAG_COMPOUND).stream().forEach((tag) -> {
			content.add(Food.fromNBT((CompoundTag) tag));
		});
		this.totalFood = compound.getInt("total");
	}
	
	@Override
	public String toString()
	{
		return content.toString();
	}

	public static class Food
	{
		public static final List<Pair<MobEffectInstance, Float>> EMPTY_EFFECTS = ImmutableList.of();
		
		private final int food;
		private final float sat;
		private final List<Pair<MobEffectInstance, Float>> effects;
		private double progress;

		public Food(FoodProperties prop, float progress)
		{
			this.food = prop.getNutrition();
			this.sat = prop.getSaturationModifier();
			this.effects = prop.getEffects();
			this.progress = progress;
		}

		public Food(int food, float sat, double prog, List<Pair<MobEffectInstance, Float>> effects)
		{
			this.food = food;
			this.sat = sat;
			this.progress = prog;
			this.effects = effects;
		}

		public Food(FoodProperties prop)
		{
			this(prop, 0);
		}
		
		protected int foodDigested()
		{
			return (int)(progress*food);
		}
		
		protected int foodRemaining()
		{
			return food - foodDigested();
		}
		
		protected double digestNeeded(int amount)
		{
			return amount/food - progress;
		}
		
		protected int foodGained(double newProg)
		{
			return (int) (Math.min(1,newProg) * food) - (int) (progress * food);
		}
		protected double satGained(double newProg)
		{
			return floorTo5(Math.min(1,newProg) * sat) - floorTo5(progress * sat);
		}
		private static double floorTo5(double f)
		{
			return ((int) (f / .5f)) * .5f;
		}

		public CompoundTag getNBT()
		{
			CompoundTag tag = new CompoundTag();
			tag.putInt("food", food);
			tag.putFloat("sat", sat);
			tag.putDouble("prog", progress);
			ListTag list = new ListTag();
			effects.forEach((pair) -> {
				CompoundTag comp = new CompoundTag();
				pair.getFirst().save(comp);
				comp.putFloat("prob", pair.getSecond());
				list.add(comp);
			});
			tag.put("effects", list);
			return tag;
		}

		public static Food fromNBT(CompoundTag tag)
		{
			return new Food(tag.getInt("food"), tag.getFloat("sat"), tag.getDouble("prog"), tag.getList("effects", Tag.TAG_COMPOUND).stream().map((nbt) -> Pair.of(MobEffectInstance.load((CompoundTag)nbt),((CompoundTag)nbt).getFloat("prob"))).toList());
		}

		public float getDigest()
		{
			return sat;
		}

		public int getFood()
		{
			return food;
		}
		
		public double getProgress()
		{
			return progress;
		}
		
		public List<Pair<MobEffectInstance, Float>> getEffects()
		{
			return effects;
		}
		
		@Override
		public String toString()
		{
			return String.format("%d %.1f - %.2f", food, sat, progress);
		}
	}
}
