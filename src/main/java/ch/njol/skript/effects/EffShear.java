/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Shear")
@Description("Shears or 'un-shears' a sheep. Please note that no wool is dropped, this only sets the 'sheared' state of the sheep.")
@Examples({"on rightclick on a sheep holding a sword:",
	"	shear the clicked sheep"})
@Since("2.0")
public class EffShear extends Effect {
	static {
		Skript.registerEffect(EffShear.class,
			"shear %livingentities%",
			"un[-]shear %livingentities%");
	}

	@SuppressWarnings("null")
	private Expression<LivingEntity> sheep;
	private boolean shear;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		sheep = (Expression<LivingEntity>) exprs[0];
		shear = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(final Event e) {
		for (final LivingEntity en : sheep.getArray(e)) {
			if (en instanceof Sheep) {
				((Sheep) en).setSheared(shear);
			}
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (shear ? "" : "un") + "shear " + sheep.toString(e, debug);
	}

}
