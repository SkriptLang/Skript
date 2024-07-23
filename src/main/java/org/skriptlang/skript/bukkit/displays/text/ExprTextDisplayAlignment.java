package org.skriptlang.skript.bukkit.displays.text;

import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAlignment;
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

@Name("Text Display Alignment")
@Description("Returns or changes the <a href='classes.html#textalignment'>alignment</a> setting of <a href='classes.html#display'>text displays</a>.")
@Examples("set text alignment of the last spawned text display to left")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprTextDisplayAlignment extends SimplePropertyExpression<Display, TextAlignment> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprTextDisplayAlignment.class, TextAlignment.class, "text alignment[s]", "displays");
	}

	@Override
	@Nullable
	public TextAlignment convert(Display display) {
		if (!(display instanceof TextDisplay))
			return null;
		return ((TextDisplay) display).getAlignment();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				break;
			case RESET:
				return CollectionUtils.array();
			case SET:
				return CollectionUtils.array(TextAlignment.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		TextAlignment alignment = mode == ChangeMode.RESET ? TextAlignment.CENTER : (TextAlignment) delta[0];
		for (Display display : getExpr().getArray(event)) {
			if (!(display instanceof TextDisplay))
				continue;
			((TextDisplay)display).setAlignment(alignment);
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
