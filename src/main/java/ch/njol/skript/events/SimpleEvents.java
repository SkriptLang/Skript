/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import ch.njol.skript.lang.util.SimpleEvent;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.*;

/**
 * @author Peter Güttinger
 */
public class SimpleEvents {
	static {
		Skript.registerEvent("Can Build Check", SimpleEvent.class, BlockCanBuildEvent.class, "[block] can build check")
				.description("Called when a player rightclicks on a block while holding a block or a placeable item. You can either cancel the event to prevent the block from being built, or uncancel it to allow it.",
						"Please note that the <a href='expressions.html#ExprDurability'>data value</a> of the block to be placed is not available in this event, only its <a href='expressions.html#ExprIdOf'>ID</a>.")
				.examples("on block can build check:",
						"\tcancel event")
				.since("1.0 (basic), 2.0 ([un]cancellable)");
		Skript.registerEvent("Block Damage", SimpleEvent.class, BlockDamageEvent.class, "block damag(ing|e)")
				.description("Called when a player starts to break a block. You can usually just use the leftclick event for this.")
				.examples("on block damaging:",
						"\tif block is log:",
						"\t\tsend \"You can't break the holy log!\"")
				.since("1.0");
		Skript.registerEvent("Flow", SimpleEvent.class, BlockFromToEvent.class, "[block] flow[ing]", "block mov(e|ing)")
				.description("Called when a blocks flows or teleports to another block. This not only applies to water and lava, but teleporting dragon eggs as well.")
				.examples("on block flow:",
						"\tif event-block is water:",
						"\t\tbroadcast \"Build more dams! It's starting to get wet in here\"")
				.since("1.0");
		Skript.registerEvent("Ignition", SimpleEvent.class, BlockIgniteEvent.class, "[block] ignit(e|ion)")
				.description("Called when a block starts burning, i.e. a fire block is placed next to it and this block is flammable.",
						"The <a href='#burn'>burn event</a> will be called when the block is about do be destroyed by the fire.")
				.examples("on block ignite:",
						"\tif event-block is a ladder:",
						"\t\tcancel event")
				.since("1.0");
		Skript.registerEvent("Physics", SimpleEvent.class, BlockPhysicsEvent.class, "[block] physics")
				.description("Called when a physics check is done on a block. By cancelling this event you can prevent some things from happening, " +
						"e.g. sand falling, dirt turning into grass, torches dropping if their supporting block is destroyed, etc." +
						"Please note that using this event might cause quite some lag since it gets called extremely often.")
				.examples("# prevents sand from falling",
						"on block physics:",
						"	block is sand",
						"	cancel event")
				.since("1.4.6");
		Skript.registerEvent("Piston Extend", SimpleEvent.class, BlockPistonExtendEvent.class, "piston extend[ing]")
				.description("Called when a piston is about to extend.")
				.examples("on piston extend:",
						"\tbroadcast \"A piston is extending!\"")
				.since("1.0");
		Skript.registerEvent("Piston Retract", SimpleEvent.class, BlockPistonRetractEvent.class, "piston retract[ing]")
				.description("Called when a piston is about to retract.")
				.examples("on piston retract:",
						"\tbroadcast \"A piston is retracting!\"")
				.since("1.0");
		Skript.registerEvent("Redstone", SimpleEvent.class, BlockRedstoneEvent.class, "redstone [current] [chang(e|ing)]")
				.description("Called when the redstone current of a block changes. This event is of not much use yet.")
				.examples("on redstone change:",
						"\tsend \"someone is using redstone\" to console")
				.since("1.0");
		Skript.registerEvent("Spread", SimpleEvent.class, BlockSpreadEvent.class, "spread[ing]")
				.description("Called when a new block <a href='#form'>forms</a> as a result of a block that can spread, e.g. water or mushrooms.")
				.examples("on spread:")
				.since("1.0");
		Skript.registerEvent("Chunk Load", SimpleEvent.class, ChunkLoadEvent.class, "chunk load[ing]")
				.description("Called when a chunk loads. The chunk might or might not contain mobs when it's loaded.")
				.examples("on chunk load:")
				.since("1.0");
		Skript.registerEvent("Chunk Generate", SimpleEvent.class, ChunkPopulateEvent.class, "chunk (generat|populat)(e|ing)")
				.description("Called after a new chunk was generated.")
				.examples("on chunk generate:")
				.since("1.0");
		Skript.registerEvent("Chunk Unload", SimpleEvent.class, ChunkUnloadEvent.class, "chunk unload[ing]")
				.description("Called when a chunk is unloaded due to not being near any player.")
				.examples("on chunk unload:")
				.since("1.0");
		Skript.registerEvent("Creeper Power", SimpleEvent.class, CreeperPowerEvent.class, "creeper power")
				.description("Called when a creeper is struck by lighting and gets powered. Cancel the event to prevent the creeper from being powered.")
				.examples("on creeper power:")
				.since("1.0");
		Skript.registerEvent("Zombie Break Door", SimpleEvent.class, EntityBreakDoorEvent.class, "zombie break[ing] [a] [wood[en]] door")
				.description("Called when a zombie is done breaking a wooden door. Can be cancelled to prevent the zombie from breaking the door.")
				.examples("on zombie breaking a wood door:")
				.since("1.0");
		Skript.registerEvent("Combust", SimpleEvent.class, EntityCombustEvent.class, "combust[ing]")
				.description("Called when an entity is set on fire, e.g. by fire or lava, a fireball, or by standing in direct sunlight (zombies, skeletons).")
				.examples("on combust:")
				.since("1.0");
		Skript.registerEvent("Explode", SimpleEvent.class, EntityExplodeEvent.class, "explo(d(e|ing)|sion)")
				.description("Called when an entity (a primed TNT or a creeper) explodes.")
				.examples("on explosion:")
				.since("1.0");
//		Skript.registerEvent(SimpleEvent.class, EntityInteractEvent.class, "interact");// = entity interacts with block, e.g. endermen?; player -> PlayerInteractEvent // likely tripwires, pressure plates, etc.
		Skript.registerEvent("Portal Enter", SimpleEvent.class, EntityPortalEnterEvent.class, "portal enter[ing]", "entering [a] portal")
				.description("Called when an entity enters a nether portal or an end portal. Please note that this event will be fired many times for a nether portal.")
				.examples("on portal enter:")
				.since("1.0");
		Skript.registerEvent("Tame", SimpleEvent.class, EntityTameEvent.class, "[entity] tam(e|ing)")
				.description("Called when a player tames a wolf or ocelot. Can be cancelled to prevent the entity from being tamed.")
				.examples("on tame:")
				.since("1.0");
		Skript.registerEvent("Explosion Prime", SimpleEvent.class, ExplosionPrimeEvent.class, "explosion prime")
				.description("Called when an explosive is primed, i.e. an entity will explode shortly. Creepers can abort the explosion if the player gets too far away, " +
						"while TNT will explode for sure after a short time.")
				.examples("on explosion prime:")
				.since("1.0");
		Skript.registerEvent("Hunger Meter Change", SimpleEvent.class, FoodLevelChangeEvent.class, "(food|hunger) (level|met(er|re)|bar) chang(e|ing)")
				.description("Called when the hunger bar of a player changes, i.e. either increases by eating or decreases over time.")
				.examples("on food bar change:")
				.since("1.4.4");
		Skript.registerEvent("Fuel Burn", SimpleEvent.class, FurnaceBurnEvent.class, "fuel burn[ing]")
				.description("Called when a furnace burns an item from its <a href='expressions.html#ExprFurnaceSlot'>fuel slot</a>.")
				.examples("on fuel burning:")
				.since("1.0");
		Skript.registerEvent("Smelt", SimpleEvent.class, FurnaceSmeltEvent.class, "[ore] smelt[ing]", "smelt[ing] of ore") //TODO SkriptEvent for "smelt[ing] of %itemtype%"
		.description("Called when a furnace smelts an item in its <a href='expressions.html#ExprFurnaceSlot'>ore slot</a>.")
				.examples("on smelt:")
				.since("1.0");
		Skript.registerEvent("Leaves Decay", SimpleEvent.class, LeavesDecayEvent.class, "leaves decay[ing]")
				.description("Called when a leaf block decays due to not being connected to a tree.")
				.examples("on leaves decay:")
				.since("1.0");
		Skript.registerEvent("Lightning Strike", SimpleEvent.class, LightningStrikeEvent.class, "lightning [strike]")
				.description("Called when lightning strikes.")
				.examples("on lightning:", "\tspawn a zombie at location of event-entity")
				.since("1.0");
		Skript.registerEvent("Pig Zap", SimpleEvent.class, PigZapEvent.class, "pig[ ]zap")
				.description("Called when a pig is stroke by lightning and transformed into a zombie pigman. Cancel the event to prevent the transformation.")
				.examples("on pig zap:")
				.since("1.0");
		Skript.registerEvent("Bed Enter", SimpleEvent.class, PlayerBedEnterEvent.class, "bed enter[ing]", "[player] enter[ing] [a] bed")
				.description("Called when a player starts sleeping.")
				.examples("on bed enter:")
				.since("1.0");
		Skript.registerEvent("Bed Leave", SimpleEvent.class, PlayerBedLeaveEvent.class, "bed leav(e|ing)", "[player] leav(e|ing) [a] bed")
				.description("Called when a player leaves a bed.")
				.examples("on player leaving a bed:")
				.since("1.0");
		Skript.registerEvent("Bucket Empty", SimpleEvent.class, PlayerBucketEmptyEvent.class, "bucket empty[ing]", "[player] empty[ing] [a] bucket")//TODO , "emptying bucket [of %itemtype%]", "emptying %itemtype% bucket") -> place of water/lava)
		.description("Called when a player empties a bucket. You can also use the <a href='#place'>place event</a> with a check for water or lava.")
				.examples("on bucket empty:")
				.since("1.0");
		Skript.registerEvent("Bucket fill", SimpleEvent.class, PlayerBucketFillEvent.class, "bucket fill[ing]", "[player] fill[ing] [a] bucket")//TODO , "filling bucket [(with|of) %itemtype%]", "filling %itemtype% bucket");)
		.description("Called when a player fills a bucket.")
				.examples("on player filling a bucket:")
				.since("1.0");
		Skript.registerEvent("Egg Throw", SimpleEvent.class, PlayerEggThrowEvent.class, "throw[ing] [of] [an] egg", "[player] egg throw")
				.description(
						"Called when a player throws an egg and it lands. You can just use the <a href='#shoot'>shoot event</a> in most cases." +
						" However, this event allows modification of properties like the hatched entity type and the number of entities to hatch."
				)
				.examples("on throw of an egg:")
				.since("1.0");
		// TODO improve - on fish [of %entitydata%] (and/or itemtype), on reel, etc.
		// Maybe something like RandomSK "[on] fishing state of %fishingstate%"
		Skript.registerEvent("Fishing", SimpleEvent.class, PlayerFishEvent.class, "[player] fish[ing]")
				.description("Called when a player fishes something. This is not of much use yet.")
				.examples("on fish:")
				.since("1.0");
		if (Skript.classExists("org.bukkit.event.player.PlayerItemBreakEvent")) {
			Skript.registerEvent("Item Break", SimpleEvent.class, PlayerItemBreakEvent.class, "[player] tool break[ing]", "[player] break[ing] (a|the|) tool")
					.description("Called when a player breaks their tool because its damage reached the maximum value.",
							"This event cannot be cancelled.")
					.examples("on tool break:")
					.since("2.1.1");
		}
		Skript.registerEvent("Item Damage", SimpleEvent.class, PlayerItemDamageEvent.class, "item damag(e|ing)")
				.description("Called when an item is damaged. Most tools are damaged by using them; armor is damaged when the wearer takes damage.")
				.examples("on item damage:",
						"\tcancel event")
				.since("2.5");
		Skript.registerEvent("Tool Change", SimpleEvent.class, PlayerItemHeldEvent.class, "[player['s]] (tool|item held|held item) chang(e|ing)")
				.description("Called whenever a player changes their held item by selecting a different slot (e.g. the keys 1-9 or the mouse wheel), <i>not</i> by dropping or replacing the item in the current slot.")
				.examples("on player's held item change:")
				.since("1.0");
		Skript.registerEvent("Join", SimpleEvent.class, PlayerJoinEvent.class, "[player] (login|logging in|join[ing])")
				.description("Called when the player joins the server. The player is already in a world when this event is called, so if you want to prevent players from joining you should prefer <a href='#connect'>on connect</a> over this event.")
				.examples("on join:",
						"	message \"Welcome on our awesome server!\"",
						"	broadcast \"%player% just joined the server!\"")
				.since("1.0");
		Skript.registerEvent("Connect", SimpleEvent.class, PlayerLoginEvent.class, "[player] connect[ing]")
				.description("Called when the player connects to the server. This event is called before the player actually joins the server, so if you want to prevent players from joining you should prefer this event over <a href='#join'>on join</a>.")
				.examples("on connect:",
						"	player doesn't have permission \"VIP\"",
						"	number of players is greater than 15",
						"	kick the player due to \"The last 5 slots are reserved for VIP players.\"")
				.since("2.0");
		Skript.registerEvent("Kick", SimpleEvent.class, PlayerKickEvent.class, "[player] (kick|being kicked)")
				.description("Called when a player is kicked from the server. You can change the <a href='expressions.html#ExprMessage'>kick message</a> or <a href='effects.html#EffCancelEvent'>cancel the event</a> entirely.")
				.examples("on kick:")
				.since("1.0");
		Skript.registerEvent("Entity Portal", SimpleEvent.class, EntityPortalEvent.class, "entity portal")
				.description("Called when an entity uses a nether or end portal. <a href='effects.html#EffCancelEvent'>Cancel the event</a> to prevent the entity from teleporting.")
				.examples("on entity portal:", "\tbroadcast \"A %type of event-entity% has entered a portal!")
				.since("2.5.3");
		Skript.registerEvent("Portal", SimpleEvent.class, PlayerPortalEvent.class, "[player] portal")
				.description("Called when a player uses a nether or end portal. <a href='effects.html#EffCancelEvent'>Cancel the event</a> to prevent the player from teleporting.")
				.examples("on player portal:")
				.since("1.0");
		Skript.registerEvent("Quit", SimpleEvent.class, PlayerQuitEvent.class, "(quit[ting]|disconnect[ing]|log[ ]out|logging out|leav(e|ing))")
				.description("Called when a player leaves the server.")
				.examples("on quit:",
						"on disconnect:")
				.since("1.0 (simple disconnection)");
		Skript.registerEvent("Respawn", SimpleEvent.class, PlayerRespawnEvent.class, "[player] respawn[ing]")
				.description("Called when a player respawns. You should prefer this event over the <a href='#death'>death event</a> as the player is technically alive when this event is called.")
				.examples("on respawn:")
				.since("1.0");
		Skript.registerEvent("Sneak Toggle", SimpleEvent.class, PlayerToggleSneakEvent.class, "[player] toggl(e|ing) sneak", "[player] sneak toggl(e|ing)")
				.description("Called when a player starts or stops sneaking. Use <a href='conditions.html#CondIsSneaking'>is sneaking</a> to get whether the player was sneaking before the event was called.")
				.examples("# make players that stop sneaking jump",
						"on sneak toggle:",
						"	player is sneaking",
						"	push the player upwards at speed 0.5")
				.since("1.0");
		Skript.registerEvent("Sprint Toggle", SimpleEvent.class, PlayerToggleSprintEvent.class, "[player] toggl(e|ing) sprint", "[player] sprint toggl(e|ing)")
				.description("Called when a player starts or stops sprinting. Use <a href='conditions.html#CondIsSprinting'>is sprinting</a> to get whether the player was sprinting before the event was called.")
				.examples("on sprint toggle:",
						"	player is not sprinting",
						"	send \"Run!\"")
				.since("1.0");
		Skript.registerEvent("Portal Create", SimpleEvent.class, PortalCreateEvent.class, "portal creat(e|ion)")
				.description("Called when a portal is created, either by a player or mob lighting an obsidian frame on fire, or by a nether portal creating its teleportation target in the nether/overworld.",
						"In Minecraft 1.14+, you can use <a href='expressions.html#ExprEntity'>the player</a> in this event.", "Please note that there may not always be a player (or other entity) in this event.")
				.examples("on portal create:")
				.requiredPlugins("Minecraft 1.14+ (event-entity support)")
				.since("1.0, 2.5.3 (event-entity support)");
		Skript.registerEvent("Projectile Hit", SimpleEvent.class, ProjectileHitEvent.class, "projectile hit")
				.description("Called when a projectile hits an entity or a block.")
				.examples("on projectile hit:",
						"\tif victim's health <= 3:",
						"\t\tdelete event-projectile")
				.since("1.0");
		
		if(Skript.classExists("com.destroystokyo.paper.event.entity.ProjectileCollideEvent"))
			Skript.registerEvent("Projectile Collide", SimpleEvent.class, ProjectileCollideEvent.class, "projectile collide")
			.description("Called when a projectile collides with an entity.")
			.requiredPlugins("Paper")
			.examples("on projectile collide:",
				"\tteleport shooter of event-projectile to event-entity")
			.since("2.5");
		Skript.registerEvent("Shoot", SimpleEvent.class, ProjectileLaunchEvent.class, "[projectile] shoot")
				.description("Called whenever a <a href='classes.html#projectile'>projectile</a> is shot. Use the <a href='expressions.html#ExprShooter'>shooter expression</a> to get who shot the projectile.")
				.examples("on shoot:",
						"\tif projectile is an arrow:",
						"\t\tsend \"you shot an arrow!\" to shooter")
				.since("1.0");
		Skript.registerEvent("Sign Change", SimpleEvent.class, SignChangeEvent.class, "sign (chang[e]|edit)[ing]", "[player] (chang[e]|edit)[ing] [a] sign")
				.description("As signs are placed empty, this event is called when a player is done editing a sign.")
				.examples("on sign change:",
						"	line 2 is empty",
						"	set line 1 to \"&lt;red&gt;%line 1%\"")
				.since("1.0");
		Skript.registerEvent("Spawn Change", SimpleEvent.class, SpawnChangeEvent.class, "[world] spawn change")
				.description("Called when the spawn point of a world changes.")
				.examples("on spawn change:",
						"\tbroadcast \"someone changed the spawn!\"")
				.since("1.0");
		Skript.registerEvent("Vehicle Create", SimpleEvent.class, VehicleCreateEvent.class, "vehicle create", "creat(e|ing|ion of) [a] vehicle")
				.description("Called when a new vehicle is created, e.g. when a player places a boat or minecart.")
				.examples("on vehicle create:")
				.since("1.0");
		Skript.registerEvent("Vehicle Damage", SimpleEvent.class, VehicleDamageEvent.class, "vehicle damage", "damag(e|ing) [a] vehicle")
				.description("Called when a vehicle gets damage. Too much damage will <a href='#vehicle_destroy'>destroy</a> the vehicle.")
				.examples("on vehicle damage:")
				.since("1.0");
		Skript.registerEvent("Vehicle Destroy", SimpleEvent.class, VehicleDestroyEvent.class, "vehicle destroy", "destr(oy[ing]|uction of) [a] vehicle")
				.description("Called when a vehicle is destroyed. Any <a href='expressions.html#ExprPassenger'>passenger</a> will be ejected and the vehicle might drop some item(s).")
				.examples("on vehicle destroy:",
						"\tcancel event")
				.since("1.0");
		Skript.registerEvent("Vehicle Enter", SimpleEvent.class, VehicleEnterEvent.class, "vehicle enter", "enter[ing] [a] vehicle")
				.description("Called when an <a href='classes.html#entity'>entity</a> enters a vehicle, either deliberately (players) or by falling into them (mobs).")
				.examples("on vehicle enter:",
						"\tentity is a player",
						"\tcancel event")
				.since("1.0");
		Skript.registerEvent("Vehicle Exit", SimpleEvent.class, VehicleExitEvent.class, "vehicle exit", "exit[ing] [a] vehicle")
				.description("Called when an entity exits a vehicle.")
				.examples("on vehicle exit:",
						"\tif event-entity is a spider:",
						"\t\tkill event-entity")
				.since("1.0");
		if (Skript.classExists("org.bukkit.event.entity.EntityMountEvent") || Skript.classExists("org.spigotmc.event.entity.EntityMountEvent")) {
			Class<? extends Event> mountEventClass = null;
			Class<? extends Event> dismountEventClass = null;
			if (Skript.classExists("org.bukkit.event.entity.EntityMountEvent")) {
				mountEventClass = EntityMountEvent.class;
				dismountEventClass = EntityDismountEvent.class;
			} else {
				try {
					mountEventClass = (Class<? extends Event>) Class.forName("org.spigotmc.event.entity.EntityMountEvent");
					dismountEventClass = (Class<? extends Event>) Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
				} catch (ClassNotFoundException e) {
					Skript.exception(e, "Failed to load legacy mount/dismount event classes. These events may not work.");
				}
			}
			if (mountEventClass != null) {
				Skript.registerEvent("Entity Mount", SimpleEvent.class, mountEventClass, "mount[ing]")
					.description("Called when entity starts riding another.")
					.examples("on mount:",
							"\tcancel event")
					.since("2.2-dev13b");
			}
			if (dismountEventClass != null) {
				Skript.registerEvent("Entity Dismount", SimpleEvent.class, dismountEventClass, "dismount[ing]")
					.description("Called when an entity dismounts.")
					.examples("on dismount:",
							"\tkill event-entity")
					.since("2.2-dev13b");
			}
		}
		if (Skript.classExists("org.bukkit.event.entity.EntityToggleGlideEvent")) {
			Skript.registerEvent("Gliding State Change", SimpleEvent.class, EntityToggleGlideEvent.class, "(gliding state change|toggl(e|ing) gliding)")
					.description("Called when an entity toggles glider on or off, or when server toggles gliding state of an entity forcibly.")
					.examples("on toggling gliding:",
							"	cancel the event # bad idea, but you CAN do it!")
					.since("2.2-dev21");
			Skript.registerEvent("AoE Cloud Effect", SimpleEvent.class, AreaEffectCloudApplyEvent.class, "(area|AoE) [cloud] effect")
					.description("Called when area effect cloud applies its potion effect. This happens every 5 ticks by default.")
					.examples("on area cloud effect:")
					.since("2.2-dev21");
		}
		Skript.registerEvent("Sheep Regrow Wool", SimpleEvent.class, SheepRegrowWoolEvent.class, "sheep [re]grow[ing] wool")
				.description("Called when sheep regrows its sheared wool back.")
				.examples("on sheep grow wool:",
						"\tcancel event")
				.since("2.2-dev21");
		Skript.registerEvent("Inventory Open", SimpleEvent.class, InventoryOpenEvent.class, "inventory open[ed]")
				.description("Called when an inventory is opened for player.")
				.examples("on inventory open:",
						"\tclose player's inventory")
				.since("2.2-dev21");
		Skript.registerEvent("Inventory Close", SimpleEvent.class, InventoryCloseEvent.class, "inventory clos(ing|e[d])")
				.description("Called when player's currently viewed inventory is closed.")
				.examples("on inventory close:",
						"\tif player's location is {location}:",
						"\t\tsend \"You exited the shop!\"")
				.since("2.2-dev21");
		Skript.registerEvent("Slime Split", SimpleEvent.class, SlimeSplitEvent.class, "slime split[ting]")
				.description("Called when a slime splits. Usually this happens when a big slime dies.")
				.examples("on slime split:")
				.since("2.2-dev26");
		Skript.registerEvent("Resurrect Attempt", SimpleEvent.class, EntityResurrectEvent.class, "[entity] resurrect[ion] [attempt]")
				.description("Called when an entity dies, always. If they are not holding a totem, this is cancelled - you can, however, uncancel it.")
				.examples(
					"on resurrect attempt:",
						"\tentity is player",
						"\tentity has permission \"admin.undying\"",
						"\tuncancel the event"
				)
				.since("2.2-dev28")
				.listeningBehavior(ListeningBehavior.ANY);
		Skript.registerEvent("Player World Change", SimpleEvent.class, PlayerChangedWorldEvent.class, "[player] world chang(ing|e[d])")
				.description("Called when a player enters a world. Does not work with other entities!")
				.examples("on player world change:",
						"	world is \"city\"",
					 	"	send \"Welcome to the City!\"")
				.since("2.2-dev28");
		Skript.registerEvent("Flight Toggle", SimpleEvent.class, PlayerToggleFlightEvent.class, "[player] flight toggl(e|ing)", "[player] toggl(e|ing) flight")
				.description("Called when a players stops/starts flying.")
				.examples("on flight toggle:",
						"	if {game::%player%::playing} exists:",
						"		cancel event")
				.since("2.2-dev36");
		if (Skript.classExists("org.bukkit.event.player.PlayerLocaleChangeEvent")) {
			Skript.registerEvent("Language Change", SimpleEvent.class, PlayerLocaleChangeEvent.class, "[player] (language|locale) chang(e|ing)", "[player] chang(e|ing) (language|locale)")
					.description("Called after a player changed their language in the game settings. You can use the <a href='expressions.html#ExprLanguage'>language</a> expression to get the current language of the player.",
							"This event requires Minecraft 1.12+.")
					.examples("on language change:",
							"	if player's language starts with \"en\":",
							"		send \"Hello!\"")
					.since("2.3");
		}
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerJumpEvent")) {
			Skript.registerEvent("Jump", SimpleEvent.class, PlayerJumpEvent.class, "[player] jump[ing]")
					.description("Called whenever a player jumps.",
							"This event requires PaperSpigot.")
					.examples("on jump:",
							"	event-player does not have permission \"jump\"",
							"	cancel event")
					.since("2.3");
		}
		if (Skript.classExists("org.bukkit.event.player.PlayerSwapHandItemsEvent")) {
			Skript.registerEvent("Hand Item Swap", SimpleEvent.class, PlayerSwapHandItemsEvent.class, "swap[ping of] [(hand|held)] item[s]")
					.description("Called whenever a player swaps the items in their main- and offhand slots.",
						     "Works also when one or both of the slots are empty.",
						     "The event is called before the items are actually swapped,",
						     "so when you use the player's tool or player's offtool expressions,",
						     "they will return the values before the swap -",
						     "this enables you to cancel the event before anything happens.")
					.examples("on swap hand items:",
							"	event-player's tool is a diamond sword",
							"	cancel event")
					.since("2.3");
		}
		Skript.registerEvent("Server List Ping", SimpleEvent.class, (Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent") ? PaperServerListPingEvent.class : ServerListPingEvent.class), "server [list] ping")
				.description("Called when a server list ping is coming in, generally when a Minecraft client pings the server to show its information in the server list.",
						"The <a href='expressions.html#ExprIP'>IP</a> expression can be used to get the IP adress of the pinger.",
						"This event can be cancelled on PaperSpigot 1.12.2+ only and this means the player will see the server as offline (but still can join).",
						"",
						"Also you can use <a href='expressions.html#ExprMOTD'>MOTD</a>, <a href='expressions.html#ExprMaxPlayers'>Max Players</a>, " +
						"<a href='expressions.html#ExprOnlinePlayersCount'>Online Players Count</a>, <a href='expressions.html#ExprProtocolVersion'>Protocol Version</a>, " +
						"<a href='expressions.html#ExprVersionString'>Version String</a>, <a href='expressions.html#ExprHoverList'>Hover List</a> and <a href='expressions.html#ExprServerIcon'>Server Icon</a> " +
						"expressions, and <a href='effects.html#EffPlayerInfoVisibility'>Player Info Visibility</a> and <a href='effects.html#EffHidePlayerFromServerList'>Hide Player from Server List</a> effects to modify the server list.")
				.examples("on server list ping:",
						"	set the motd to \"Welcome %{player-by-IP::%ip%}%! Join now!\" if {player-by-IP::%ip%} is set, else \"Join now!\"",
						"	set the fake max players count to (online players count + 1)",
						"	set the shown icon to a random server icon out of {server-icons::*}")
				.since("2.3");
		if (Skript.classExists("org.bukkit.event.entity.EntityToggleSwimEvent")) {
			Skript.registerEvent("Swim Toggle", SimpleEvent.class, EntityToggleSwimEvent.class, "[entity] toggl(e|ing) swim",
					"[entity] swim toggl(e|ing)")
					.description("Called when an entity swims or stops swimming.")
					.requiredPlugins("1.13 or newer")
					.examples("on swim toggle:",
							"	event-entity does not have permission \"swim\"",
							"	cancel event")
					.since("2.3");
		}
		if (Skript.classExists("org.bukkit.event.player.PlayerRiptideEvent")) {
			Skript.registerEvent("Riptide", SimpleEvent.class, PlayerRiptideEvent.class, "[use of] riptide [enchant[ment]]")
				.description("Called when the player activates the riptide enchantment, using their trident to propel them through the air.",
					"Note: the riptide action is performed client side, so manipulating the player in this event may have undesired effects.")
				.examples("on riptide:",
					"	send \"You are riptiding!\"")
				.since("2.5");
		}
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent")) {
			Skript.registerEvent("Armor Change", SimpleEvent.class, PlayerArmorChangeEvent.class, "[player] armo[u]r change[d]")
				.description("Called when armor pieces of a player are changed.")
				.requiredPlugins("Paper")
				.keywords("armour")
				.examples(
						"on armor change:",
							"\tsend \"You equipped %event-item%!\""
				).since("2.5");
		}
		if (Skript.classExists("org.bukkit.event.block.SpongeAbsorbEvent")) {
			Skript.registerEvent("Sponge Absorb", SimpleEvent.class, SpongeAbsorbEvent.class, "sponge absorb")
					.description("Called when a sponge absorbs blocks.")
					.requiredPlugins("Minecraft 1.13 or newer")
					.examples("on sponge absorb:",
							"\tloop absorbed blocks:",
							"\t\tbroadcast \"%loop-block% was absorbed by a sponge\"!")
					.since("2.5");
		}
		Skript.registerEvent("Enchant Prepare", SimpleEvent.class, PrepareItemEnchantEvent.class, "[item] enchant prepare")
			.description("Called when a player puts an item into enchantment table. This event may be called multiple times.",
				" To get the enchant item, see the <a href='expressions.html#ExprEnchantEventsEnchantItem'>enchant item expression</a>")
			.examples("on enchant prepare:",
				"\tset enchant offer 1 to sharpness 1",
				"\tset the cost of enchant offer 1 to 10 levels")
			.since("2.5");
		Skript.registerEvent("Enchant", SimpleEvent.class, EnchantItemEvent.class, "[item] enchant")
		.description("Called when a player successfully enchants an item.",
			" To get the enchanted item, see the <a href='expressions.html#ExprEnchantEventsEnchantItem'>enchant item expression</a>")
		.examples("on enchant:",
			"\tif the clicked button is 1: # offer 1",
			"\t\tset the applied enchantments to sharpness 10 and unbreaking 10")
		.since("2.5");
		Skript.registerEvent("Inventory Pickup", SimpleEvent.class, InventoryPickupItemEvent.class, "inventory pick[ ]up")
				.description("Called when an inventory (a hopper, a hopper minecart, etc.) picks up an item")
				.examples("on inventory pickup:")
				.since("2.5.1");
		Skript.registerEvent("Horse Jump", SimpleEvent.class, HorseJumpEvent.class, "horse jump")
			.description("Called when a horse jumps.")
			.examples("on horse jump:", "\tpush event-entity upwards at speed 2")
			.since("2.5.1");
		if(Skript.classExists("org.bukkit.event.block.BlockFertilizeEvent"))
			Skript.registerEvent("Block Fertilize", SimpleEvent.class, BlockFertilizeEvent.class, "[block] fertilize")
			.description("Called when a player fertilizes blocks.")
			.requiredPlugins("Minecraft 1.13 or newer")
			.examples("on block fertilize:",
				"\tsend \"Fertilized %size of fertilized blocks% blocks got fertilized.\"")
			.since("2.5");
		Skript.registerEvent("Arm Swing", SimpleEvent.class, PlayerAnimationEvent.class, "[player] arm swing")
			.description("Called when a player swings their arm.")
			.examples("on arm swing:",
				"\tsend \"You swung your arm!\"")
			.since("2.5.1");
		if (Skript.classExists("org.bukkit.event.player.PlayerItemMendEvent")) {
			Skript.registerEvent("Item Mend", SimpleEvent.class, PlayerItemMendEvent.class, "item mend[ing]")
				.description("Called when a player has an item repaired via the Mending enchantment.")
				.requiredPlugins("Minecraft 1.13 or newer")
				.examples("on item mend:",
					"\tchance of 50%:",
					"\t\tcancel the event",
					"\t\tsend \"Oops! Mending failed!\" to player")
				.since("2.5.1");
		}
		Skript.registerEvent("Anvil Prepare", SimpleEvent.class, PrepareAnvilEvent.class, "anvil prepar(e|ing)")
			.description("Called when an item is put in a slot for repair by an anvil. Please note that this event is called multiple times in a single item slot move.")
			.examples("on anvil prepare:",
				"\tevent-item is set # result item",
				"\tchance of 5%:",
				"\t\tset repair cost to repair cost * 50%",
				"\t\tsend \"You're LUCKY! You got 50% discount.\" to player")
			.since("2.7");
		if (Skript.classExists("io.papermc.paper.event.player.PlayerTradeEvent")) {
			Skript.registerEvent("Player Trade", SimpleEvent.class, PlayerTradeEvent.class, "player trad(e|ing)")
				.description("Called when a player has traded with a villager.")
				.requiredPlugins("Paper 1.16.5+")
				.examples("on player trade:",
					"\tchance of 50%:",
					"\t\tcancel event",
					"\t\tsend \"The trade was somehow denied!\" to player")
				.since("2.7");
		}
		if (Skript.classExists("com.destroystokyo.paper.event.entity.EntityJumpEvent")) {
			Skript.registerEvent("Entity Jump", SimpleEvent.class, EntityJumpEvent.class, "entity jump[ing]")
				.description("Called when an entity jumps.")
				.requiredPlugins("Paper 1.15.2+")
				.examples("on entity jump:",
					"\tif entity is a wither skeleton:",
					"\t\tcancel event")
				.since("2.7");
		}
		if (Skript.classExists("com.destroystokyo.paper.event.block.AnvilDamagedEvent")) {
			Skript.registerEvent("Anvil Damage", SimpleEvent.class, AnvilDamagedEvent.class, "anvil damag(e|ing)")
				.description("Called when an anvil is damaged/broken from being used to repair/rename items.",
					 		 "Note: this does not include anvil damage from falling.")
				.requiredPlugins("Paper")
				.examples("on anvil damage:",
					"\tcancel the event")
				.since("2.7");
		}

		if (Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent")) {
			Skript.registerEvent("Stop Using Item", SimpleEvent.class, PlayerStopUsingItemEvent.class,
							"[player] (stop|end) (using item|item use)")
					.description("Called when a player stops using an item. For example, when the player releases the " +
							"interact button when holding a bow, an edible item, or a spyglass.",
							"Note that event-timespan will return the time the item was used for.")
					.requiredPlugins("Paper 1.18.2+")
					.examples(
						"on player stop using item:",
							"\tbroadcast \"%player% used %event-item% for %event-timespan%.\"")
					.since("2.8.0");
		}

		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerReadyArrowEvent")) {
			Skript.registerEvent("Ready Arrow", SimpleEvent.class, PlayerReadyArrowEvent.class, "[player] ((ready|choose|draw|load) arrow|arrow (choose|draw|load))")
					.description("Called when a player is firing a bow and the server is choosing an arrow to use.",
							"Cancelling this event will skip the current arrow item and fire a new event for the next arrow item.",
							"The arrow and bow in the event can be accessed with the Readied Arrow/Bow expression.")
					.requiredPlugins("Paper")
					.examples(
						"on player ready arrow:",
							"\tselected bow's name is \"Spectral Bow\"",
							"\tif selected arrow is not a spectral arrow:",
								"\t\tcancel event"
					)
					.since("2.8.0");
		}

