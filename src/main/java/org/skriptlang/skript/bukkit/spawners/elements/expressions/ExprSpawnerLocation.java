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

@Name("Spawner Location")
@Description("""
	Returns the spawner's location in the pre-spawner-spawn event.
	""")
@Example("""
	on pre spawner spawn:
		broadcast the spawner's location
	""")
@Since("INSERT VERSION")
public class ExprSpawnerLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSpawnerLocation.class, Location.class)
			.supplier(ExprSpawnerLocation::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPatterns(
				"[the] spawner's location",
				"[the] location of [the] spawner")
			.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
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
		return new Location[]{spawnEvent.getSpawnerLocation()};
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
		return "the spawner's location";
	}

}
