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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Silence Entity")
@Description("Controls whether or not an entity is silent.")
@Examples("make target entity silent")
@Since("2.5")
public class EffSilence extends Effect {
	
	static {
		Skript.registerEffect(EffSilence.class,
			"silence %entities%",
			"unsilence %entities%",
			"make %entities% silent",
			"make %entities% not silent");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	private boolean silence;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		silence = matchedPattern % 2 == 0;
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		for (Entity entity : entities.getArray(e)) {
			entity.setSilent(silence);
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (silence ? "silence " : "unsilence ") + entities.toString(e, debug);
	}
}
