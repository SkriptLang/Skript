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
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.SecConditional;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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
public class EffExit extends Effect {

	static {
		Skript.registerEffect(EffExit.class,
			"(exit|stop) [trigger]",
			"(exit|stop) [(1|a|the|this)] (section|1:loop|2:conditional)",
			"(exit|stop) <[1-9][0-9]*> (section|1:loop|2:conditional)s",
			"(exit|stop) all (section|1:loop|2:conditional)s");
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends TriggerSection>[] types = new Class[]{TriggerSection.class, LoopSection.class, SecConditional.class};
	private static final String[] names = {"sections", "loops", "conditionals"};
	private int type;

	private int breakLevels;
	private @UnknownNullability List<TriggerSection> sectionsToExit;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		switch (matchedPattern) {
			case 0 -> {
				sectionsToExit = getParser().getCurrentSections();
				breakLevels = sectionsToExit.size() + 1;
			}
			case 1, 2 -> {
				breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parser.regexes.get(0).group());
				type = parser.mark;
				sectionsToExit = Section.getSections(breakLevels, types[type]);
				int levels = getParser().getCurrentSections(types[type]).size();
				if (breakLevels > levels) {
					if (levels == 0) {
						Skript.error("Can't stop any " + names[type] + " as there are no " + names[type] + " present");
					} else {
						Skript.error("Can't stop " + breakLevels + " " + names[type] + " as there are only " + levels + " " + names[type] + " present");
					}
					return false;
				}
			}
			case 3 -> {
				type = parser.mark;
				List<? extends TriggerSection> sections = getParser().getCurrentSections(types[type]);
				breakLevels = sections.size();
				if (sections.isEmpty()) {
					Skript.error("Can't stop any " + names[type] + " as there are no " + names[type] + " present");
					return false;
				}
				TriggerSection firstSection = sections.get(0);
				sectionsToExit = Section.getSectionsUntil(firstSection);
				sectionsToExit.add(0, firstSection);
			}
		}
		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		debug(event, false);
		for (TriggerSection section : sectionsToExit) {
			if (section instanceof SectionExitHandler exitHandler)
				exitHandler.exit(event);
		}
		if (breakLevels > sectionsToExit.size())
			return null;
		TriggerSection section = sectionsToExit.get(0);
		return section instanceof LoopSection loopSection ? loopSection.getActualNext() : section.getNext();
	}

	@Override
	protected void execute(Event event) {
		assert false;
	}

	@Override
	public @Nullable ExecutionIntent executionIntent() {
		return ExecutionIntent.stopSections(breakLevels);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "stop " + breakLevels + " " + names[type];
	}

}
