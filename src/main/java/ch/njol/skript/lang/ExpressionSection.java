package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A dummy trigger item representing the 'section' aspect of a {@link SectionExpression}.
 * This is not a parsed or registered syntax in itself, and can be used for getting access to the parse-time features
 * of a section syntax (e.g. loading body code).
 * <p>
 * This is not safe to be used during runtime, since it is not a part of the trigger item tree.
 *
 * @see SectionExpression
 * @see Section
 * @see EffectSectionEffect
 */
@ApiStatus.Internal
public class ExpressionSection extends Section {

	protected final SectionExpression<?> expression;

	public ExpressionSection(SectionExpression<?> expression) {
		this.expression = expression;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		SectionContext context = getParser().getData(SectionContext.class);
		if (context.sectionNode == null && expression.isSectionOnly()) {
			Skript.error("This expression requires a section.");
			return false;
		}
		// log handler to prevent claim error from showing up if we fail to claim the section but can init without it
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			// try to claim section
			boolean claimedSection = context.claim(this, parseResult.expr);
			if (claimedSection) {
				// now that we have claimed the section, we have to commit
				boolean init = expression.init(expressions, matchedPattern, isDelayed, parseResult, context.sectionNode, context.triggerItems);
				if (init) {
					log.printLog();
					return true;
				} else {
					context.unclaim(this);
					log.printError();
					return false;
				}
			}
			// section already claimed
			// we would also like to error if there are 2 expression sections claiming the same section, as the user
			// has no actual control over which one gets to claim it, causing ambiguity and bad user experience
			if (expression.isSectionOnly() || context.owner instanceof ExpressionSection) {
				log.printError();
				return false;
			}
			// need to clear the error caused by failing to claim the section, since the following init may succeed
			log.clear();
		}
		return expression.init(expressions, matchedPattern, isDelayed, parseResult, null, null);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
						SkriptParser.ParseResult parseResult,
						@Nullable SectionNode sectionNode, List<TriggerItem> triggerItems) {
		return expression.init(expressions, matchedPattern, isDelayed, parseResult, sectionNode, triggerItems);
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expression.toString(event, debug);
	}

	public SectionExpression<?> getAsExpression() {
		return expression;
	}

	@Override
	public void loadCode(SectionNode sectionNode) {
		super.loadCode(sectionNode);
	}

	@Override
	public void loadOptionalCode(SectionNode sectionNode) {
		super.loadOptionalCode(sectionNode);
	}

	public boolean runSection(Event event) {
		return TriggerItem.walk(this.first, event);
	}

	@Override
	public void setTriggerItems(List<TriggerItem> items) {
		super.setTriggerItems(items);
	}

	@SafeVarargs
	public final Trigger loadCodeTask(SectionNode sectionNode, String name,
			@Nullable Runnable beforeLoading, @Nullable Runnable afterLoading, Class<? extends Event>... events) {
		return super.loadCode(sectionNode, name, beforeLoading, afterLoading, events);
	}

}
