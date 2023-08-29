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
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("Continue")
@Description("Immediately moves the (while) loop on to the next iteration.")
@Examples({
	"# Broadcast online moderators",
	"loop all players:",
		"\tif loop-value does not have permission \"moderator\":",
			"\t\tcontinue # filter out non moderators",
		"\tbroadcast \"%loop-player% is a moderator!\" # Only moderators get broadcast",
	" ",
	"# Game starting counter",
	"set {_counter} to 11",
	"while {_counter} > 0:",
		"\tremove 1 from {_counter}",
		"\twait a second",
		"\tif {_counter} != 1, 2, 3, 5 or 10:",
			"\t\tcontinue # only print when counter is 1, 2, 3, 5 or 10",
		"\tbroadcast \"Game starting in %{_counter}% second(s)\"",
})
@Since("2.2-dev37, 2.7 (while loops)")
public class EffContinue extends Effect {

	static {
		Skript.registerEffect(EffContinue.class, "continue [loop]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private LoopSection loop;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		List<LoopSection> currentLoops = getParser().getCurrentSections(LoopSection.class);
		
		if (currentLoops.isEmpty()) {
			Skript.error("The 'continue' effect may only be used in while and regular loops");
			return false;
		}
		
		loop = currentLoops.get(currentLoops.size() - 1);
		return true;
	}

	@Override
	protected void execute(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		return loop;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "continue";
	}

}
