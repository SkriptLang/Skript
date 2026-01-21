package org.skriptlang.skript.bukkit.text.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.kyori.adventure.text.Component;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Colored/Formatted/Uncolored")
@Description("Parses or removes colors and, optionally, chat styles in/from a message.")
@Example("""
	on chat:
		set message to colored message # only safe tags, such as colors, will be parsed
	""")
@Example("""
	command /fade <player>:
		trigger:
			set the display name of the player-argument to the uncolored display name of the player-argument
	""")
@Example("""
	command /format <text>:
		trigger:
			message formatted text-argument # parses all tags, but this is okay as the output is sent back to the executor
	""")
@Since({
	"2.0",
	"INSERT VERSION ('uncolored' vs 'unformatted' distinction)"
})
public class ExprColored extends SimplePropertyExpression<String, Object> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprColored.class, Object.class)
			.supplier(ExprColored::new)
			.addPatterns("[negated:(un|non)[-]](colo[u]r-|colo[u]red )%strings%",
				"[negated:(un|non)[-]](format-|formatted )%strings%")
			.build());
	}

	private boolean isColor;
	private boolean isFormat;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isColor = !parseResult.hasTag("negated");
		isFormat = matchedPattern == 1;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Object convert(String string) {
		if (isColor) {
			return TextComponentParser.instance().parse(string, !isFormat);
		}
		return TextComponentParser.instance().stripFormatting(string, isFormat);
	}

	@Override
	public Class<?> getReturnType() {
		return isColor ? Component.class : String.class;
	}

	@Override
	protected String getPropertyName() {
		if (isColor) {
			return isFormat ? "formatted" : "colored";
		}
		return isFormat ? "unformatted" : "uncolored";
	}

	/**
	 * @deprecated This method is only available for compatibility purposes.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public boolean isUnsafeFormat() {
		return isColor && isFormat;
	}

}
