package ch.njol.skript.lang;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the Effect aspect of an EffectSection. This allows for the use of EffectSections as effects, rather than just sections.
 */
public class EffectSectionEffect extends Effect {

	private final EffectSection effectSection;

	public EffectSectionEffect(EffectSection effectSection) {
		this.effectSection = effectSection;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return effectSection.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	protected void execute(Event event) { }

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return effectSection.walk(event);
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
	public String toString(@Nullable Event event, boolean debug) {
		return effectSection.toString(event, debug);
	}

}
