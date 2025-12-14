package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.interactions.InteractionModule.InteractionType;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Last Interaction Player")
@Description("""
	Returns the last player to attack (left click), or interact (right click) with an interaction entity.
	If 'click on' or 'clicked on' are used, this will return the last player to either attack or interact with the entity \
	whichever was most recent.
	""")
@Example("kill the last player that attacked the last spawned interaction")
@Example("feed the last player who interacted with {_i}")
@Since("INSERT VERSION")
public class ExprLastInteractionPlayer extends SimplePropertyExpression<Entity, OfflinePlayer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprLastInteractionPlayer.class, OfflinePlayer.class)
				.addPatterns(
					"[the] last player[s] to (attack|1:interact with|2:click [on]) %entities%",
					"[the] last player[s] (who|that) (attacked|1:interacted with|2:clicked [on]) %entities%"
				)
				.supplier(ExprLastInteractionPlayer::new)
				.build());
	}

	private InteractionType interactionType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		interactionType = InteractionType.values()[parseResult.mark];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable OfflinePlayer convert(Entity entity) {
		if (entity instanceof Interaction interaction) {
			PreviousInteraction lastInteraction = switch (interactionType) {
				case ATTACK -> interaction.getLastAttack();
				case INTERACT -> interaction.getLastInteraction();
				case BOTH -> InteractionModule.getLatestInteraction(interaction);
			};
			if (lastInteraction == null)
				return null;
			return lastInteraction.getPlayer();
		}
		return null;
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	protected String getPropertyName() {
		return "UNUSED";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the last player to")
			.append(switch (interactionType) {
				case ATTACK -> "attack";
				case INTERACT -> "interact with";
				case BOTH -> "click on";
			})
			.append(getExpr())
			.toString();
	}

}
