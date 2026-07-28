package org.skriptlang.skript.bukkit.entity.player.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import io.papermc.paper.event.player.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Method;

@SuppressWarnings("deprecation")
public class PlayerEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Arm Swing")
			.addEvent(PlayerAnimationEvent.class)
			.addPatterns("[player] arm swing")
			.addDescription("""
				Called when a player swings their arm.
				""")
			.addExample("""
				on player arm swing:
					send "You swung your arm!" to player
				""")
			.addSince("2.5.1")
			.supplier(() -> new SimpleEvent("player arm swing"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Bed Enter")
			.addEvent(PlayerBedEnterEvent.class)
			.addPatterns("bed enter[ing]",
				"[player] enter[ing] [a] bed")
			.addDescription("""
				Called when a player starts sleeping.
				""")
			.addExample("""
				on player entering a bed:
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player entering bed"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerBedEnterEvent.class, Block.class)
			.getter(PlayerBedEnterEvent::getBed)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Bed Leave")
			.addEvent(PlayerBedLeaveEvent.class)
			.addPatterns("bed leav(e|ing)",
				"[player] leav(e|ing) [a] bed")
			.addDescription("""
				Called when a player leaves a bed.
				""")
			.addExample("""
				on player leaving a bed:
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player leaving a bed"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerBedLeaveEvent.class, Block.class)
			.getter(PlayerBedLeaveEvent::getBed)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Tool Break")
			.addEvent(PlayerItemBreakEvent.class)
			.addPatterns("[player] tool break[ing]",
				"[player] break[ing] [a|the[ir]] tool")
			.addDescription("""
				Called when a player breaks their tool because its damage reached the maximum value.
				This event cannot be cancelled.
				""")
			.addExample("""
				on player tool breaking:
					broadcast "well.. its gone now"
				""")
			.addSince("2.1.1")
			.supplier(() -> new SimpleEvent("player breaking a tool"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerItemBreakEvent.class, ItemStack.class)
			.getter(PlayerItemBreakEvent::getBrokenItem)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Change Beacon Effect")
			.addEvent(PlayerChangeBeaconEffectEvent.class)
			.addPatterns(
				"[player] chang(e[s]|ing) [of] beacon effect",
				"[on] beacon change effect",
				"[on] beacon effect change"
			)
			.addDescription("""
				Called when a player changes the effects of a beacon.
				""")
			.addExample("""
				on player changing of beacon effect:
					broadcast "The player who did this: %player%"
					broadcast "The location: %location of event-block%"
					broadcast "Hurry to the given location!"
				""")
			.addSince("2.10")
			.supplier(() -> new SimpleEvent("player changing of beacon effect"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerChangeBeaconEffectEvent.class, Block.class)
			.getter(PlayerChangeBeaconEffectEvent::getBeacon)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Tool Break")
			.addEvent(PlayerItemDamageEvent.class)
			.addPatterns("item damag(e|ing)")
			.addDescription("""
				Called when an item is damaged. Most tools are damaged by using them; armor is damaged when the wearer takes damage.
				""")
			.addExample("""
				on item damaging:
					send actionbar "One of your items is taking damage!" to player
				""")
			.addSince("2.5")
			.supplier(() -> new SimpleEvent("item damaging"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerItemDamageEvent.class, ItemStack.class)
			.getter(PlayerItemDamageEvent::getItem)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Deep Sleep")
			.addEvent(PlayerDeepSleepEvent.class)
			.addPatterns("[player] deep sleep[ing]")
			.addDescription("""
				Called when a player has slept long enough to count as passing the night/storm.
				Cancelling this event will prevent the player from being counted as deeply sleeping unless they exit and re-enter the bed.
				""")
			.addExample("""
				on player deep sleeping:
					send "Have a good sleep!" to player
					send actionbar "Zzzz..." to player
				""")
			.addSince("2.7")
			.supplier(() -> new SimpleEvent("player deep sleeping"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Elytra Boost")
			.addEvent(PlayerElytraBoostEvent.class)
			.addPatterns("[player] elytra boost[ing]")
			.addDescription("""
				Called when a player uses a firework to boost their fly speed when flying with an elytra.
				""")
			.addExample("""
				on elytra boost:
					push player up at speed 3
					send "You go forward and up!" to player
				""")
			.addSince("1.10")
			.supplier(() -> new SimpleEvent("player elytra boosting"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerElytraBoostEvent.class, ItemStack.class)
			.getter(PlayerElytraBoostEvent::getItemStack)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerElytraBoostEvent.class, Entity.class)
			.getter(PlayerElytraBoostEvent::getFirework)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Empty Bucket")
			.addEvent(PlayerBucketEmptyEvent.class)
			.addPatterns("bucket empty[ing]",
				"[player] empty[ing] [a] bucket")
			.addDescription("""
				Called when a player empties a bucket.
				You can also use the <a href='#place'>place event</a> with a check for water or lava.
				""")
			.addExample("""
				on player emptying a bucket:
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player empty a bucket"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerBucketEmptyEvent.class, Block.class)
			.getter(event -> event.getBlockClicked().getRelative(event.getBlockFace()))
			.time(EventValue.Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerBucketEmptyEvent.class, Block.class)
			.getter(event -> {
				BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
				state.setType(event.getBucket() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA);
				return new BlockStateBlock(state, true);
			})
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Experience Cooldown Change")
			.addEvent(PlayerExpCooldownChangeEvent.class)
			.addPatterns("[player] (experience|[e]xp) cooldown change")
			.addDescription("""
				Called when a player's experience cooldown changes.
				Experience cooldown is how long until a player can pick up another orb of experience.
				""")
			.addExample("""
				on player experience cooldown change:
					broadcast player
					broadcast event-timespan
					broadcast past event-timespan
					broadcast xp cooldown change reason
				""")
			.addSince("2.10")
			.supplier(() -> new SimpleEvent("player experience cooldown change"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerExpCooldownChangeEvent.class, PlayerExpCooldownChangeEvent.ChangeReason.class)
			.getter(PlayerExpCooldownChangeEvent::getReason)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerExpCooldownChangeEvent.class, Timespan.class)
			.getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getPlayer().getExpCooldown()))
			.time(EventValue.Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerExpCooldownChangeEvent.class, Timespan.class)
			.getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getNewCooldown()))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Flight Toggle")
			.addEvent(PlayerToggleFlightEvent.class)
			.addPatterns("[player] flight toggl(e|ing)",
				"[player] toggl(e|ing) flight")
			.addDescription("""
				Called when a players stops/starts flying.
				""")
			.addExample("""
				on flight toggle:
					player is not operator
					kill player
					send "You tried to use an admin ability!" to player
				""")
			.addSince("2.2-dev36")
			.supplier(() -> new SimpleEvent("player toggle flight"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Tool Change")
			.addEvent(PlayerItemHeldEvent.class)
			.addPatterns("[player['s]] (tool|item held|held item) chang(e|ing)")
			.addDescription("""
				Called whenever a player changes their held item by selecting a different slot (e.g. the keys 1-9 or the mouse wheel), <i>not</i> by dropping or replacing the item in the current slot.
				""")
			.addExample("""
				on player's held item change:
					send "You changed your held item!" to player
				""")
			.addSince("1.0")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerItemHeldEvent.class, Slot.class)
			.getter(event -> new InventorySlot(event.getPlayer().getInventory(), event.getNewSlot()))
			.build());
		eventValueRegistry.register(EventValue.builder(PlayerItemHeldEvent.class, Slot.class)
			.getter(event -> new InventorySlot(event.getPlayer().getInventory(), event.getPreviousSlot()))
			.time(EventValue.Time.PAST)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Inventory Slot Change")
			.addEvent(PlayerInventorySlotChangeEvent.class)
			.addPatterns("[player] inventory slot chang(e|ing)")
			.addDescription("""
				Called when a slot in a player's inventory is changed."
				Warning: setting the event-slot to a new item can result in an infinite loop.
				""")
			.addExample("""
				on inventory slot change:
					event-item is a diamond
					send "Nice diamond you got there!" to player
					chance of 30%:
						remove 1 diamond from player
						send "One diamond for me!" to player
				""")
			.addSince("2.7")
			.supplier(() -> new SimpleEvent("player inventory slot change"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerInventorySlotChangeEvent.class, ItemStack.class)
			.getter(PlayerInventorySlotChangeEvent::getNewItemStack)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerInventorySlotChangeEvent.class, ItemStack.class)
			.getter(PlayerInventorySlotChangeEvent::getOldItemStack)
			.time(EventValue.Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerInventorySlotChangeEvent.class, Slot.class)
			.getter(event -> {
				PlayerInventory inventory = event.getPlayer().getInventory();
				int slotIndex = event.getSlot();
				// Not all indices point to inventory slots. Equipment, for example
				if (slotIndex >= 36) {
					return new EquipmentSlot(event.getPlayer(), slotIndex);
				} else {
					return new InventorySlot(inventory, slotIndex);
				}
			})
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Join")
			.addEvent(PlayerJoinEvent.class)
			.addPatterns("[player] (login|logging in|join[ing])")
			.addDescription("""
				Called when the player joins the server.
				The player is already in a world when this event is called, so if you want to prevent players from joining you should prefer <a href='#connect'>on connect</a> over this event.
				See <a href='#join message'>join message</a> for how to set the join message.
				""")
			.addExample("""
				on join:
					send "Hello %player%!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player join"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Jump")
			.addEvent(PlayerJumpEvent.class)
			.addPatterns("[player] jump[ing]")
			.addDescription("""
				Called whenever a player jumps.
				""")
			.addExample("""
				on jump:
					add 1 to {-example::%player's uuid%}
					if {-example::%player's uuid%} >= 3:
						delete {-example::%player's uuid%}
						push player forwards at speed 2
						send "Forward you go!" to player
					wait 10 ticks
					delete {-example::%player's uuid%}
				""")
			.addSince("2.3")
			.supplier(() -> new SimpleEvent("player jumping"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Kick")
			.addEvent(PlayerKickEvent.class)
			.addPatterns("[player] (kick|being kicked)")
			.addDescription("""
				Called when a player is kicked from the server.
				You can change the <a href='#ExprMessage'>kick message</a> or <a href='#EffCancelEvent'>cancel the event</a> entirely.
				""")
			.addExample("""
				on kick:
					send "%player% just got kicked!" to all operators
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player kick"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Locale Change")
			.addEvent(PlayerLocaleChangeEvent.class)
			.addPatterns("[player] (language|locale) chang(e|ing)",
				"[player] chang(e|ing) (language|locale)")
			.addDescription("""
				Called after a player changed their language in the game settings.
				You can use the <a href='#ExprLanguage'>language</a> expression to get the current language of the player.
				""")
			.addExample("""
				on language change:
					player's language starts with "en"
					send "Hello!" to player
				""")
			.addSince("2.3")
			.supplier(() -> new SimpleEvent("player language change"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Connect")
			.addEvent(PlayerLoginEvent.class)
			.addPatterns("[player] connect[ing]")
			.addDescription("""
				Called when the player connects to the server.
				This event is called before the player actually joins the server, so if you want to prevent players from joining you should prefer this event over <a href='#join'>on join</a>.
				""")
			.addExample("""
				on connect:
					if all:
						player doesn't have permission "group.vip"
						size of all players >= (max players - 5)
					then:
						kick player due to "The last 5 slots are reserved for those with VIP rank!"
				""")
			.addSince("2.0")
			.supplier(() -> new SimpleEvent("player connecting"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Item Mend")
			.addEvent(PlayerItemMendEvent.class)
			.addPatterns("[player] item mend[(ed|ing)]",
				"[player] mend item")
			.addDescription("""
				Called when a player has an item repaired via the Mending enchantment.
				""")
			.addExample("""
				on item mend:
					 send "One of your tools was mended!" to player
				""")
			.addSince("2.5.1")
			.supplier(() -> new SimpleEvent("player item mending"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerItemMendEvent.class, ItemStack.class)
			.getter(PlayerItemMendEvent::getItem)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerItemMendEvent.class, Entity.class)
			.getter(PlayerItemMendEvent::getExperienceOrb)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerItemMendEvent.class, Integer.class)
			.getter(PlayerItemMendEvent::getConsumedExperience)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Quit")
			.addEvent(PlayerQuitEvent.class)
			.addPatterns("[player] (quit[ting]|disconnect[ing]|log[ ]out|logging out|leav(e|ing))")
			.addDescription("""
				Called when a player leaves the server.
				This event cannot be cancelled.
				""")
			.addExample("""
				on quit:
					set the quit message to "%player% just left :("
				""")
			.addSince("1.0 (simple disconnection)")
			.supplier(() -> new SimpleEvent("player quitting"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerQuitEvent.class, PlayerQuitEvent.QuitReason.class)
			.getter(PlayerQuitEvent::getReason)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Ready Arrow")
			.addEvent(PlayerReadyArrowEvent.class)
			.addPatterns("[player] ((ready|choose|draw|load) arrow|arrow (choose|draw|load))")
			.addDescription("""
				Called when a player is firing a bow and the server is choosing an arrow to use.
				Cancelling this event will skip the current arrow item and fire a new event for the next arrow item.
				The arrow and bow in the event can be accessed with the Readied Arrow/Bow expression.
				""")
			.addExample("""
				on player ready arrow:
					if all:
						selected bow's name is "Spectral Bow"
						selected arrow is not a spectral arrow
					then:
						cancel event
						send "You need a spectral arrow to use a spectral bow!" to player
				""")
			.addSince("1.8.0")
			.supplier(() -> new SimpleEvent("player ready arrow"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Riptide")
			.addEvent(PlayerRiptideEvent.class)
			.addPatterns("[use of] riptide [enchant[ment]]")
			.addDescription("""
				Called when the player activates the riptide enchantment, using their trident to propel them through the air.
				Note: the riptide action is performed client side, so manipulating the player in this event may have undesired effects.
				""")
			.addExample("""
				on riptide:
					chance of 10%:
						set the weather to clear
						send "You got unlucky.. the sky cleared up!" to player
				""")
			.addSince("2.5")
			.supplier(() -> new SimpleEvent("use of riptide enchantment"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerRiptideEvent.class, ItemStack.class)
			.getter(PlayerRiptideEvent::getItem)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Sneak Toggle")
			.addEvent(PlayerToggleSneakEvent.class)
			.addPatterns("[player] toggl(e|ing) sneak",
				"[player] sneak toggl(e|ing)")
			.addDescription("""
				Called when a player starts or stops sneaking.
				Use <a href='#CondIsSneaking'>is sneaking</a> to get whether the player was sneaking before the event was called.
				""")
			.addExample("""
				on sneak toggle:
					player is sneaking
					push player upwards at 0t.5
					send "UP!" to player
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player toggling sneak"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Sprint Toggle")
			.addEvent(PlayerToggleSprintEvent.class)
			.addPatterns("[player] toggl(e|ing) sprint",
				"[player] sprint toggl(e|ing)")
			.addDescription("""
				Called when a player starts or stops sprinting.
				Use <a href='#CondIsSprinting'>is sprinting</a> to get whether the player was sprinting before the event was called.
				""")
			.addExample("""
				on sprint toggle:
					player is not sprinting
					chance of 30%:
						spawn wither behind player
						send "Run.." to player
						stop
					send "You got lucky this time.." to player
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player toggling sprint"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Stop Using Item")
			.addEvent(PlayerStopUsingItemEvent.class)
			.addPatterns("[player] (stop|end) (using item|item use)")
			.addDescription("""
				Called when a player stops using an item. For example,
				when the player releases the interact button when holding a bow, an edible item, or a spyglass.
				""")
			.addExample("""
				on player stop using item:
					send "You just used %event-item% for %event-timespan% :)" to player
				""")
			.addSince("2.8.0")
			.supplier(() -> new SimpleEvent("player stop using item"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerStopUsingItemEvent.class, ItemStack.class)
			.getter(PlayerStopUsingItemEvent::getItem)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerStopUsingItemEvent.class, Timespan.class)
			.getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getTicksHeldFor()))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Hand Swap Items")
			.addEvent(PlayerSwapHandItemsEvent.class)
			.addPatterns("[player] swap[ping of] [(hand|held)] item[s]")
			.addDescription("""
				Called whenever a player swaps the items in their main- and offhand slots.
				Works also when one or both of the slots are empty.
				The event is called before the items are actually swapped,
				so when you use the player's tool or player's offtool expressions,
				they will return the values before the swap - this enables you to cancel the event before anything happens.
				""")
			.addExample("""
				on swap hand items:
					player's tool is a totem of undying
					chance of 50%:
						 send "Failed! Please try again!" to player
						 cancel event
				""")
			.addSince("2.3")
			.supplier(() -> new SimpleEvent("player swapping of held item"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Throw Egg")
			.addEvent(PlayerEggThrowEvent.class)
			.addPatterns("throw[ing] [of] [an] egg",
				"[player] egg throw")
			.addDescription("""
				Called when a player throws an egg and it lands.
				You can just use the <a href='#shoot'>shoot event</a> in most cases.
				However, this event allows modification of properties like the hatched entity type and the number of entities to hatch.
				""")
			.addExample("""
				on throw of an egg:
					broadcast "An egg has been thrown!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("throwing of an egg"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerEggThrowEvent.class, Egg.class)
			.getter(PlayerEggThrowEvent::getEgg)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Trade")
			.addEvent(PlayerTradeEvent.class)
			.addPatterns("[player] trad(e|ing)")
			.addDescription("""
				Called when a player trades with a villager or wandering trader.
				""")
			.addExample("""
				on player trading:
					send "Did you get a good deal?" to player
				""")
			.addSince("2.7")
			.supplier(() -> new SimpleEvent("player trading"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerTradeEvent.class, AbstractVillager.class)
			.getter(PlayerTradeEvent::getVillager)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player World Change")
			.addEvent(PlayerChangedWorldEvent.class)
			.addPatterns("[player] world chang(ing|e[d])")
			.addDescription("""
				Called when a player enters a world.
				""")
			.addExample("""
				on player world change:
					event-world is "world_the_end"
					send "Its the end!" to player
				""")
			.addSince("2.2-dev28")
			.supplier(() -> new SimpleEvent("player world changing"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerChangedWorldEvent.class, World.class)
			.getter(PlayerChangedWorldEvent::getFrom)
			.time(EventValue.Time.PAST)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Chat")
			.addEvent(AsyncChatEvent.class)
			.addPattern("[player] chat[ting]")
			.addDescription("""
				Called whenever a player chats.
				Use <a href='#ExprChatFormat'>chat format</a> to change message format.
				Use <a href='#ExprChatRecipients'>chat recipients</a> to edit chat recipients.
				""")
			.addExample("""
				on chat:
					if the player has permission "owner":
						set the chat format to "<red>[player]<light gray>: <light red>[message]"
					else if the player has permission "admin":
						set the chat format to "<light red>[player]<light gray>: <orange>[message]"
					else: # default message format
						set the chat format to "<orange>[player]<light gray>: <white>[message]"
				""")
			.addSince("1.4.1, INSERT VERSION (pattern change)")
			.supplier(() -> new SimpleEvent("player chat"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Pickup Arrow")
			.addEvent(PlayerPickupArrowEvent.class)
			.addPattern("[player] (pick[ing| ]up [an] arrow|arrow pick[ing| ]up)")
			.addDescription("Called when a palyer picks up an arrow from the ground.")
			.addExample("""
				on player picking up an arrow:
				    cancel event
				    teleport event-projectile to (location of event-projectile ~ vector(0, 5, 0))
				""")
			.addSince("2.8.0")
			.supplier(() -> new SimpleEvent("player pickup arrow"))
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerPickupArrowEvent.class, Projectile.class)
			.getter(PlayerPickupArrowEvent::getArrow)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerPickupArrowEvent.class, ItemStack.class)
			.getter(event -> event.getItem().getItemStack())
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Respawn")
			.addEvent(PlayerRespawnEvent.class)
			.addPatterns("[player] respawn[ing]")
			.addDescription("""
				Called when a player respawns via death or entering the end portal in the end.
				You should prefer this event over the <a href='#death'>death event</a> as the player is technically alive when this event is called.")
				""")
			.addExample("""
				on respawn:
					send "Hi there!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("player respawn"))
			.build());

		// Skripts minimum version is  1.21.1 as of 2.15 & as of 2.16 will still only be 1.21.4
		// 1.21.5+ added AbstractRespawnEvent as a base class, where prior to that, getRespawnReason was in PlayerRespawnEvent
		if (Skript.classExists("org.bukkit.event.player.AbstractRespawnEvent")) {
			eventValueRegistry.register(EventValue.builder(PlayerRespawnEvent.class, PlayerRespawnEvent.RespawnReason.class)
				.getter(PlayerRespawnEvent::getRespawnReason)
				.build());
		} else {
			try {
				Method method = PlayerRespawnEvent.class.getMethod("getRespawnReason");
				eventValueRegistry.register(EventValue.builder(PlayerRespawnEvent.class, PlayerRespawnEvent.RespawnReason.class)
					.getter(event -> {
						try {
							return (RespawnReason) method.invoke(event);
						} catch (Exception e) {
							return null;
						}
					})
					.build());
			} catch (NoSuchMethodException ignored) {}
		}
	}

}
