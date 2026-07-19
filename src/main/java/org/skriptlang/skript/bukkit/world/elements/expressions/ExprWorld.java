package org.skriptlang.skript.bukkit.world.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("World")
@Description("The world of something.")
@Example("""
    if world is "world_nether":
        broadcast "We're in another dimension!"
    """)
@Example("teleport the player to the world's spawn")
@Example("set the weather in the player's world to rain")
@Example("set {_world} to world of event-chunk")
@Since("1.0")
public class ExprWorld extends PropertyExpression<Object, World> {

	// TODO: turn this into a type property
	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprWorld.class,
				World.class,
				"[the] world",
				"locations/entities/chunk",
				false
			)
				.supplier(ExprWorld::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(expressions[0]);
		return true;
	}

	@Override
	protected World[] get(Event event, Object[] source) {
		return get(source, object -> {
			// if getTime is not 0, we know:
			// - Not delayed
			// - In a PlayerTeleportEvent
			// - the source expr was the event-value
			// check the event anyway since it casts for us
			if (event instanceof PlayerTeleportEvent playerEvent) {
				if (getTime() == 1) {
					return playerEvent.getTo().getWorld();
				} else if (getTime() == -1) {
					return playerEvent.getFrom().getWorld();
				}
			}
			return switch (object) {
				case Entity entity -> entity.getWorld();
				case Location location -> location.getWorld();
				case Chunk chunk -> chunk.getWorld();
				default -> null;
			};
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && getExpr().canReturn(Location.class))
			return CollectionUtils.array(World.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;

		World world = (World) delta[0];

		// TODO: this is suspicious - test if it works in all cases + whether we should have a Location changer instead.
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Location location)
				location.setWorld(world);
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), PlayerTeleportEvent.class);
	}

	@Override
	public Class<World> getReturnType() {
		return World.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the world of" + getExpr().toString(event, debug);
	}

}
