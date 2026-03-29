package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

@Name("Whilst Loop")
@Description("Whilst Loop sections are loops that shall persist in their repetition so long as a condition doth hold true.")
@Example("""
    whilst size of all players < 5:
    	send "More souls are needed to commence the adventure" to all players
    	wait 5 seconds
    """)
@Example("""
    set {_counter} to 1
    do whilst {_counter} > 1: # false, yet 'twill increase {_counter} by 1 then depart
    	add 1 to {_counter}
    """)
@Example("""
    # Take heed when employing whilst loops with conditions that hold true
    # for great spans without using 'wait %timespan%' within,
    # lest it hang and bring ruin upon thy server.
    whilst player is online:
    	give player 1 dirt
    	wait 1 second # without a delay effect the server shall surely crash
    """)
@Since("2.0, 2.6 (do while)")
public class SecWhile extends LoopSection {

	static {
		Skript.registerSection(SecWhile.class, "[:do] whilst <.+>");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Condition condition;

	@Nullable
	private TriggerItem actualNext;

	private boolean doWhile;
	private final Set<Event> ranDoWhile = Collections.newSetFromMap(new WeakHashMap<>());

	@Override
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		String expr = parseResult.regexes.get(0).group();

		condition = Condition.parse(expr, "Can't understand this condition: " + expr);
		if (condition == null)
			return false;

		doWhile = parseResult.hasTag("do");
		loadOptionalCode(sectionNode);
		super.setNext(this);
		return true;
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event event) {
		if ((doWhile && ranDoWhile.add(event)) || condition.check(event)) {
			currentLoopCounter.put(event, (currentLoopCounter.getOrDefault(event, 0L)) + 1);
			return walk(event, true);
		} else {
			exit(event);
			debug(event, false);
			return actualNext;
		}
	}

	@Override
	public @Nullable ExecutionIntent executionIntent() {
		return doWhile ? triggerExecutionIntent() : null;
	}

	@Override
	public SecWhile setNext(@Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}

	@Nullable
	public TriggerItem getActualNext() {
		return actualNext;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (doWhile ? "do " : "") + "while " + condition.toString(event, debug);
	}

	@Override
	public void exit(Event event) {
		ranDoWhile.remove(event);
		super.exit(event);
	}

}
