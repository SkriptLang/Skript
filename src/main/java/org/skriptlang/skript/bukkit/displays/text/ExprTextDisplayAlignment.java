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
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Text Display Alignment")
@Description("Returns or changes the <a href='classes.html#textalignment'>alignment</a> setting of <a href='classes.html#display'>text displays</a>.")
@Examples("set text alignment of the last spawned text display to left")
@Since("INSERT VERSION")
public class ExprTextDisplayAlignment extends SimplePropertyExpression<Display, TextAlignment> {

	static {
		registerDefault(ExprTextDisplayAlignment.class, TextAlignment.class, "text alignment[s]", "displays");
	}

	@Override
	public @Nullable TextAlignment convert(Display display) {
		if (display instanceof TextDisplay textDisplay)
			return textDisplay.getAlignment();
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case RESET -> CollectionUtils.array();
			case SET -> CollectionUtils.array(TextAlignment.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		//noinspection ConstantConditions
		TextAlignment alignment = mode == ChangeMode.RESET ? TextAlignment.CENTER : (TextAlignment) delta[0];
		for (Display display : getExpr().getArray(event)) {
			if (display instanceof TextDisplay textDisplay)
				textDisplay.setAlignment(alignment);
		}
	}

	@Override
	public Class<? extends TextAlignment> getReturnType() {
		return TextAlignment.class;
	}

	@Override
	protected String getPropertyName() {
		return "text alignment";
	}

}
