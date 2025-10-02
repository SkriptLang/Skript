package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("An Eternity")
@Description({
	"Represents a timespan with an infinite duration. " +
	"An eternity is also created when arithmetic results in a timespan larger than about 292 million years.",
	"Infinite timespans generally follow the rules of infinity, where most math operations do nothing. " +
	"However, operations that would return NaN with numbers will instead return a timespan of 0 seconds.",
	"Note that an eternity will often be treated as the longest duration something supports, rather than a true eternity."
})
@Example("set fire to the player for an eternity")
@Since("2.12")
public class LitEternity extends SimpleLiteral<Timespan> {

	static {
		Skript.registerExpression(LitEternity.class, Timespan.class, ExpressionType.SIMPLE,
				"[an] eternity",
				"forever",
				"[an] (indefinite|infinite) (duration|timespan)");
	}

	public LitEternity() {
		super(new Timespan[]{Timespan.infinite()}, Timespan.class, true);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "an eternity";
	}

}
