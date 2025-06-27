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
import org.joml.Vector3d;
import org.joml.Vector3f;

@Name("Display Transformation Scale/Translation")
@Description("Returns or changes the transformation scale or translation of <a href='classes.html#display'>displays</a>.")
@Examples("set transformation translation of display to vector from -0.5, -0.5, -0.5 # Center the display in the same position as a block")
@Since("2.10")
public class ExprDisplayTransformationScaleTranslation extends SimplePropertyExpression<Display, Vector3d> {

	static {
		register(ExprDisplayTransformationScaleTranslation.class, Vector3d.class, "(display|[display] transformation) (:scale|translation)", "displays");
	}

	private boolean scale;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		scale = parseResult.hasTag("scale");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Vector3d convert(Display display) {
		Transformation transformation = display.getTransformation();
		return new Vector3d(scale ? transformation.getScale() : transformation.getTranslation());
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> CollectionUtils.array(Vector3d.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Vector3f vector = new Vector3f();
		if (mode == ChangeMode.RESET && scale)
			vector.set(1F, 1F, 1F);
		if (delta != null)
			vector.set((Vector3d) delta[0]);
		if (!vector.isFinite())
			return;
		for (Display display : getExpr().getArray(event)) {
			Transformation transformation = display.getTransformation();
			Transformation change;
			if (scale) {
				change = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), vector, transformation.getRightRotation());
			} else {
				change = new Transformation(vector, transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
			}
			display.setTransformation(change);
		}
	}

	@Override
	public Class<? extends Vector3d> getReturnType() {
		return Vector3d.class;
	}

	@Override
	protected String getPropertyName() {
		return "transformation " + (scale ? "scale" : "translation");
	}

}
