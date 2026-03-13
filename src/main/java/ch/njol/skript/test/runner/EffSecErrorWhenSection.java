package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NoDoc
public class EffSecErrorWhenSection extends EffectSection {

	static {
		if (TestMode.ENABLED)
			Skript.registerSection(EffSecErrorWhenSection.class, "error [%-*string%] when using [a] section");
	}

	private @Nullable Literal<String> error;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		//noinspection unchecked
		error = expressions.length == 1 ? (Literal<String>) expressions[0] : null;
		if (sectionNode != null) {
			if (error != null)
				Skript.error(error.getSingle());
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("error");
		builder.appendIf(error != null, error);
		builder.append("when using a section");
		return builder.toString();
	}

}
