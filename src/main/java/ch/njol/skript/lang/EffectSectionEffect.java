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
package ch.njol.skript.lang;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EffectSectionEffect extends Effect {

	private final EffectSection effectSection;

	public EffectSectionEffect(EffectSection effectSection) {
		this.effectSection = effectSection;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return effectSection.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	protected void execute(Event e) { }

	@Override
	protected @Nullable TriggerItem walk(Event e) {
		return effectSection.walk(e);
	}

	@Override
	public String getIndentation() {
		return effectSection.getIndentation();
	}

	@Override
	public TriggerItem setParent(@Nullable TriggerSection parent) {
		return effectSection.setParent(parent);
	}

	@Override
	public TriggerItem setNext(@Nullable TriggerItem next) {
		return effectSection.setNext(next);
	}

	@Override
	public @Nullable TriggerItem getNext() {
		return effectSection.getNext();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return effectSection.toString(e, debug);
	}

}
