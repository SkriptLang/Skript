package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
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
			"(exit|stop) [1|a|the|this] (section|1:loop|2:conditional)",
			"(exit|stop) <" + JavaClasses.INTEGER_NUMBER_PATTERN + "> (section|1:loop|2:conditional)s",
			"(exit|stop) all (section|1:loop|2:conditional)s");
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends TriggerSection>[] types = new Class[]{TriggerSection.class, LoopSection.class, SecConditional.class};
	private static final String[] names = {"sections", "loops", "conditionals"};
	private int type;

	private int breakLevels;
	private TriggerSection outerSection;
	private @UnknownNullability List<SectionExitHandler> sectionsToExit;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		List<TriggerSection> innerSections = null;
		switch (matchedPattern) {
			case 0 -> {
				innerSections = getParser().getCurrentSections();
				breakLevels = innerSections.size() + 1;
			}
			case 1, 2 -> {
				breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parseResult.regexes.get(0).group());
				if (breakLevels < 1)
					return false;
				type = parseResult.mark;
				ParserInstance parser = getParser();
				int levels = parser.getCurrentSections(types[type]).size();
				if (breakLevels > levels) {
					if (levels == 0) {
						Skript.error("Can't stop any " + names[type] + " as there are no " + names[type] + " present");
					} else {
						Skript.error("Can't stop " + breakLevels + " " + names[type] + " as there are only " + levels + " " + names[type] + " present");
					}
					return false;
				}
				innerSections = parser.getSections(breakLevels, types[type]);
				outerSection = innerSections.get(0);
			}
			case 3 -> {
				ParserInstance parser = getParser();
				type = parseResult.mark;
				List<? extends TriggerSection> sections = parser.getCurrentSections(types[type]);
				if (sections.isEmpty()) {
					Skript.error("Can't stop any " + names[type] + " as there are no " + names[type] + " present");
					return false;
				}
				outerSection = sections.get(0);
				innerSections = parser.getSectionsUntil(outerSection);
				innerSections.add(0, outerSection);
				breakLevels = innerSections.size();
			}
		}
        assert innerSections != null;
		sectionsToExit = innerSections.stream()
			.filter(SectionExitHandler.class::isInstance)
			.map(SectionExitHandler.class::cast)
			.toList();
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, false);
		for (SectionExitHandler section : sectionsToExit)
			section.exit(event);
		if (outerSection == null)
			return null;
		return outerSection instanceof LoopSection loopSection ? loopSection.getActualNext() : outerSection.getNext();
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
