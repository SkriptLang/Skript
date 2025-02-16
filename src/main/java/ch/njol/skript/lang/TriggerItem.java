package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.StringUtils;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.condition.Conditional;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

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

	/**
	 * Gets the first preceding element if it's instance of {@code tClass}
	 * Returns null if the first element is not instance of {@code tClass}
	 * @param tClass The class of the expected element
	 * @param triggerItems The trigger items to look through
	 * @return The expected element
	 * @param <T>
	 */
	public static <T extends SyntaxElement> @Nullable T getPrecedingElement(Class<T> tClass, List<TriggerItem> triggerItems) {
		return getPrecedingElement(tClass, triggerItems, null, null);
	}

	/**
	 * Get the first preceding element if it's instance of {@code tClass} and passes {@code checker}.
	 * Returns null if the first element is not instance of {@code tClass}.
	 * Returns null if the first element does not pass {@code checker}.
	 * @param tClass The class of the expected element.
	 * @param triggerItems The trigger items to look through.
	 * @param checker The {@link Predicate} to return the element if it passes or if null.
	 * @return The expected element.
	 * @param <T>
	 */
	public static <T extends SyntaxElement> @Nullable T getPrecedingElement(Class<T> tClass, List<TriggerItem> triggerItems, Predicate<T> checker) {
		return getPrecedingElement(tClass, triggerItems, checker, o -> !checker.test(o));
	}

	/**
	 * Get the first preceding element if it's instance of {@code tClass} and passes {@code checker}.
	 * Returns null if the first element is not instance of {@code tClass}.
	 * Returns expected element if {@code checker} is null, or it passes.
	 * Returns null if {@code stopper} is set and passes.
	 * @param tClass The class of the expected element.
	 * @param triggerItems The trigger items to look through.
	 * @param checker The {@link Predicate} to return the element if it passes or if null.
	 * @param stopper The {@link Predicate} to return null if the element passes.
	 * @return The expected element
	 * @param <T>
	 */
	public static <T extends SyntaxElement> @Nullable T getPrecedingElement(Class<T> tClass, List<TriggerItem> triggerItems, @Nullable Predicate<T> checker, @Nullable Predicate<T> stopper) {
		// loop through the triggerItems in reverse order so that we find the most recent items first
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (!tClass.isInstance(triggerItem))
				return null;
			//noinspection unchecked
			T element = (T) triggerItem;
			if (stopper != null && stopper.test(element))
				return null;
			if (checker == null || checker.test(element))
				return element;
		}
		return null;
	}

	/**
	 * Gets all consecutive preceding elements that are instance of {@code tClass}.
	 * @param tclass The class of the expected elements.
	 * @param triggerItems The trigger items to look through.
	 * @return {@link List} of expected elements.
	 * @param <T>
	 */
	public static <T extends SyntaxElement> List<T> getPrecedingElements(Class<T> tclass, List<TriggerItem> triggerItems) {
		return getPrecedingElements(tclass, triggerItems, null);
	}

	/**
	 * Gets all consecutive preceding elements that are instance of {@code tClass} and pass {@code checker}.
	 * If an element fails {@code checker} , the elements that have already passed are returned.
	 * @param tClass The class of the expected elements.
	 * @param triggerItems The trigger items to look through.
	 * @param checker The {@link Predicate} to check the elements.
	 * @return {@link List} of expected elements.
	 * @param <T>
	 */
	public static <T extends SyntaxElement> List<T> getPrecedingElements(Class<T> tClass, List<TriggerItem> triggerItems, @Nullable Predicate<T> checker)  {
		List<T> elementList = new ArrayList<>();
		// loop through the triggerItems in reverse order so that we find the most recent items first
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (!tClass.isInstance(triggerItem))
				break;
			//noinspection unchecked
			T element = (T) triggerItem;
			if (checker != null && !checker.test(element))
				break;
			elementList.add(element);
		}
		return elementList;
	}

	/**
	 * Check if the following {@link Node} , directly after {@code sectionNode}, is instance of {@link SectionNode} and {@link Node#getKey()} matches {@code pattern}
	 * @param sectionNode The {@link SectionNode} of the current element
	 * @param parser A {@link ParserInstance}
	 * @param pattern A {@link SkriptPattern} to match {@link Node#getKey()} of the following {@link Node}
	 * @return true if the following {@link Node} is a {@link SectionNode} and {@link Node#getKey()} matches {@code pattern}
	 */
	public static boolean checkFollowingElement(SectionNode sectionNode, ParserInstance parser, SkriptPattern pattern) {
		return checkFollowingElement(sectionNode, parser, node -> {
			if (!(node instanceof SectionNode) || node.getKey() == null)
				return false;
			String nextKey = ScriptLoader.replaceOptions(node.getKey());
			if (pattern.match(nextKey) == null)
				return false;
			return true;
		});
	}

	/**
	 * Check if the following {@link Node} , directly after {@code sectionNode} exists and passes {@code checker}.
	 * @param sectionNode The {@link SectionNode} of the current element
	 * @param parser A {@link ParserInstance}
	 * @param checker The {@link Predicate} to check the {@link Node}
	 * @return true if the following {@link Node} passes {@code checker}
	 */
	public static boolean checkFollowingElement(SectionNode sectionNode, ParserInstance parser, Predicate<Node> checker) {
		// iterating over the parent node causes the current node to change, so we need to store it to reset it later
		Node originalNode = parser.getNode();
		SectionNode parentNode = sectionNode.getParent();
		if (parentNode == null)
			return false;
		Iterator<Node> nodeIterator = parentNode.iterator();
		Node nextNode = null;
		while (nodeIterator.hasNext()) {
			Node current = nodeIterator.next();
			if (current == sectionNode) {
				nextNode = nodeIterator.hasNext() ? nodeIterator.next() : null;
				break;
			}
		}
		parser.setNode(originalNode);
		if (nextNode == null)
			return false;
		return checker.test(nextNode);
	}

	/**
	 * Parse a multiline condition to ensure the conditions are valid.
	 * Returns null if any of the conditions fail to parse or there is not a minimum of two conditions.
	 * @param sectionNode The {@link SectionNode} of the current element and houses the conditions.
	 * @param parser A {@link ParserInstance}.
	 * @param sectionType String displaying the section type to be used within {@link Skript#error(String)} if conditions fail to parse.
	 * @return {@link List} of the successfully parsed {@link Conditional<Event>}.
	 */
	public static @Nullable List<Conditional<Event>> parseMultiline(SectionNode sectionNode, ParserInstance parser, String sectionType) {
		return parseMultiline(sectionNode, parser, sectionType, 2);
	}

	/**
	 * Parse a multiline condition to ensure the conditions are valid.
	 * Returns null if any of the conditions fail to parse or the number of conditions does not meet the requirement of {@code minimum}.
	 * @param sectionNode The {@link SectionNode} of the current element that contains the conditions.
	 * @param parser A {@link ParserInstance}.
	 * @param sectionType String displaying the section type to be used within {@link Skript#error(String)} if conditions fail to parse.
	 * @param minimum Minimum number of conditions that need to be present.
	 * @return {@link List} of the successfully parsed {@link Conditional<Event>}.
	 */
	public static @Nullable List<Conditional<Event>> parseMultiline(SectionNode sectionNode, ParserInstance parser, String sectionType, int minimum)  {
		List<Conditional<Event>> conditionals = new ArrayList<>();
		int nodeCount = Iterables.size(sectionNode);
		// we have to get the size of the iterator here as SectionNode#size includes empty/void nodes
		if (nodeCount < minimum) {
			Skript.error(sectionType + " sections must contain atleast " + minimum + " condition(s).");
			return null;
		}
		for (Node childNode : sectionNode) {
			if (childNode instanceof SectionNode) {
				Skript.error(sectionType + " sections may not contain other sections.");
				return null;
			}
			String childKey = childNode.getKey();
			if (childKey == null)
				continue;
			childKey = ScriptLoader.replaceOptions(childKey);
			parser.setNode(childNode);
			Condition condition = Condition.parse(childKey, "Can't understand this condition: '" + childKey + "'");
			// if this condition was invalid, don't bother parsing the rest
			if (condition == null)
				return null;
			conditionals.add(condition);
		}
		parser.setNode(sectionNode);
		return conditionals;
	}

}
