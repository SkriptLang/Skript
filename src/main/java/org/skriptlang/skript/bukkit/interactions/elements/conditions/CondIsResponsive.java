package org.skriptlang.skript.bukkit.interactions.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Expression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;

@Name("Is Responsive")
@Description({
	"Checks whether an interaction entity is responsive"
})
@Examples({
	"if last spawned interaction is responsive:", "if last spawned interaction is unresponsive:"
})
@Since("INSERT VERSION")
public class CondIsResponsive extends PropertyCondition<Entity> {

	static {
		register(CondIsResponsive.class, "(responsive|1¦unresponsive)", "entities");
	}

	private boolean isNegated;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		isNegated = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity entity) {
		if (entity instanceof Interaction i) {
			return !isNegated == i.isResponsive();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return isNegated ? "unresponsive" : "responsive";
	}
}