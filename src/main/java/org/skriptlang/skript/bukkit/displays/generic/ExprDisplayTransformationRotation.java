package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Name("Display Transformation Rotation")
@Description({
        "Returns or changes the transformation rotation of <a href='classes.html#display'>displays</a>.",
        "The left rotation is applied first, with the right rotation then being applied based on the rotated axis."
})
@Examples("set left transformation rotation of last spawned block display to quaternion(1, 0, 0, 0) # reset block display")
@Since("2.10")
public class ExprDisplayTransformationRotation extends SimplePropertyExpression<Display, Quaternionf> {

	static {
		registerDefault(ExprDisplayTransformationRotation.class, Quaternionf.class, "(:left|right) [transformation] rotation", "displays");
	}

	private boolean left;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		left = parseResult.hasTag("left");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Quaternionf convert(Display display) {
		Transformation transformation = display.getTransformation();
		return left ? transformation.getLeftRotation() : transformation.getRightRotation();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> CollectionUtils.array(Quaternionf.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Quaternionf quaternion = null;
		if (mode == ChangeMode.RESET)
			quaternion = new Quaternionf(0, 0, 0, 1);
		if (delta != null) {
			quaternion = (Quaternionf) delta[0];
		}
		if (quaternion == null || !quaternion.isFinite())
			return;
		for (Display display : getExpr().getArray(event)) {
			Transformation transformation = display.getTransformation();
			Transformation change;
			if (left) {
				change = new Transformation(transformation.getTranslation(), quaternion, transformation.getScale(), transformation.getRightRotation());
			} else {
				change = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), quaternion);
			}
			display.setTransformation(change);
		}
	}

	@Override
	public Class<? extends Quaternionf> getReturnType() {
		return Quaternionf.class;
	}

	@Override
	protected String getPropertyName() {
		return (left ? "left" : "right") + " transformation rotation";
	}

}
