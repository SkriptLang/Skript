package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.skriptlang.skript.lang.script.ScriptWarning.printDeprecationWarning;

@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class EvtProjectileCollide extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtProjectileCollide.class, "Projectile Collide")
			.supplier(EvtProjectileCollide::new)
			.addEvent(ProjectileCollideEvent.class)
			.addPatterns("projectile collid(e|ing)")
			.addDescription("""
				Called when a projectile collides with an entity.
				You should generally prefer <a href='#projectile_hit'>projectile hit event</a> and check if victim is set over this.
				""")
			.addExample("""
				on projectile collide:
					teleport shooter of event-projectile to event-entity
				""")
			.addSince("2.5, INSERT VERSION (ing)")
			.build());

		eventValueRegistry.register(EventValue.builder(ProjectileCollideEvent.class, Projectile.class)
			.getter(ProjectileCollideEvent::getEntity)
			.build());

		eventValueRegistry.register(EventValue.builder(ProjectileCollideEvent.class, Entity.class)
			.getter(ProjectileCollideEvent::getCollidedWith)
			.build());
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		printDeprecationWarning("This event is deprecated and scheduled for removal. Please use the projectile hit event instead.");
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "projectile collide";
	}

}
