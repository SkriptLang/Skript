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
package ch.njol.skript.classes.data;

import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Timespan;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderEvent;

public final class PaperEventValues {
	
	public PaperEventValues() {}

	static {
		if (Skript.classExists("io.papermc.paper.event.world.border.WorldBorderEvent")) {
			EventValues.registerEventValue(WorldBorderEvent.class, WorldBorder.class, new Getter<WorldBorder, WorldBorderEvent>() {
				@Override
				@Nullable
				public WorldBorder get(WorldBorderEvent event) {
					return event.getWorldBorder();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(WorldBorderEvent.class, World.class, new Getter<World, WorldBorderEvent>() {
				@Override
				@Nullable
				public World get(WorldBorderEvent event) {
					return event.getWorld();
				}
			}, EventValues.TIME_NOW);
		}
		if (Skript.classExists("io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent")) {
			EventValues.registerEventValue(WorldBorderBoundsChangeEvent.class, Timespan.class, new Getter<Timespan, WorldBorderBoundsChangeEvent>() {
				@Override
				@Nullable
				public Timespan get(WorldBorderBoundsChangeEvent event) {
					return new Timespan(event.getDuration());
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(WorldBorderBoundsChangeEvent.class, Double.class, new Getter<Double, WorldBorderBoundsChangeEvent>() {
				@Override
				@Nullable
				public Double get(WorldBorderBoundsChangeEvent event) {
					return event.getOldSize();
				}
			}, EventValues.TIME_PAST);
			EventValues.registerEventValue(WorldBorderBoundsChangeEvent.class, Double.class, new Getter<Double, WorldBorderBoundsChangeEvent>() {
				@Override
				@Nullable
				public Double get(WorldBorderBoundsChangeEvent event) {
					return event.getNewSize();
				}
			}, EventValues.TIME_FUTURE);
			EventValues.registerEventValue(WorldBorderBoundsChangeEvent.class, Double.class, new Getter<Double, WorldBorderBoundsChangeEvent>() {
				@Override
				@Nullable
				public Double get(WorldBorderBoundsChangeEvent event) {
					return event.getNewSize();
				}
			}, EventValues.TIME_NOW);
		}
	}

}
