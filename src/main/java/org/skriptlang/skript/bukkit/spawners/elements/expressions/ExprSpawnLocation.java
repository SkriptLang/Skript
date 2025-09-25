package org.skriptlang.skript.bukkit.spawners.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawn Location")
@Description("""
	Returns the spawned entity's spawn location in the pre-spawner-spawn event.
	""")
@Example("""
	on pre spawner spawn:
		broadcast the spawner entity's spawn location
	""")
@Since("INSERT VERSION")
public class ExprSpawnLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSpawnLocation.class, Location.class)
			.supplier(ExprSpawnLocation::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPatterns(
				"[the] spawn location of [the] spawner entity",
				"[the] spawner entity's spawn location")
			.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PreSpawnerSpawnEvent.class);
	}

	@Override
	protected Location @Nullable [] get(Event event) {
		if (!(event instanceof PreSpawnerSpawnEvent spawnEvent))
			return null;
		return new Location[]{spawnEvent.getSpawnLocation()};
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
		return "the entity's spawn location";
	}

}
