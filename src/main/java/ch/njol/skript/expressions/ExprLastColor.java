package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

@Name("Last Color")
@Description("The colors used at the end of a string. The colors of the returned string will be formatted with their symbols.")
@Examples("set {_color} to the last colors of \"<red>hey<blue>yo\"")
@Since("2.6")
public class ExprLastColor extends SimplePropertyExpression<String, String> {

	static {
		register(ExprLastColor.class, String.class, "last color[s]", "strings");
	}

	@Nullable
	@Override
	public String convert(String string) {
		return ChatColor.getLastColors(string);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "last colors";
	}

}
