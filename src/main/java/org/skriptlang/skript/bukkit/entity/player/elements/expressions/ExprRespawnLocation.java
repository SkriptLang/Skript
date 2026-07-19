package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.lang.EventRestrictedSyntax;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import io.papermc.paper.event.player.AbstractRespawnEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Respawn location")
@Description("The location that a player should respawn at. This is used within the respawn event.")
@Example("""
	on respawn:
		set respawn location to {example::spawn}
	""")
@Since("2.2-dev35")
public class ExprRespawnLocation extends SimpleExpression<Location> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, DefaultSyntaxInfos.Expression.builder(ExprRespawnLocation.class, Location.class)
			.supplier(ExprRespawnLocation::new)
			.addPattern("[the] respawn location")
			.build());
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Location @Nullable [] get(Event event) {
		if (!(event instanceof AbstractRespawnEvent respawnEvent)) {
			return null;
		}

		return CollectionUtils.array(respawnEvent.getRespawnLocation());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.SET)
			return null;

		if (getParser().isCurrentEvent(PlayerPostRespawnEvent.class)) {
			Skript.error("The respawn location cannot be changed after the player has respawned.");
			return null;
		}

		return CollectionUtils.array(Location.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null || (!(event instanceof PlayerRespawnEvent respawnEvent)))
			return;

		Location respawnLocation = (Location) delta[0];
		if (respawnLocation == null)
			return;

		respawnEvent.setRespawnLocation(respawnLocation);
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
		return "the respawn location " + ((event != null) ? ": " + ((AbstractRespawnEvent) event).getRespawnLocation() : "");
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(AbstractRespawnEvent.class);
	}

}
