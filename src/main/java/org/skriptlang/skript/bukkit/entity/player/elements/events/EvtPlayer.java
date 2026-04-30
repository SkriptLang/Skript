package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import org.bukkit.event.Event;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.elements.events.EvtEntity;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import io.papermc.paper.event.player.*;

public class EvtPlayer extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Bed Enter")
				.addEvent(PlayerBedEnterEvent.class)
				.addPatterns("bed enter[ing]", "[player] enter[ing] [a] bed")
				.addDescription("Called when a player starts sleeping.")
				.addExample("on bed enter:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Bed Leave")
				.addEvent(PlayerBedLeaveEvent.class)
				.addPatterns("bed leav(e|ing)", "[player] leav(e|ing) [a] bed")
				.addDescription("Called when a player leaves a bed.")
				.addExample("on player leaving a bed:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Bucket Empty")
				.addEvent(PlayerBucketEmptyEvent.class)
				.addPatterns("bucket empty[ing]", "[player] empty[ing] [a] bucket")
				.addDescription("Called when a player empties a bucket. You can also use the place event with a check for water or lava.")
				.addExample("on bucket empty:")
				.addSince("1.0")
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Bucket fill")
				.addEvent(PlayerBucketFillEvent.class)
				.addPatterns("bucket fill[ing]", "[player] fill[ing] [a] bucket")
				.addDescription("Called when a player fills a bucket.")
				.addExample("on player filling a bucket:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Egg Throw")
				.addEvent(PlayerEggThrowEvent.class)
				.addPatterns("throw[ing] [of] [an] egg", "[player] egg throw")
				.addDescription("Called when a player throws an egg and it lands. This event allows modification of properties like the hatched entity type and the number of entities to hatch.")
				.addExample("on throw of an egg:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Item Break")
				.addEvent(PlayerItemBreakEvent.class)
				.addPatterns("[player] tool break[ing]", "[player] break[ing] (a|the|) tool")
				.addDescription("Called when a player breaks their tool because its damage reached the maximum value.", "This event cannot be cancelled.")
				.addExample("on tool break:")
				.addSince("2.1.1")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Item Damage")
				.addEvent(PlayerItemDamageEvent.class)
				.addPatterns("item damag(e|ing)")
				.addDescription("Called when an item is damaged. Most tools are damaged by using them; armor is damaged when the wearer takes damage.")
				.addExample("""
					on item damage:
						cancel event
					""")
				.addSince("2.5")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Tool Change")
				.addEvent(PlayerItemHeldEvent.class)
				.addPatterns("[player['s]] (tool|item held|held item) chang(e|ing)")
				.addDescription("Called whenever a player changes their held item by selecting a different slot (e.g. the keys 1-9 or the mouse wheel).")
				.addExample("on player's held item change:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Join")
				.addEvent(PlayerJoinEvent.class)
				.addPatterns("[player] (login|logging in|join[ing])")
				.addDescription("Called when the player joins the server. The player is already in a world when this event is called.")
				.addExample("""
					on join:
						message "Welcome on our awesome server!"
						broadcast "%player% just joined the server!"
					""")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Connect")
				.addEvent(PlayerLoginEvent.class)
				.addPatterns("[player] connect[ing]")
				.addDescription("Called when the player connects to the server. This event is called before the player actually joins.")
				.addExample("""
					on connect:
						player doesn't have permission "VIP"
						number of players is greater than 15
						kick the player due to "The last 5 slots are reserved for VIP players."
					""")
				.addSince("2.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Kick")
				.addEvent(PlayerKickEvent.class)
				.addPatterns("[player] (kick|being kicked)")
				.addDescription("Called when a player is kicked from the server.")
				.addExample("on kick:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Quit")
				.addEvent(PlayerQuitEvent.class)
				.addPatterns("(quit[ting]|disconnect[ing]|log[ ]out|logging out|leav(e|ing))")
				.addDescription("Called when a player leaves the server.")
				.addExample("""
					on quit:
					on disconnect:
					""")
				.addSince("1.0 (simple disconnection)")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Respawn")
				.addEvent(PlayerRespawnEvent.class)
				.addPatterns("[player] respawn[ing]")
				.addDescription("Called when a player respawns via death or entering the end portal in the end.")
				.addExample("on respawn:")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Sneak Toggle")
				.addEvent(PlayerToggleSneakEvent.class)
				.addPatterns("[player] toggl(e|ing) sneak", "[player] sneak toggl(e|ing)")
				.addDescription("Called when a player starts or stops sneaking.")
				.addExample("""
					on sneak toggle:
						player is sneaking
						push the player upwards at speed 0.5
					""")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Sprint Toggle")
				.addEvent(PlayerToggleSprintEvent.class)
				.addPatterns("[player] toggl(e|ing) sprint", "[player] sprint toggl(e|ing)")
				.addDescription("Called when a player starts or stops sprinting.")
				.addExample("""
					on sprint toggle:
						player is not sprinting
						send "Run!"
					""")
				.addSince("1.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Player World Change")
				.addEvent(PlayerChangedWorldEvent.class)
				.addPatterns("[player] world chang(ing|e[d])")
				.addDescription("Called when a player enters a world. Does not work with other entities!")
				.addExample("""
					on player world change:
						world is "city"
						send "Welcome to the City!"
					""")
				.addSince("2.2-dev28")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Flight Toggle")
				.addEvent(PlayerToggleFlightEvent.class)
				.addPatterns("[player] flight toggl(e|ing)", "[player] toggl(e|ing) flight")
				.addDescription("Called when a players stops/starts flying.")
				.addExample("""
					on flight toggle:
						if {game::%player%::playing} exists:
							cancel event
					""")
				.addSince("2.2-dev36")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Language Change")
				.addEvent(PlayerLocaleChangeEvent.class)
				.addPatterns("[player] (language|locale) chang(e|ing)", "[player] chang(e|ing) (language|locale)")
				.addDescription("Called after a player changed their language in the game settings.")
				.addExample("""
					on language change:
						if player's language starts with "en":
							send "Hello!"
					""")
				.addSince("2.3")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Hand Item Swap")
				.addEvent(PlayerSwapHandItemsEvent.class)
				.addPatterns("swap[ping of] [(hand|held)] item[s]")
				.addDescription("Called whenever a player swaps the items in their main- and offhand slots.")
				.addExample("""
					on swap hand items:
						event-player's tool is a diamond sword
						cancel event
					""")
				.addSince("2.3")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Riptide")
				.addEvent(PlayerRiptideEvent.class)
				.addPatterns("[use of] riptide [enchant[ment]]")
				.addDescription("Called when the player activates the riptide enchantment.")
				.addExample("""
					on riptide:
						send "You are riptiding!"
					""")
				.addSince("2.5")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Arm Swing")
				.addEvent(PlayerAnimationEvent.class)
				.addPatterns("[player] arm swing")
				.addDescription("Called when a player swings their arm.")
				.addExample("""
					on arm swing:
						send "You swung your arm!"
					""")
				.addSince("2.5.1")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Item Mend")
				.addEvent(PlayerItemMendEvent.class)
				.addPatterns("item mend[ing]")
				.addDescription("Called when a player has an item repaired via the Mending enchantment.")
				.addRequiredPlugins("Minecraft 1.13 or newer")
				.addExample("""
					on item mend:
						chance of 50%:
							cancel the event
							send "Oops! Mending failed!" to player
					""")
				.addSince("2.5.1")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Player Pickup Arrow")
				.addEvent(PlayerPickupArrowEvent.class)
				.addPatterns("[player] (pick[ing| ]up [an] arrow|arrow pick[ing| ]up)")
				.addDescription("Called when a player picks up an arrow from the ground.")
				.addExample("""
					on arrow pickup:
						cancel the event
						teleport event-projectile to block 5 above event-projectile
					""")
				.addSince("2.8.0")
				.supplier(EvtPlayer::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Experience Cooldown Change")
				.addEvent(PlayerExpCooldownChangeEvent.class)
				.addPatterns("player (experience|[e]xp) cooldown change")
				.addDescription("Called when a player's experience cooldown changes.")
				.addExample("""
					on player experience cooldown change:
						broadcast xp cooldown change reason
					""")
				.addSince("2.10")
				.supplier(EvtPlayer::new)
				.build()
		);

		if (Skript.classExists("io.papermc.paper.event.player.PlayerTradeEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Player Trade")
					.addEvent(PlayerTradeEvent.class)
					.addPatterns("player trad(e|ing)")
					.addDescription("Called when a player has traded with a villager.")
					.addSince("2.7")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Stop Using Item")
					.addEvent(PlayerStopUsingItemEvent.class)
					.addPatterns("[player] (stop|end) (using item|item use)")
					.addDescription("Called when a player stops using an item.")
					.addSince("2.8.0")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("io.papermc.paper.event.player.PlayerDeepSleepEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Player Deep Sleep")
					.addEvent(PlayerDeepSleepEvent.class)
					.addPatterns("[player] deep sleep[ing]")
					.addDescription("Called when a player has slept long enough to count as passing the night/storm.")
					.addSince("2.7")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Inventory Slot Change")
					.addEvent(PlayerInventorySlotChangeEvent.class)
					.addPatterns("[player] inventory slot chang(e|ing)")
					.addDescription("Called when a slot in a player's inventory is changed.")
					.addSince("2.7")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Beacon Change Effect")
					.addEvent(PlayerChangeBeaconEffectEvent.class)
					.addPatterns("beacon change effect", "beacon effect change", "player chang(e[s]|ing) [of] beacon effect")
					.addDescription("Called when a player changes the effects of a beacon.")
					.addSince("2.10")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerJumpEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Jump")
					.addEvent(PlayerJumpEvent.class)
					.addPatterns("[player] jump[ing]")
					.addDescription("Called whenever a player jumps.",
						"This event requires PaperSpigot.")
					.addExample("""
					on jump:
						event-player does not have permission "jump"
						cancel event
					""")
					.addSince("2.3")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerReadyArrowEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Ready Arrow")
					.addEvent(PlayerReadyArrowEvent.class)
					.addPatterns("[player] ((ready|choose|draw|load) arrow|arrow (choose|draw|load))")
					.addDescription("Called when a player is firing a bow and the server is choosing an arrow to use.",
						"Cancelling this event will skip the current arrow item and fire a new event for the next arrow item.",
						"The arrow and bow in the event can be accessed with the Readied Arrow/Bow expression.")
					.addExample("""
					on player ready arrow:
						selected bow's name is "Spectral Bow"
						if selected arrow is not a spectral arrow:
							cancel event
					""")
					.addSince("2.8.0")
					.supplier(EvtPlayer::new)
					.build()
			);
		}

		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtPlayer.class, "Elytra Boost")
					.addEvent(PlayerElytraBoostEvent.class)
					.addPatterns("elytra boost")
					.addDescription("Called when a player uses a firework to boost their fly speed when flying with an elytra.",
						"Cancelling this event will prevent the firework from being consumed.")
					.addExample("""
						on elytra boost:
							if the used firework will be consumed:
								prevent the used firework from being consume
						""")
					.addSince("2.10")
					.supplier(EvtPlayer::new)
					.build()
			);
		}
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player event";
	}
}
