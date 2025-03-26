package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * To be used in sections that delay the execution of their code through a {@link Trigger}.
 * @see Section#loadCode(SectionNode, String, Class[])
 */
public class SectionSkriptEvent extends SkriptEvent {

	private final String name;
	private final Section section;

	public SectionSkriptEvent(String name, Section section) {
		this.name = name;
		this.section = section;
	}

	public Section getSection() {
		return section;
	}

	public final boolean isSection(Class<? extends Section> section) {
		return section.isInstance(this.section);
	}

	@SafeVarargs
	public final boolean isSection(Class<? extends Section>... sections) {
		for (Class<? extends Section> section : sections) {
			if (isSection(section))
				return true;
		}
		return false;
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		throw new SkriptAPIException("init should never be called for a SectionSkriptEvent.");
	}

	@Override
	public boolean check(Event event) {
		throw new SkriptAPIException("check should never be called for a SectionSkriptEvent.");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return name;
	}

}
