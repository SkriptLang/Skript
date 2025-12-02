package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.Nullable;

@Name("Last Interaction Date")
@Description({
	"Returns the date of the last attack (left click), or interaction (right click) on an interaction entity"
})
@Examples({
	"if last interaction date of last spawned interaction < 5 seconds ago"
})
@Since("INSERT VERSION")
public class ExprLastInteractionDate extends SimplePropertyExpression<Entity, Date> {

	static {
		register(ExprLastInteractionDate.class, Date.class, "last (attack|1¦interaction) date", "entities");
	}

	private boolean interaction;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		interaction = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}



	@Override
	public @Nullable Date convert(Entity entity) {
		if (entity instanceof Interaction i) {
			if (interaction) {
				return new Date(i.getLastInteraction().getTimestamp());
			} else {
				return new Date(i.getLastAttack().getTimestamp());
			}

		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "last attack/interaction date";
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}
}