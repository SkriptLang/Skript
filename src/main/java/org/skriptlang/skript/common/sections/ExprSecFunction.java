package org.skriptlang.skript.common.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.common.function.FunctionReference.Argument;
import org.skriptlang.skript.common.function.FunctionReference.ArgumentType;
import org.skriptlang.skript.common.function.FunctionReferenceParser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Function Section")
@Description("""
	Runs a function with the specified arguments.
	""")
@Example("""
	local function multiply(x: number, y: number) returns number:
		return {_x} * {_y}
	
	set {_x} to result of function multiply with arguments:
		x set to 2
		y set to 3
	
	broadcast "%{_x}%" # returns 6
	""")
@Since("INSERT VERSION")
public class ExprSecFunction extends SectionExpression<Object> {

	/**
	 * The pattern for a valid function name.
	 * Functions must start with a letter or underscore and can only contain letters, numbers, and underscores.
	 */
	private final static Pattern FUNCTION_NAME_PATTERN = Pattern.compile(Functions.functionNamePattern);

	/**
	 * The pattern for an argument that can be passed in the children of this section.
	 */
	private static final Pattern ARGUMENT_PATTERN = Pattern.compile("(?:argument )?(?<name>%s) set to (?<value>.+)".formatted(FUNCTION_NAME_PATTERN.toString()));

	static {
		Skript.registerExpression(ExprSecFunction.class, Object.class, ExpressionType.SIMPLE,
				"[the] result[s] of [running|executing] function <.+> with [the] arg[ument][s]");
	}

	private FunctionReference<?> reference;
	private final List<Argument<String>> arguments = new ArrayList<>();

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result,
						@Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		assert node != null;

		if (node.isEmpty()) {
			Skript.error("A function section must contain arguments.");
			return false;
		}

		for (Node child : node) {
			if (!(child instanceof SimpleNode) || child.getKey() == null) {
				Skript.error(Language.get("functions.invalid argument in section"), child.getKey());
				return false;
			}

			Matcher matcher = ARGUMENT_PATTERN.matcher(child.getKey());
			if (!matcher.matches()) {
				Skript.error(Language.get("functions.invalid argument in section"), child.getKey());
				return false;
			}

			arguments.add(new Argument<>(ArgumentType.NAMED, matcher.group("name"), matcher.group("value")));
		}

		String name = result.regexes.getFirst().group();
		if (!FUNCTION_NAME_PATTERN.matcher(name).matches()) {
			Skript.error(Language.get("functions.does not exist"), name);
			return false;
		}

		FunctionReferenceParser parser = new FunctionReferenceParser(ParseContext.DEFAULT, SkriptParser.PARSE_EXPRESSIONS);
		//noinspection unchecked
		Argument<String>[] array = (Argument<String>[]) arguments.toArray(new Argument[0]);
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			reference = parser.parseFunctionReference(name, array, log);
		}

		if (reference == null || this.arguments.isEmpty()) {
			doesNotExist(name);
			return false;
		}

		if (reference.signature().returnType() == null) {
			Skript.error(Language.get("functions.does not return"), name);
			return false;
		}

		return true;
	}

	/**
	 * Prints the error for when a function does not exist.
	 *
	 * @param name      The function name.
	 */
	private void doesNotExist(String name) {
		StringJoiner joiner = new StringJoiner(", ");

		for (Argument<String> argument : arguments) {
			SkriptParser parser = new SkriptParser(argument.value(), SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);

			Expression<?> expression = LiteralUtils.defendExpression(parser.parseExpression(Object.class));
			if (!LiteralUtils.canInitSafely(expression)) {
				joiner.add(argument.name() + ": ?");
				continue;
			}

			Noun className = Classes.getSuperClassInfo(expression.getReturnType()).getName();
			if (expression.isSingle()) {
				joiner.add(argument.name() + ": " + className.getSingular());
			} else {
				joiner.add(argument.name() + ": " + className.getPlural());
			}
		}

		Skript.error(Language.get("functions.does not exist"), "%s(%s)".formatted(name, joiner));
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (reference == null) {
			return null;
		}

		Class<?> returnType = reference.signature().returnType();
		if (returnType == null) {
			return null;
		}

		Object result = reference.execute(event);
		if (result == null) {
			return null;
		}

		reference.function().resetReturnValue();

		if (result.getClass().isArray()) {
			return (Object[]) result;
		} else {
			return new Object[] { result };
		}
	}

	@Override
	public boolean isSingle() {
		return reference.isSingle();
	}

	@Override
	public boolean isSectionOnly() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return reference.signature().returnType() != null ? Utils.getComponentType(reference.signature().returnType()) : null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
				.append("run function")
				.append(reference.name());

		if (arguments.size() > 1) {
			builder.append("with arguments");
		} else {
			builder.append("with argument");
		}

		arguments.forEach(argument -> builder.append(argument.name() + ": " + argument.value() + ", "));

		return builder.toString();
	}

}
