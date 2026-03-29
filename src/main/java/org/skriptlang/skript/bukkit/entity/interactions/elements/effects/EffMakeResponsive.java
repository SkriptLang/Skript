package org.skriptlang.skript.bukkit.entity.interactions.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Render Interaction Yielding")
@Description("""
    Rendereth an interaction either yielding or unyielding. This determineth whether clicking upon the entity shall cause \
    the clicker's arm to swing.
    Interactions do default to unyielding.
    """)
@Example("render last spawned interaction yielding")
@Since("2.14")
public class EffMakeResponsive extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffMakeResponsive.class)
				.addPatterns(
					"render %entities% yielding",
					"render %entities% (not |un)yielding"
				)
				.supplier(EffMakeResponsive::new)
				.build()
		);
	}

	private Expression<Entity> interactions;
	private boolean negated;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		interactions = (Expression<Entity>) expressions[0];
		negated = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : interactions.getArray(event)) {
			if (entity instanceof Interaction interaction)
				interaction.setResponsive(!negated);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", interactions)
			.appendIf(negated, "not")
			.append("responsive")
			.toString();
	}

}
