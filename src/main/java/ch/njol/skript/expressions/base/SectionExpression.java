package ch.njol.skript.expressions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An implementation of the {@link Expression} interface that is allowed to start a section.
 * <pre>{@code
 * 	set {var} to (my section expression):
 *      this is inside a section
 *      this section can be 'managed' by (my section expression)
 * }</pre>
 * <br/>
 * <br/>
 * A section expression can be used in any expression slot, provided that:
 * <ol>
 *     <li>The line itself is not already a {@link Section} or an {@link EffectSection}.</li>
 *     <li>The line does not already contain another {@link SectionExpression}.</li>
 * </ol>
 * <br/>
 * <br/>
 * The intended purpose of section expressions is to make features like the below possible,
 * without the need to modify individual effect classes:
 * <pre>{@code
 * 	set {var} to a new lambda:
 *      broadcast "hello"
 *      # this section is managed by `a new lambda` rather than EffChange
 * }</pre>
 * <br/>
 * <br/>
 * Please note that section expressions will not be given a {@link SectionNode} during `init`
 * if they are used in a line <b>that does not start a section</b>!
 * It is up to the implementation to check whether this is provided.
 * <p>
 * Section node provided:
 * <pre>{@code
 * 	set {var} to a new lambda:
 *      broadcast "hello"
 * }</pre>
 * <p>
 * Section node not provided:
 * <pre>{@code
 * 	set {var} to a new lambda
 * }</pre>
 * <br/>
 * <br/>
 * Please also note that expressions are not 'executed' individually as effects/sections are,
 * and so some behaviour (such as trigger items, orders, etc.) may be different from a regular section.
 *
 * @see Skript#registerExpression(Class, Class, ExpressionType, String...)
 * @see Section
 * @see SimpleExpression
 */
public abstract class SectionExpression<Value> extends SimpleExpression<Value> {

	protected final ExpressionSection section = new ExpressionSection(this);

	/**
	 * Called just after the constructor.
	 *
	 * @param expressions all %expr%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out, it will still be included in this list
	 *            holding the default value of the desired type, which usually depends on the event
	 * @param pattern The index of the pattern which matched
	 * @param delayed Whether this expression is used after a delay or not (i.e. if the event has already passed when this expression will be called)
	 * @param result Additional information about the match
	 * @param node The section node representing the un-handled code in the body of the approaching section. If this expression does not start a section, the node will be null.
	 * @param triggerItems A list of preceding trigger nodes before this line
	 *
	 * @return Whether this expression was initialised successfully. An error should be printed prior to returning false to specify the cause.
	 */
	public abstract boolean init(Expression<?>[] expressions,
								 int pattern,
								 Kleenean delayed,
								 ParseResult result,
								 @Nullable SectionNode node,
								 @Nullable List<TriggerItem> triggerItems);

	@Override
	public final boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
                              ParseResult parseResult) {
		return section.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	/**
	 * @return A dummy trigger item representing the section belonging to this
	 */
	public final Section getAsSection() {
		return section;
	}

	/**
	 * Loads the code in the given {@link SectionNode},
	 * appropriately modifying {@link ParserInstance#getCurrentSections()}.
	 * <br>
	 * This method differs from {@link #loadCode(SectionNode)} in that it
	 * is meant for code that will be executed at another time and potentially with different context.
	 * The section's contents are parsed with the understanding that they have no relation
	 *  to the section itself, along with any other code that may come before and after the section.
	 * The {@link ParserInstance} is modified to reflect that understanding.
	 *
	 * @param sectionNode The section node to load.
	 * @param name The name of the event(s) being used.
	 * @param afterLoading A Runnable to execute after the SectionNode has been loaded.
	 * This occurs before {@link ParserInstance} states are reset.
	 * @param events The event(s) during the section's execution.
	 * @return A trigger containing the loaded section. This should be stored and used
	 * to run the section one or more times.
	 */
	protected Trigger loadCode(SectionNode sectionNode, String name,
							   @Nullable Runnable afterLoading, Class<? extends Event>... events) {
		return section.loadCodeTask(sectionNode, name, afterLoading, events);
	}

	/**
	 * Loads the code in the given {@link SectionNode},
	 * appropriately modifying {@link ParserInstance#getCurrentSections()}.
	 * <br>
	 * This method itself does not modify {@link ParserInstance#getHasDelayBefore()}
	 * (although the loaded code may change it), the calling code must deal with this.
	 */
	protected void loadCode(SectionNode sectionNode) {
		this.section.loadCode(sectionNode);
	}

	/**
	 * Loads the code using {@link #loadCode(SectionNode)}.
	 * <br>
	 * This method also adjusts {@link ParserInstance#getHasDelayBefore()} to expect the code
	 * to be called zero or more times. This is done by setting {@code hasDelayBefore} to {@link Kleenean#UNKNOWN}
	 * if the loaded section has a possible or definite delay in it.
	 */
	protected void loadOptionalCode(SectionNode sectionNode) {
		this.section.loadOptionalCode(sectionNode);
	}

	/**
	 * Establishes the {@link TriggerItem} tree of this section
	 *  without modifying {@link ParserInstance#getCurrentSections()}.
	 */
	protected void setTriggerItems(List<TriggerItem> items) {
		this.section.setTriggerItems(items);
	}

	/**
	 * Executes the code within the section associated with this expression.
	 * Before calling this method, the section must have been loaded through:
	 * {@link #loadCode(SectionNode)},
	 * {@link #loadOptionalCode(SectionNode)},
	 * or {@link #setTriggerItems(List)}.
	 * @param event The event to pass as context.
	 * @return False if an exception occurred while executing the section.
	 */
	protected boolean runSection(Event event) {
		return this.section.runSection(event);
	}

}
