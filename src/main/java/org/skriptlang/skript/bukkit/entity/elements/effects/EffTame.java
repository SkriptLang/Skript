package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Tame / Untame")
@Description("Tame a tameable entity (horse, parrot, cat, etc.).")
@Example("tame {_horse}")
@Example("untame {_horse}")
@Since("2.10")
public class EffTame extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffTame.class)
				.addPattern("[:un](tame|domesticate) %entities%")
				.supplier(EffTame::new)
				.build()
		);
	}

	private boolean tame;
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tame = !parseResult.hasTag("un");
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof Tameable tameable)
				tameable.setTamed(tame);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (tame ? "tame " : "untame ") + entities.toString(event, debug);
	}

}
