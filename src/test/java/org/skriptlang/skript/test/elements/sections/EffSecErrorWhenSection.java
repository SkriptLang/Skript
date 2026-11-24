package org.skriptlang.skript.test.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@NoDoc
public class EffSecErrorWhenSection extends EffectSection {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.SECTION, SyntaxInfo.simple(EffSecErrorWhenSection.class, EffSecErrorWhenSection::new,
			"error [%-*string%] when using [a] section [with %-object%]"));
	}

	private @Nullable Literal<String> error;
	private @Nullable Expression<?> with;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		//noinspection unchecked
		error = (Literal<String>) expressions[0];
		with = LiteralUtils.defendExpression(expressions[1]);
		if (sectionNode != null) {
			if (error != null)
				Skript.error(error.getSingle());
			return false;
		}
		return with == null || LiteralUtils.canInitSafely(with);
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
		builder.appendIf(with != null, "with", with);
		return builder.toString();
	}

}
