package org.skriptlang.skript.bukkit.displays.text;

import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Text Display Line Width")
@Description("Returns or changes the line width of <a href='classes.html#display'>text displays</a>. Default is 200.")
@Examples("set the line width of the last spawned text display to 300")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprTextDisplayLineWidth extends SimplePropertyExpression<Display, Integer> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprTextDisplayLineWidth.class, Integer.class, "line width", "displays");
	}

	@Override
	@Nullable
	public Integer convert(Display display) {
		if (!(display instanceof TextDisplay))
			return null;
		return ((TextDisplay) display).getLineWidth();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int change = delta == null ? 200 : ((Number) delta[0]).intValue();
		change = Math.max(0, change);
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = -change;
			case ADD:
				for (Display display : displays) {
					if (!(display instanceof TextDisplay))
						continue;
					TextDisplay textDisplay = (TextDisplay) display;
					int value = Math.max(0, textDisplay.getLineWidth() + change);
					textDisplay.setLineWidth(value);
				}
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Display display : displays) {
					if (!(display instanceof TextDisplay))
						continue;
					((TextDisplay) display).setLineWidth(change);
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
