package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Test Location")
@Description("The location the testing is taking place at.")
@Examples({
	"test \"example\":",
		"\tspawn zombie at test location",
		"\tassert last spawned zombie's world is test world with \"zombie did not spawn in test world\""
})
@NoDoc
public class ExprTestLocation extends SimpleExpression<Location> {

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprTestLocation.class, Location.class, ExpressionType.SIMPLE,
					"[the] test(-| )location");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Location @Nullable [] get(Event event) {
		return new Location[]{SkriptJUnitTest.getTestLocation()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the test location";
	}

}
