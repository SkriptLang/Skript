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
import org.jetbrains.annotations.Nullable;
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
	private boolean isComponent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isColor = !parseResult.hasTag("negated");
		isFormat = matchedPattern == 1;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Object convert(String string) {
		if (isComponent) {
			return TextComponentParser.instance().parse(string, !isFormat);
		}
		if (isColor) {
			return TextComponentParser.instance().toLegacyString(string, isFormat);
		}
		return TextComponentParser.instance().stripFormatting(string, isFormat);
	}

	@Override
	public Class<?> getReturnType() {
		return isComponent ? Component.class : String.class;
	}

	@Override
	protected String getPropertyName() {
		if (isColor && isFormat) {
			return "formatted";
		}
		if (isColor) {
			return "colored";
		}
		if (isFormat) {
			return "unformatted";
		}
		return "uncolored";
	}

	@Override
	@SafeVarargs
	public final @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (isColor) {
			for (Class<R> clazz : to) {
				if (Component.class.isAssignableFrom(clazz)) {
					ExprColored converted = new ExprColored();
					converted.setExpr(this.getExpr());
					converted.isColor = true;
					converted.isFormat = this.isFormat;
					converted.isComponent = true;
					//noinspection unchecked
					return (Expression<? extends R>) converted;
				}
			}
		}
		return super.getConvertedExpression(to);
	}

}
