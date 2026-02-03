package org.skriptlang.skript.bukkit.interactions.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Responsive")
@Description("""
	Checks whether an interaction is responsive or not. Responsiveness determines whether clicking the entity will cause \
	the clicker's arm to swing.
	""")
@Example("if last spawned interaction is responsive:")
@Example("if last spawned interaction is unresponsive:")
@Since("2.14")
public class CondIsResponsive extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsResponsive.class, PropertyType.BE, "(responsive|:unresponsive)", "entities")
				.supplier(CondIsResponsive::new)
				.build());
	}

	private boolean responsive;
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		responsive = !parseResult.hasTag("unresponsive");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	@Override
	public boolean check(Entity entity) {
		if (entity instanceof Interaction interaction) {
			return interaction.isResponsive() == responsive;
		}
		return false;
	}
	@Override
	protected String getPropertyName() {
		return responsive ? "responsive" : "unresponsive";
	}

}
