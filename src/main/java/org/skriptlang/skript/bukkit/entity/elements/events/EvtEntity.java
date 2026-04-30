package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntity extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Creeper Power")
				.addEvent(CreeperPowerEvent.class)
				.addPatterns("creeper power")
				.addDescription("Called when a creeper is struck by lighting and gets powered. Cancel the event to prevent the creeper from being powered.")
				.addExample("on creeper power:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Zombie Break Door")
				.addEvent(EntityBreakDoorEvent.class)
				.addPatterns("zombie break[ing] [a] [wood[en]] door")
				.addDescription("Called when a zombie is done breaking a wooden door. Can be cancelled to prevent the zombie from breaking the door.")
				.addExample("on zombie breaking a wood door:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Combust")
				.addEvent(EntityCombustEvent.class)
				.addPatterns("combust[ing]")
				.addDescription("Called when an entity is set on fire, e.g. by fire or lava, a fireball, or by standing in direct sunlight (zombies, skeletons).")
				.addExample("on combust:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Explode")
				.addEvent(EntityExplodeEvent.class)
				.addPatterns("explo(d(e|ing)|sion)")
				.addDescription("Called when an entity (a primed TNT or a creeper) explodes.")
				.addExample("on explosion:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Portal Enter")
				.addEvent(EntityPortalEnterEvent.class)
				.addPatterns("portal enter[ing]", "entering [a] portal")
				.addDescription("Called when an entity enters a nether portal or an end portal. Please note that this event will be fired many times for a nether portal.")
				.addExample("on portal enter:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Tame")
				.addEvent(EntityTameEvent.class)
				.addPatterns("[entity] tam(e|ing)")
				.addDescription("Called when a player tames a wolf or ocelot. Can be cancelled to prevent the entity from being tamed.")
				.addExample("on tame:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Explosion Prime")
				.addEvent(ExplosionPrimeEvent.class)
				.addPatterns("explosion prime")
				.addDescription("Called when an explosive is primed, i.e. an entity will explode shortly. Creepers can abort the explosion if the player gets too far away, while TNT will explode for sure after a short time.")
				.addExample("on explosion prime:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Hunger Meter Change")
				.addEvent(FoodLevelChangeEvent.class)
				.addPatterns("(food|hunger) (level|met(er|re)|bar) chang(e|ing)")
				.addDescription("Called when the hunger bar of a player changes, i.e. either increases by eating or decreases over time.")
				.addExample("on food bar change:")
				.addSince("1.4.4")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Pig Zap")
				.addEvent(PigZapEvent.class)
				.addPatterns("pig[ ]zap")
				.addDescription("Called when a pig is stroke by lightning and transformed into a zombie pigman. Cancel the event to prevent the transformation.")
				.addExample("on pig zap:")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Projectile Hit")
				.addEvent(ProjectileHitEvent.class)
				.addPatterns("projectile hit")
				.addDescription("Called when a projectile hits an entity or a block.")
				.addExample("""
					on projectile hit:
						if victim's health <= 3:
							delete event-projectile
					""")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Shoot")
				.addEvent(ProjectileLaunchEvent.class)
				.addPatterns("[projectile] (shoot|launch)")
				.addDescription("Called whenever a projectile is shot. Use the shooter expression to get who shot the projectile.")
				.addExample("""
					on shoot:
						if projectile is an arrow:
							send "you shot an arrow!" to shooter
					""")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Entity Mount")
				.addEvent(EntityMountEvent.class)
				.addPatterns("mount[ing]")
				.addDescription("Called when entity starts riding another.")
				.addExample("""
					on mount:
						cancel event
					""")
				.addSince("2.2-dev13b")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Entity Dismount")
				.addEvent(EntityDismountEvent.class)
				.addPatterns("dismount[ing]")
				.addDescription("Called when an entity dismounts.")
				.addExample("""
					on dismount:
						kill event-entity
					""")
				.addSince("2.2-dev13b")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Gliding State Change")
				.addEvent(EntityToggleGlideEvent.class)
				.addPatterns("(gliding state change|toggl(e|ing) gliding)")
				.addDescription("Called when an entity toggles glider on or off, or when server toggles gliding state of an entity forcibly.")
				.addExample("""
					on toggling gliding:
						cancel the event # bad idea, but you CAN do it!
					""")
				.addSince("2.2-dev21")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "AoE Cloud Effect")
				.addEvent(AreaEffectCloudApplyEvent.class)
				.addPatterns("(area|AoE) [cloud] effect")
				.addDescription("Called when area effect cloud applies its potion effect. This happens every 5 ticks by default.")
				.addExample("on area cloud effect:")
				.addSince("2.2-dev21")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Sheep Regrow Wool")
				.addEvent(SheepRegrowWoolEvent.class)
				.addPatterns("sheep [re]grow[ing] wool")
				.addDescription("Called when sheep regrows its sheared wool back.")
				.addExample("""
					on sheep grow wool:
						cancel event
					""")
				.addSince("2.2-dev21")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Slime Split")
				.addEvent(SlimeSplitEvent.class)
				.addPatterns("slime split[ting]")
				.addDescription("Called when a slime splits. Usually this happens when a big slime dies.")
				.addExample("on slime split:")
				.addSince("2.2-dev26")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Resurrect Attempt")
				.addEvent(EntityResurrectEvent.class)
				.addPatterns("[entity] resurrect[ion] [attempt]")
				.addDescription("Called when an entity dies, always. If they are not holding a totem, this is cancelled - you can, however, uncancel it.")
				.addExample("""
					on resurrect attempt:
						entity is player
						entity has permission "admin.undying"
						uncancel the event
					""")
				.addSince("2.2-dev28")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Swim Toggle")
				.addEvent(EntityToggleSwimEvent.class)
				.addPatterns("[entity] toggl(e|ing) swim", "[entity] swim toggl(e|ing)")
				.addDescription("Called when an entity swims or stops swimming.")
				.addRequiredPlugins("Minecraft 1.13 or newer")
				.addExample("""
					on swim toggle:
						event-entity does not have permission "swim"
						cancel event
					""")
				.addSince("2.3")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Horse Jump")
				.addEvent(HorseJumpEvent.class)
				.addPatterns("horse jump")
				.addDescription("Called when a horse jumps.")
				.addExample("""
					on horse jump:
						push event-entity upwards at speed 2
					""")
				.addSince("2.5.1")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Piglin Barter")
				.addEvent(PiglinBarterEvent.class)
				.addPatterns("piglin (barter[ing]|trad(e|ing))")
				.addDescription("Called when a piglin finishes bartering. A piglin may start bartering after picking up an item on its bartering list.",
					"Cancelling will prevent piglins from dropping items, but will still make them pick up the input.")
				.addExample("""
					on piglin barter:
						if barter drops contain diamond:
							send "Diamonds belong in the money pit!" to player
							cancel event
					""")
				.addSince("2.10")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Bat Toggle Sleep")
				.addEvent(BatToggleSleepEvent.class)
				.addPatterns("bat toggle sleep")
				.addDescription("Called when a bat attempts to go to sleep or wakes up.")
				.addExample("on bat toggle sleep:")
				.addSince("2.11")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Villager Career Change")
				.addEvent(VillagerCareerChangeEvent.class)
				.addPatterns("villager career chang(e[d]|ing)")
				.addDescription("Called when a villager changes its career. Can be caused by being employed or losing their job.")
				.addExample("""
					on villager career change:
						if all:
							event-career change reason is employment
							event-villager profession is armorer profession
						then:
							cancel event
					""")
				.addSince("2.12")
				.supplier(EvtEntity::new)
				.build()
		);

		if (Skript.classExists("com.destroystokyo.paper.event.entity.ProjectileCollideEvent")){
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Projectile Collide")
					.addEvent(ProjectileCollideEvent.class)
					.addPatterns("projectile collid(e|ing)")
					.addDescription("Called when a projectile collides with an entity.")
					.addExample("""
						on projectile collide:
							teleport shooter of event-projectile to event-entity
						""")
					.addSince("2.5")
					.supplier(EvtEntity::new)
					.build()
			);
		}

		if (Skript.classExists("com.destroystokyo.paper.event.entity.EntityJumpEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Entity Jump")
					.addEvent(EntityJumpEvent.class)
					.addPatterns("entity jump[ing]")
					.addDescription("Called when an entity jumps.")
					.addExample("""
						on entity jump:
							if entity is a wither skeleton:
								cancel event
						""")
					.addSince("2.7")
					.supplier(EvtEntity::new)
					.build()
			);
		}

		if (Skript.classExists("com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Enderman Enrage")
					.addEvent(com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent.class)
					.addPatterns("enderman (enrag(e|ing)|anger)")
					.addDescription(
						"Called when an enderman gets mad because a player looked at them.",
						"Note: This does not stop endermen from targeting the player as a result of getting damaged."
					)
					.addExample("""
						on enderman enrage:
							if player has permission "safeFrom.enderman":
								cancel event
						""")
					.addSince("2.9.0")
					.supplier(EvtEntity::new)
					.build()
			);
		}

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Death")
				.addEvent(EntityDeathEvent.class)
				.addPatterns("death [of %-entitydatas%]")
				.addDescription("Called when a living entity (including players) dies.")
				.addExamples("on death:", "on death of player:", "on death of a wither or ender dragon:", "  broadcast \"A %entity% has been slain in %world%!\"")
				.addSince("1.0")
				.supplier(EvtEntity::new)
				.build()
		);

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtEntity.class, "Spawn")
				.addEvent(EntitySpawnEvent.class)
				.addPatterns("spawn[ing] [of %-entitydatas%]")
				.addDescription("Called when an entity spawns (excluding players).")
				.addExamples("on spawn of a zombie:", "on spawn of an ender dragon:", "  broadcast \"A dragon has been sighted in %world%!\"")
				.addSince("1.0, 2.5.1 (non-living entities)")
				.supplier(EvtEntity::new)
				.build()
		);

	}

	@Nullable
	private EntityData<?>[] types;

	private boolean spawn;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = args.length > 0 && args[0] != null ? ((Literal<EntityData<?>>) args[0]).getAll() : null;
		spawn = StringUtils.startsWithIgnoreCase(parser.expr, "spawn");
		if (types != null) {
			if (spawn) {
				for (final EntityData<?> d : types) {
					if (HumanEntity.class.isAssignableFrom(d.getType())) {
						Skript.error("The spawn event does not work for human entities", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
				}
			} else {
				for (final EntityData<?> d : types) {
					if (!LivingEntity.class.isAssignableFrom(d.getType())) {
						Skript.error("The death event only works for living entities", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean check(final Event event) {
		if (types == null)
			return true;
		final Entity en = event instanceof EntityDeathEvent ? ((EntityDeathEvent) event).getEntity() : ((EntitySpawnEvent) event).getEntity();
		for (final EntityData<?> d : types) {
			if (d.isInstance(en))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (spawn ? "spawn" : "death") + (types != null ? " of " + Classes.toString(types, false) : "");
	}

}
