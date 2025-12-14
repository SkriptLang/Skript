package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.interactions.InteractionModule.InteractionType;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Last Interaction Date")
@Description("""
	Returns the date of the last attack (left click), or interaction (right click) on an interaction entity
	Using 'clicked on' will return the latest attack or interaction, whichever was more recent.
	""")
@Examples("if the last time {_interaction} was clicked < 5 seconds ago")
@Since("INSERT VERSION")
public class ExprLastInteractionDate extends SimplePropertyExpression<Entity, Date> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprLastInteractionDate.class, Date.class)
				.addPatterns(
					"[the] last (date|time)[s] [that|when] %entities% (were|was) (attacked|1:interacted with|2:clicked [on])"
				)
				.supplier(ExprLastInteractionDate::new)
				.build());
	}

	private InteractionType interactionType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		interactionType = InteractionType.values()[parseResult.mark];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Date convert(Entity entity) {
		if (entity instanceof Interaction interaction) {
			PreviousInteraction lastInteraction = switch (interactionType) {
				case ATTACK -> interaction.getLastAttack();
				case INTERACT -> interaction.getLastInteraction();
				case BOTH -> InteractionModule.getLatestInteraction(interaction);
			};
			if (lastInteraction == null)
				return null;
			return new Date(lastInteraction.getTimestamp());
		}
		return null;
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}

	@Override
	protected String getPropertyName() {
		return "UNUSED";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the last date that")
			.append(getExpr())
			.append(getExpr().isSingle() ? "was" : "were")
			.append(switch (interactionType) {
				case ATTACK -> "attacked";
				case INTERACT -> "interacted with";
				case BOTH -> "clicked on";
			})
			.toString();
	}

}
