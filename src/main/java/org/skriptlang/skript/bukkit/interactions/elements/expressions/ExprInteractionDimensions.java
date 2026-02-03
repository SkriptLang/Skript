package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Interaction Height/Width")
@Description("""
	Returns the height or width of an interaction entity's hitbox. Both default to 1.
	The width of the hitbox determines the x/z widths
	""")
@Example("set interaction height of last spawned interaction to 5.3")
@Example("set interaction width of last spawned interaction to 2")
@Since("2.14")
public class ExprInteractionDimensions extends SimplePropertyExpression<Entity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprInteractionDimensions.class, Number.class,
						"interaction (height|:width)[s]", "entities",
						true)
				.supplier(ExprInteractionDimensions::new)
				.build());
	}

	private boolean width;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		width = parseResult.hasTag("width");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Number convert(Entity entity) {
		if (entity instanceof Interaction interaction)
			return width ? interaction.getInteractionWidth() :  interaction.getInteractionHeight();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, RESET -> new Class[]{ Number.class };
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		if (entities.length == 0)
			return;
		float deltaValue = delta == null ? 1.0f : ((Number) delta[0]).floatValue();
		for (Entity entity : entities) {
			if (!(entity instanceof Interaction interaction))
				continue;
			switch (mode) {
				case REMOVE:
					deltaValue = -deltaValue;
					// fallthrough
				case ADD:
					if (width) {
						interaction.setInteractionWidth(Math.max(interaction.getInteractionWidth() + deltaValue, 0));
					} else {
						interaction.setInteractionHeight(Math.max(interaction.getInteractionHeight() + deltaValue, 0));
					}
					break;
				case SET, RESET:
					deltaValue = Math.max(deltaValue, 0);
					if (width) {
						interaction.setInteractionWidth(deltaValue);
					} else {
						interaction.setInteractionHeight(deltaValue);
					}
					break;
			}
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "interaction " + (width ? "width" : "height");
	}

}
