package org.skriptlang.skript.bukkit.interactions.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;

@Name("Is Responsive")
@Description("""
	Checks whether an interaction is responsive or not. Responsiveness determines whether clicking the entity will cause \
	the clicker's arm to swing.
	""")
@Example("if last spawned interaction is responsive:")
@Example("if last spawned interaction is unresponsive:")
@Since("INSERT VERSION")
public class CondIsResponsive extends PropertyCondition<Entity> {

	static {
		register(CondIsResponsive.class, "(responsive|1:unresponsive)", "entities");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!super.init(exprs, matchedPattern, isDelayed, parseResult))
			return false;
		setNegated(isNegated() ^ parseResult.mark == 1); // toggles negation if `unresponsive` is used. 
		return true;
	}

	@Override
	public boolean check(Entity entity) {
		if (entity instanceof Interaction interaction) {
			return interaction.isResponsive() ^ isNegated();
		}
		return isNegated();
	}

	@Override
	protected String getPropertyName() {
		return isNegated() ? "unresponsive" : "responsive";
	}

}
