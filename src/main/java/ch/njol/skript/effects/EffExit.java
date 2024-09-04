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
import ch.njol.skript.lang.SectionExitHandler;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.sections.SecConditional;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Exit")
@Description("Exits a given amount of loops and conditionals, or the entire trigger.")
@Examples({
	"if player has any ore:",
		"\tstop",
	"message \"%player% has no ores!\"",
	"loop blocks above the player:",
		"\tloop-block is not air:",
			"\t\texit 2 sections",
		"\tset loop-block to water"
})
@Since("<i>unknown</i> (before 2.1)")
public class EffExit extends Effect { // TODO [code style] warn user about code after a stop effect

	static {
		Skript.registerEffect(EffExit.class,
				"(exit|stop) [trigger]",
				"(exit|stop) [(1|a|the|this)] (section|1:loop|2:conditional)",
				"(exit|stop) <\\d+> (section|1:loop|2:conditional)s",
				"(exit|stop) all (section|1:loop|2:conditional)s");
	}
	
	private int breakLevels;
	
	private static final int EVERYTHING = 0;
	private static final int LOOPS = 1;
	private static final int CONDITIONALS = 2;
	private static final String[] names = {"sections", "loops", "conditionals"};
	private int type;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		switch (matchedPattern) {
			case 0:
				breakLevels = getParser().getCurrentSections().size() + 1;
				type = EVERYTHING;
				break;
			case 1:
			case 2:
				breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parser.regexes.get(0).group());
				type = parser.mark;
				if (breakLevels > numLevels(type)) {
					if (numLevels(type) == 0)
						Skript.error("can't stop any " + names[type] + " as there are no " + names[type] + " present", ErrorQuality.SEMANTIC_ERROR);
					else
						Skript.error("can't stop " + breakLevels + " " + names[type] + " as there are only " + numLevels(type) + " " + names[type] + " present", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
				break;
			case 3:
				type = parser.mark;
				breakLevels = numLevels(type);
				if (breakLevels == 0) {
					Skript.error("can't stop any " + names[type] + " as there are no " + names[type] + " present", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
				break;
		}
		return true;
	}
	
	private static int numLevels(int type) {
		List<TriggerSection> currentSections = ParserInstance.get().getCurrentSections();
		if (type == EVERYTHING)
			return currentSections.size();
		int level = 0;
		for (TriggerSection section : currentSections) {
			if (type == CONDITIONALS ? section instanceof SecConditional : section instanceof LoopSection)
				level++;
		}
		return level;
	}
	
	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		debug(event, false);
		TriggerItem node = this;
		for (int i = breakLevels; i > 0;) {
			node = node.getParent();
			if (node == null) {
				assert false : this;
				return null;
			}
			if (node instanceof SectionExitHandler)
				((SectionExitHandler) node).exit(event);

			if (type == EVERYTHING || type == CONDITIONALS && node instanceof SecConditional || type == LOOPS && (node instanceof LoopSection))
				i--;
		}
		return node instanceof LoopSection ? ((LoopSection) node).getActualNext() : node.getNext();
	}
	
	@Override
	protected void execute(Event event) {
		assert false;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "stop " + breakLevels + " " + names[type];
	}
	
}
