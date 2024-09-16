/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.SpawnChangeEvent;
import org.jetbrains.annotations.Nullable;

@Name("Spawn")
@Description("The spawn point of a world.")
@Examples({
	"teleport all players to spawn",
	"set the spawn point of \"world\" to the player's location"
})
@Since("1.4.2")
public class ExprSpawn extends PropertyExpression<World, Location> {

	static {
		Skript.registerExpression(ExprSpawn.class, Location.class, ExpressionType.PROPERTY,
				"[the] spawn[s] [(point|location)[s]] [of %worlds%]",
				"%worlds%'[s] spawn[s] [(point|location)[s]]"
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends World>) exprs[0]);
		return true;
	}

	@Override
	protected Location[] get(Event event, World[] source) {
		if (getTime() == -1 && event instanceof SpawnChangeEvent && !Delay.isDelayed(event))
			return new Location[] {((SpawnChangeEvent) event).getPreviousLocation()};
		return get(source, World::getSpawnLocation);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		//noinspection ConstantConditions
		if (delta == null)
			return;

		Location originalLocation = (Location) delta[0];
		assert originalLocation != null;
		for (World world : getExpr().getArray(event)) {
			Location location = originalLocation.clone();
			World locationWorld = location.getWorld();
			if (locationWorld == null) {
				location.setWorld(world);
				world.setSpawnLocation(location);
			} else if (locationWorld.equals(world)) {
				world.setSpawnLocation(location);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
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
