package ch.njol.skript.lang.util.loops;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

public record AbstractExpressionInitializer(
        Expression<?>[] vars,
        int matchedPattern,
        Kleenean isDelayed,
        SkriptParser.ParseResult parser
) { }
