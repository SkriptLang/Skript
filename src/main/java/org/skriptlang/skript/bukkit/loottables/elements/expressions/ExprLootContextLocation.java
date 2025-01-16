package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;

@Name("Loot Location of Loot Context")
@Description("Returns the loot location of a loot context.")
@Examples({
	"set {_player} to player",
	"set {_context} to a loot context at player:",
		"\tif {_player} is in \"world_nether\":",
			"\t\tset loot location to location of last spawned pig",
	"send loot location of {_context} to player"
})
@Since("2.10")
public class ExprLootContextLocation extends SimplePropertyExpression<LootContext, Location> {

	static {
		registerDefault(ExprLootContextLocation.class, Location.class, "loot[ing] [context] location", "lootcontexts");
	}

	@Override
	public Location convert(LootContext context) {
		return context.getLocation();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!getParser().isCurrentEvent(LootContextCreateEvent.class)) {
			Skript.error("You cannot set the loot context location of an existing loot context.");
			return null;
		}

		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		assert delta != null;
		createEvent.getContextWrapper().setLocation((Location) delta[0]);
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot location";
	}

}
