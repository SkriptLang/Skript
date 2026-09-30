package org.skriptlang.skript.common.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Locally Suppress Warning")
@Description("Suppresses target warnings from the current script.")
@Example("locally suppress missing conjunction warnings")
@Example("suppress the variable save warnings")
@Since({"2.3", "INSERT VERSION (suppressing in a section)"})
public class EffSecSuppressWarnings extends EffectSection {

	public static void register(SyntaxRegistry syntaxRegistry) {
		StringBuilder warnings = new StringBuilder();
		ScriptWarning[] values = ScriptWarning.values();
		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				warnings.append('|');
			}
			warnings.append(values[i].ordinal())
				.append(':')
				.append(values[i].getPattern());
		}
		syntaxRegistry.register(SyntaxRegistry.SECTION, SyntaxInfo.simple(EffSecSuppressWarnings.class, EffSecSuppressWarnings::new,
			"[local[ly]] [stop:un]suppress [the] (" + warnings + ") warning[s]",
			"[start|:stop] [local[ly]] suppressing [the] (" + warnings + ") warning[s]"));
	}

	private ScriptWarning warning;
	private boolean stop;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
						@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		ParserInstance parser = getParser();
		if (!parser.isActive()) {
			Skript.error("You can't suppress warnings outside of a script!");
			return false;
		}

		warning = ScriptWarning.values()[parseResult.mark];
		stop = parseResult.hasTag("stop");
		if (warning.isDeprecated()) {
			Skript.warning(warning.getDeprecationMessage());
		}

		Script script = parser.getCurrentScript();
		boolean wasSuppressed = script.suppressesWarning(warning);
		if (stop) {
			script.allowWarning(warning);
		} else {
			script.suppressWarning(warning);
		}
		if (sectionNode != null) {
			loadCode(sectionNode);
			if (wasSuppressed) {
				script.suppressWarning(warning);
			} else {
				script.allowWarning(warning);
			}
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return walk(event, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (stop ? "un" : "") + "suppress " + warning.getWarningName() + " warnings";
	}

}
