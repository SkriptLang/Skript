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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Invisible")
@Description({
	"Makes a living entity or a boss bar visible/invisible. This is not a potion and therefore does not have features such as a time limit or particles.",
	"When setting an entity to invisible while using an invisibility potion on it, the potion will be overridden and when it runs out the entity keeps its invisibility."
})
@Examples("make target entity invisible")
@Since("2.7")
public class EffInvisible extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible"))
			Skript.registerEffect(EffInvisible.class,
				"make %livingentities/bossbars% (invisible|not visible)",
				"make %livingentities/bossbars% (visible|not invisible)");
	}

	private boolean invisible;
	private Expression<?> targets;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.targets = (Expression<Object>) exprs[0];
		this.invisible = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object target : targets.getArray(event)) {
			if (target instanceof LivingEntity entity) {
				entity.setInvisible(invisible);
			} else if (target instanceof BossBar bar) {
				bar.setVisible(!invisible);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + targets.toString(event, debug) + " " + (invisible ? "in" : "") + "visible";
	}

}
