package org.skriptlang.skript.bukkit.entity.elements.events;


import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityTarget extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityTarget.class, "Entity Target")
			.supplier(EvtEntityTarget::new)
			.addEvent(EntityTargetEvent.class)
			.addPatterns("[entity] target", "[entity] un[-]target")
			.addDescription("""
				Called when a mob starts/stops following/attacking another entity, usually a player.
				See <a href="Target">target</a>. for how to get the entity being targeted.
				""")
			.addExample("""
				on entity target:
				    broadcast "I detect an entity is targeting another.."
				""")
			.addExample("""
				on entity untarget:
				    broadcast "Phew.. it looks like they stopped.."
				""")
			.addSince("1.0")
			.build());
	}

	private boolean target;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		target = matchedPattern == 0;
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityTargetEvent entityEvent = (EntityTargetEvent) event;
		return target == (entityEvent.getTarget() != null);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("entity")
			.appendIf(!target,"un")
			.append("target")
			.toString();
	}

}
