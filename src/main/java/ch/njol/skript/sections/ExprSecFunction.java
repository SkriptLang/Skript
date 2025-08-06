package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.FunctionRegistry.Retrieval;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Name("Function Section")
@Description("""
        Runs a function with the specified arguments.
        """)
public class ExprSecFunction extends SectionExpression<Object> {

    private static final String AMBIGUOUS_ERROR =
            "Skript cannot determine which function named '%s' to call. " +
                    "The following functions were matched: %s. " +
                    "Try clarifying the type of the arguments using the 'value within' expression.";

    /**
     * The pattern for a valid function name.
     * Functions must start with a letter or underscore and can only contain letters, numbers, and underscores.
     */
    private final static Pattern FUNCTION_NAME_PATTERN = Pattern.compile("[A-z_][A-z_0-9]*");

    /**
     * The pattern for an argument that can be passed in the children of this section.
     */
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("(?<name>%s) set to (?<value>.+)".formatted(FUNCTION_NAME_PATTERN.toString()));

    static {
        Skript.registerExpression(ExprSecFunction.class, Object.class, ExpressionType.SIMPLE, "function <.+> with argument[s]");
    }

    private Function<?> function;
    private final LinkedHashMap<String, Expression<?>> arguments = new LinkedHashMap<>();

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result,
                        @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
        if (node == null) {
            Skript.error("A section must follow this expression.");
            return false;
        } else if (node.isEmpty()) {
            Skript.error("A function section must contain code.");
            return false;
        }

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

            String parameterName = matcher.group("name");
            String value = matcher.group("value");

            Expression<?> expression = new SkriptParser(value, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                    .parseExpression(Object.class);

            if (expression == null) {
                Skript.error("Invalid argument in argument declaration for a function section: ", value);
                return false;
            }

            arguments.put(parameterName, expression);
        }

        String namespace = ParserInstance.get().getCurrentScript().getConfig().getFileName();
        String name = result.regexes.get(0).group();

        if (!FUNCTION_NAME_PATTERN.matcher(name).matches()) {
            Skript.error("The function %s() does not exist.".formatted(name));
            return false;
        }

        Class<?>[] types = arguments.values().stream().map(Expression::getReturnType).toArray(Class<?>[]::new);

        Retrieval<Function<?>> retrieval = FunctionRegistry.getRegistry().getFunction(namespace, name, types);
        if (retrieval.result() == RetrievalResult.NOT_REGISTERED) {
            Skript.error("The function %s() does not exist.".formatted(name));
            return false;
        } else if (retrieval.result() == RetrievalResult.AMBIGUOUS) {
            List<String> conflicts = new ArrayList<>();
            for (Class<?>[] classes : retrieval.conflictingArgs()) {
                conflicts.add("%s(%s)".formatted(name, Arrays.stream(classes)
                        .map(Classes::getExactClassInfo)
                        .filter(Objects::nonNull)
                        .map(ClassInfo::getCodeName)
                        .collect(Collectors.joining(", "))));
            }

            Skript.error(AMBIGUOUS_ERROR.formatted(name, StringUtils.join(conflicts, ",", " and ")));
            return false;
        }

        function = retrieval.retrieved();

        LinkedHashMap<String, Parameter<?>> parameters = Arrays.stream(function.getParameters()).collect(Collectors.toMap(
                Parameter::getName,
                p -> p,
                (a, b) -> b,
                LinkedHashMap::new
        ));

        for (String key : arguments.keySet()) {
            arguments.computeIfPresent(key, (s, expression) -> {
                Class<?> c = parameters.get(s).getType().getC();

                //noinspection unchecked
                return expression.getConvertedExpression(c);
            });
        }

        return true;
    }

    @Override
    protected Object @Nullable [] get(Event event) {
        Object[][] args = new Object[arguments.size()][];
        int i = 0;
        for (Expression<?> value : arguments.values()) {
            args[i] = value.getArray(event);
            i++;
        }

        return function.execute(args);
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
