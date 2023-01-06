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
		content.remove(content.size() - 1);
		if(!player.level.isClientSide)
		{
			totalFood = 0;
			content.forEach((food) -> {
				totalFood += food.foodRemaining();
			});
			sendUpdatePacket(player);
		}
	}

	private int foodDigested = 0;
	private float satDigested = 0;

	private double digest(Food food, double amount, int maxGain)
	{
		double nProgress = food.progress + amount / food.sat;
		double spill = 0;
		if(nProgress > 1)
		{
			spill = (nProgress - 1) * food.sat;
			nProgress = 1;
		}
		int foodGain = food.foodGained(nProgress);
		if(maxGain < foodGain)
		{
			nProgress = food.factorDigestNeeded(maxGain);
			foodGain = maxGain;
		}
		foodDigested += foodGain;
		satDigested += food.satGained(nProgress);

		food.progress = nProgress;

		return spill;
	}

	public void digest(Player player, double amount)
	{
		if(!player.level.isClientSide)
		{
			foodDigested = 0;
			satDigested = 0;

			FoodData data = player.getFoodData();
			double part = amount;
			int maxFood = 20 - data.getFoodLevel();
			for(int i = 0; i < content.size() && maxFood > 0; i++)
			{
				if(i != content.size() - 1)
					part /= 2;
				Food food = content.get(i);

				digest(food, part, maxFood);
				if(food.progress >= 1)
				{
					for(Pair<MobEffectInstance, Float> pair : food.effects)
						if(pair.getFirst() != null && player.level.random.nextFloat() < pair.getSecond())
							player.addEffect(new MobEffectInstance(pair.getFirst()));
					content.remove(i--);
				}
				maxFood = 20 - data.getFoodLevel() - foodDigested;
				if(maxFood < 1)
					break;
			}

			data.setFoodLevel(data.getFoodLevel() + foodDigested);
			data.setSaturation(Math.min(data.getFoodLevel(), data.getSaturationLevel() + satDigested));

			totalFood = 0;
			content.forEach((food) -> {
				totalFood += food.foodRemaining();
			});

			MobEffectInstance ss = player.getEffect(Registration.STRONG_STOMACH.get());
			int ext;
			if((ext = totalFood - 20 - (ss == null ? 0 : (ss.getAmplifier() + 1) * 8)) > 0 && !player.hasEffect(Registration.VOMITING.get()))
			{
				player.addEffect(new MobEffectInstance(Registration.OVERSTUFFED.get(), 80, ext / 8 - (ss == null ? 0 : ss.getAmplifier())));
			}

			sendUpdatePacket(player);
		}
	}

	public static void sendUpdatePacket(Player player)
	{
		Messages.sendToPlayer(new StomachPacket(player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).orElse(new PlayerStomach())), (ServerPlayer) player);
	}

	public void copyFrom(PlayerStomach source)
	{
		this.content.clear();
		for(Food food : source.content)
			this.content.add(food);
		this.totalFood = source.totalFood;
	}

	public void saveNBTData(CompoundTag compound)
	{
		ListTag list = new ListTag();
		for(Food food : content)
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
			return (int) (progress * food);
		}

		protected int foodRemaining()
		{
			return food - foodDigested();
		}

		protected double factorDigestNeeded(int amount)
		{
			return (double) (amount + foodDigested()) / food;
		}

		protected int foodGained(double newProg)
		{
			return (int) (Math.min(1, newProg) * food) - (int) (progress * food);
		}

		protected float satGained(double newProg)
		{
			return (float) (Math.min(1, newProg) * sat - progress * sat);
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
			return new Food(tag.getInt("food"), tag.getFloat("sat"), tag.getDouble("prog"), tag.getList("effects", Tag.TAG_COMPOUND).stream().map((nbt) -> Pair.of(MobEffectInstance.load((CompoundTag) nbt), ((CompoundTag) nbt).getFloat("prob"))).toList());
		}

		@Override
		public String toString()
		{
			return String.format("%d %.1f - %.2f", food, sat, progress);
		}
	}
}
