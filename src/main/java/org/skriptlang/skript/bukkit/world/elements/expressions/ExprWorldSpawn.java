package org.skriptlang.skript.bukkit.world.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.SpawnChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("World Spawn")
@Description("The spawn point of a world.")
@Example("teleport all players to spawn")
@Example("set the spawn point of \"world\" to the player's location")
@Since("1.4.2")
public class ExprWorldSpawn extends PropertyExpression<World, Location> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprWorldSpawn.class,
				Location.class,
				"spawn[s] [(point|location)[s]]",
				"worlds",
				false
			)
				.supplier(ExprWorldSpawn::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends World>) expressions[0]);
		return true;
	}

	@Override
	protected Location[] get(Event event, World[] source) {
		if (getTime() == -1 && event instanceof SpawnChangeEvent worldEvent && !Delay.isDelayed(event))
			return new Location[]{worldEvent.getPreviousLocation()};
		return get(source, World::getSpawnLocation);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;

		Location originalLocation = (Location) delta[0];
		if (originalLocation == null)
			return;

		for (World world : getExpr().getArray(event)) {
			Location location = originalLocation.clone();
			World locationWorld = location.getWorld();

			if (locationWorld != null && !locationWorld.equals(world))
				continue;

			location.setWorld(world);
			world.setSpawnLocation(location);
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), SpawnChangeEvent.class);
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the spawn point of " + getExpr().toString(event, debug);
	}

}
