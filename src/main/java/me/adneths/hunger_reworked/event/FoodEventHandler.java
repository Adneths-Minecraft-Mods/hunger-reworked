package me.adneths.hunger_reworked.event;

import me.adneths.hunger_reworked.HungerReworked;
import me.adneths.hunger_reworked.capability.PlayerStomach;
import me.adneths.hunger_reworked.capability.PlayerStomach.Food;
import me.adneths.hunger_reworked.capability.PlayerStomachProvider;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FoodEventHandler
{

	@SubscribeEvent
	public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Player && !event.getObject().getCapability(PlayerStomachProvider.PLAYER_STOMACH).isPresent())
			event.addCapability(new ResourceLocation(HungerReworked.MODID, "player_stomach"), new PlayerStomachProvider());
	}

	@SubscribeEvent
	public static void onPlayerCloned(PlayerEvent.Clone event)
	{
		if (!event.isWasDeath())
		{
			event.getOriginal().reviveCaps();
			event.getOriginal().getCapability(PlayerStomachProvider.PLAYER_STOMACH).ifPresent(oldStore -> {
				event.getPlayer().getCapability(PlayerStomachProvider.PLAYER_STOMACH).ifPresent(newStore -> {
					newStore.copyFrom(oldStore);
				});
			});
			event.getOriginal().invalidateCaps();
		}
	}

	@SubscribeEvent
	public static void onPlayerJoined(PlayerLoggedInEvent event)
	{
		Player player = event.getPlayer();
		if (!player.level.isClientSide)
			PlayerStomach.sendUpdatePacket(player);
	}

	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(PlayerStomach.class);
	}

	private static int tickCounter = 0;

	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event)
	{
		if (tickCounter++ > 60)
		{
			if (!event.player.getAbilities().instabuild)
				event.player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).ifPresent((stomach) -> stomach.digest(event.player, 0.1f));
			tickCounter = 0;
		}
	}

	@SubscribeEvent
	public static void onPlayerEatCake(PlayerInteractEvent.RightClickBlock event)
	{
		ItemStack stack = event.getItemStack();
		BlockState state = event.getWorld().getBlockState(event.getPos());
		Player player = event.getPlayer();
		if (state.getBlock().equals(Blocks.CAKE))
		{
			if (!stack.is(ItemTags.CANDLES) || state.getValue(CakeBlock.BITES) != 0 || !(Block.byItem(stack.getItem()) instanceof CandleBlock))
			{
				event.setCancellationResult(InteractionResult.SUCCESS);
				event.setCanceled(true);
				
				player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).ifPresent((stomach) -> {
					stomach.addFood(event.getPlayer(), new Food(2, 0.1f, 0, Food.EMPTY_EFFECTS));

					Level level = player.level;
					player.awardStat(Stats.EAT_CAKE_SLICE);
					int i = state.getValue(CakeBlock.BITES);
					level.gameEvent(player, GameEvent.EAT, event.getPos());
					if (i < 6)
					{
						level.setBlock(event.getPos(), state.setValue(CakeBlock.BITES, Integer.valueOf(i + 1)), 3);
					}
					else
					{
						level.removeBlock(event.getPos(), false);
						level.gameEvent(player, GameEvent.BLOCK_DESTROY, event.getPos());
					}
				});
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerEat(LivingEntityUseItemEvent.Tick event)
	{
		if (event.getEntityLiving() instanceof Player player)
		{
			ItemStack pFood = event.getItem();
			if (player.getUseItemRemainingTicks() == 1 && pFood.isEdible())
			{
				event.setCanceled(true);

				player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).ifPresent((stomach) -> {
					stomach.addFood(player, new PlayerStomach.Food(event.getItem().getFoodProperties(player)));
					
					Level pLevel = player.level;
					player.awardStat(Stats.ITEM_USED.get(pFood.getItem()));
					pLevel.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
					if (player instanceof ServerPlayer)
						CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, pFood);

					pLevel.gameEvent(player, GameEvent.EAT, player.eyeBlockPosition());
					pLevel.playSound((Player) null, player.getX(), player.getY(), player.getZ(), player.getEatingSound(pFood), SoundSource.NEUTRAL, 1.0F,
							1.0F + (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.4F);
					player.gameEvent(GameEvent.EAT);

					if (!pLevel.isClientSide || player.isUsingItem())
					{
						InteractionHand interactionhand = player.getUsedItemHand();
						if (!player.getUseItem().equals(player.getItemInHand(interactionhand)))
							player.releaseUsingItem();
						else if (!player.getUseItem().isEmpty() && player.isUsingItem())
						{
							player.triggerItemUseEffects(player.getUseItem(), 16);
							player.stopUsingItem();
						}
					}
				});
				
				ItemStack old = pFood.copy();
				if (!player.getAbilities().instabuild)
					pFood.shrink(1);
				ForgeEventFactory.onItemUseFinish(player, old, 0, pFood);
			}
		}
	}
}
