package org.skriptlang.skript.common.elements.effects;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Suppress Type Hints (Experimental)")
@Description("""
	An effect to suppress local variable type hint errors for the syntax lines that follow this effect.
	NOTE: Suppressing type hints also prevents syntax from providing new type hints. \
	For example, with type hints suppressed, 'set {_x} to true' would not provide 'boolean' as a type hint for '{_x}'
	""")
@Example("""
	start suppressing local variable type hints
	# potentially unsafe code goes here
	stop suppressing local variable type hints
	""")
@Since({"2.12", "INSERT VERSION (suppressing in a section)"})
public class EffSecSuppressTypeHints extends EffectSection implements SimpleExperimentalSyntax {

	private static final ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.TYPE_HINTS);

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.SECTION, SyntaxInfo.simple(EffSecSuppressTypeHints.class, EffSecSuppressTypeHints::new,
			"[stop:un]suppress [local variable] type hints",
			"(start|:stop) suppressing [local variable] type hints"));
	}

	private boolean stopSuppression;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
						@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		stopSuppression = parseResult.hasTag("stop");
		HintManager hintManager = getParser().getHintManager();
		boolean wasActive = hintManager.isActive();
		hintManager.setActive(stopSuppression);
		if (sectionNode != null) {
			loadCode(sectionNode);
			hintManager.setActive(wasActive);
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return walk(event, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (stopSuppression ? "stop" : "start") + " suppressing type hints";
	}

	@Override
	public ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
