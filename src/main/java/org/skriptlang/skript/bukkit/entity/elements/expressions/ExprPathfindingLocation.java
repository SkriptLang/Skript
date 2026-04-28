package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pathfinding Target Location")
@Description("The location that the entity is pathfinding towards.")
@Example("""
	on pathfind:
		if the pathfinding target location is within radius 5 of {mylocation}:
		    broadcast "A mob tried to pathfind near a forbidden location!"
		    cancel event
	""")
@Since("INSERT VERSION")
@Events("pathfind")
public class ExprPathfindingLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPathfindingLocation.class, Location.class)
			.supplier(ExprPathfindingLocation::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[the] path[ ]finding target location")
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return new Class[]{EntityPathfindEvent.class};
	}

	@Override
	protected Location[] get(Event event) {
		if (!(event instanceof EntityPathfindEvent pathfindEvent))
			return null;
		Location location = pathfindEvent.getLoc();
		return new Location[]{location};
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
		return "the pathfinding location";
	}

}
