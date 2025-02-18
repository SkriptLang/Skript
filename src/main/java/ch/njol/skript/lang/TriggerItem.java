package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;

/**
 * Represents a trigger item, i.e. a trigger section, a condition or an effect.
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Trigger
 * @see Statement
 */
public abstract class TriggerItem implements Debuggable {

	protected @Nullable TriggerSection parent = null;
	private @Nullable TriggerItem next = null;

	protected TriggerItem() {}

	protected TriggerItem(TriggerSection parent) {
		this.parent = parent;
	}

	/**
	 * Executes this item and returns the next item to run.
	 * <p>
	 * Overriding classes must call {@link #debug(Event, boolean)}. If this method is overridden, {@link #run(Event)} is not used anymore and can be ignored.
	 * 
	 * @param event The event
	 * @return The next item to run or null to stop execution
	 */
	protected @Nullable TriggerItem walk(Event event) {
		if (run(event)) {
			debug(event, true);
			return next;
		} else {
			debug(event, false);
			TriggerSection parent = this.parent;
			return parent == null ? null : parent.getNext();
		}
	}

	/**
	 * Executes this item.
	 * 
	 * @param event The event to run this item with
	 * @return True if the next item should be run, or false for the item following this item's parent.
	 */
	protected abstract boolean run(Event event);

	/**
	 * @param start The item to start at
	 * @param event The event to run the items with
	 * @return false if an exception occurred
	 */
	public static boolean walk(TriggerItem start, Event event) {
		TriggerItem triggerItem = start;
		try {
			while (triggerItem != null)
				triggerItem = triggerItem.walk(event);

			return true;
		} catch (StackOverflowError err) {
			Trigger trigger = start.getTrigger();
			String scriptName = "<unknown>";
			if (trigger != null) {
				Script script = trigger.getScript();
				if (script != null) {
					File scriptFile = script.getConfig().getFile();
					if (scriptFile != null)
						scriptName = scriptFile.getName();
				}
			}
			Skript.adminBroadcast("<red>The script '<gold>" + scriptName + "<red>' infinitely (or excessively) repeated itself!");
			if (Skript.debug())
				err.printStackTrace();
		} catch (Exception ex) {
			if (ex.getStackTrace().length != 0) // empty exceptions have already been printed
				Skript.exception(ex, triggerItem);
		} catch (Throwable throwable) {
			// not all Throwables are Exceptions, but we usually don't want to catch them (without rethrowing)
			Skript.markErrored();
			throw throwable;
		}
		return false;
	}

	/**
	 * Returns whether this item stops the execution of the current trigger or section(s).
	 * <br>
	 * If present, and there are statement(s) after this one, the parser will print a warning
	 * to the user.
	 * <p>
	 * <b>Note: This method is used purely to print warnings and doesn't affect parsing, execution or anything else.</b>
	 *
	 * @return whether this item stops the execution of the current trigger or section.
	 */
	public @Nullable ExecutionIntent executionIntent() {
		return null;
	}

	/**
	 * how much to indent each level
	 */
	private final static String INDENT = "  ";

	private @Nullable String indentation = null;

	public String getIndentation() {
		if (indentation == null) {
			int level = 0;
			TriggerItem triggerItem = this;
			while ((triggerItem = triggerItem.parent) != null)
				level++;
			indentation = StringUtils.multiply(INDENT, level);
		}
		return indentation;
	}

	protected final void debug(Event event, boolean run) {
		if (!Skript.debug())
			return;
		Skript.debug(SkriptColor.replaceColorChar(getIndentation() + (run ? "" : "-") + toString(event, true)));
	}

	@Override
	public final String toString() {
		return toString(null, false);
	}

	public TriggerItem setParent(@Nullable TriggerSection parent) {
		this.parent = parent;
		return this;
	}

	public final @Nullable TriggerSection getParent() {
		return parent;
	}

	/**
	 * @return The trigger this item belongs to, or null if this is a stand-alone item (e.g. the effect of an effect command)
	 */
	public final @Nullable Trigger getTrigger() {
		TriggerItem triggerItem = this;
		while (triggerItem != null && !(triggerItem instanceof Trigger))
			triggerItem = triggerItem.getParent();
		return (Trigger) triggerItem;
	}

	public TriggerItem setNext(@Nullable TriggerItem next) {
		this.next = next;
		return this;
	}

	public @Nullable TriggerItem getNext() {
		return next;
	}

	/**
	 * This method guarantees to return next {@link TriggerItem} after this item.
	 * This is not always the case for {@link #getNext()}, for example, {@code getNext()}
	 * of a {@link ch.njol.skript.sections.SecLoop loop section} usually returns itself.
	 * 
	 * @return The next {@link TriggerItem}.
	 */
	public @Nullable TriggerItem getActualNext() {
		return next;
	}

}
