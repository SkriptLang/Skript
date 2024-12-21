package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Text Display Opacity")
@Description({
	"Returns or changes the opacity of <a href='classes.html#display'>text displays</a>.",
	"Values are between -127 and 127. The value of 127 represents it being completely opaque."
})
@Examples("set the opacity of the last spawned text display to -1 # Reset")
@Since("INSERT VERSION")
public class ExprTextDisplayOpacity extends SimplePropertyExpression<Display, Byte> {

	static {
		registerDefault(ExprTextDisplayOpacity.class, Byte.class, "[display] opacity", "displays");
	}

	@Override
	public @Nullable Byte convert(Display display) {
		if (display instanceof TextDisplay textDisplay)
			return textDisplay.getTextOpacity();
		return null;
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
		int change = delta == null ? -1 : ((Number) delta[0]).intValue();
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = -change;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					if (display instanceof TextDisplay textDisplay) {
						byte value = (byte) Math2.fit(-127, textDisplay.getTextOpacity() + change, 127);
						textDisplay.setTextOpacity(value);
					}
				}
				break;
			case DELETE:
			case RESET:
			case SET:
				change = Math2.fit(-127, change, 127);
				for (Display display : displays) {
					if (display instanceof TextDisplay textDisplay)
						textDisplay.setTextOpacity((byte) change);
				}
				break;
		}
	}

	@Override
	public Class<? extends Byte> getReturnType() {
		return Byte.class;
	}

	@Override
	protected String getPropertyName() {
		return "opacity";
	}

}
