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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Silence Entity")
@Description("Controls whether or not an entity is silent.")
@Example("make target entity silent")
@Since("2.5")
public class EffSilence extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffSilence.class)
				.addPatterns(
					"silence %entities%",
					"unsilence %entities%",
					"make %entities% silent",
					"make %entities% not silent"
				).supplier(EffSilence::new)
				.build()
		);
	}

	private Expression<Entity> entities;
	private boolean silence;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		silence = matchedPattern % 2 == 0;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			entity.setSilent(silence);
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (silence ? "silence " : "unsilence ") + entities.toString(event, debug);
	}

}
