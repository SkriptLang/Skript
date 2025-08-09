package org.skriptlang.skript.bukkit.misc.expressions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Text Of")
@Description({
	"Returns or changes the <a href='#string'>text/string</a> of <a href='#display'>displays</a>.",
	"Note that currently you can only use Skript chat codes when running Paper."
})
@Examples("set text of the last spawned text display to \"example\"")
@Since("2.10")
public class ExprTextOf extends SimplePropertyExpression<Object, String> {

	static {
		String types = "";
		if (Skript.classExists("org.bukkit.entity.Display")) {
			types += "displays";
		}
		// This is because this expression is setup to support future types.
		// Remove this if non-versioning.
		if (!types.isEmpty())
			register(ExprTextOf.class, String.class, "text[s]", types);
	}

	@Override
	public @Nullable String convert(Object object) {
		if (object instanceof TextDisplay textDisplay)
			return textDisplay.getText();
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case RESET -> CollectionUtils.array();
			case SET -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Component component = delta == null ? null : (Component) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (!(object instanceof TextDisplay textDisplay))
				continue;
			textDisplay.text(component);
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "text";
	}

}
