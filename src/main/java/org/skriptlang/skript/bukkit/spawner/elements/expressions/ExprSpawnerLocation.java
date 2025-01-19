package org.skriptlang.skript.bukkit.spawner.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
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
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawner Location")
@Description("The location of the spawner in the pre spawner spawn event.")
@Examples({
	"on pre spawner spawn:",
		"\tset {_location} to the location of the spawner"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.21+")
public class ExprSpawnerLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent")) {
			var info = SyntaxInfo.Expression.builder(ExprSpawnerLocation.class, Location.class)
				.origin(SyntaxOrigin.of(Skript.instance()))
				.supplier(ExprSpawnerLocation::new)
				.priority(SyntaxInfo.SIMPLE)
				.addPatterns("[the] spawner's location", "[the] location of [the] spawner")
				.build();

			SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
		}
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
		return new Location[]{((PreSpawnerSpawnEvent) event).getSpawnerLocation()};
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
