package ch.njol.skript.sections;

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
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Name("Function Section")
@Description("""
        Runs a function with the specified arguments.
        """)
@Example("""
	local function multiply(x: number, y: number) returns number:
		return {_x} * {_y}
	
	set {_x} to function multiply with arguments:
		x as 2
		y as 3
	
	broadcast "%{_x}%" # returns 6
	""")
@Since("INSERT VERSION")
public class ExprSecFunction extends SectionExpression<Object> {

    /**
     * The pattern for a valid function name.
     * Functions must start with a letter or underscore and can only contain letters, numbers, and underscores.
     */
    private final static Pattern FUNCTION_NAME_PATTERN = Pattern.compile("[A-z_][A-z_0-9]*");

    /**
     * The pattern for an argument that can be passed in the children of this section.
     */
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("(?:(?:the )?argument )?(?<name>%s) set to (?<value>.+)".formatted(FUNCTION_NAME_PATTERN.toString()));

    static {
        Skript.registerExpression(ExprSecFunction.class, Object.class, ExpressionType.SIMPLE, "[the] function <.+> with [the] arg[ument][s]");
    }

    private Function<?> function;
    private LinkedHashMap<String, Expression<?>> arguments = null;

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result,
                        @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		assert node != null;

        if (node.isEmpty()) {
            Skript.error("A function section must contain arguments.");
            return false;
        }

        LinkedHashMap<String, String> args = new LinkedHashMap<>();
        for (Node n : node) {
            if (!(n instanceof SimpleNode) || n.getKey() == null) {
                Skript.error("Invalid argument declaration for a function section: ", n.getKey());
                return false;
            }

            Matcher matcher = ARGUMENT_PATTERN.matcher(n.getKey());
            if (!matcher.matches()) {
                Skript.error("Invalid argument declaration for a function section: ", n.getKey());
                return false;
            }

            args.put(matcher.group("name"), matcher.group("value"));
        }

        String namespace = ParserInstance.get().getCurrentScript().getConfig().getFileName();
        String name = result.regexes.get(0).group();

        if (!FUNCTION_NAME_PATTERN.matcher(name).matches()) {
            Skript.error("The function %s does not exist.", name);
            return false;
        }

        // todo use FunctionParser
        function = findFunction(namespace, name, args);

        if (function == null || arguments == null || arguments.isEmpty()) {
            doesNotExist(name, args);
            return false;
        }

		if (function.getReturnType() == null) {
			Skript.error("The function %s does not return anything.", name);
			return false;
		}

        return true;
    }

    /**
     * Attempts to find the function to execute given the arguments.
     *
     * @param namespace The current script.
     * @param name The name of the function.
     * @param args The passed arguments.
     * @return The function given the arguments, or null if no function is found.
     */
    private Function<?> findFunction(String namespace, String name, LinkedHashMap<String, String> args) {
        signatures:
        for (Signature<?> signature : FunctionRegistry.getRegistry().getSignatures(namespace, name)) {
            LinkedHashMap<String, Expression<?>> arguments = new LinkedHashMap<>();

            LinkedHashMap<String, Parameter<?>> parameters = Arrays.stream(signature.getParameters())
                    .collect(Collectors.toMap(Parameter::getName, p -> p, (a, b) -> b, LinkedHashMap::new));
            for (Entry<String, String> entry : args.entrySet()) {
                Parameter<?> parameter = parameters.get(entry.getKey());

                if (parameter == null) {
                    continue signatures;
                }

                //noinspection unchecked
                Expression<?> expression = new SkriptParser(entry.getValue(), SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                        .parseExpression(parameter.getType().getC());

                if (expression == null || LiteralUtils.hasUnparsedLiteral(expression)) {
                    continue signatures;
                }

                arguments.put(entry.getKey(), expression);
            }

            Class<?>[] signatureArgs = Arrays.stream(signature.getParameters())
                    .map(it -> {
                        if (it.isSingleValue()) {
                            return it.getType().getC();
                        } else {
                            return it.getType().getC().arrayType();
                        }
                    })
                    .toArray(Class<?>[]::new);

            Retrieval<Function<?>> retrieval = FunctionRegistry.getRegistry().getFunction(namespace, name, signatureArgs);
            if (retrieval.result() == RetrievalResult.EXACT) {
                this.arguments = arguments;
                return retrieval.retrieved();
            }
        }

        return null;
    }

    /**
     * Prints the error for when a function does not exist.
     *
     * @param name      The function name.
     * @param arguments The passed arguments to the function call.
     */
    private void doesNotExist(String name, LinkedHashMap<String, String> arguments) {
        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            SkriptParser parser = new SkriptParser(entry.getValue(), SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);

            Expression<?> expression = LiteralUtils.defendExpression(parser.parseExpression(Object.class));

            if (expression == null || LiteralUtils.hasUnparsedLiteral(expression)) {
                joiner.add(entry.getKey() + ": ?");
                continue;
            }

            if (expression.isSingle()) {
                joiner.add(entry.getKey() + ": " + Classes.getSuperClassInfo(expression.getReturnType()).getName().getSingular());
            } else {
                joiner.add(entry.getKey() + ": " + Classes.getSuperClassInfo(expression.getReturnType()).getName().getPlural());
            }
        }

        Skript.error("The function %s(%s) does not exist.", name, joiner);
    }

    @Override
    protected Object @Nullable [] get(Event event) {
		if (function == null) {
			return null;
		}

        Object[][] args = new Object[function.getParameters().length][];
        int i = 0;
        for (Parameter<?> value : function.getParameters()) {
			Expression<?> expression = arguments.get(value.getName());

			if (expression == null) {
				return null;
			}

			args[i] = expression.getArray(event);
            i++;
        }

        try {
			return function.execute(args);
		} finally {
			function.resetReturnValue();
		}
    }

    @Override
    public boolean isSingle() {
        return function.isSingle();
    }

    @Override
    public boolean isSectionOnly() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return function.getReturnType() != null ? function.getReturnType().getC() : null;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return new SyntaxStringBuilder(event, debug)
                .append("run function")
                .append(function.getName())
                .append("with arguments")
                .toString();
    }

}
