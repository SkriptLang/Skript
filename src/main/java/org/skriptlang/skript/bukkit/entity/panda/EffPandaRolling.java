package org.skriptlang.skript.bukkit.entity.panda;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Panda Roll")
@Description("Make a panda start/stop rolling.")
@Example("""
	if last spawned panda is not rolling:
		make last spawned panda start rolling
	""")
@Since("2.11")
public class EffPandaRolling extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffPandaRolling.class)
				.addPatterns(
					"make %livingentities% (start:(start rolling|roll)|stop rolling)",
					"force %livingentities% to (:start|stop) rolling"
				).supplier(EffPandaRolling::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean start;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		start = parseResult.hasTag("start");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Panda panda) {
				panda.setRolling(start);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (start) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("rolling");
		return builder.toString();
	}

}