		if (Skript.classExists("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
			Skript.registerEvent("Inventory Slot Change", SimpleEvent.class, PlayerInventorySlotChangeEvent.class, "[player] inventory slot chang(e|ing)")
					.description("Called when a slot in a player's inventory is changed.", "Warning: setting the event-slot to a new item can result in an infinite loop.")
					.requiredPlugins("Paper 1.19.2+")
					.examples(
						"on inventory slot change:",
							"\tif event-item is a diamond:",
								"\t\tsend \"You obtained a diamond!\" to player"
					)
					.since("2.7");
		}
    
		//noinspection deprecation
		Skript.registerEvent("Chat", SimpleEvent.class, AsyncPlayerChatEvent.class, "chat")
			.description(
				"Called whenever a player chats.",
				"Use <a href='./expressions.html#ExprChatFormat'>chat format</a> to change message format.",
				"Use <a href='./expressions.html#ExprChatRecipients'>chat recipients</a> to edit chat recipients."
			)
			.examples(
				"on chat:",
				"\tif player has permission \"owner\":",
				"\t\tset chat format to \"&lt;red&gt;[player]&lt;light gray&gt;: &lt;light red&gt;[message]\"",
				"\telse if player has permission \"admin\":",
				"\t\tset chat format to \"&lt;light red&gt;[player]&lt;light gray&gt;: &lt;orange&gt;[message]\"",
				"\telse: #default message format",
				"\t\tset chat format to \"&lt;orange&gt;[player]&lt;light gray&gt;: &lt;white&gt;[message]\""
			)
			.since("1.4.1");
		if (Skript.classExists("org.bukkit.event.world.LootGenerateEvent")) {
			Skript.registerEvent("Loot Generate", SimpleEvent.class, LootGenerateEvent.class, "loot generat(e|ing)")
					.description(
						"Called when a loot table of an inventory is generated in the world.",
						"For example, when opening a shipwreck chest."
					)
					.examples(
						"on loot generate:",
						"\tchance of 10%",
						"\tadd 64 diamonds to the loot",
						"\tsend \"You hit the jackpot at %event-location%!\""
					)
					.since("2.7")
					.requiredPlugins("MC 1.16+");
		}
		if (Skript.classExists("io.papermc.paper.event.player.PlayerDeepSleepEvent")) {
			Skript.registerEvent("Player Deep Sleep", SimpleEvent.class, PlayerDeepSleepEvent.class, "[player] deep sleep[ing]")
					.description(
							"Called when a player has slept long enough to count as passing the night/storm.",
							"Cancelling this event will prevent the player from being counted as deeply sleeping unless they exit and re-enter the bed."
					)
					.examples(
							"on player deep sleeping:",
							"\tsend \"Zzzz..\" to player"
					)
					.since("2.7")
					.requiredPlugins("Paper 1.16+");
		}

		Skript.registerEvent("Player Pickup Arrow", SimpleEvent.class, PlayerPickupArrowEvent.class, "[player] (pick[ing| ]up [an] arrow|arrow pick[ing| ]up)")
				.description("Called when a player picks up an arrow from the ground.")
				.examples(
						"on arrow pickup:",
								"\tcancel the event",
								"\tteleport event-projectile to block 5 above event-projectile"
				)
				.since("2.8.0")
				.requiredPlugins("Minecraft 1.14+ (event-projectile)");

		Skript.registerEvent("Inventory Drag", SimpleEvent.class, InventoryDragEvent.class, "inventory drag[ging]")
				.description("Called when a player drags an item in their cursor across the inventory.")
				.examples(
						"on inventory drag:",
						"\tif player's current inventory is {_gui}:",
						"\t\tsend \"You can't drag your items here!\" to player",
						"\t\tcancel event"
				)
				.since("2.7");

		if (Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent")) {
			Skript.registerEvent("Piglin Barter", SimpleEvent.class, PiglinBarterEvent.class, "piglin (barter[ing]|trad(e|ing))")
				.requiredPlugins("Minecraft 1.16+")
				.description(
					"Called when a piglin finishes bartering. A piglin may start bartering after picking up an item on its bartering list.",
					"Cancelling will prevent piglins from dropping items, but will still make them pick up the input.")
				.examples(
					"on piglin barter:",
					"\tif barter drops contain diamond:",
					"\t\tsend \"Diamonds belong in the money pit!\" to player",
					"\t\tcancel event"
				)
				.since("INSERT VERSION");
		}
		{
			final Class<? extends Event> eventClass;
			if (Skript.classExists("org.bukkit.event.block.BellRingEvent")) {
				eventClass = org.bukkit.event.block.BellRingEvent.class;
			} else if (Skript.classExists("io.papermc.paper.event.block.BellRingEvent")) {
				//noinspection deprecation
				eventClass = io.papermc.paper.event.block.BellRingEvent.class;
			} else {
				eventClass = null;
			}

			if (eventClass != null) {
				Skript.registerEvent("Bell Ring", SimpleEvent.class, eventClass, "bell ring[ing]")
						.description("Called when a bell is rung.")
						.examples(
							"on bell ring:",
								"\tsend \"<gold>Ding-dong!<reset>\" to all players in radius 10 of event-block"
						)
						.since("2.9.0")
						.requiredPlugins("Spigot 1.19.4+ or Paper 1.16.5+ (no event-direction)");
			}
		}

		/*
		* Paper supported this in 1.16.5 via io.papermc.paper.event.block.BellRevealRaiderEvent.
		* The Paper event, however, is called for each raider, while the Spigot event is called once for all raiders.
		* Supporting both would cause confusing behaviour, with the event being triggered in different ways depending
		* on the server software and version, so we're only supporting the Spigot event.
		*/
		if (Skript.classExists("org.bukkit.event.block.BellResonateEvent")) {
			Skript.registerEvent("Bell Resonate", SimpleEvent.class, org.bukkit.event.block.BellResonateEvent.class, "bell resonat(e|ing)")
					.description("Called when a bell resonates, highlighting nearby raiders.")
					.examples(
						"on bell resonate:",
							"\tsend \"<red>Raiders are nearby!\" to all players in radius 32 around event-block"
					)
					.since("2.9.0")
					.requiredPlugins("Spigot 1.19.4+");

		}

		if (Skript.classExists("com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent")) {
			Skript.registerEvent("Enderman Enrage", SimpleEvent.class, com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent.class, "enderman (enrage|anger)")
					.description(
						"Called when an enderman gets mad because a player looked at them.",
						"Note: This does not stop enderman from targeting the player as a result of getting damaged."
					)
					.examples(
						"# Stops endermen from getting angry players with the permission \"safeFrom.enderman\"",
						"on enderman enrage:",
							"\tif player has permission \"safeFrom.enderman\":",
								"\t\tcancel event"
					)
					.since("2.9.0")
					.requiredPlugins("Paper");
		}

		Skript.registerEvent("Broadcast", SimpleEvent.class, BroadcastMessageEvent.class, "broadcast")
			.description("Called when a message is broadcasted.")
			.examples(
				"on broadcast:",
					"\tset broadcast-message to \"&c[BROADCAST] %broadcasted message%\""
			)
			.since("INSERT VERSION");

		Skript.registerEvent("Experience Cooldown Change", SimpleEvent.class, PlayerExpCooldownChangeEvent.class, "player (experience|[e]xp) cooldown change")
			.description(
				"Called when a player's experience cooldown changes.",
				"Experience cooldown is how long until a player can pick up another orb of experience."
			)
			.examples(
				"on player experience cooldown change:",
					"\tbroadcast event-player",
					"\tbroadcast event-timespan",
					"\tbroadcast past event-timespan",
					"\tbroadcast xp cooldown change reason"
			)
			.since("INSERT VERSION");

		Skript.registerEvent("Vehicle Move", SimpleEvent.class, VehicleMoveEvent.class, "vehicle move")
			.description(
				"Called when a vehicle moves.",
				"Please note that using this event can cause lag if there are multiple vehicle entities, i.e. Horse, Pig, Boat, Minecart")
			.examples(
				"on vehicle move:",
					"\tbroadcast past event-location",
					"\tbroadcast event-location"
			)
			.since("INSERT VERSION");

	}

}
