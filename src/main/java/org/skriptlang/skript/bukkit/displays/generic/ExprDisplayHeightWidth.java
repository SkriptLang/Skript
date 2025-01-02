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
import org.jetbrains.annotations.Nullable;

@Name("Display Height/Width")
@Description({
	"Returns or changes the height or width of <a href='classes.html#display'>displays</a>.",
	"The rendering culling bounding box spans horizontally width/2 from entity position, " +
	"which determines the point at which the display will be frustum culled (no longer rendered because the game " +
	"determines you are no longer able to see it).",
	"If set to 0, no culling will occur on both the vertical and horizontal directions. Default is 0.0."
})
@Examples("set display height of the last spawned text display to 2.5")
@Since("2.10")
public class ExprDisplayHeightWidth extends SimplePropertyExpression<Display, Float> {

	static {
		registerDefault(ExprDisplayHeightWidth.class, Float.class, "display (:height|width)", "displays");
	}

	private boolean height;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		height = parseResult.hasTag("height");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Float convert(Display display) {
		return height ? display.getDisplayHeight() : display.getDisplayWidth();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, RESET, SET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);

		float change = delta == null ? 0F : ((Number) delta[0]).floatValue();
		if (Float.isInfinite(change) || Float.isNaN(change))
			return;

		switch (mode) {
			case REMOVE:
				change = -change;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					if (height) {
						float value = Math.max(0F, display.getDisplayHeight() + change);
						if (Float.isInfinite(value))
							continue;
						display.setDisplayHeight(value);
					} else {
						float value = Math.max(0F, display.getDisplayWidth() + change);
						if (Float.isInfinite(value))
							continue;
						display.setDisplayWidth(value);
					}
				}
				break;
			case RESET:
			case SET:
				change = Math.max(0F, change);
				for (Display display : displays) {
					if (height) {
						display.setDisplayHeight(change);
					} else {
						display.setDisplayWidth(change);
					}
				}
				break;
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return height ? "height" : "width";
	}

}
