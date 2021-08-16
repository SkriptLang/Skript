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

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;

@Name("Experience")
@Description("How much experience was spawned in an experience spawn or block break event. Can be changed.")
@Examples({"on experience spawn:",
		"\tadd 5 to the spawned experience",
		"on break of coal ore:",
		"\tclear dropped experience",
		"on break of diamond ore:",
		"\tif tool of player = diamond pickaxe:",
		"\t\tadd 100 to dropped experience",
		"on fishing:",
		"\tadd 70 to dropped experience"})
@Since("2.1, 2.5.3 (block break event)")
@Events({"experience spawn", "break / mine", "fish"})
public class ExprExperience extends SimpleExpression<Experience> {
	static {
		Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE, "[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ExperienceSpawnEvent.class, BlockBreakEvent.class, PlayerFishEvent.class)) {
			Skript.error("The experience expression can only be used in experience spawn, block break and fishing events");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Experience[] get(Event e) {
		if (e instanceof ExperienceSpawnEvent)
			return new Experience[] {new Experience(((ExperienceSpawnEvent) e).getSpawnedXP())};
		else if (e instanceof BlockBreakEvent)
			return new Experience[] {new Experience(((BlockBreakEvent) e).getExpToDrop())};
		else if (e instanceof PlayerFishEvent)
			return new Experience[] {new Experience(((PlayerFishEvent) e).getExpToDrop())};
		else
			return new Experience[0];
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				return new Class[] {Experience[].class, Number[].class};
			case SET:
				return new Class[] {Experience.class, Number.class};
			case RESET:
				return null;
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int d;
		if (e instanceof ExperienceSpawnEvent)
			d = ((ExperienceSpawnEvent) e).getSpawnedXP();
		else if (e instanceof BlockBreakEvent)
			d = ((BlockBreakEvent) e).getExpToDrop();
		else if (e instanceof PlayerFishEvent)
			d = ((PlayerFishEvent) e).getExpToDrop();
		else
			return;
		
		if (delta != null)
			for (Object o : delta) {
				int v = o instanceof Experience ? ((Experience) o).getXP() : ((Number) o).intValue();
				switch (mode) {
					case ADD:
						d += v;
						break;
					case SET:
						d = v;
						break;
					case REMOVE:
					case REMOVE_ALL:
						d -= v;
						break;
					case RESET:
					case DELETE:
						assert false;
						break;
				}
			}
		else
			d = 0;
		
		d = Math.max(0, Math.round(d));
		if (e instanceof ExperienceSpawnEvent)
			((ExperienceSpawnEvent) e).setSpawnedXP(d);
		else if (e instanceof PlayerFishEvent)
			((PlayerFishEvent) e).setExpToDrop(d);
		else
			((BlockBreakEvent) e).setExpToDrop(d);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Experience> getReturnType() {
		return Experience.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the experience";
	}
	
}
