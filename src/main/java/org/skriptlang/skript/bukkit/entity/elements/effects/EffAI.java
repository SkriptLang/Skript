package org.skriptlang.skript.bukkit.entity.elements.effects;

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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Artificial Intelligence")
@Description("Enable or disable the artificial intelligence of a living entity.")
@Example("""
	if {_entity} has ai:
		disable the ai of {_entity}
	""")
@Since("INSERT VERSION")
public class EffAI extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffAI.class)
				.addPatterns("(enable|:disable) [the] (ai|artificial intelligence) of %livingentities%")
				.supplier(EffAI::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		enable = !parseResult.hasTag("disable");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setAI(enable);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (enable) {
			builder.append("enable");
		} else {
			builder.append("disable");
		}
		builder.append("the ai of", enable);
		return builder.toString();
	}

}
