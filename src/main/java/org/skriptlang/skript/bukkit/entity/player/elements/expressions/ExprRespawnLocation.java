package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.doc.Events;
import ch.njol.skript.lang.EventRestrictedSyntax;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Respawn Location")
@Description("""
	The location that a player should respawn at. \
	This is used within the respawn event.
	""")
@Example("""
	on respawn:
		set respawn location to {example::spawn}
	""")
@Since("2.2-dev35")
@Events("respawn")
public class ExprRespawnLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprRespawnLocation.class, ExprRespawnLocation::new, Location.class,
				"[the] respawn location"));
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerRespawnEvent.class);
	}

	@Override
	protected Location[] get(Event event) {
		if (event instanceof PlayerRespawnEvent respawnEvent) {
			return new Location[]{respawnEvent.getRespawnLocation()};
		}
		return new Location[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return CollectionUtils.array(Location.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (event instanceof PlayerRespawnEvent respawnEvent) {
			assert delta != null;
			Location location = (Location) delta[0];
			if (location.getWorld() == null) { // if no world was provided, use the default world
				location = location.clone();
				location.setWorld(Bukkit.getWorlds().getFirst());
			}
			respawnEvent.setRespawnLocation(location);
		}
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
		return "the respawn location";
	}

}
