package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Script Name")
@Description("Holds the current script's name (the file name without '.sk').")
@Examples({
	"on script load:",
	"\tset {running::%script%} to true",
	"on script unload:",
	"\tset {running::%script%} to false"
})
@Since("2.0")
@Events("Script Load/Unload")
public class ExprScript extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprScript.class, String.class, ExpressionType.SIMPLE,
			"[the] script[['s] name]",
			"name of [the] script"
		);
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private String name;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ParserInstance parser = getParser();
		if (!parser.isActive()) {
			Skript.error("You can't use the script expression outside of scripts!");
			return false;
		}
		String name = parser.getCurrentScript().getConfig().getFileName();
		if (name.contains("."))
			name = name.substring(0, name.lastIndexOf('.'));
		this.name = name;
		return true;
	}
	
	@Override
	protected String[] get(Event event) {
		return new String[]{name};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the script's name";
	}
	
}
