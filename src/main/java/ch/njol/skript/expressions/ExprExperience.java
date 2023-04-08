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

import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
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

/**
 * @author Peter Güttinger
 */
@Name("Experience")
@Description("How much experience was spawned in an experience spawn or block break event. Can be changed.")
@Examples({
		"on experience spawn:",
		"\tadd 5 to the spawned experience",
		"",
		"on break of coal ore:",
		"\tclear dropped experience",
		"",
		"on break of diamond ore:",
		"\tif tool of player = diamond pickaxe:",
		"\t\tadd 100 to dropped experience",
		"",
		"on breed:",
		"\tbreeding father is a cow",
		"\tset dropped experience to 10"
})
@Since("2.1, 2.5.3 (block break event), 2.7 (experience change event), INSERT VERSION (breeding event)")
@Events({"experience spawn", "break / mine", "experience change", "entity breeding"})
public class ExprExperience extends SimpleExpression<Experience> {

	static {
		Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE, "[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ExperienceSpawnEvent.class, BlockBreakEvent.class, PlayerExpChangeEvent.class, EntityBreedEvent.class)) {
			Skript.error("The experience expression can only be used in experience spawn, block break, player experience change and entity breeding events");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Experience[] get(Event event) {
		if (event instanceof ExperienceSpawnEvent)
			return new Experience[] {new Experience(((ExperienceSpawnEvent) event).getSpawnedXP())};
		else if (event instanceof BlockBreakEvent)
			return new Experience[] {new Experience(((BlockBreakEvent) event).getExpToDrop())};
		else if (event instanceof PlayerExpChangeEvent)
			return new Experience[] {new Experience(((PlayerExpChangeEvent) event).getAmount())};
		else if (event instanceof EntityBreedEvent)
			return new Experience[] {new Experience(((EntityBreedEvent) event).getExperience())};
		else
			return new Experience[0];
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case DELETE:
			case RESET:
				return CollectionUtils.array(ExprExperience.class, Integer.class);
			case ADD:
			case REMOVE:
				return CollectionUtils.array(ExprExperience[].class, Integer[].class);
		}
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int eventExp;
		if (event instanceof ExperienceSpawnEvent) {
			eventExp = ((ExperienceSpawnEvent) event).getSpawnedXP();
		} else if (event instanceof BlockBreakEvent) {
			eventExp = ((BlockBreakEvent) event).getExpToDrop();
		} else if (event instanceof PlayerExpChangeEvent) {
			eventExp = ((PlayerExpChangeEvent) event).getAmount();
		} else if (event instanceof EntityBreedEvent) {
			eventExp = ((EntityBreedEvent) event).getExperience();
		} else {
			return;
		}

		if (delta == null) {
			eventExp = 0;
		} else {
			for (Object obj : delta) {
				int value = obj instanceof Experience ? ((Experience) obj).getXP() : (Integer) obj;
				switch (mode) {
					case ADD:
						eventExp += value;
						break;
					case SET:
						eventExp = value;
						break;
					case REMOVE:
						eventExp -= value;
						break;
				}
			}
		}

		eventExp = Math.max(0, eventExp);
		if (event instanceof ExperienceSpawnEvent) {
			((ExperienceSpawnEvent) event).setSpawnedXP(eventExp);
		} else if (event instanceof BlockBreakEvent) {
			((BlockBreakEvent) event).setExpToDrop(eventExp);
		} else if (event instanceof PlayerExpChangeEvent) {
			((PlayerExpChangeEvent) event).setAmount(eventExp);
		} else if (event instanceof EntityBreedEvent) {
			((EntityBreedEvent) event).setExperience(eventExp);
		}
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
	public String toString(@Nullable Event event, boolean debug) {
		return "the experience";
	}
	
}
