package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Text Display Line Width")
@Description("Returns or changes the line width of <a href='classes.html#display'>text displays</a>. Default is 200.")
@Examples("set the line width of the last spawned text display to 300")
@Since("INSERT VERSION")
public class ExprTextDisplayLineWidth extends SimplePropertyExpression<Display, Integer> {

	static {
		registerDefault(ExprTextDisplayLineWidth.class, Integer.class, "line width", "displays");
	}

	@Override
	public @Nullable Integer convert(Display display) {
		if (display instanceof TextDisplay textDisplay)
			return textDisplay.getLineWidth();
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET -> CollectionUtils.array(Number.class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int change = delta == null ? 200 : ((Number) delta[0]).intValue();
		switch (mode) {
			case REMOVE:
				change = -change;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					if (display instanceof TextDisplay textDisplay) {
						int value = Math.max(0, textDisplay.getLineWidth() + change);
						textDisplay.setLineWidth(value);
					}
				}
				break;
			case RESET:
			case SET:
				change = Math.max(0, change);
				for (Display display : displays) {
					if (display instanceof TextDisplay textDisplay)
						textDisplay.setLineWidth(change);
				}
				break;
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "line width";
	}

}
