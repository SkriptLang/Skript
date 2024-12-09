package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
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
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

@Name("Loot Location of Loot Context")
@Description("Returns the loot context location of a loot context.")
@Examples({
	"set {_player} to player",
	"set {_context} to a loot context at player:",
		"\tif {_player} is in \"world_nether\":",
			"\t\tset loot context location to location of last spawned pig",
	"send loot context location of {_context} to player"
})
@Since("INSERT VERSION")
public class ExprLootContextLocation extends SimplePropertyExpression<LootContext, Location> {

	static {
		registerDefault(ExprLootContextLocation.class, Location.class, "loot [context] location", "lootcontexts");
	}

	@Override
	public @Nullable Location convert(LootContext context) {
		return context.getLocation();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (!getParser().isCurrentEvent(LootContextCreateEvent.class))
			Skript.error("You cannot set the loot context location of an existing loot context.");
		else if (mode == Changer.ChangeMode.SET)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		if (!(event instanceof LootContextCreateEvent createEvent))
			return;

		LootContextWrapper wrapper = createEvent.getContextWrapper();
		wrapper.setLocation((Location) delta[0]);
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
