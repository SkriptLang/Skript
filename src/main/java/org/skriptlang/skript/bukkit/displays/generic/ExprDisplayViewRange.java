package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Display View Range")
@Description({
	"Returns or changes the view range of <a href='classes.html#display'>displays</a>.",
	"Default value is 1.0. This value is then multiplied by 64 and the player's entity view distance setting to determine the actual range.",
	"For example, a player with 150% entity view distance will see a block display with a view range of 1.2 at 1.2 * 64 * 150% = 115.2 blocks away."
})
@Examples("set view range of the last spawned text display to 2.9")
@Since("INSERT VERSION")
public class ExprDisplayViewRange extends SimplePropertyExpression<Display, Float> {

	static {
		registerDefault(ExprDisplayViewRange.class, Float.class, "[display] view (range|radius)", "displays");
	}

	@Override
	public @Nullable Float convert(Display display) {
		return display.getViewRange();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE -> CollectionUtils.array(Number.class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		float change = delta == null ? 1F : ((Number) delta[0]).floatValue();
		if (Float.isNaN(change) || Float.isInfinite(change))
			return;
		switch (mode) {
			case REMOVE:
				change = -change;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					float value = Math.max(0F, display.getViewRange() + change);
					if (Float.isInfinite(value))
						continue;
					display.setViewRange(value);
				}
				break;
			case RESET:
			case SET:
				change = Math.max(0F, change);
				for (Display display : displays)
					display.setViewRange(change);
				break;
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "view range";
	}

}
