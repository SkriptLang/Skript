package org.skriptlang.skript.bukkit.entity.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time.PAST;

public class EntityEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {

		//
		// Entity Event Values
		//

		eventValueRegistry.register(EventValue.builder(EntityEvent.class, Entity.class)
			.getter(EntityEvent::getEntity)
			.excludes(EntityDamageEvent.class, EntityDeathEvent.class)
			.excludedErrorMessage("Use 'attacker' and/or 'victim' in damage/death events")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityEvent.class, CommandSender.class)
			.getter(EntityEvent::getEntity)
			.excludes(EntityDamageEvent.class, EntityDeathEvent.class)
			.excludedErrorMessage("Use 'attacker' and/or 'victim' in damage/death events")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityEvent.class, World.class)
			.getter(event -> event.getEntity().getWorld())
			.build());

		eventValueRegistry.register(EventValue.builder(EntityEvent.class, Location.class)
			.getter(event -> event.getEntity().getLocation())
			.build());

		eventValueRegistry.register(EventValue.builder(EntityEvent.class, EntityData.class)
			.getter(event -> EntityData.fromEntity(event.getEntity()))
			.excludes(EntityDamageEvent.class, EntityDeathEvent.class)
			.excludedErrorMessage("Use 'type of attacker/victim' in damage/death events.")
			.build());

		//
		// Entity Events
		//

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Combust")
			.addEvent(EntityCombustEvent.class)
			.addPatterns("[entity] combust[ing]")
			.addDescription("""
				Called when an entity is set on fire,
				e.g. when a player gets hit by a fireball, or a zombie goes into sunlight during the day.
				""")
			.addExample("""
				on entity combust:
					broadcast "It burns!!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("entity combusting"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Explode")
			.addEvent(EntityExplodeEvent.class)
			.addPatterns("[entity] explo(d(e|ing)|sion)")
			.addDescription("""
				Called when an entity (usually a primed TNT or creeper) explodes.
				""")
			.addExample("""
				on entity explode:
					broadcast "*Explosion*"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("entity explode"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Portal Enter")
			.addEvent(EntityPortalEnterEvent.class)
			.addPatterns("[entity] enter[ing] [a] portal", "[entity] portal enter[ing]")
			.addDescription("""
				Called when an entity enters a nether portal or an end portal.
				Please note that this event will be fired many times for a nether portal.
				""")
			.addExample("""
				on portal enter:
					kill event-entity
					broadcast "%event-entity% never got to see a new dimension.."
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("entity entering a portal"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Tame")
			.addEvent(EntityTameEvent.class)
			.addPatterns("[entity] tam(e|ing)")
			.addDescription("""
				Called when a player tames a wolf or ocelot.
				Can be cancelled to prevent the entity from being tamed.
				""")
			.addExample("""
				on tame:
					broadcast "Best friends for life!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("entity taming"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Mount")
			.addEvent(EntityMountEvent.class)
			.addPatterns("[entity] mount[ing]")
			.addDescription("""
				Called when an entity starts riding another."
				""")
			.addExample("""
				on entity mount:
					cancel event
				""")
			.addSince("2.2-dev13b")
			.supplier(() -> new SimpleEvent("entity mounting"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Dismount")
			.addEvent(EntityDismountEvent.class)
			.addPatterns("[entity] dismount[ing]")
			.addDescription("""
				Called when an entity dismounts another.
				""")
			.addExample("""
				on entity dismount:
					kill event-entity
				""")
			.addSince("2.2-dev13b")
			.supplier(() -> new SimpleEvent("entity dismounting"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Resurrect Attempt")
			.addEvent(EntityResurrectEvent.class)
			.addPatterns("[entity] resurrect[ion] [attempt]")
			.listeningBehavior(SkriptEvent.ListeningBehavior.ANY)
			.addDescription("""
				Called when an entity dies.
				If they are not holding a totem, this is cancellable - you can, however, uncancel it.
				""")
			.addExample("""
				on entity resurrect attempt:
					if all:
						event-entity is a player
						event-entity has permission "god"
					then:
						send "You seem to be immortal.. how" to event-entity
				""")
			.addSince("2.2-dev28")
			.supplier(() -> new SimpleEvent("entity resurrection attempt"))
			.build());

		eventValueRegistry.register(EventValue.builder(EntityResurrectEvent.class, Slot.class)
			.getter(event -> {
				org.bukkit.inventory.EquipmentSlot hand = event.getHand();
				EntityEquipment equipment = event.getEntity().getEquipment();
				if (equipment == null || hand == null)
					return null;
				return new EquipmentSlot(equipment, hand);
			})
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Jump")
			.addEvent(EntityJumpEvent.class)
			.addPatterns("entity jump[ing]")
			.addDescription("""
				Called when an entity jumps.
				""")
			.addExample("""
				on entity jump:
					push event-entity up at speed 1
				""")
			.addSince("2.7")
			.supplier(() -> new SimpleEvent("entity jumping"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Entity Toggle Swim")
			.addEvent(EntityToggleSwimEvent.class)
			.addPatterns(
				"[entity] toggl(e|ing) swim",
				"[on] [entity] swim toggl(e|ing)"
			)
			.addDescription("Called when an entity swims or stops swimming.")
			.addExample("""
				on swim toggle:
					event-entity does not have the permission "perk.swim"
					cancel event
				""")
			.addSince("2.3")
			.supplier(() -> new SimpleEvent("entity toggling swim"))
			.build());

		//
		// Entity specific events (e.g. CreeperPowerEvent)
		//

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Creeper Power")
			.addEvent(CreeperPowerEvent.class)
			.addPatterns("creeper power")
			.addDescription("""
				Called when a creeper is struck by lighting and gets powered.
				Cancelling the event will prevent the creeper from being powered.
				""")
			.addExample("""
				on creeper power:
					cancel event
					broadcast "No charged creepers in this world!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("creeper power"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Sheep Regrow Wool")
			.addEvent(SheepRegrowWoolEvent.class)
			.addPatterns("sheep [re]grow[ing] wool")
			.addDescription("Called when sheep regrows its sheared wool back.")
			.addExample("""
				on sheep grow wool:
					loop all players in radius 50 of event-entity:
						send "Theres free wool nearby!" to loop-value
				""")
			.addSince("2.2-dev21")
			.supplier(() -> new SimpleEvent("sheep regrowing wool"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Slime Split")
			.addEvent(SlimeSplitEvent.class)
			.addPatterns("slime split[ting]")
			.addDescription("""
				Called when a slime splits.
				Usually when a big slime dies.
				""")
			.addExample("""
				on slime split:
					broadcast "More slime minions have spawned!"
				""")
			.addSince("2.2-dev26")
			.supplier(() -> new SimpleEvent("slime splitting"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Horse Jump")
			.addEvent(HorseJumpEvent.class)
			.addPatterns("horse jump[ing]")
			.addDescription("Called when a horse jumps.")
			.addExample("""
				on horse jumping:
					push event-entity upwards at speed 2
					send "Wow that horse can really go high.." to (all players in radius 3 of event-entity)
				""")
			.addSince("2.5.1")
			.supplier(() -> new SimpleEvent("horse jumping"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Piglin Barter")
			.addEvent(PiglinBarterEvent.class)
			.addPatterns("piglin (barter[ing]|trad(e|ing))")
			.addDescription("""
				Called when a piglin finishes bartering.
				Note that a piglin may start bartering after picking up an item on its bartering list.
				If this event is cancelled it will prevent piglins from dropping items, but will still make them pick up the input.
				""")
			.addExample("""
				on piglin barter:
					if barter drops contain diamond:
						broadcast "Diamonds are too rare for this world.."
						cancel event
				""")
			.addSince("2.10")
			.supplier(() -> new SimpleEvent("piglin bartering"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Pig Zap")
			.addEvent(PigZapEvent.class)
			.addPatterns("pig[ ]zap")
			.addDescription("""
				Called when a pig is stroke by lightning and transformed into a zombie pigman.
				Cancelling this will prevent the transformation.
				""")
			.addExample("""
				on pig zap:
					broadcast "I'm feeling a bit static!"
				""")
			.addSince("1.0")
			.supplier(() -> new SimpleEvent("pig zap"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Bat Toggle Sleep")
			.addEvent(BatToggleSleepEvent.class)
			.addPatterns("bat toggl(e|ing) sleep")
			.addDescription("Called when a bat attempts to go to sleep or wakes up.")
			.addExample("""
				on bat toggle sleep:
					kill event-entity
					broadcast "Another bat tried to sleep and perished.."
				""")
			.addSince("2.11")
			.supplier(() -> new SimpleEvent("bat toggling sleep"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Villager Career Change")
			.addEvent(VillagerCareerChangeEvent.class)
			.addPatterns("villager career chang(e[d]|ing)")
			.addDescription("""
				Called when a villager changes its career.
				Can be caused by being employed or losing their job.
				""")
			.addExample("""
				on villager career change:
					if all:
						event-career change reason is employment
						event-villager profession is armorer profession
					then:
						cancel event
				""")
			.addSince("2.12")
			.supplier(() -> new SimpleEvent("villager career change"))
			.build());

		eventValueRegistry.register(EventValue.builder(VillagerCareerChangeEvent.class, Villager.Profession.class)
			.getter(VillagerCareerChangeEvent::getProfession)
			.registerChanger(ChangeMode.SET, (event, profession) -> {
				if (profession == null)
					return;
				event.setProfession(profession);
			})
			.build());

		eventValueRegistry.register(EventValue.builder(VillagerCareerChangeEvent.class, VillagerCareerChangeEvent.ChangeReason.class)
			.getter(VillagerCareerChangeEvent::getReason)
			.build());

		eventValueRegistry.register(EventValue.builder(VillagerCareerChangeEvent.class, Villager.Profession.class)
			.getter(event -> event.getEntity().getProfession())
			.time(PAST)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Enderman Enrage")
			.addEvent(EndermanAttackPlayerEvent.class)
			.addPatterns("enderman (enrage|anger)")
			.addDescription("""
				Called when an enderman gets mad because a player looked at them.
				Note: This does not stop enderman from targeting the player as a result of getting damaged
				""")
			.addExample("""
				on enderman enrage:
					player has permission "safe.from.enderman"
					cancel event
					send "well you got lucky this time.." to event-player
				""")
			.addSince("2.9.0")
			.supplier(() -> new SimpleEvent("enderman enrage"))
			.build());

		eventValueRegistry.register(EventValue.builder(EndermanAttackPlayerEvent.class, Player.class)
			.getter(EndermanAttackPlayerEvent::getPlayer)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Pathfind")
			.addDescription("Called whenever an entity tries to pathfind to a location or another entity.")
			.addExample("""
				on pathfind:
						broadcast "%event-entity% is about to move to %event-location%!"
				""")
			.addSince("2.16")
			.addPattern("[entity] [start[s]] pathfind[ing]")
			.addEvent(EntityPathfindEvent.class)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityPathfindEvent.class, Location.class)
			.getter(EntityPathfindEvent::getLoc)
			.patterns("target location")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityPathfindEvent.class, Entity.class)
			.getter(EntityPathfindEvent::getTargetEntity)
			.patterns("target entity")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityPathfindEvent.class, Location.class)
			.getter(event -> event.getEntity().getLocation())
			.build());
	}

}
